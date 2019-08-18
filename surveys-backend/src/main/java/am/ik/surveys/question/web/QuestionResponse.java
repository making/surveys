package am.ik.surveys.question.web;

import am.ik.surveys.question.Question;
import am.ik.surveys.question.SelectiveQuestion;
import am.ik.surveys.questionchoice.QuestionChoiceRepository;
import com.fasterxml.jackson.annotation.JsonUnwrapped;
import reactor.core.publisher.Mono;

public class QuestionResponse {

    @JsonUnwrapped
    private Question question;

    public QuestionResponse(Question question) {
        this.question = question;
    }

    public Question getQuestion() {
        return question;
    }

    public void setQuestion(Question question) {
        this.question = question;
    }

    public static Mono<QuestionResponse> from(Question question, QuestionChoiceRepository questionChoiceRepository) {
        if (question instanceof SelectiveQuestion) {
            return questionChoiceRepository.findAllByQuestionId(question.getQuestionId())
                .collectList()
                .map(questionChoices -> new SelectiveQuestionResponse(question, questionChoices));
        }
        return Mono.just(new QuestionResponse(question));
    }
}
