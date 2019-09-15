package am.ik.surveys.surveyquestion.web;

import am.ik.surveys.Fixtures;
import am.ik.surveys.TestUtils;
import am.ik.surveys.infra.sql.SqlSupplier;
import am.ik.surveys.question.Question;
import am.ik.surveys.question.QuestionRepository;
import am.ik.surveys.questionchoice.QuestionChoiceRepository;
import am.ik.surveys.survey.Survey;
import am.ik.surveys.survey.SurveyRepository;
import am.ik.surveys.surveyquestion.SurveyQuestion;
import am.ik.surveys.surveyquestion.SurveyQuestionRepository;
import com.fasterxml.jackson.databind.JsonNode;
import de.huxhorn.sulky.ulid.ULID;
import io.r2dbc.spi.ConnectionFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.data.r2dbc.core.DatabaseClient;
import org.springframework.http.MediaType;
import org.springframework.restdocs.RestDocumentationContextProvider;
import org.springframework.restdocs.RestDocumentationExtension;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.transaction.reactive.TransactionalOperator;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.List;
import java.util.Map;
import java.util.Objects;

import static com.epages.restdocs.apispec.WebTestClientRestDocumentationWrapper.document;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.restdocs.headers.HeaderDocumentation.headerWithName;
import static org.springframework.restdocs.headers.HeaderDocumentation.requestHeaders;
import static org.springframework.restdocs.headers.HeaderDocumentation.responseHeaders;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.requestFields;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.pathParameters;

@ExtendWith({RestDocumentationExtension.class})
class SurveyQuestionHandlerTest {

    private ULID ulid = new ULID();

    private WebTestClient testClient;

    private QuestionRepository questionRepository;

    private SurveyQuestionRepository surveyQuestionRepository;

    private SurveyRepository surveyRepository;

    @BeforeEach
    void setUp(RestDocumentationContextProvider restDocumentation) {
        final ConnectionFactory connectionFactory = TestUtils.connectionFactory();
        final DatabaseClient databaseClient = TestUtils.databaseClient(connectionFactory);
        final TransactionalOperator transactionalOperator = TestUtils.transactionalOperator(connectionFactory);
        final SqlSupplier sqlSupplier = TestUtils.sqlSupplier();
        final QuestionChoiceRepository questionChoiceRepository = new QuestionChoiceRepository(databaseClient, transactionalOperator, sqlSupplier);
        this.questionRepository = new QuestionRepository(databaseClient, transactionalOperator, sqlSupplier);
        this.surveyQuestionRepository = new SurveyQuestionRepository(databaseClient, transactionalOperator, sqlSupplier);
        this.surveyRepository = new SurveyRepository(databaseClient, transactionalOperator, sqlSupplier);
        final SurveyRepository surveyRepository = new SurveyRepository(databaseClient, transactionalOperator, sqlSupplier);
        final SurveyQuestionHandler surveyQuestionHandler = new SurveyQuestionHandler(surveyRepository, surveyQuestionRepository, questionRepository, questionChoiceRepository);
        this.testClient = TestUtils.webTestClient(surveyQuestionHandler.routes(), restDocumentation)
            .build();
        Fixtures.surveys.forEach(survey ->
            StepVerifier.create(this.surveyRepository.insert(Mono.just(survey)))
                .expectNext(survey)
                .verifyComplete());
        Fixtures.questions.forEach(question ->
            StepVerifier.create(this.questionRepository.insert(Mono.just(question)))
                .expectNext(question)
                .verifyComplete());
        Fixtures.surveyQuestions.forEach(surveyQuestion ->
            StepVerifier.create(this.surveyQuestionRepository.insert(surveyQuestion))
                .expectNext(surveyQuestion)
                .verifyComplete());
    }

