package am.ik.surveys.answer;

import am.ik.surveys.infra.sql.SqlSupplier;
import am.ik.surveys.questionchoice.QuestionChoice;
import am.ik.surveys.survey.Survey;
import org.springframework.data.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.reactive.TransactionalOperator;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Comparator;
import java.util.List;
import java.util.function.Function;

@Repository
public class AnswerDetailRepository {

    private final DatabaseClient databaseClient;

    private final TransactionalOperator transactionalOperator;

    private final SqlSupplier sqlSupplier;

    public AnswerDetailRepository(DatabaseClient databaseClient, TransactionalOperator transactionalOperator, SqlSupplier sqlSupplier) {
        this.databaseClient = databaseClient;
        this.transactionalOperator = transactionalOperator;
        this.sqlSupplier = sqlSupplier;
    }

    public Flux<AnswerDetail<?>> findAllByAnswerId(Answer.Id answerId) {
        final Flux<AnswerDetail<?>> chosenAnswers = this.databaseClient.execute(this.sqlSupplier.file("sql/answer/findAllChosenAnswerById.sql"))
            .bind("answer_id", answerId.toString())
            .map(row -> new ChosenAnswer.Builder()
                .withAnswerId(Answer.Id.valueOf(row.get("answer_id", String.class)))
                .withQuestionChoiceId(QuestionChoice.Id.valueOf(row.get("question_choice_id", String.class)))
                .withAnswerText(row.get("answer_text", String.class))
                .build())
            .all()
            .map(Function.identity());
        final Flux<AnswerDetail<?>> descriptiveAnswers = this.databaseClient.execute(this.sqlSupplier.file("sql/answer/findAllDescriptiveAnswerById.sql"))
            .bind("answer_id", answerId.toString())
            .map(row -> new DescriptiveAnswer.Builder()
                .withAnswerId(Answer.Id.valueOf(row.get("answer_id", String.class)))
                .withAnswerText(row.get("answer_text", String.class))
                .build())
            .all()
            .map(Function.identity());
        return chosenAnswers
            .concatWith(descriptiveAnswers);
    }

    public Flux<AnswerDetail<?>> findAllBySurveyId(Survey.Id surveyId) {
        final Flux<AnswerDetail<?>> chosenAnswers = this.databaseClient.execute(this.sqlSupplier.file("sql/answer/findAllChosenAnswerBySurveyId.sql"))
            .bind("survey_id", surveyId.toString())
            .map(row -> new ChosenAnswer.Builder()
                .withAnswerId(Answer.Id.valueOf(row.get("answer_id", String.class)))
                .withQuestionChoiceId(QuestionChoice.Id.valueOf(row.get("question_choice_id", String.class)))
                .withAnswerText(row.get("answer_text", String.class))
                .build())
            .all()
            .map(Function.identity());
        final Flux<AnswerDetail<?>> descriptiveAnswers = this.databaseClient.execute(this.sqlSupplier.file("sql/answer/findAllDescriptiveAnswerBySurveyId.sql"))
            .bind("survey_id", surveyId.toString())
            .map(row -> new DescriptiveAnswer.Builder()
                .withAnswerId(Answer.Id.valueOf(row.get("answer_id", String.class)))
                .withAnswerText(row.get("answer_text", String.class))
                .build())
            .all()
            .map(Function.identity());
        return chosenAnswers
            .concatWith(descriptiveAnswers);
    }

    public Flux<AnswerDetail<?>> insert(List<AnswerDetail<?>> answerDetails) {
        final Flux<AnswerDetail<?>> chosenAnswers = Flux.fromStream(answerDetails.stream().filter(a -> a instanceof ChosenAnswer))
            .cast(ChosenAnswer.class).delayUntil(chosenAnswer ->
            {
                final DatabaseClient.GenericExecuteSpec spec = this.databaseClient.execute(this.sqlSupplier.file("sql/answer/insertChosenAnswer.sql"))
                    .bind("answer_id", chosenAnswer.getAnswerId().toString())
                    .bind("question_choice_id", chosenAnswer.getquestionChoiceId().toString());
                if (chosenAnswer.getAnswerText() == null) {
                    return spec
                        .bindNull("answer_text", String.class)
                        .then();
                } else {
                    return spec
                        .bind("answer_text", chosenAnswer.getAnswerText())
                        .then();
                }
            })
            .map(Function.identity());
        final Flux<AnswerDetail<?>> descriptiveAnswers = Flux.fromStream(answerDetails.stream().filter(a -> a instanceof DescriptiveAnswer))
            .cast(DescriptiveAnswer.class)
            .delayUntil(chosenAnswer ->
            {
                final DatabaseClient.GenericExecuteSpec spec = this.databaseClient.execute(this.sqlSupplier.file("sql/answer/insertDescriptiveAnswer.sql"))
                    .bind("answer_id", chosenAnswer.getAnswerId().toString());
                if (chosenAnswer.getAnswerText() == null) {
                    return spec
                        .bindNull("answer_text", String.class)
                        .then();
                } else {
                    return spec
                        .bind("answer_text", chosenAnswer.getAnswerText())
                        .then();
                }
            })
            .map(Function.identity());
        return chosenAnswers
            .concatWith(descriptiveAnswers)
            .as(transactionalOperator::transactional)
            .sort(Comparator.comparing(a -> a.getAnswerId().toString()));
    }

    public Mono<Void> delete(AnswerDetail<?> answerDetail) {
        if (answerDetail instanceof ChosenAnswer) {
            final ChosenAnswer chosenAnswer = (ChosenAnswer) answerDetail;
            return this.databaseClient.execute(this.sqlSupplier.file("sql/answer/deleteChosenAnswerById.sql"))
                .bind("answer_id", chosenAnswer.getAnswerId().toString())
                .bind("question_choice_id", chosenAnswer.getquestionChoiceId().toString())
                .then()
                .as(transactionalOperator::transactional);
        } else {
            return this.databaseClient.execute(this.sqlSupplier.file("sql/answer/deleteDescriptiveAnswerById.sql"))
                .bind("answer_id", answerDetail.getAnswerId().toString())
                .then()
                .as(transactionalOperator::transactional);
        }
    }
}
