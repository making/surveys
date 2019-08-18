package am.ik.surveys.answer.web;

import am.ik.surveys.answer.Answer;
import am.ik.surveys.answer.AnswerDetail;
import am.ik.surveys.answer.ChosenAnswer;
import am.ik.surveys.answer.DescriptiveAnswer;
import am.ik.surveys.questionchoice.QuestionChoice;

public class AnswerDetailRequest {

    private String answerText;

    private QuestionChoice.Id questionChoiceId;

    public String getAnswerText() {
        return answerText;
    }

    public void setAnswerText(String answerText) {
        this.answerText = answerText;
    }

    public QuestionChoice.Id getQuestionChoiceId() {
        return questionChoiceId;
    }

    public void setQuestionChoiceId(QuestionChoice.Id questionChoiceId) {
        this.questionChoiceId = questionChoiceId;
    }

    public AnswerDetail<?> toAnswerDetail(Answer.Id answerId) {
        if (this.questionChoiceId != null) {
            return new ChosenAnswer.Builder()
                .withAnswerId(answerId)
                .withQuestionChoiceId(this.questionChoiceId)
                .withAnswerText(this.answerText)
                .build();
        } else {
            return new DescriptiveAnswer.Builder()
                .withAnswerId(answerId)
                .withAnswerText(this.answerText)
                .build();
        }
    }
}
