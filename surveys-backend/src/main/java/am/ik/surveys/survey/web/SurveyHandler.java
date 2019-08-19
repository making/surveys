package am.ik.surveys.survey.web;

import am.ik.surveys.question.QuestionRepository;
import am.ik.surveys.questionchoice.QuestionChoiceRepository;
import am.ik.surveys.survey.Survey;
import am.ik.surveys.survey.SurveyRepository;
import am.ik.surveys.surveyquestion.SurveyQuestion;
import am.ik.surveys.surveyquestion.SurveyQuestionRepository;
import am.ik.surveys.surveyquestion.web.SurveyQuestionResponse;
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
public class SurveyHandler {

    private final QuestionChoiceRepository questionChoiceRepository;

    private final QuestionRepository questionRepository;

    private final SurveyQuestionRepository surveyQuestionRepository;

    private final SurveyRepository surveyRepository;

    private final ULID ulid;

    public SurveyHandler(ULID ulid, SurveyRepository surveyRepository, SurveyQuestionRepository surveyQuestionRepository, QuestionRepository questionRepository,
                         QuestionChoiceRepository questionChoiceRepository) {
        this.ulid = ulid;
        this.surveyRepository = surveyRepository;
        this.surveyQuestionRepository = surveyQuestionRepository;
        this.questionRepository = questionRepository;
        this.questionChoiceRepository = questionChoiceRepository;
    }

    public RouterFunction<ServerResponse> routes() {
        return RouterFunctions.route()
            .GET("/surveys", this::getSurveys)
            .POST("/surveys", this::postSurveys)
            .GET("/surveys/{survey_id}", this::getSurvey)
            .DELETE("/surveys/{survey_id}", this::deleteSurvey)
            .build();
    }

    Mono<ServerResponse> deleteSurvey(ServerRequest req) {
        final Survey.Id surveyId = Survey.Id.valueOf(req.pathVariable("survey_id"));
        return this.surveyQuestionRepository.deleteBySurveyId(surveyId)
            .then(this.surveyRepository.deleteById(surveyId))
            .then(ServerResponse.noContent().build());
    }

    Mono<ServerResponse> getSurvey(ServerRequest req) {
        final Survey.Id surveyId = Survey.Id.valueOf(req.pathVariable("survey_id"));
        final Mono<Survey> surveyMono = this.surveyRepository.findById(surveyId);
        final Flux<SurveyQuestion> surveyQuestionFlux = this.surveyQuestionRepository.findBySurveyId(surveyId);
        final Flux<SurveyQuestionResponse> surveyQuestionResponseFlux = surveyQuestionFlux.flatMap(surveyQuestion -> SurveyQuestionResponse.from(surveyQuestion, this.questionRepository,
            this.questionChoiceRepository));
        final Mono<SurveyResponse> surveyResponseMono = surveyMono.zipWith(surveyQuestionResponseFlux.collectList())
            .map(tpl -> new SurveyResponse(tpl.getT1(), tpl.getT2()));
        return ServerResponse.ok().body(surveyResponseMono, SurveyResponse.class);
    }

    Mono<ServerResponse> getSurveys(ServerRequest req) {
        return ServerResponse.ok().body(this.surveyRepository.findAll(), Survey.class);
    }

    Mono<ServerResponse> postSurveys(ServerRequest req) {
        final Survey.Id surveyId = Survey.Id.nextValue(this.ulid);
        final URI location = req.uriBuilder().replacePath("surveys/{survey_id}").build(surveyId);
        final Mono<Survey> surveyMono = req.bodyToMono(SurveyRequest.class).map(surveyRequest -> surveyRequest.toSurvey(surveyId));
        final Mono<Survey> insert = this.surveyRepository.insert(surveyMono);
        return ServerResponse.created(location).body(insert, Survey.class);
    }
}
