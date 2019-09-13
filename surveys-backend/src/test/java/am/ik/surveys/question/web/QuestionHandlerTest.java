package am.ik.surveys.question.web;

import am.ik.surveys.Fixtures;
import am.ik.surveys.TestUtils;
import am.ik.surveys.question.Question;
import am.ik.surveys.question.QuestionRepository;
import am.ik.surveys.question.SelectiveQuestion;
import am.ik.surveys.questionchoice.QuestionChoice;
import am.ik.surveys.questionchoice.QuestionChoiceRepository;
import com.fasterxml.jackson.databind.JsonNode;
import de.huxhorn.sulky.ulid.ULID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.http.MediaType;
import org.springframework.restdocs.RestDocumentationContextProvider;
import org.springframework.restdocs.RestDocumentationExtension;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.net.URI;
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
class QuestionHandlerTest {

    private ULID ulid = new ULID();

    private WebTestClient testClient;

    private QuestionRepository questionRepository;

    private QuestionChoiceRepository questionChoiceRepository;

    @BeforeEach
    void setUp(RestDocumentationContextProvider restDocumentation) {
        this.questionChoiceRepository = new QuestionChoiceRepository();
        this.questionRepository = new QuestionRepository();
        final QuestionHandler questionHandler = new QuestionHandler(this.ulid, questionRepository, questionChoiceRepository);

        this.testClient = TestUtils.webTestClient(questionHandler.routes(), restDocumentation)
            .build();
    }

    @Test
    void postQuestions() {
        final Map<String, String> requestBody = Map.of("question_text", "自由回答の質問");
        this.testClient.post()
            .uri("/questions")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(requestBody)
            .exchange()
            .expectStatus().isCreated()
            .expectBody(JsonNode.class)
            .consumeWith(result -> {
                final JsonNode res = result.getResponseBody();
                assertThat(res).isNotNull();
                final String questionId = res.get("question_id").asText();
                assertThat(questionId).isNotEmpty();
                assertThat(ULID.parseULID(questionId)).isNotNull();
                assertThat(res.get("question_text").asText()).isEqualTo("自由回答の質問");
                final URI location = result.getResponseHeaders().getLocation();
                assertThat(location).isNotNull();
                assertThat(location.toString()).endsWith("/questions/" + questionId);
                final Mono<Question> questionMono = this.questionRepository.findById(Question.Id.valueOf(questionId));
                StepVerifier.create(questionMono)
                    .consumeNextWith(question -> {
                        assertThat(question.getQuestionId().toString()).isEqualTo(questionId);
                        assertThat(question.getQuestionText()).isEqualTo("自由回答の質問");
                    })
                    .expectComplete()
                    .verify();
            })
            .consumeWith(
                document("post-questions",
                    requestHeaders(headerWithName(CONTENT_TYPE).description(APPLICATION_JSON_VALUE)),
                    requestFields(
                        fieldWithPath("question_text").type(JsonFieldType.STRING).description("設問文")),
                    responseHeaders(
                        headerWithName(CONTENT_TYPE).description(APPLICATION_JSON_VALUE),
                        headerWithName(LOCATION).description("設問のURL")),
                    responseFields(
                        fieldWithPath("question_id").type(JsonFieldType.STRING).description("設問ID"),
                        fieldWithPath("question_text").type(JsonFieldType.STRING).description("設問文"))));
    }

