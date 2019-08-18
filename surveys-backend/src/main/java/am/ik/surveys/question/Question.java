package am.ik.surveys.question;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import de.huxhorn.sulky.ulid.ULID;

import java.util.Objects;

/**
 * 設問
 */
@JsonDeserialize(builder = Question.Builder.class)
public class Question {

    /**
     * 設問ID
     */
    private final Question.Id questionId;

    /**
     * 設問文
     */
    private final String questionText;

    public Question(Id questionId, String questionText) {
        this.questionId = Objects.requireNonNull(questionId);
        this.questionText = Objects.requireNonNull(questionText);
    }

    public Id getQuestionId() {
        return questionId;
    }

    public String getQuestionText() {
        return questionText;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Question question = (Question) o;
        return questionId.equals(question.questionId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(questionId);
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

        private Id questionId;

        private String questionText;

        public Question build() {
            return new Question(questionId, questionText);
        }

        public Builder withQuestionId(Id questionId) {
            this.questionId = questionId;
            return this;
        }

        public Builder withQuestionText(String questionText) {
            this.questionText = questionText;
            return this;
        }
    }
}
