package am.ik.surveys.answer;

import am.ik.surveys.Fixtures;
import am.ik.surveys.survey.Survey;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;

@Repository
public class AnswerRepository {

    public Flux<Answer> findAllBySurveyId(Survey.Id surveyId) {
        return Flux.fromStream(this.answers.stream()
            .filter(answer -> Objects.equals(answer.getSurveyId(), surveyId)));
    }

    public Mono<Answer> insert(Answer answer) {
        return Mono.fromCallable(() -> {
            this.answers.add(answer);
            return answer;
        });
    }

    public Mono<Answer> findById(Answer.Id answerId) {
        return Mono.justOrEmpty(this.answers.stream().filter(answer -> Objects.equals(answer.getAnswerId(), answerId)).findAny());
    }

    public Mono<Void> deleteById(Answer.Id answerId) {
        return Mono.fromRunnable(() -> this.answers.removeIf(answer -> Objects.equals(answer.getAnswerId(), answerId)));
    }

    private final CopyOnWriteArrayList<Answer> answers = new CopyOnWriteArrayList<>(Fixtures.answers);
}
