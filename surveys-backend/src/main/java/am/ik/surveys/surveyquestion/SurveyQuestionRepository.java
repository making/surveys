package am.ik.surveys.surveyquestion;

import am.ik.surveys.Fixtures;
import am.ik.surveys.survey.Survey;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;

@Repository
public class SurveyQuestionRepository {

    public Flux<SurveyQuestion> findBySurveyId(Survey.Id surveyId) {
        return Flux.fromStream(this.surveyQuestions.stream()
            .filter(sq -> Objects.equals(sq.getSurveyId(), surveyId)));
    }

    public Mono<SurveyQuestion> insert(Mono<SurveyQuestion> surveyQuestionMono) {
        return surveyQuestionMono.map(surveyQuestion -> {
            this.surveyQuestions.add(surveyQuestion);
            return surveyQuestion;
        });
    }

    public Mono<SurveyQuestion> insert(SurveyQuestion surveyQuestion) {
        return Mono.fromCallable(() -> {
            this.surveyQuestions.add(surveyQuestion);
            return surveyQuestion;
        });
    }

    public Mono<Void> delete(SurveyQuestion surveyQuestion) {
        return Mono.fromRunnable(() -> this.surveyQuestions.removeIf(sq -> Objects.equals(sq, surveyQuestion)));
    }

    public Mono<Void> deleteBySurveyId(Survey.Id surveyId) {
        return Mono.fromRunnable(() -> this.surveyQuestions.removeIf(sq -> Objects.equals(sq.getSurveyId(), surveyId)));
    }

    private final CopyOnWriteArrayList<SurveyQuestion> surveyQuestions = new CopyOnWriteArrayList<>(Fixtures.surveyQuestions);
}
