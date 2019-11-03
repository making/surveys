package am.ik.surveys.answer.web;

import java.util.List;
import java.util.Map;

import am.ik.surveys.Fixtures;
import am.ik.surveys.TestUtils;
import am.ik.surveys.answer.Answer;
import am.ik.surveys.answer.AnswerDetail;
import am.ik.surveys.answer.AnswerDetailRepository;
import am.ik.surveys.answer.AnswerRepository;
import am.ik.surveys.answer.ChosenAnswer;
import am.ik.surveys.answer.DescriptiveAnswer;
import am.ik.surveys.infra.sql.SqlSupplier;
import am.ik.surveys.question.Question;
import am.ik.surveys.question.QuestionRepository;
import am.ik.surveys.questionchoice.QuestionChoice;
import am.ik.surveys.questionchoice.QuestionChoiceRepository;
import am.ik.surveys.survey.Survey;
import am.ik.surveys.survey.SurveyRepository;
import com.fasterxml.jackson.databind.JsonNode;
import de.huxhorn.sulky.ulid.ULID;
import io.r2dbc.spi.ConnectionFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import org.springframework.data.r2dbc.core.DatabaseClient;
import org.springframework.http.MediaType;
import org.springframework.restdocs.RestDocumentationContextProvider;
import org.springframework.restdocs.RestDocumentationExtension;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.transaction.reactive.TransactionalOperator;

import static am.ik.surveys.TestUtils.basicAdmin;
import static com.epages.restdocs.apispec.WebTestClientRestDocumentationWrapper.document;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
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
class AnswerHandlerTest {

	private ULID ulid = new ULID();

	private WebTestClient testClient;

	private AnswerRepository answerRepository;

	private AnswerDetailRepository answerDetailRepository;

	@BeforeEach
	void setUp(RestDocumentationContextProvider restDocumentation) {
		final ConnectionFactory connectionFactory = TestUtils.connectionFactory();
		final DatabaseClient databaseClient = TestUtils.databaseClient(connectionFactory);
		final TransactionalOperator transactionalOperator = TestUtils.transactionalOperator(connectionFactory);
		final SqlSupplier sqlSupplier = TestUtils.sqlSupplier();
		this.answerRepository = new AnswerRepository(databaseClient, transactionalOperator, sqlSupplier);
		this.answerDetailRepository = new AnswerDetailRepository(databaseClient, transactionalOperator, sqlSupplier);
		final SurveyRepository surveyRepository = new SurveyRepository(databaseClient, transactionalOperator,
				sqlSupplier);
		final QuestionRepository questionRepository = new QuestionRepository(databaseClient, transactionalOperator,
				sqlSupplier);
		final QuestionChoiceRepository questionChoiceRepository = new QuestionChoiceRepository(databaseClient,
				transactionalOperator, sqlSupplier);
		final AnswerHandler answerHandler = new AnswerHandler(this.ulid, answerRepository, answerDetailRepository);
		this.testClient = TestUtils.webTestClient(answerHandler.routes(), restDocumentation).build();

		Fixtures.surveys.forEach(survey -> StepVerifier.create(surveyRepository.insert(Mono.just(survey)))
				.expectNext(survey).verifyComplete());
		Fixtures.questions.forEach(question -> StepVerifier.create(questionRepository.insert(Mono.just(question)))
				.expectNext(question).verifyComplete());
		Fixtures.questionChoices.forEach(
				questionChoice -> StepVerifier.create(questionChoiceRepository.insert(Mono.just(questionChoice)))
						.expectNext(questionChoice).verifyComplete());
		Fixtures.answers.forEach(answer -> StepVerifier.create(this.answerRepository.insert(answer)).expectNext(answer)
				.verifyComplete());
		this.answerDetailRepository.insert(Fixtures.answerDetails).blockLast();
	}

