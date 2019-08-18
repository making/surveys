package am.ik.surveys.survey.web;

import am.ik.surveys.survey.Survey;
import am.ik.surveys.surveyquestion.web.SurveyQuestionResponse;
import com.fasterxml.jackson.annotation.JsonUnwrapped;

import java.util.List;

public class SurveyResponse {

    @JsonUnwrapped
    private Survey survey;

    private List<SurveyQuestionResponse> surveyQuestions;

    public SurveyResponse(Survey survey, List<SurveyQuestionResponse> surveyQuestions) {
        this.survey = survey;
        this.surveyQuestions = surveyQuestions;
    }

    public Survey getSurvey() {
        return survey;
    }

    public void setSurvey(Survey survey) {
        this.survey = survey;
    }

    public List<SurveyQuestionResponse> getSurveyQuestions() {
        return surveyQuestions;
    }

    public void setSurveyQuestions(List<SurveyQuestionResponse> surveyQuestions) {
        this.surveyQuestions = surveyQuestions;
    }
}
