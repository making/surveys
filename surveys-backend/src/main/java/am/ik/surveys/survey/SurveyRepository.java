package am.ik.surveys.survey;

import am.ik.surveys.Fixtures;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;

@Repository
public class SurveyRepository {

    public Flux<Survey> findAll() {
        return Flux.fromIterable(this.surveys);
    }

    public Mono<Survey> findById(Survey.Id surveyId) {
        return Mono.justOrEmpty(this.surveys.stream().filter(s -> Objects.equals(s.getSurveyId(), surveyId)).findAny());
    }

    public Mono<Survey> insert(Mono<Survey> surveyMono) {
        return surveyMono.map(survey -> {
            this.surveys.add(survey);
            return survey;
        });
    }

    public Mono<Void> deleteById(Survey.Id surveyId) {
        return Mono.fromRunnable(() -> this.surveys.removeIf(s -> Objects.equals(s.getSurveyId(), surveyId)));
    }

    private final CopyOnWriteArrayList<Survey> surveys = new CopyOnWriteArrayList<>(Fixtures.surveys);
}