	@Test
	void getAnswersBySurveyId() {
		final Survey survey = Fixtures.surveys.get(0);
		this.testClient.get().uri("/surveys/{survey_id}/answers", survey.getSurveyId()).headers(basicAdmin()).exchange()
				.expectStatus().isOk().expectBody(JsonNode.class).consumeWith(result -> {
					final JsonNode res = result.getResponseBody();
					assertThat(res).isNotNull();
					assertThat(res).hasSize(9);
				})
				.consumeWith(document("get-answers-by-survey-id",
						pathParameters(parameterWithName("survey_id").description("アンケートID")),
						requestHeaders(headerWithName(AUTHORIZATION).description("Basic認証")),
						responseHeaders(headerWithName(CONTENT_TYPE).description(APPLICATION_JSON_VALUE)),
						responseFields(fieldWithPath("[].answer_id").type(JsonFieldType.STRING).description("回答ID"),
								fieldWithPath("[].survey_id").type(JsonFieldType.STRING).description("アンケートID"),
								fieldWithPath("[].question_id").type(JsonFieldType.STRING).description("設問ID"),
								fieldWithPath("[].respondent_id").type(JsonFieldType.STRING).description("回答者ID"),
								fieldWithPath("[].details").type(JsonFieldType.ARRAY).description("回答詳細"),
								fieldWithPath("[].details[].answer_id").type(JsonFieldType.STRING).description("回答ID"),
								fieldWithPath("[].details[].answer_text").type(JsonFieldType.STRING).description("回答内容")
										.optional(),
								fieldWithPath("[].details[].question_choice_id").type(JsonFieldType.STRING)
										.description("設問選択肢ID").optional())));
	}

	@Test
	void postDescriptiveAnswers() {
		final Survey survey = Fixtures.surveys.get(0);
		final Question question = Fixtures.questions.get(1);
		final Map<String, Object> requestBody = Map.of("question_id", question.getQuestionId(), "respondent_id", "xyz",
				"details", List.of(Map.of("answer_text", "とても良い")));
		this.testClient.post().uri("/surveys/{survey_id}/answers", survey.getSurveyId())
				.contentType(MediaType.APPLICATION_JSON).bodyValue(requestBody).exchange().expectStatus().isCreated()
				.expectBody(JsonNode.class).consumeWith(result -> {
					final JsonNode res = result.getResponseBody();
					assertThat(res).isNotNull();
					final String answerId = res.get("answer_id").asText();
					assertThat(answerId).isNotEmpty();
					assertThat(Answer.Id.valueOf(answerId)).isNotNull();
					assertThat(res.get("survey_id").asText()).isEqualTo(survey.getSurveyId().toString());
					assertThat(res.get("question_id").asText()).isEqualTo(question.getQuestionId().toString());
					assertThat(res.get("respondent_id").asText()).isEqualTo("xyz");
					final JsonNode details = res.get("details");
					assertThat(details).hasSize(1);
					assertThat(details.get(0).get("answer_id").asText()).isEqualTo(answerId);
					assertThat(details.get(0).get("answer_text").asText()).isEqualTo("とても良い");
					final Mono<Answer> answerMono = this.answerRepository.findById(Answer.Id.valueOf(answerId));
					StepVerifier.create(answerMono).consumeNextWith(answer -> {
						assertThat(answer.getAnswerId().toString()).isEqualTo(answerId);
						assertThat(answer.getSurveyId()).isEqualTo(survey.getSurveyId());
						assertThat(answer.getQuestionId()).isEqualTo(question.getQuestionId());
						assertThat(answer.getRespondentId().toString()).isEqualTo("xyz");
					}).expectComplete().verify();
					final Flux<AnswerDetail<?>> answerDetailFlux = this.answerDetailRepository
							.findAllByAnswerId(Answer.Id.valueOf(answerId));
					StepVerifier.create(answerDetailFlux).consumeNextWith(answerDetail -> {
						assertThat(answerDetail.getAnswerId().toString()).isEqualTo(answerId);
						assertThat(answerDetail).isInstanceOf(DescriptiveAnswer.class);
						assertThat(((DescriptiveAnswer) answerDetail).getAnswerText()).isEqualTo("とても良い");
					}).expectComplete().verify();
				})
				.consumeWith(document("post-descriptive-answers",
						pathParameters(parameterWithName("survey_id").description("アンケートID")),
						requestHeaders(headerWithName(CONTENT_TYPE).description(APPLICATION_JSON_VALUE)),
						requestFields(fieldWithPath("question_id").type(JsonFieldType.STRING).description("設問ID"),
								fieldWithPath("respondent_id").type(JsonFieldType.STRING).description("回答者ID"),
								fieldWithPath("details").type(JsonFieldType.ARRAY).description("回答詳細"),
								fieldWithPath("details[].answer_text").type(JsonFieldType.STRING).description("回答内容")),
						responseHeaders(headerWithName(CONTENT_TYPE).description(APPLICATION_JSON_VALUE),
								headerWithName(LOCATION).description("回答のURL")),
						responseFields(fieldWithPath("answer_id").type(JsonFieldType.STRING).description("回答ID"),
								fieldWithPath("survey_id").type(JsonFieldType.STRING).description("アンケートID"),
								fieldWithPath("question_id").type(JsonFieldType.STRING).description("設問ID"),
								fieldWithPath("respondent_id").type(JsonFieldType.STRING).description("回答者ID"),
								fieldWithPath("details").type(JsonFieldType.ARRAY).description("回答詳細"),
								fieldWithPath("details[].answer_id").type(JsonFieldType.STRING).description("回答ID"),
								fieldWithPath("details[].answer_text").type(JsonFieldType.STRING)
										.description("回答内容"))));
	}

