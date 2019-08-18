package am.ik.surveys.questionchoice;

import am.ik.surveys.Fixtures;
import am.ik.surveys.question.Question;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;

@Repository
public class QuestionChoiceRepository {

    public Mono<QuestionChoice> findById(QuestionChoice.Id questionChoiceId) {
        return Mono.justOrEmpty(this.questionChoices.stream()
            .filter(qc -> Objects.equals(qc.getQuestionChoiceId(), questionChoiceId))
            .findAny());
    }

    public Flux<QuestionChoice> findAllByQuestionId(Question.Id questionId) {
        return Flux.fromStream(this.questionChoices.stream()
            .filter(c -> Objects.equals(c.getQuestionId(), questionId)));
    }

    public Mono<QuestionChoice> insert(Mono<QuestionChoice> questionChoiceMono) {
        return questionChoiceMono.map(questionChoice -> {
            this.questionChoices.add(questionChoice);
            return questionChoice;
        });
    }

    public Mono<Void> deleteById(QuestionChoice.Id questionChoiceId) {
        return Mono.fromRunnable(() -> this.questionChoices.removeIf(qc -> Objects.equals(qc.getQuestionChoiceId(), questionChoiceId)));
    }


    private final CopyOnWriteArrayList<QuestionChoice> questionChoices = new CopyOnWriteArrayList<>(Fixtures.questionChoices);
}
