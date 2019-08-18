package am.ik.surveys.questionchoice;

import am.ik.surveys.question.Question;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import de.huxhorn.sulky.ulid.ULID;

import java.util.Objects;

/**
 * 設問選択肢
 */
@JsonDeserialize(builder = QuestionChoice.class)
public class QuestionChoice {

    /**
     * 設問選択肢ID
     */
    private final QuestionChoice.Id questionChoiceId;

    /**
     * 設問ID
     */
    private final Question.Id questionId;

    /**
     * 選択肢本文
     */
    private final String questionChoiceText;

    /**
     * 自由記述可
     */
    private final boolean allowFreeText;

    public QuestionChoice(Id questionChoiceId, Question.Id questionId, String questionChoiceText, boolean allowFreeText) {
        this.questionChoiceId = questionChoiceId;
        this.questionId = questionId;
        this.questionChoiceText = questionChoiceText;
        this.allowFreeText = allowFreeText;
    }

    public Id getQuestionChoiceId() {
        return questionChoiceId;
    }

    public Question.Id getQuestionId() {
        return questionId;
    }

    public String getQuestionChoiceText() {
        return questionChoiceText;
    }

    public boolean isAllowFreeText() {
        return allowFreeText;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        QuestionChoice that = (QuestionChoice) o;
        return questionChoiceId.equals(that.questionChoiceId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(questionChoiceId);
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

        private boolean allowFreeText = false;

        private Id questionChoiceId;

        private String questionChoiceText;

        private Question.Id questionId;

        public QuestionChoice build() {
            return new QuestionChoice(questionChoiceId, questionId, questionChoiceText, allowFreeText);
        }

        public Builder withAllowFreeText(boolean allowFreeText) {
            this.allowFreeText = allowFreeText;
            return this;
        }

        public Builder withQuestionChoiceId(Id questionChoiceId) {
            this.questionChoiceId = questionChoiceId;
            return this;
        }

        public Builder withQuestionChoiceText(String questionChoiceText) {
            this.questionChoiceText = questionChoiceText;
            return this;
        }

        public Builder withQuestionId(Question.Id questionId) {
            this.questionId = questionId;
            return this;
        }
    }
}