    @Test
    void postSelectiveQuestions() {
        final Map<String, Object> requestBody = Map.of("question_text", "選択回答の質問", "max_choices", 3);
        this.testClient.post()
            .uri("/questions")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(requestBody)
            .exchange()
            .expectStatus().isCreated()
            .expectBody(JsonNode.class)
            .consumeWith(result -> {
                final JsonNode res = result.getResponseBody();
                assertThat(res).isNotNull();
                final String questionId = res.get("question_id").asText();
                assertThat(questionId).isNotEmpty();
                assertThat(ULID.parseULID(questionId)).isNotNull();
                assertThat(res.get("question_text").asText()).isEqualTo("選択回答の質問");
                assertThat(res.get("max_choices").asInt()).isEqualTo(3);
                final URI location = result.getResponseHeaders().getLocation();
                assertThat(location).isNotNull();
                assertThat(location.toString()).endsWith("/questions/" + questionId);
                final Mono<Question> questionMono = this.questionRepository.findById(Question.Id.valueOf(questionId));
                StepVerifier.create(questionMono)
                    .consumeNextWith(question -> {
                        assertThat(question).isInstanceOf(SelectiveQuestion.class);
                        assertThat(question.getQuestionId().toString()).isEqualTo(questionId);
                        assertThat(question.getQuestionText()).isEqualTo("選択回答の質問");
                        assertThat(((SelectiveQuestion) question).getMaxChoices()).isEqualTo(3);
                    })
                    .expectComplete()
                    .verify();
            })
            .consumeWith(
                document("post-selective-questions",
                    requestHeaders(headerWithName(CONTENT_TYPE).description(APPLICATION_JSON_VALUE)),
                    requestFields(
                        fieldWithPath("question_text").type(JsonFieldType.STRING).description("設問文"),
                        fieldWithPath("max_choices").type(JsonFieldType.NUMBER).description("選択可能数")),
                    responseHeaders(
                        headerWithName(CONTENT_TYPE).description(APPLICATION_JSON_VALUE),
                        headerWithName(LOCATION).description("設問のURL")),
                    responseFields(
                        fieldWithPath("question_id").type(JsonFieldType.STRING).description("設問ID"),
                        fieldWithPath("question_text").type(JsonFieldType.STRING).description("設問文"),
                        fieldWithPath("max_choices").type(JsonFieldType.NUMBER).description("選択可能数"))));
    }

    @Test
    void getQuestion() {
        final Question question = Fixtures.questions.get(0);
        this.testClient.get()
            .uri("/questions/{question_id}", question.getQuestionId())
            .exchange()
            .expectStatus().isOk()
            .expectBody(JsonNode.class)
            .consumeWith(result -> {
                final JsonNode res = result.getResponseBody();
                assertThat(res).isNotNull();
                final String questionId = question.getQuestionId().toString();
                assertThat(res.get("question_id").asText()).isEqualTo(questionId);
                assertThat(res.get("question_text").asText()).isEqualTo("この設計はいけてますか?");
                assertThat(res.get("max_choices").asInt()).isEqualTo(1);
                final JsonNode questionChoices = res.get("question_choices");
                assertThat(questionChoices).hasSize(2);
                assertThat(questionChoices.get(0).get("question_choice_id").asText()).isNotEmpty();
                assertThat(questionChoices.get(0).get("question_id").asText()).isEqualTo(questionId);
                assertThat(questionChoices.get(0).get("question_choice_text").asText()).isEqualTo("はい");
                assertThat(questionChoices.get(0).get("allow_free_text").asBoolean()).isEqualTo(false);
                assertThat(questionChoices.get(1).get("question_choice_id").asText()).isNotEmpty();
                assertThat(questionChoices.get(1).get("question_id").asText()).isEqualTo(questionId);
                assertThat(questionChoices.get(1).get("question_choice_text").asText()).isEqualTo("いいえ");
                assertThat(questionChoices.get(1).get("allow_free_text").asBoolean()).isEqualTo(false);
            })
            .consumeWith(
                document("get-question",
                    pathParameters(parameterWithName("question_id").description("設問ID")),
                    responseHeaders(headerWithName(CONTENT_TYPE).description(APPLICATION_JSON_VALUE)),
                    responseFields(
                        fieldWithPath("question_id").type(JsonFieldType.STRING).description("設問ID"),
                        fieldWithPath("question_text").type(JsonFieldType.STRING).description("設問文"),
                        fieldWithPath("max_choices").type(JsonFieldType.NUMBER).description("選択可能数").optional(),
                        fieldWithPath("question_choices").type(JsonFieldType.ARRAY).description("設問選択肢").optional(),
                        fieldWithPath("question_choices[].question_choice_id").type(JsonFieldType.STRING).description("設問選択肢ID").optional(),
                        fieldWithPath("question_choices[].question_id").type(JsonFieldType.STRING).description("設問ID").optional(),
                        fieldWithPath("question_choices[].question_choice_text").type(JsonFieldType.STRING).description("選択肢本文").optional(),
                        fieldWithPath("question_choices[].allow_free_text").type(JsonFieldType.BOOLEAN).description("自由記述可").optional())));
    }

