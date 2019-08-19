package am.ik.surveys.question;

import am.ik.surveys.Fixtures;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;

@Repository
public class QuestionRepository {

    public Mono<Question> findById(Question.Id questionId) {
        return Mono.justOrEmpty(this.questions.stream().filter(q -> Objects.equals(q.getQuestionId(), questionId)).findAny());
    }


    public Mono<Question> insert(Mono<Question> questionMono) {
        return questionMono.map(question -> {
            this.questions.add(question);
            return question;
        });
    }

    public Mono<Void> delete(Question question) {
        return Mono.fromRunnable(() -> this.questions.removeIf(q -> Objects.equals(q, question)));
    }

    private final CopyOnWriteArrayList<Question> questions = new CopyOnWriteArrayList<>(Fixtures.questions);
}
