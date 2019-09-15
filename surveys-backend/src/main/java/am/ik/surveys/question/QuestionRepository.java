package am.ik.surveys.question;

import am.ik.surveys.infra.sql.SqlSupplier;
import org.springframework.data.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.reactive.TransactionalOperator;
import reactor.core.publisher.Mono;

@Repository
public class QuestionRepository {

    private final DatabaseClient databaseClient;

    private final TransactionalOperator transactionalOperator;

    private final SqlSupplier sqlSupplier;

    public QuestionRepository(DatabaseClient databaseClient, TransactionalOperator transactionalOperator, SqlSupplier sqlSupplier) {
        this.databaseClient = databaseClient;
        this.transactionalOperator = transactionalOperator;
        this.sqlSupplier = sqlSupplier;
    }

    @SuppressWarnings("ConstantConditions")
    public Mono<Question> findById(Question.Id questionId) {
        final Mono<Question> question = this.databaseClient.execute(this.sqlSupplier.file("sql/question/findQuestionById.sql"))
            .bind("question_id", questionId.toString())
            .map(row -> new Question.Builder()
                .withQuestionId(Question.Id.valueOf(row.get("question_id", String.class)))
                .withQuestionText(row.get("question_text", String.class))
                .build())
            .one();
        final Mono<Question> selectiveQuestion = this.databaseClient.execute(this.sqlSupplier.file("sql/question/findSelectiveQuestionById.sql"))
            .bind("question_id", questionId.toString())
            .map(row -> new SelectiveQuestion.Builder()
                .withQuestionId(Question.Id.valueOf(row.get("question_id", String.class)))
                .withQuestionText(row.get("question_text", String.class))
                .withMaxChoices(row.get("max_choices", Integer.class))
                .build())
            .one()
            .cast(Question.class);
        return selectiveQuestion.switchIfEmpty(question);
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
            .as(transactionalOperator::transactional)
            .flatMap(question -> this.findById(question.getQuestionId()));
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
