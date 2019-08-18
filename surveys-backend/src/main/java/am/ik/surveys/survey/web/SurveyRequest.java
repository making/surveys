package am.ik.surveys.survey.web;

import am.ik.surveys.survey.Survey;

import java.time.OffsetDateTime;

public class SurveyRequest {

    private OffsetDateTime startDateTime = OffsetDateTime.MIN;

    private OffsetDateTime endDateTime = OffsetDateTime.MAX;

    public OffsetDateTime getStartDateTime() {
        return startDateTime;
    }

    public void setStartDateTime(OffsetDateTime startDateTime) {
        this.startDateTime = startDateTime;
    }

    public OffsetDateTime getEndDateTime() {
        return endDateTime;
    }

    public void setEndDateTime(OffsetDateTime endDateTime) {
        this.endDateTime = endDateTime;
    }

    public Survey toSurvey(Survey.Id surveyId) {
        return new Survey.Builder()
            .withSurveyId(surveyId)
            .withStartDateTime(this.startDateTime)
            .withEndDateTime(this.endDateTime)
            .build();
    }
}
