package am.ik.surveys.survey.web;

import am.ik.surveys.survey.Survey;

import java.time.Instant;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

public class SurveyRequest {

    private OffsetDateTime startDateTime = Instant.ofEpochSecond(0).atOffset(ZoneOffset.UTC);

    private OffsetDateTime endDateTime = LocalDate.of(3000, 1, 1).atStartOfDay().atOffset(ZoneOffset.UTC);

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
