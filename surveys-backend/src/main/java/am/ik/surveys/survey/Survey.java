package am.ik.surveys.survey;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import de.huxhorn.sulky.ulid.ULID;

import java.time.OffsetDateTime;
import java.util.Objects;

/**
 * アンケート
 */
@JsonDeserialize(builder = Survey.Builder.class)
public class Survey {

    /**
     * アンケートID
     */
    private final Survey.Id surveyId;

    /**
     * 開始予定日時
     */
    private final OffsetDateTime startDateTime;

    /**
     * 終了予定日時
     */
    private final OffsetDateTime endDateTime;

    public Survey(Id surveyId, OffsetDateTime startDateTime, OffsetDateTime endDateTime) {
        this.surveyId = Objects.requireNonNull(surveyId);
        this.startDateTime = Objects.requireNonNull(startDateTime);
        this.endDateTime = Objects.requireNonNull(endDateTime);
    }

    public Id getSurveyId() {
        return surveyId;
    }

    public OffsetDateTime getStartDateTime() {
        return startDateTime;
    }

    public OffsetDateTime getEndDateTime() {
        return endDateTime;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Survey survey = (Survey) o;
        return surveyId.equals(survey.surveyId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(surveyId);
    }

    @JsonSerialize(using = ToStringSerializer.class)
    public static class Id {

        private final ULID.Value value;

        public Id(ULID.Value value) {
            this.value = Objects.requireNonNull(value);
        }

        public static Id valueOf(String value) {
            return new Id(ULID.parseULID(value));
        }

        public static Id nextValue(ULID ulid) {
            return new Id(ulid.nextValue());
        }

        public ULID.Value getValue() {
            return value;
        }

        @Override
        public String toString() {
            return this.value.toString();
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            Id id = (Id) o;
            return value.equals(id.value);
        }

        @Override
        public int hashCode() {
            return Objects.hash(value);
        }
    }

    @JsonPOJOBuilder
    public static class Builder {

        private OffsetDateTime endDateTime;

        private OffsetDateTime startDateTime;

        private Id surveyId;

        public Survey build() {
            return new Survey(surveyId, startDateTime, endDateTime);
        }

        public Builder withEndDateTime(OffsetDateTime endDateTime) {
            this.endDateTime = endDateTime;
            return this;
        }

        public Builder withStartDateTime(OffsetDateTime startDateTime) {
            this.startDateTime = startDateTime;
            return this;
        }

        public Builder withSurveyId(Id surveyId) {
            this.surveyId = surveyId;
            return this;
        }
    }
}
