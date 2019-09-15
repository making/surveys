package am.ik.surveys.survey.web;

import am.ik.surveys.Fixtures;
import am.ik.surveys.TestUtils;
import am.ik.surveys.infra.sql.SqlSupplier;
import am.ik.surveys.question.QuestionRepository;
import am.ik.surveys.questionchoice.QuestionChoiceRepository;
import am.ik.surveys.survey.Survey;
import am.ik.surveys.survey.SurveyRepository;
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

import java.net.URI;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Map;

import static com.epages.restdocs.apispec.WebTestClientRestDocumentationWrapper.document;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.HttpHeaders.LOCATION;
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
class SurveyHandlerTest {

    private ULID ulid = new ULID();

    private WebTestClient testClient;

    private SurveyRepository surveyRepository;

    private QuestionRepository questionRepository;

    private SurveyQuestionRepository surveyQuestionRepository;

    @BeforeEach
    void setUp(RestDocumentationContextProvider restDocumentation) {
        final ConnectionFactory connectionFactory = TestUtils.connectionFactory();
        final DatabaseClient databaseClient = TestUtils.databaseClient(connectionFactory);
        final TransactionalOperator transactionalOperator = TestUtils.transactionalOperator(connectionFactory);
        final SqlSupplier sqlSupplier = TestUtils.sqlSupplier();
        final QuestionChoiceRepository questionChoiceRepository = new QuestionChoiceRepository(databaseClient, transactionalOperator, sqlSupplier);
        final QuestionRepository questionRepository = new QuestionRepository(databaseClient, transactionalOperator, sqlSupplier);
        final SurveyQuestionRepository surveyQuestionRepository = new SurveyQuestionRepository(databaseClient, transactionalOperator, sqlSupplier);
        this.surveyRepository = new SurveyRepository(databaseClient, transactionalOperator, sqlSupplier);
        this.questionRepository = new QuestionRepository(databaseClient, transactionalOperator, sqlSupplier);
        this.surveyQuestionRepository = new SurveyQuestionRepository(databaseClient, transactionalOperator, sqlSupplier);
        final SurveyHandler surveyHandler = new SurveyHandler(this.ulid, surveyRepository, surveyQuestionRepository, questionRepository, questionChoiceRepository);
        this.testClient = TestUtils.webTestClient(surveyHandler.routes(), restDocumentation)
            .build();

        Fixtures.surveys.forEach(survey ->
            StepVerifier.create(this.surveyRepository.insert(Mono.just(survey)))
                .expectNext(survey)
                .verifyComplete());
        Fixtures.questions.forEach(question ->
            StepVerifier.create(this.questionRepository.insert(Mono.just(question)))
                .expectNext(question)
                .verifyComplete());
        Fixtures.questionChoices.forEach(questionChoice ->
            StepVerifier.create(questionChoiceRepository.insert(Mono.just(questionChoice)))
                .expectNext(questionChoice)
                .verifyComplete());
        Fixtures.surveyQuestions.forEach(surveyQuestion ->
            StepVerifier.create(this.surveyQuestionRepository.insert(surveyQuestion))
                .expectNext(surveyQuestion)
                .verifyComplete());
    }

    @Test
    void deleteSurvey() {
        final Survey survey = Fixtures.surveys.get(0);
        this.testClient.delete()
            .uri("/surveys/{survey_id}", survey.getSurveyId())
            .exchange()
            .expectStatus().isNoContent()
            .expectBody()
            .consumeWith(
                document("delete-survey",
                    pathParameters(parameterWithName("survey_id").description("アンケートID"))));
        final Mono<Survey> surveyMono = this.surveyRepository.findById(survey.getSurveyId());
        StepVerifier.create(surveyMono)
            .expectComplete()
            .verify();
    }

    @Test
    void getSurvey() {
        final Survey survey = Fixtures.surveys.get(0);
        this.testClient.get()
            .uri("/surveys/{survey_id}", survey.getSurveyId())
            .exchange()
            .expectStatus().isOk()
            .expectBody(JsonNode.class)
            .consumeWith(result -> {
                final JsonNode res = result.getResponseBody();
                assertThat(res).isNotNull();
                assertThat(res.get("survey_id").asText()).isEqualTo(survey.getSurveyId().toString());
                assertThat(OffsetDateTime.parse(res.get("start_date_time").asText())).isEqualTo(survey.getStartDateTime());
                assertThat(OffsetDateTime.parse(res.get("end_date_time").asText())).isEqualTo(survey.getEndDateTime());
                final JsonNode surveyQuestions = res.get("survey_questions");
                assertThat(surveyQuestions).hasSize(3);
            })
            .consumeWith(
                document("get-survey",
                    pathParameters(parameterWithName("survey_id").description("アンケートID")),
                    responseHeaders(headerWithName(CONTENT_TYPE).description(APPLICATION_JSON_VALUE)),
                    responseFields(
                        fieldWithPath("survey_id").type(JsonFieldType.STRING).description("アンケートID"),
                        fieldWithPath("start_date_time").type(JsonFieldType.STRING).description("開始予定時刻"),
                        fieldWithPath("end_date_time").type(JsonFieldType.STRING).description("終了予定時刻"),
                        fieldWithPath("survey_questions").type(JsonFieldType.ARRAY).description("アンケート設問"),
                        fieldWithPath("survey_questions[].required").type(JsonFieldType.BOOLEAN).description("回答必須"),
                        fieldWithPath("survey_questions[].question_id").type(JsonFieldType.STRING).description("設問ID"),
                        fieldWithPath("survey_questions[].survey_id").type(JsonFieldType.STRING).description("アンケートID"),
                        fieldWithPath("survey_questions[].question_text").type(JsonFieldType.STRING).description("設問文"),
                        fieldWithPath("survey_questions[].max_choices").type(JsonFieldType.NUMBER).description("選択可能数").optional(),
                        fieldWithPath("survey_questions[].question_choices").type(JsonFieldType.ARRAY).description("設問選択肢").optional(),
                        fieldWithPath("survey_questions[].question_choices[].question_choice_id").type(JsonFieldType.STRING).description("設問選択肢ID").optional(),
                        fieldWithPath("survey_questions[].question_choices[].question_id").type(JsonFieldType.STRING).description("設問ID").optional(),
                        fieldWithPath("survey_questions[].question_choices[].question_choice_text").type(JsonFieldType.STRING).description("選択肢本文").optional(),
                        fieldWithPath("survey_questions[].question_choices[].allow_free_text").type(JsonFieldType.BOOLEAN).description("自由記述可").optional())));
    }

