package am.ik.surveys.question;

import am.ik.surveys.infra.sql.SqlSupplier;
import am.ik.surveys.survey.Survey;
import io.r2dbc.spi.Row;
import org.springframework.data.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.reactive.TransactionalOperator;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.function.Function;

@Repository
public class QuestionRepository {

    private final DatabaseClient databaseClient;

    private final TransactionalOperator transactionalOperator;

    private final SqlSupplier sqlSupplier;

    private final Function<Row, Question> questionRowMapper = row -> {
        final String questionId = row.get("question_id", String.class);
        final Integer maxChoices = row.get("max_choices", Integer.class);
        final String questionText = row.get("question_text", String.class);
        if (maxChoices == null) {
            return new Question.Builder()
                .withQuestionId(Question.Id.valueOf(questionId))
                .withQuestionText(questionText)
                .build();
        } else {
            return new SelectiveQuestion.Builder()
                .withQuestionId(Question.Id.valueOf(questionId))
                .withQuestionText(questionText)
                .withMaxChoices(maxChoices)
                .build();
        }
    };

    public QuestionRepository(DatabaseClient databaseClient, TransactionalOperator transactionalOperator, SqlSupplier sqlSupplier) {
        this.databaseClient = databaseClient;
        this.transactionalOperator = transactionalOperator;
        this.sqlSupplier = sqlSupplier;
    }

    public Mono<Question> findById(Question.Id questionId) {
        return this.databaseClient.execute(this.sqlSupplier.file("sql/question/findQuestionById.sql"))
            .bind("question_id", questionId.toString())
            .map(this.questionRowMapper)
            .one();
    }

    public Flux<Question> findBySurveyId(Survey.Id surveyId) {
        return this.databaseClient.execute(this.sqlSupplier.file("sql/question/findAllQuestionBySurveyId.sql"))
            .bind("survey_id", surveyId.toString())
            .map(this.questionRowMapper)
            .all();
    }

    public Mono<Question> insert(Mono<Question> questionMono) {
        return questionMono.delayUntil(question -> {
            if (question instanceof SelectiveQuestion) {
                final SelectiveQuestion selectiveQuestion = (SelectiveQuestion) question;
                return this.databaseClient.execute(this.sqlSupplier.file("sql/question/insertSelectiveQuestion.sql"))
                    .bind("question_id", question.getQuestionId().toString())
                    .bind("question_text", question.getQuestionText())
                    .bind("max_choices", selectiveQuestion.getMaxChoices())
                    .then();
            } else {
                return this.databaseClient.execute(this.sqlSupplier.file("sql/question/insertQuestion.sql"))
                    .bind("question_id", question.getQuestionId().toString())
                    .bind("question_text", question.getQuestionText())
                    .then();
            }
        })
            .flatMap(question -> this.findById(question.getQuestionId()))
            .as(transactionalOperator::transactional);
    }

    public Mono<Void> delete(Question.Id questionId) {
        final Mono<Void> deleteQuestion = this.databaseClient.execute(this.sqlSupplier.file("sql/question/deleteQuestion.sql"))
            .bind("question_id", questionId.toString())
            .then();
        final Mono<Void> deleteSelectiveQuestion = this.databaseClient.execute(this.sqlSupplier.file("sql/question/deleteSelectiveQuestion.sql"))
            .bind("question_id", questionId.toString())
            .then();
        return Mono.when(deleteQuestion, deleteSelectiveQuestion)
            .as(transactionalOperator::transactional);
    }
}
