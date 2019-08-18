package am.ik.surveys.answer.web;

import am.ik.surveys.answer.Answer;
import am.ik.surveys.answer.AnswerDetail;
import am.ik.surveys.answer.Respondent;
import am.ik.surveys.question.Question;
import am.ik.surveys.survey.Survey;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class AnswerRequest {

    private Question.Id questionId;

    private Respondent.Id respondentId;

    private List<AnswerDetailRequest> details;

    public Question.Id getQuestionId() {
        return questionId;
    }

    public void setQuestionId(Question.Id questionId) {
        this.questionId = questionId;
    }

    public Respondent.Id getRespondentId() {
        return respondentId;
    }

    public void setRespondentId(Respondent.Id respondentId) {
        this.respondentId = respondentId;
    }

    public List<AnswerDetailRequest> getDetails() {
        return details;
    }

    public void setDetails(List<AnswerDetailRequest> details) {
        this.details = details;
    }

    public Answer toAnswer(Answer.Id answerId, Survey.Id surveyId) {
        return new Answer.Builder()
            .withAnswerId(answerId)
            .withSurveyId(surveyId)
            .withQuestionId(this.questionId)
            .withRespondentId(this.respondentId)
            .build();
    }

    public List<AnswerDetail<?>> toAnswerDetails(Answer.Id answerId) {
        if (this.details == null) {
            return Collections.emptyList();
        }
        return this.details.stream()
            .map(d -> d.toAnswerDetail(answerId))
            .collect(Collectors.toList());
    }
}
