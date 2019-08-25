package am.ik.surveys.questionchoice.web;

import am.ik.surveys.Fixtures;
import am.ik.surveys.TestUtils;
import am.ik.surveys.questionchoice.QuestionChoice;
import am.ik.surveys.questionchoice.QuestionChoiceRepository;
import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.restdocs.RestDocumentationContextProvider;
import org.springframework.restdocs.RestDocumentationExtension;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static com.epages.restdocs.apispec.WebTestClientRestDocumentationWrapper.document;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.restdocs.headers.HeaderDocumentation.headerWithName;
import static org.springframework.restdocs.headers.HeaderDocumentation.responseHeaders;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.pathParameters;

@ExtendWith({RestDocumentationExtension.class})
class QuestionChoiceHandlerTest {

    private WebTestClient testClient;

    private QuestionChoiceRepository questionChoiceRepository;

    @BeforeEach
    void setUp(RestDocumentationContextProvider restDocumentation) {
        this.questionChoiceRepository = new QuestionChoiceRepository();
        final QuestionChoiceHandler questionChoiceHandler = new QuestionChoiceHandler(questionChoiceRepository);
        this.testClient = TestUtils.webTestClient(questionChoiceHandler.routes(), restDocumentation)
            .build();
    }

    @Test
    void getQuestionChoice() {
        final QuestionChoice questionChoice = Fixtures.questionChoices.get(0);
        this.testClient.get()
            .uri("/question_choices/{question_choice_id}", questionChoice.getQuestionChoiceId())
            .exchange()
            .expectStatus().isOk()
            .expectBody(JsonNode.class)
            .consumeWith(result -> {
                final JsonNode res = result.getResponseBody();
                assertThat(res).isNotNull();
                assertThat(res.get("question_choice_id").asText()).isEqualTo(questionChoice.getQuestionChoiceId().toString());
                assertThat(res.get("question_id").asText()).isEqualTo(questionChoice.getQuestionId().toString());
                assertThat(res.get("question_choice_text").asText()).isEqualTo("在庫");
                assertThat(res.get("allow_free_text").asBoolean()).isEqualTo(false);
            })
            .consumeWith(
                document("get-question-choice",
                    pathParameters(parameterWithName("question_choice_id").description("設問選択肢ID")),
                    responseHeaders(headerWithName(CONTENT_TYPE).description(APPLICATION_JSON_VALUE)),
                    responseFields(
                        fieldWithPath("question_choice_id").type(JsonFieldType.STRING).description("設問選択肢ID"),
                        fieldWithPath("question_id").type(JsonFieldType.STRING).description("設問ID"),
                        fieldWithPath("question_choice_text").type(JsonFieldType.STRING).description("選択肢本文"),
                        fieldWithPath("allow_free_text").type(JsonFieldType.BOOLEAN).description("自由記述可"))));
    }

    @Test
    void deleteQuestionChoice() {
        final QuestionChoice questionChoice = Fixtures.questionChoices.get(0);
        this.testClient.delete()
            .uri("/question_choices/{question_choice_id}", questionChoice.getQuestionChoiceId())
            .exchange()
            .expectStatus().isNoContent()
            .expectBody(JsonNode.class)
            .consumeWith(
                document("delete-question-choice",
                    pathParameters(parameterWithName("question_choice_id").description("設問選択肢ID"))));

        final Mono<QuestionChoice> questionChoiceMono = this.questionChoiceRepository.findById(questionChoice.getQuestionChoiceId());
        StepVerifier.create(questionChoiceMono)
            .expectComplete()
            .verify();
    }
}