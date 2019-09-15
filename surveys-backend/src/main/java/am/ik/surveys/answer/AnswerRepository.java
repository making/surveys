package am.ik.surveys.answer;

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
public class AnswerRepository {

    private final DatabaseClient databaseClient;

    private final TransactionalOperator transactionalOperator;

    private final SqlSupplier sqlSupplier;

    private final Function<Row, Answer> answerRowMapper = row -> new Answer.Builder()
        .withAnswerId(Answer.Id.valueOf(row.get("answer_id", String.class)))
        .withSurveyId(Survey.Id.valueOf(row.get("survey_id", String.class)))
        .withQuestionId(Question.Id.valueOf(row.get("question_id", String.class)))
        .withRespondentId(Respondent.Id.valueOf(row.get("respondent_id", String.class)))
        .build();

    public AnswerRepository(DatabaseClient databaseClient, TransactionalOperator transactionalOperator, SqlSupplier sqlSupplier) {
        this.databaseClient = databaseClient;
        this.transactionalOperator = transactionalOperator;
        this.sqlSupplier = sqlSupplier;
    }

    public Mono<Answer> findById(Answer.Id answerId) {
        return this.databaseClient.execute(this.sqlSupplier.file("sql/answer/findAllAnswerById.sql"))
            .bind("answer_id", answerId.toString())
            .map(answerRowMapper)
            .one();
    }

    public Flux<Answer> findAllBySurveyId(Survey.Id surveyId) {
        return this.databaseClient.execute(this.sqlSupplier.file("sql/answer/findAllAnswerBySurveyId.sql"))
            .bind("survey_id", surveyId.toString())
            .map(answerRowMapper)
            .all();
    }

    public Mono<Answer> insert(Answer answer) {
        return this.databaseClient.execute(this.sqlSupplier.file("sql/answer/insertAnswer.sql"))
            .bind("answer_id", answer.getAnswerId().toString())
            .bind("survey_id", answer.getSurveyId().toString())
            .bind("question_id", answer.getQuestionId().toString())
            .bind("respondent_id", answer.getRespondentId().toString())
            .then()
            .as(transactionalOperator::transactional)
            .then(Mono.just(answer));
    }


    public Mono<Void> deleteById(Answer.Id answerId) {
        return this.databaseClient.execute(this.sqlSupplier.file("sql/answer/deleteAnswerById.sql"))
            .bind("answer_id", answerId.toString())
            .then()
            .as(transactionalOperator::transactional);
    }
}
