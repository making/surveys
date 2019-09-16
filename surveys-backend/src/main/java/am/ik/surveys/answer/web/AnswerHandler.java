package am.ik.surveys.answer.web;

import am.ik.surveys.answer.Answer;
import am.ik.surveys.answer.AnswerDetail;
import am.ik.surveys.answer.AnswerDetailRepository;
import am.ik.surveys.answer.AnswerRepository;
import am.ik.surveys.survey.Survey;
import de.huxhorn.sulky.ulid.ULID;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuples;

import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class AnswerHandler {

    private final ULID ulid;

    private final AnswerRepository answerRepository;

    private final AnswerDetailRepository answerDetailRepository;

    public AnswerHandler(ULID ulid, AnswerRepository answerRepository, AnswerDetailRepository answerDetailRepository) {
        this.ulid = ulid;
        this.answerRepository = answerRepository;
        this.answerDetailRepository = answerDetailRepository;
    }

    public RouterFunction<ServerResponse> routes() {
        return RouterFunctions.route()
            .GET("/surveys/{survey_id}/answers", this::getAnswersBySurveyId)
            .POST("/surveys/{survey_id}/answers", this::postAnswers)
            .GET("/answers/{answer_id}", this::getAnswer)
            .DELETE("/answers/{answer_id}", this::deleteAnswer)
            .build();
    }

    private Mono<ServerResponse> getAnswersBySurveyId(ServerRequest req) {
        final Survey.Id surveyId = Survey.Id.valueOf(req.pathVariable("survey_id"));
        final Flux<Answer> answerFlux = this.answerRepository.findAllBySurveyId(surveyId);
        final Mono<Map<Answer.Id, List<AnswerDetail<?>>>> answerDetailMapMono = this.answerDetailRepository.findAllBySurveyId(surveyId)
            .collect(Collectors.groupingBy(AnswerDetail::getAnswerId));
        final Flux<AnswerResponse> answerResponseFlux = answerFlux.collectList().zipWith(answerDetailMapMono)
            .flatMapMany(tpl -> {
                final List<Answer> answers = tpl.getT1();
                final Map<Answer.Id, List<AnswerDetail<?>>> detailMap = tpl.getT2();
                return Flux.fromStream(answers.stream()
                    .map(answer -> {
                        final Answer.Id answerId = answer.getAnswerId();
                        final List<AnswerDetail<?>> answerDetails = detailMap.get(answerId);
                        return new AnswerResponse(answer, answerDetails);
                    }));
            });
        return ServerResponse.ok().body(answerResponseFlux, AnswerResponse.class);
    }

    private Mono<ServerResponse> postAnswers(ServerRequest req) {
        final Survey.Id surveyId = Survey.Id.valueOf(req.pathVariable("survey_id"));
        final Answer.Id answerId = Answer.Id.nextValue(this.ulid);
        final Mono<AnswerResponse> answerResponseMono = req.bodyToMono(AnswerRequest.class)
            .map(answerRequest -> Tuples.of(answerRequest.toAnswer(answerId, surveyId),
                answerRequest.toAnswerDetails(answerId)
            ))
            .flatMap(tpl -> this.answerRepository.insert(tpl.getT1())
                .flatMap(answer -> this.answerDetailRepository.insert(tpl.getT2()).collectList()
                    .map(details -> new AnswerResponse(answer, details))));
        final URI location = req.uriBuilder().replacePath("answers/{answer_id}").build(answerId);
        return ServerResponse.created(location).body(answerResponseMono, AnswerResponse.class);
    }

    private Mono<ServerResponse> getAnswer(ServerRequest req) {
        final Answer.Id answerId = Answer.Id.valueOf(req.pathVariable("answer_id"));
        final Mono<Answer> answerMono = this.answerRepository.findById(answerId);
        final Mono<AnswerResponse> answerResponseMono = answerMono.flatMap(answer -> AnswerResponse.from(answer, answerDetailRepository));
        return ServerResponse.ok().body(answerResponseMono, AnswerResponse.class);
    }

    private Mono<ServerResponse> deleteAnswer(ServerRequest req) {
        final Answer.Id answerId = Answer.Id.valueOf(req.pathVariable("answer_id"));
        final Mono<Void> deleteAnswer = this.answerRepository.deleteById(answerId);
        final Flux<Void> deleteDetails = this.answerDetailRepository.findAllByAnswerId(answerId)
            .flatMap(this.answerDetailRepository::delete);
        return deleteDetails
            .then(deleteAnswer)
            .then(ServerResponse.noContent().build());
    }
}
