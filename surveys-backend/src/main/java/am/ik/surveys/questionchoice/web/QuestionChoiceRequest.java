package am.ik.surveys.questionchoice.web;

import am.ik.surveys.questionchoice.QuestionChoice;
import am.ik.surveys.question.Question;

public class QuestionChoiceRequest {

    private String questionChoiceText;

    private boolean allowFreeText = false;

    public String getQuestionChoiceText() {
        return questionChoiceText;
    }

    public void setQuestionChoiceText(String questionChoiceText) {
        this.questionChoiceText = questionChoiceText;
    }

    public boolean isAllowFreeText() {
        return allowFreeText;
    }

    public void setAllowFreeText(boolean allowFreeText) {
        this.allowFreeText = allowFreeText;
    }

    public QuestionChoice toQuestionChoice(QuestionChoice.Id questionChoiceId, Question.Id questionId) {
        return new QuestionChoice.Builder()
            .withQuestionChoiceId(questionChoiceId)
            .withQuestionId(questionId)
            .withQuestionChoiceText(questionChoiceText)
            .withAllowFreeText(allowFreeText)
            .build();
    }
}
