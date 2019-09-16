package am.ik.surveys.survey.web;

import am.ik.surveys.answer.AnswerDetailRepository;
import am.ik.surveys.answer.AnswerRepository;
import am.ik.surveys.question.Question;
import am.ik.surveys.question.QuestionRepository;
import am.ik.surveys.questionchoice.QuestionChoice;
import am.ik.surveys.questionchoice.QuestionChoiceRepository;
import am.ik.surveys.survey.Survey;
import am.ik.surveys.survey.SurveyRepository;
import am.ik.surveys.surveyquestion.SurveyQuestion;
import am.ik.surveys.surveyquestion.SurveyQuestionRepository;
import am.ik.surveys.surveyquestion.web.SurveyQuestionResponse;
import de.huxhorn.sulky.ulid.ULID;
import org.springframework.stereotype.Component;
import org.springframework.transaction.reactive.TransactionalOperator;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class SurveyHandler {

    private final QuestionChoiceRepository questionChoiceRepository;

    private final QuestionRepository questionRepository;

    private final SurveyQuestionRepository surveyQuestionRepository;

    private final SurveyRepository surveyRepository;

    private final AnswerRepository answerRepository;

    private final AnswerDetailRepository answerDetailRepository;

    private final TransactionalOperator transactionalOperator;

    private final ULID ulid;

    public SurveyHandler(ULID ulid, SurveyRepository surveyRepository, SurveyQuestionRepository surveyQuestionRepository, QuestionRepository questionRepository,
                         QuestionChoiceRepository questionChoiceRepository, AnswerRepository answerRepository, AnswerDetailRepository answerDetailRepository,
                         TransactionalOperator transactionalOperator) {
        this.ulid = ulid;
        this.surveyRepository = surveyRepository;
        this.surveyQuestionRepository = surveyQuestionRepository;
        this.questionRepository = questionRepository;
        this.questionChoiceRepository = questionChoiceRepository;
        this.answerRepository = answerRepository;
        this.answerDetailRepository = answerDetailRepository;
        this.transactionalOperator = transactionalOperator;
    }

    public RouterFunction<ServerResponse> routes() {
        return RouterFunctions.route()
            .GET("/surveys", this::getSurveys)
            .POST("/surveys", this::postSurveys)
            .GET("/surveys/{survey_id}", this::getSurvey)
            .DELETE("/surveys/{survey_id}", this::deleteSurvey)
            .build();
    }

    private Mono<ServerResponse> deleteSurvey(ServerRequest req) {
        final Survey.Id surveyId = Survey.Id.valueOf(req.pathVariable("survey_id"));
        return this.surveyQuestionRepository.deleteBySurveyId(surveyId)
            .then(this.answerDetailRepository.deleteBySurveyId(surveyId))
            .then(this.answerRepository.deleteBySurveyId(surveyId))
            .then(this.surveyRepository.deleteById(surveyId))
            .as(this.transactionalOperator::transactional)
            .then(ServerResponse.noContent().build());
    }

    private Mono<ServerResponse> getSurvey(ServerRequest req) {
        final Survey.Id surveyId = Survey.Id.valueOf(req.pathVariable("survey_id"));
        final Mono<Survey> surveyMono = this.surveyRepository.findById(surveyId);
        final Flux<SurveyQuestion> surveyQuestionFlux = this.surveyQuestionRepository.findBySurveyId(surveyId);
        final Mono<Map<Question.Id, Question>> questionMapMono = this.questionRepository.findBySurveyId(surveyId)
            .collectMap(Question::getQuestionId, Function.identity());
        final Mono<Map<Question.Id, List<QuestionChoice>>> questionChoicesMapMono = this.questionChoiceRepository.findAllBySurveyId(surveyId)
            .collect(Collectors.groupingBy(QuestionChoice::getQuestionId));
        final Flux<SurveyQuestionResponse> surveyQuestionResponseFlux = Mono.zip(surveyQuestionFlux.collectList(), questionMapMono, questionChoicesMapMono)
            .flatMapMany(tpl -> {
                final List<SurveyQuestion> surveyQuestions = tpl.getT1();
                final Map<Question.Id, Question> questionMap = tpl.getT2();
                final Map<Question.Id, List<QuestionChoice>> questionChoicesMap = tpl.getT3();
                return Flux.fromStream(surveyQuestions.stream()
                    .map(surveyQuestion -> {
                        final Question.Id questionId = surveyQuestion.getQuestionId();
                        final Question question = questionMap.get(questionId);
                        final List<QuestionChoice> questionChoices = questionChoicesMap.get(questionId);
                        return SurveyQuestionResponse.create(surveyQuestion, question, questionChoices);
                    })
                );
            });
        final Mono<SurveyResponse> surveyResponseMono = surveyMono.zipWith(surveyQuestionResponseFlux.collectList())
            .map(tpl -> new SurveyResponse(tpl.getT1(), tpl.getT2()));
        return ServerResponse.ok().body(surveyResponseMono, SurveyResponse.class);
    }

    private Mono<ServerResponse> getSurveys(ServerRequest req) {
        return ServerResponse.ok().body(this.surveyRepository.findAll(), Survey.class);
    }

    private Mono<ServerResponse> postSurveys(ServerRequest req) {
        final Survey.Id surveyId = Survey.Id.nextValue(this.ulid);
        final URI location = req.uriBuilder().replacePath("surveys/{survey_id}").build(surveyId);
        final Mono<Survey> surveyMono = req.bodyToMono(SurveyRequest.class).map(surveyRequest -> surveyRequest.toSurvey(surveyId));
        final Mono<Survey> insert = this.surveyRepository.insert(surveyMono);
        return ServerResponse.created(location).body(insert, Survey.class);
    }
}