	@Test
	void postSelectiveAnswers() {
		final Survey survey = Fixtures.surveys.get(0);
		final Question question = Fixtures.questions.get(0);
		final QuestionChoice questionChoice = Fixtures.questionChoices.get(5);
		final Map<String, Object> requestBody = Map.of("question_id", question.getQuestionId(), "respondent_id", "xyz",
				"details", List.of(Map.of("question_choice_id", questionChoice.getQuestionChoiceId())));
		this.testClient.post().uri("/surveys/{survey_id}/answers", survey.getSurveyId())
				.contentType(MediaType.APPLICATION_JSON).bodyValue(requestBody).exchange().expectStatus().isCreated()
				.expectBody(JsonNode.class).consumeWith(result -> {
					final JsonNode res = result.getResponseBody();
					assertThat(res).isNotNull();
					final String answerId = res.get("answer_id").asText();
					assertThat(answerId).isNotEmpty();
					assertThat(Answer.Id.valueOf(answerId)).isNotNull();
					assertThat(res.get("survey_id").asText()).isEqualTo(survey.getSurveyId().toString());
					assertThat(res.get("question_id").asText()).isEqualTo(question.getQuestionId().toString());
					assertThat(res.get("respondent_id").asText()).isEqualTo("xyz");
					final JsonNode details = res.get("details");
					assertThat(details).hasSize(1);
					assertThat(details.get(0).get("answer_id").asText()).isEqualTo(answerId);
					assertThat(details.get(0).get("question_choice_id").asText())
							.isEqualTo(questionChoice.getQuestionChoiceId().toString());
					final Mono<Answer> answerMono = this.answerRepository.findById(Answer.Id.valueOf(answerId));
					StepVerifier.create(answerMono).consumeNextWith(answer -> {
						assertThat(answer.getAnswerId().toString()).isEqualTo(answerId);
						assertThat(answer.getSurveyId()).isEqualTo(survey.getSurveyId());
						assertThat(answer.getQuestionId()).isEqualTo(question.getQuestionId());
						assertThat(answer.getRespondentId().toString()).isEqualTo("xyz");
					}).expectComplete().verify();
					final Flux<AnswerDetail<?>> answerDetailFlux = this.answerDetailRepository
							.findAllByAnswerId(Answer.Id.valueOf(answerId));
					StepVerifier.create(answerDetailFlux).consumeNextWith(answerDetail -> {
						assertThat(answerDetail.getAnswerId().toString()).isEqualTo(answerId);
						assertThat(answerDetail).isInstanceOf(ChosenAnswer.class);
						assertThat(((ChosenAnswer) answerDetail).getquestionChoiceId())
								.isEqualTo(questionChoice.getQuestionChoiceId());
					}).expectComplete().verify();
				})
				.consumeWith(document("post-selective-answers",
						pathParameters(parameterWithName("survey_id").description("アンケートID")),
						requestHeaders(headerWithName(CONTENT_TYPE).description(APPLICATION_JSON_VALUE)),
						requestFields(fieldWithPath("question_id").type(JsonFieldType.STRING).description("設問ID"),
								fieldWithPath("respondent_id").type(JsonFieldType.STRING).description("回答者ID"),
								fieldWithPath("details").type(JsonFieldType.ARRAY).description("回答詳細"),
								fieldWithPath("details[].question_choice_id").type(JsonFieldType.STRING)
										.description("設問選択肢ID"),
								fieldWithPath("details[].answer_text").type(JsonFieldType.STRING).description("回答内容")
										.optional()),
						responseHeaders(headerWithName(CONTENT_TYPE).description(APPLICATION_JSON_VALUE),
								headerWithName(LOCATION).description("回答のURL")),
						responseFields(fieldWithPath("answer_id").type(JsonFieldType.STRING).description("回答ID"),
								fieldWithPath("survey_id").type(JsonFieldType.STRING).description("アンケートID"),
								fieldWithPath("question_id").type(JsonFieldType.STRING).description("設問ID"),
								fieldWithPath("respondent_id").type(JsonFieldType.STRING).description("回答者ID"),
								fieldWithPath("details").type(JsonFieldType.ARRAY).description("回答詳細"),
								fieldWithPath("details[].answer_id").type(JsonFieldType.STRING).description("回答ID"),
								fieldWithPath("details[].answer_text").type(JsonFieldType.STRING).description("回答内容")
										.optional(),
								fieldWithPath("details[].question_choice_id").type(JsonFieldType.STRING)
										.description("設問選択肢ID").optional())));
	}

