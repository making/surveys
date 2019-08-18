package am.ik.surveys.question.web;

import am.ik.surveys.question.Question;
import am.ik.surveys.questionchoice.QuestionChoice;

import java.util.List;

public class SelectiveQuestionResponse extends QuestionResponse {

    private List<QuestionChoice> questionChoices;

    public SelectiveQuestionResponse(Question question, List<QuestionChoice> questionChoices) {
        super(question);
        this.questionChoices = questionChoices;
    }

    public List<QuestionChoice> getQuestionChoices() {
        return questionChoices;
    }

    public void setQuestionChoices(List<QuestionChoice> questionChoices) {
        this.questionChoices = questionChoices;
    }
}
