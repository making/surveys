package am.ik.surveys.survey;

import am.ik.surveys.infra.sql.SqlSupplier;
import io.r2dbc.spi.Row;
import org.springframework.data.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.reactive.TransactionalOperator;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.OffsetDateTime;
import java.util.function.Function;

@Repository
public class SurveyRepository {

    private final DatabaseClient databaseClient;

    private final TransactionalOperator transactionalOperator;

    private final SqlSupplier sqlSupplier;

    private final Function<Row, Survey> surveyRowMapper = row -> new Survey.Builder()
        .withSurveyId(Survey.Id.valueOf(row.get("survey_id", String.class)))
        .withStartDateTime(row.get("start_date_time", OffsetDateTime.class))
        .withEndDateTime(row.get("end_date_time", OffsetDateTime.class))
        .build();

    public SurveyRepository(DatabaseClient databaseClient, TransactionalOperator transactionalOperator, SqlSupplier sqlSupplier) {
        this.databaseClient = databaseClient;
        this.transactionalOperator = transactionalOperator;
        this.sqlSupplier = sqlSupplier;
    }

    public Flux<Survey> findAll() {
        return this.databaseClient.execute(this.sqlSupplier.file("sql/survey/findAll.sql"))
            .map(this.surveyRowMapper)
            .all();
    }

    public Mono<Survey> findById(Survey.Id surveyId) {
        return this.databaseClient.execute(this.sqlSupplier.file("sql/survey/findById.sql"))
            .bind("survey_id", surveyId.toString())
            .map(this.surveyRowMapper)
            .one();
    }

    public Mono<Survey> insert(Mono<Survey> surveyMono) {
        return surveyMono.delayUntil(survey ->
            this.databaseClient.execute(this.sqlSupplier.file("sql/survey/insert.sql"))
                .bind("survey_id", survey.getSurveyId().toString())
                .bind("start_date_time", survey.getStartDateTime())
                .bind("end_date_time", survey.getEndDateTime())
                .then())
            .flatMap(survey -> this.findById(survey.getSurveyId()))
            .as(transactionalOperator::transactional);
    }

    public Mono<Void> deleteById(Survey.Id surveyId) {
        return this.databaseClient.execute(this.sqlSupplier.file("sql/survey/deleteById.sql"))
            .bind("survey_id", surveyId.toString())
            .then()
            .as(transactionalOperator::transactional);
    }
}