	@Test
	void getSelectiveAnswer() {
		final Answer answer = Fixtures.answers.get(2);
		this.testClient.get().uri("/answers/{answer_id}", answer.getAnswerId()).headers(basicAdmin()).exchange()
				.expectStatus().isOk().expectBody(JsonNode.class).consumeWith(result -> {
					final JsonNode res = result.getResponseBody();
					assertThat(res).isNotNull();
					assertThat(res.get("answer_id").asText()).isEqualTo(answer.getAnswerId().toString());
					assertThat(res.get("survey_id").asText()).isEqualTo(answer.getSurveyId().toString());
					assertThat(res.get("question_id").asText()).isEqualTo(answer.getQuestionId().toString());
					assertThat(res.get("respondent_id").asText()).isEqualTo(answer.getRespondentId().toString());
					assertThat(res.get("details")).hasSize(2);
				})
				.consumeWith(document("get-selective-answer",
						pathParameters(parameterWithName("answer_id").description("回答ID")),
						requestHeaders(headerWithName(AUTHORIZATION).description("Basic認証")),
						responseHeaders(headerWithName(CONTENT_TYPE).description(APPLICATION_JSON_VALUE)),
						responseFields(fieldWithPath("answer_id").type(JsonFieldType.STRING).description("回答ID"),
								fieldWithPath("survey_id").type(JsonFieldType.STRING).description("アンケートID"),
								fieldWithPath("question_id").type(JsonFieldType.STRING).description("設問ID"),
								fieldWithPath("respondent_id").type(JsonFieldType.STRING).description("回答者ID"),
								fieldWithPath("details").type(JsonFieldType.ARRAY).description("回答詳細"),
								fieldWithPath("details[].answer_id").type(JsonFieldType.STRING).description("回答ID"),
								fieldWithPath("details[].answer_text").type(JsonFieldType.STRING).description("回答内容")
										.optional(),
								fieldWithPath("details[].question_choice_id").type(JsonFieldType.STRING)
										.description("設問選択肢ID"))));
	}

	@Test
	void getDescriptiveAnswer() {
		final Answer answer = Fixtures.answers.get(1);
		this.testClient.get().uri("/answers/{answer_id}", answer.getAnswerId()).headers(basicAdmin()).exchange()
				.expectStatus().isOk().expectBody(JsonNode.class).consumeWith(result -> {
					final JsonNode res = result.getResponseBody();
					assertThat(res).isNotNull();
					assertThat(res.get("answer_id").asText()).isEqualTo(answer.getAnswerId().toString());
					assertThat(res.get("survey_id").asText()).isEqualTo(answer.getSurveyId().toString());
					assertThat(res.get("question_id").asText()).isEqualTo(answer.getQuestionId().toString());
					assertThat(res.get("respondent_id").asText()).isEqualTo(answer.getRespondentId().toString());
					assertThat(res.get("details")).hasSize(1);
				})
				.consumeWith(document("get-descriptive-answer",
						pathParameters(parameterWithName("answer_id").description("回答ID")),
						requestHeaders(headerWithName(AUTHORIZATION).description("Basic認証")),
						responseHeaders(headerWithName(CONTENT_TYPE).description(APPLICATION_JSON_VALUE)),
						responseFields(fieldWithPath("answer_id").type(JsonFieldType.STRING).description("回答ID"),
								fieldWithPath("survey_id").type(JsonFieldType.STRING).description("アンケートID"),
								fieldWithPath("question_id").type(JsonFieldType.STRING).description("設問ID"),
								fieldWithPath("respondent_id").type(JsonFieldType.STRING).description("回答者ID"),
								fieldWithPath("details").type(JsonFieldType.ARRAY).description("回答詳細"),
								fieldWithPath("details[].answer_id").type(JsonFieldType.STRING).description("回答ID"),
								fieldWithPath("details[].answer_text").type(JsonFieldType.STRING)
										.description("回答内容"))));
	}

	@Test
	void deleteAnswer() {
		final Answer answer = Fixtures.answers.get(0);
		this.testClient.delete().uri("/answers/{answer_id}", answer.getAnswerId()).headers(basicAdmin()).exchange()
				.expectStatus().isNoContent().expectBody(JsonNode.class).consumeWith(
						document("delete-answer", pathParameters(parameterWithName("answer_id").description("回答ID")),
								requestHeaders(headerWithName(AUTHORIZATION).description("Basic認証"))));
	}
}