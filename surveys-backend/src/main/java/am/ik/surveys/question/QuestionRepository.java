package am.ik.surveys.question;

import am.ik.surveys.Fixtures;
import am.ik.surveys.survey.Survey;
import am.ik.surveys.surveyquestion.SurveyQuestion;
import am.ik.surveys.surveyquestion.SurveyQuestionRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;

import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toMap;

@Repository
public class QuestionRepository {

    private final SurveyQuestionRepository surveyQuestionRepository;

    public QuestionRepository(SurveyQuestionRepository surveyQuestionRepository) {
        this.surveyQuestionRepository = surveyQuestionRepository;
    }

    public Mono<Question> findById(Question.Id questionId) {
        return Mono.justOrEmpty(this.questions.stream().filter(q -> Objects.equals(q.getQuestionId(), questionId)).findAny());
    }

    public Flux<Question> findAllBySurveyId(Survey.Id surveyId) {
        final Flux<SurveyQuestion> surveyQuestionFlux = this.surveyQuestionRepository.findBySurveyId(surveyId);
        final Map<Question.Id, Question> questionMap = this.questions.stream()
            .collect(toMap(Question::getQuestionId, identity()));
        return surveyQuestionFlux.map(sq -> questionMap.get(sq.getQuestionId()));
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