    @Test
    void getQuestionChoices() {
        final Question question = Fixtures.questions.get(0);
        this.testClient.get()
            .uri("/questions/{question_id}/question_choices", question.getQuestionId())
            .exchange()
            .expectStatus().isOk()
            .expectBody(JsonNode.class)
            .consumeWith(result -> {
                final JsonNode res = result.getResponseBody();
                assertThat(res).isNotNull();
                assertThat(res).hasSize(2);
                final String questionId = question.getQuestionId().toString();
                assertThat(res.get(0).get("question_choice_id").asText()).isNotEmpty();
                assertThat(res.get(0).get("question_id").asText()).isEqualTo(questionId);
                assertThat(res.get(0).get("question_choice_text").asText()).isEqualTo("はい");
                assertThat(res.get(0).get("allow_free_text").asBoolean()).isEqualTo(false);
                assertThat(res.get(1).get("question_choice_id").asText()).isNotEmpty();
                assertThat(res.get(1).get("question_id").asText()).isEqualTo(questionId);
                assertThat(res.get(1).get("question_choice_text").asText()).isEqualTo("いいえ");
                assertThat(res.get(1).get("allow_free_text").asBoolean()).isEqualTo(false);
            })
            .consumeWith(
                document("get-question-question-choices",
                    pathParameters(parameterWithName("question_id").description("設問ID")),
                    responseHeaders(headerWithName(CONTENT_TYPE).description(APPLICATION_JSON_VALUE)),
                    responseFields(
                        fieldWithPath("[].question_choice_id").type(JsonFieldType.STRING).description("設問選択肢ID"),
                        fieldWithPath("[].question_id").type(JsonFieldType.STRING).description("設問ID"),
                        fieldWithPath("[].question_choice_text").type(JsonFieldType.STRING).description("選択肢本文"),
                        fieldWithPath("[].allow_free_text").type(JsonFieldType.BOOLEAN).description("自由記述可"))));
    }

    @Test
    void postQuestionChoices() {
        final Question question = Fixtures.questions.get(0);
        final Map<String, Object> requestBody = Map.of("question_choice_text", "その他", "allow_free_text", true);
        this.testClient.post()
            .uri("/questions/{question_id}/question_choices", question.getQuestionId())
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(requestBody)
            .exchange()
            .expectStatus().isCreated()
            .expectBody(JsonNode.class)
            .consumeWith(result -> {
                final JsonNode res = result.getResponseBody();
                assertThat(res).isNotNull();
                final String questionId = res.get("question_id").asText();
                final String questionChoiceId = res.get("question_choice_id").asText();
                assertThat(questionId).isNotEmpty();
                assertThat(ULID.parseULID(questionId)).isNotNull();
                assertThat(questionChoiceId).isNotEmpty();
                assertThat(ULID.parseULID(questionChoiceId)).isNotNull();
                assertThat(res.get("question_choice_text").asText()).isEqualTo("その他");
                assertThat(res.get("allow_free_text").asBoolean()).isEqualTo(true);
                final URI location = result.getResponseHeaders().getLocation();
                assertThat(location).isNotNull();
                assertThat(location.toString()).endsWith("/question_choices/" + questionChoiceId);
                final Mono<QuestionChoice> questionChoiceMono = this.questionChoiceRepository
                    .findById(QuestionChoice.Id.valueOf(questionChoiceId));
                StepVerifier.create(questionChoiceMono)
                    .consumeNextWith(questionChoice -> {
                        assertThat(questionChoice.getQuestionId().toString()).isEqualTo(questionId);
                        assertThat(questionChoice.getQuestionChoiceId().toString()).isEqualTo(questionChoiceId);
                        assertThat(questionChoice.getQuestionChoiceText()).isEqualTo("その他");
                        assertThat(questionChoice.isAllowFreeText()).isEqualTo(true);
                    })
                    .expectComplete()
                    .verify();
            })
            .consumeWith(
                document("post-question-question-choices",
                    requestHeaders(headerWithName(CONTENT_TYPE).description(APPLICATION_JSON_VALUE)),
                    requestFields(
                        fieldWithPath("question_choice_text").type(JsonFieldType.STRING).description("選択肢本文"),
                        fieldWithPath("allow_free_text").type(JsonFieldType.BOOLEAN).description("自由記述可").optional()),
                    responseHeaders(
                        headerWithName(CONTENT_TYPE).description(APPLICATION_JSON_VALUE),
                        headerWithName(LOCATION).description("設問選択肢のURL")),
                    responseFields(
                        fieldWithPath("question_choice_id").type(JsonFieldType.STRING).description("設問選択肢ID"),
                        fieldWithPath("question_id").type(JsonFieldType.STRING).description("設問ID"),
                        fieldWithPath("question_choice_text").type(JsonFieldType.STRING).description("選択肢本文"),
                        fieldWithPath("allow_free_text").type(JsonFieldType.BOOLEAN).description("自由記述可"))));
    }
}