package am.ik.surveys.question.web;

import am.ik.surveys.question.Question;
import am.ik.surveys.question.QuestionRepository;
import am.ik.surveys.questionchoice.QuestionChoice;
import am.ik.surveys.questionchoice.QuestionChoiceRepository;
import am.ik.surveys.questionchoice.web.QuestionChoiceRequest;
import de.huxhorn.sulky.ulid.ULID;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.net.URI;

@Component
public class QuestionHandler {

    private final ULID ulid;

    private final QuestionRepository questionRepository;

    private final QuestionChoiceRepository questionChoiceRepository;

    public QuestionHandler(ULID ulid, QuestionRepository questionRepository, QuestionChoiceRepository questionChoiceRepository) {
        this.ulid = ulid;
        this.questionRepository = questionRepository;
        this.questionChoiceRepository = questionChoiceRepository;
    }

    public RouterFunction<ServerResponse> routes() {
        return RouterFunctions.route()
            .POST("/questions", this::postQuestions)
            .GET("/questions/{questionId}", this::getQuestion)
            .GET("/questions/{questionId}/question_choices", this::getQuestionChoices)
            .POST("/questions/{questionId}/question_choices", this::postQuestionChoices)
            .build();
    }

    Mono<ServerResponse> postQuestions(ServerRequest req) {
        final Question.Id questionId = Question.Id.nextValue(this.ulid);
        final Mono<Question> questionMono = req.bodyToMono(QuestionRequest.class)
            .map(questionRequest -> questionRequest.toQuestion(questionId));
        final Mono<Question> insert = this.questionRepository.insert(questionMono);
        final URI location = req.uriBuilder().replacePath("questions/{questionId}").build(questionId);
        return ServerResponse.created(location).body(insert, QuestionResponse.class);
    }

    Mono<ServerResponse> getQuestion(ServerRequest req) {
        final Question.Id questionId = Question.Id.valueOf(req.pathVariable("questionId"));
        final Mono<QuestionResponse> questionResponseMono = this.questionRepository.findById(questionId)
            .flatMap(question -> QuestionResponse.from(question, this.questionChoiceRepository));
        return ServerResponse.ok().body(questionResponseMono, QuestionResponse.class);
    }

    Mono<ServerResponse> getQuestionChoices(ServerRequest req) {
        final Question.Id questionId = Question.Id.valueOf(req.pathVariable("questionId"));
        final Flux<QuestionChoice> questionChoiceFlux = this.questionChoiceRepository.findAllByQuestionId(questionId);
        return ServerResponse.ok().body(questionChoiceFlux, QuestionChoice.class);
    }

    Mono<ServerResponse> postQuestionChoices(ServerRequest req) {
        final Question.Id questionId = Question.Id.valueOf(req.pathVariable("questionId"));
        final QuestionChoice.Id questionChoiceId = QuestionChoice.Id.nextValue(this.ulid);
        final Mono<QuestionChoice> questionChoiceMono = req.bodyToMono(QuestionChoiceRequest.class)
            .map(questionChoiceRequest -> questionChoiceRequest.toQuestionChoice(questionChoiceId, questionId));
        final Mono<QuestionChoice> insert = this.questionChoiceRepository.insert(questionChoiceMono);
        final URI location = req.uriBuilder().replacePath("question_choices/{questionChoiceId}").build(questionChoiceId);
        return ServerResponse.created(location).body(insert, QuestionChoice.class);
    }

}