    @Test
    void getSurveyQuestions() {
        final Survey survey = Fixtures.surveys.get(0);
        this.testClient.get()
            .uri("/surveys/{survey_id}/survey_questions", survey.getSurveyId())
            .exchange()
            .expectStatus().isOk()
            .expectBody(JsonNode.class)
            .consumeWith(result -> {
                final JsonNode res = result.getResponseBody();
                assertThat(res).isNotNull();
                assertThat(res).hasSize(3);
            })
            .consumeWith(
                document("get-survey-questions",
                    pathParameters(
                        parameterWithName("survey_id").description("アンケートID")),
                    responseHeaders(headerWithName(CONTENT_TYPE).description(APPLICATION_JSON_VALUE)),
                    responseFields(
                        fieldWithPath("[].required").type(JsonFieldType.BOOLEAN).description("回答必須"),
                        fieldWithPath("[].question_id").type(JsonFieldType.STRING).description("設問ID"),
                        fieldWithPath("[].survey_id").type(JsonFieldType.STRING).description("アンケートID"),
                        fieldWithPath("[].question_text").type(JsonFieldType.STRING).description("設問文"),
                        fieldWithPath("[].max_choices").type(JsonFieldType.NUMBER).description("選択可能数").optional(),
                        fieldWithPath("[].question_choices").type(JsonFieldType.ARRAY).description("設問選択肢").optional(),
                        fieldWithPath("[].question_choices[].question_choice_id").type(JsonFieldType.STRING).description("設問選択肢ID").optional(),
                        fieldWithPath("[].question_choices[].question_id").type(JsonFieldType.STRING).description("設問ID").optional(),
                        fieldWithPath("[].question_choices[].question_choice_text").type(JsonFieldType.STRING).description("選択肢本文").optional(),
                        fieldWithPath("[].question_choices[].allow_free_text").type(JsonFieldType.BOOLEAN).description("自由記述可").optional())));
    }

    @Test
    void deleteSurveyQuestion() {
        final SurveyQuestion surveyQuestion = Fixtures.surveyQuestions.get(0);
        this.testClient.delete()
            .uri("/surveys/{survey_id}/survey_questions/{question_id}", surveyQuestion.getSurveyId(), surveyQuestion.getQuestionId())
            .exchange()
            .expectStatus().isNoContent()
            .expectBody()
            .consumeWith(
                document("delete-survey-question",
                    pathParameters(
                        parameterWithName("survey_id").description("アンケートID"),
                        parameterWithName("question_id").description("設問ID"))));
        final Mono<Boolean> isDeleted = this.surveyQuestionRepository.findBySurveyId(surveyQuestion.getSurveyId())
            .filter(x -> Objects.equals(x.getQuestionId(), surveyQuestion.getQuestionId()))
            .collectList()
            .map(List::isEmpty);
        StepVerifier.create(isDeleted)
            .expectNext(true)
            .expectComplete()
            .verify();
    }

    @Test
    void postSurveyQuestion() {
        final Survey survey = Fixtures.surveys.get(0);
        final Question.Id questionId = Question.Id.nextValue(this.ulid);
        final Question question = new Question.Builder()
            .withQuestionId(questionId)
            .withQuestionText("その他コメント")
            .build();
        final Map<String, Boolean> requestBody = Map.of("required", false);
        StepVerifier.create(this.questionRepository.insert(Mono.just(question)))
            .expectNext(question)
            .expectComplete()
            .verify();
        this.testClient.post()
            .uri("/surveys/{survey_id}/survey_questions/{question_id}", survey.getSurveyId(), questionId)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(requestBody)
            .exchange()
            .expectStatus().isOk()
            .expectBody(JsonNode.class)
            .consumeWith(
                document("post-survey-questions",
                    requestHeaders(headerWithName(CONTENT_TYPE).description(APPLICATION_JSON_VALUE)),
                    pathParameters(
                        parameterWithName("survey_id").description("アンケートID"),
                        parameterWithName("question_id").description("設問ID")),
                    requestFields(
                        fieldWithPath("required").type(JsonFieldType.BOOLEAN).description("回答必須"))));
        final Mono<Boolean> isCreated = this.surveyQuestionRepository.findBySurveyId(survey.getSurveyId())
            .filter(surveyQuestion -> Objects.equals(surveyQuestion.getQuestionId(), questionId))
            .collectList()
            .map(l -> l.size() == 1);
        StepVerifier.create(isCreated)
            .expectNext(true)
            .expectComplete()
            .verify();
    }
}