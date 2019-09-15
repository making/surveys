package am.ik.surveys.surveyquestion;

import am.ik.surveys.infra.sql.SqlSupplier;
import am.ik.surveys.question.Question;
import am.ik.surveys.survey.Survey;
import org.springframework.data.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.reactive.TransactionalOperator;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public class SurveyQuestionRepository {

    private final DatabaseClient databaseClient;

    private final TransactionalOperator transactionalOperator;

    private final SqlSupplier sqlSupplier;

    public SurveyQuestionRepository(DatabaseClient databaseClient, TransactionalOperator transactionalOperator, SqlSupplier sqlSupplier) {
        this.databaseClient = databaseClient;
        this.transactionalOperator = transactionalOperator;
        this.sqlSupplier = sqlSupplier;
    }

    @SuppressWarnings("ConstantConditions")
    public Flux<SurveyQuestion> findBySurveyId(Survey.Id surveyId) {
        return this.databaseClient.execute(this.sqlSupplier.file("sql/surveyquestion/findBySurveyId.sql"))
            .bind("survey_id", surveyId.toString())
            .map(row -> new SurveyQuestion.Builder()
                .withSurveyId(Survey.Id.valueOf(row.get("survey_id", String.class)))
                .withQuestionId(Question.Id.valueOf(row.get("question_id", String.class)))
                .withRequired(row.get("required", Boolean.class))
                .build())
            .all();
    }

    public Mono<SurveyQuestion> insert(Mono<SurveyQuestion> surveyQuestionMono) {
        return surveyQuestionMono.delayUntil(surveyQuestion ->
            this.databaseClient.execute(this.sqlSupplier.file("sql/surveyquestion/insert.sql"))
                .bind("survey_id", surveyQuestion.getSurveyId().toString())
                .bind("question_id", surveyQuestion.getQuestionId().toString())
                .bind("required", surveyQuestion.isRequired())
                .then())
            .as(transactionalOperator::transactional);
    }

    public Mono<SurveyQuestion> insert(SurveyQuestion surveyQuestion) {
        return this.insert(Mono.just(surveyQuestion));
    }

    public Mono<Void> delete(SurveyQuestion surveyQuestion) {
        return this.databaseClient.execute(this.sqlSupplier.file("sql/surveyquestion/delete.sql"))
            .bind("survey_id", surveyQuestion.getSurveyId().toString())
            .bind("question_id", surveyQuestion.getQuestionId().toString())
            .then()
            .as(transactionalOperator::transactional);
    }

    public Mono<Void> deleteBySurveyId(Survey.Id surveyId) {
        return this.databaseClient.execute(this.sqlSupplier.file("sql/surveyquestion/deleteBySurveyId.sql"))
            .bind("survey_id", surveyId.toString())
            .then()
            .as(transactionalOperator::transactional);
    }
}
