package am.ik.surveys.questionchoice;

import am.ik.surveys.infra.sql.SqlSupplier;
import am.ik.surveys.question.Question;
import am.ik.surveys.survey.Survey;
import io.r2dbc.spi.Row;
import org.springframework.data.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.reactive.TransactionalOperator;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.function.Function;

@Repository
public class QuestionChoiceRepository {

    private final DatabaseClient databaseClient;

    private final TransactionalOperator transactionalOperator;

    private final SqlSupplier sqlSupplier;

    @SuppressWarnings("ConstantConditions")
    private final Function<Row, QuestionChoice> questionChoiceRowMapper = row -> new QuestionChoice.Builder()
        .withQuestionChoiceId(QuestionChoice.Id.valueOf(row.get("question_choice_id", String.class)))
        .withQuestionChoiceText(row.get("question_choice_text", String.class))
        .withQuestionId(Question.Id.valueOf(row.get("question_id", String.class)))
        .withAllowFreeText(row.get("allow_free_text", Boolean.class))
        .build();

    public QuestionChoiceRepository(DatabaseClient databaseClient, TransactionalOperator transactionalOperator, SqlSupplier sqlSupplier) {
        this.databaseClient = databaseClient;
        this.transactionalOperator = transactionalOperator;
        this.sqlSupplier = sqlSupplier;
    }

    public Mono<QuestionChoice> findById(QuestionChoice.Id questionChoiceId) {
        return this.databaseClient.execute(this.sqlSupplier.file("sql/questionchoice/findById.sql"))
            .bind("question_choice_id", questionChoiceId.toString())
            .map(questionChoiceRowMapper)
            .one();
    }

    public Flux<QuestionChoice> findAllByQuestionId(Question.Id questionId) {
        return this.databaseClient.execute(this.sqlSupplier.file("sql/questionchoice/findAllByQuestionId.sql"))
            .bind("question_id", questionId.toString())
            .map(questionChoiceRowMapper)
            .all();
    }

    public Flux<QuestionChoice> findAllBySurveyId(Survey.Id surveyId) {
        return this.databaseClient.execute(this.sqlSupplier.file("sql/questionchoice/findAllBySurveyId.sql"))
            .bind("survey_id", surveyId.toString())
            .map(questionChoiceRowMapper)
            .all();
    }

    public Mono<QuestionChoice> insert(Mono<QuestionChoice> questionChoiceMono) {
        return questionChoiceMono.delayUntil(questionChoice ->
            this.databaseClient.execute(this.sqlSupplier.file("sql/questionchoice/insert.sql"))
                .bind("question_choice_id", questionChoice.getQuestionChoiceId().toString())
                .bind("question_id", questionChoice.getQuestionId().toString())
                .bind("question_choice_text", questionChoice.getQuestionChoiceText())
                .bind("allow_free_text", questionChoice.isAllowFreeText())
                .then())
            .as(transactionalOperator::transactional);
    }

    public Mono<Void> deleteById(QuestionChoice.Id questionChoiceId) {
        return this.databaseClient.execute(this.sqlSupplier.file("sql/questionchoice/deleteById.sql"))
            .bind("question_choice_id", questionChoiceId.toString())
            .then()
            .as(transactionalOperator::transactional);
    }
}
