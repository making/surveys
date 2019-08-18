package am.ik.surveys.question.web;

import am.ik.surveys.question.Question;
import am.ik.surveys.question.SelectiveQuestion;

public class QuestionRequest {

    private String questionText;

    private Integer maxChoices;

    public String getQuestionText() {
        return questionText;
    }

    public void setQuestionText(String questionText) {
        this.questionText = questionText;
    }

    public Integer getMaxChoices() {
        return maxChoices;
    }

    public void setMaxChoices(Integer maxChoices) {
        this.maxChoices = maxChoices;
    }

    public Question toQuestion(Question.Id questionId) {
        if (maxChoices != null) {
            return new SelectiveQuestion.Builder()
                .withQuestionId(questionId)
                .withQuestionText(this.questionText)
                .withMaxChoices(this.maxChoices)
                .build();
        } else {
            return new Question.Builder()
                .withQuestionId(questionId)
                .withQuestionText(this.questionText)
                .build();
        }
    }
}