    @Test
    void getSurveys() {
        final Survey survey1 = Fixtures.surveys.get(0);
        this.testClient.get()
            .uri("/surveys")
            .exchange()
            .expectStatus().isOk()
            .expectBody(JsonNode.class)
            .consumeWith(result -> {
                final JsonNode res = result.getResponseBody();
                assertThat(res).isNotNull();
                assertThat(res.size()).isEqualTo(1);
                assertThat(res.get(0).get("survey_id").asText()).isEqualTo(survey1.getSurveyId().toString());
                assertThat(OffsetDateTime.parse(res.get(0).get("start_date_time").asText())).isEqualTo(survey1.getStartDateTime());
                assertThat(OffsetDateTime.parse(res.get(0).get("end_date_time").asText())).isEqualTo(survey1.getEndDateTime());
            })
            .consumeWith(
                document("get-surveys",
                    responseHeaders(headerWithName(CONTENT_TYPE).description(APPLICATION_JSON_VALUE)),
                    responseFields(
                        fieldWithPath("[].survey_id").type(JsonFieldType.STRING).description("アンケートID"),
                        fieldWithPath("[].start_date_time").type(JsonFieldType.STRING).description("開始予定時刻"),
                        fieldWithPath("[].end_date_time").type(JsonFieldType.STRING).description("終了予定時刻"))));
    }

    @Test
    void postSurveys() {
        final OffsetDateTime startDateTime = OffsetDateTime.of(2019, 9, 1, 0, 0, 0, 0, ZoneOffset.UTC);
        final OffsetDateTime endDateTime = OffsetDateTime.of(2019, 9, 1, 0, 0, 0, 0, ZoneOffset.UTC);
        final Map<String, Object> requestBody = Map.of("start_date_time", startDateTime.toString(), "end_date_time", endDateTime.toString());

        this.testClient.post()
            .uri("/surveys")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(requestBody)
            .exchange()
            .expectStatus().isCreated()
            .expectBody(JsonNode.class)
            .consumeWith(result -> {
                final JsonNode res = result.getResponseBody();
                assertThat(res).isNotNull();
                final String surveyId = res.get("survey_id").asText();
                assertThat(surveyId).isNotEmpty();
                assertThat(ULID.parseULID(surveyId)).isNotNull();
                final URI location = result.getResponseHeaders().getLocation();
                assertThat(location).isNotNull();
                assertThat(location.toString()).endsWith("/surveys/" + surveyId);
                assertThat(OffsetDateTime.parse(res.get("start_date_time").asText())).isEqualTo(startDateTime);
                assertThat(OffsetDateTime.parse(res.get("end_date_time").asText())).isEqualTo(endDateTime);
                final Mono<Survey> surveyMono = this.surveyRepository.findById(Survey.Id.valueOf(surveyId));
                StepVerifier.create(surveyMono)
                    .consumeNextWith(survey -> {
                        assertThat(survey.getSurveyId().toString()).isEqualTo(surveyId);
                        assertThat(survey.getStartDateTime()).isEqualTo(startDateTime);
                        assertThat(survey.getEndDateTime()).isEqualTo(endDateTime);
                    })
                    .expectComplete()
                    .verify();
            })
            .consumeWith(
                document("post-surveys",
                    requestHeaders(headerWithName(CONTENT_TYPE).description(APPLICATION_JSON_VALUE)),
                    requestFields(
                        fieldWithPath("start_date_time").type(JsonFieldType.STRING).description("開始予定時刻"),
                        fieldWithPath("end_date_time").type(JsonFieldType.STRING).description("終了予定時刻")),
                    responseHeaders(
                        headerWithName(CONTENT_TYPE).description(APPLICATION_JSON_VALUE),
                        headerWithName(LOCATION).description("アンケートのURL")),
                    responseFields(
                        fieldWithPath("survey_id").type(JsonFieldType.STRING).description("アンケートID"),
                        fieldWithPath("start_date_time").type(JsonFieldType.STRING).description("開始予定時刻"),
                        fieldWithPath("end_date_time").type(JsonFieldType.STRING).description("終了予定時刻"))));


    }
}