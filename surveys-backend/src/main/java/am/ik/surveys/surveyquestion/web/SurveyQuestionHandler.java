package am.ik.surveys.surveyquestion.web;

import am.ik.surveys.question.Question;
import am.ik.surveys.question.QuestionRepository;
import am.ik.surveys.questionchoice.QuestionChoiceRepository;
import am.ik.surveys.survey.Survey;
import am.ik.surveys.survey.SurveyRepository;
import am.ik.surveys.surveyquestion.SurveyQuestion;
import am.ik.surveys.surveyquestion.SurveyQuestionRepository;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
public class SurveyQuestionHandler {

    private final SurveyRepository surveyRepository;

    private final SurveyQuestionRepository surveyQuestionRepository;

    private final QuestionRepository questionRepository;

    private final QuestionChoiceRepository questionChoiceRepository;

    public SurveyQuestionHandler(SurveyRepository surveyRepository, SurveyQuestionRepository surveyQuestionRepository, QuestionRepository questionRepository,
                                 QuestionChoiceRepository questionChoiceRepository) {
        this.surveyRepository = surveyRepository;
        this.surveyQuestionRepository = surveyQuestionRepository;
        this.questionRepository = questionRepository;
        this.questionChoiceRepository = questionChoiceRepository;
    }

    public RouterFunction<ServerResponse> routes() {
        return RouterFunctions.route()
            .GET("/surveys/{surveyId}/survey_questions", this::getSurveyQuestionsBySurveyId)
            .POST("/surveys/{surveyId}/survey_questions/{questionId}", this::postSurveyQuestion)
            .DELETE("/surveys/{surveyId}/survey_questions/{questionId}", this::deleteSurveyQuestion)
            .build();
    }

    Mono<ServerResponse> getSurveyQuestionsBySurveyId(ServerRequest req) {
        final Survey.Id surveyId = Survey.Id.valueOf(req.pathVariable("surveyId"));
        final Flux<SurveyQuestion> surveyQuestionFlux = this.surveyQuestionRepository.findBySurveyId(surveyId);
        final Flux<SurveyQuestionResponse> surveyQuestionResponseFlux = surveyQuestionFlux.flatMap(surveyQuestion -> SurveyQuestionResponse.from(surveyQuestion, this.questionRepository,
            this.questionChoiceRepository));
        return ServerResponse.ok().body(surveyQuestionResponseFlux, SurveyQuestionResponse.class);
    }

    Mono<ServerResponse> postSurveyQuestion(ServerRequest req) {
        final Survey.Id surveyId = Survey.Id.valueOf(req.pathVariable("surveyId"));
        final Question.Id questionId = Question.Id.valueOf(req.pathVariable("questionId"));
        final Mono<Survey> surveyMono = this.surveyRepository.findById(surveyId);
        final Mono<Question> questionMono = this.questionRepository.findById(questionId);
        final Mono<SurveyQuestionRequest> surveyQuestionRequestMono = req.bodyToMono(SurveyQuestionRequest.class);
        final Mono<SurveyQuestion> surveyQuestionMono = Mono.zip(surveyMono, questionMono, surveyQuestionRequestMono)
            .map(tpl -> new SurveyQuestion.Builder()
                .withSurveyId(tpl.getT1().getSurveyId())
                .withQuestionId(tpl.getT2().getQuestionId())
                .withRequired(tpl.getT3().isRequired())
                .build());
        final Mono<SurveyQuestion> insert = this.surveyQuestionRepository.insert(surveyQuestionMono);
        return insert.flatMap(__ -> ServerResponse.ok().build())
            .switchIfEmpty(Mono.defer(() -> ServerResponse.notFound().build()));
    }

    Mono<ServerResponse> deleteSurveyQuestion(ServerRequest req) {
        final Survey.Id surveyId = Survey.Id.valueOf(req.pathVariable("surveyId"));
        final Question.Id questionId = Question.Id.valueOf(req.pathVariable("questionId"));
        final SurveyQuestion surveyQuestion = new SurveyQuestion.Builder()
            .withSurveyId(surveyId)
            .withQuestionId(questionId)
            .build();
        return this.surveyQuestionRepository.delete(surveyQuestion)
            .then(ServerResponse.noContent().build());
    }
}
