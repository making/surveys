package am.ik.surveys.answer;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;

import java.util.Objects;

/**
 * 回答者
 */
@JsonDeserialize(builder = Respondent.Builder.class)
public class Respondent {

    /**
     * 回答者ID
     */
    private final Respondent.Id respondentId;

    public Respondent(Id respondentId) {
        this.respondentId = Objects.requireNonNull(respondentId);
    }

    public Id getRespondentId() {
        return respondentId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Respondent that = (Respondent) o;
        return respondentId.equals(that.respondentId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(respondentId);
    }

    @JsonSerialize(using = ToStringSerializer.class)
    public static class Id {

        private final String value;

        public Id(String value) {
            this.value = value;
        }

        public static Id valueOf(String value) {
            return new Id(value);
        }

        @Override
        public String toString() {
            return this.value;
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

        private Id respondentId;

        public Respondent build() {
            return new Respondent(respondentId);
        }

        public Builder withRespondentId(Id respondentId) {
            this.respondentId = respondentId;
            return this;
        }
    }
}
