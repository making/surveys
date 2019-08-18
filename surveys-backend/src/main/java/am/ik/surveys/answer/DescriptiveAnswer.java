package am.ik.surveys.answer;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;

import java.util.Objects;

/**
 * 記述式設問回答
 */
@JsonDeserialize(builder = DescriptiveAnswer.Builder.class)
public class DescriptiveAnswer implements AnswerDetail<Answer.Id> {

    /**
     * 回答ID
     */
    private final Answer.Id answerId;

    /**
     * 回答内容
     */
    private final String answerText;

    public DescriptiveAnswer(Answer.Id answerId, String answerText) {
        this.answerId = Objects.requireNonNull(answerId);
        this.answerText = Objects.requireNonNull(answerText);
    }

    public Answer.Id getAnswerId() {
        return answerId;
    }

    public String getAnswerText() {
        return answerText;
    }

    @Override
    public Answer.Id id() {
        return this.answerId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        DescriptiveAnswer that = (DescriptiveAnswer) o;
        return answerId.equals(that.answerId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(answerId);
    }

    @JsonPOJOBuilder
    public static class Builder {

        private Answer.Id answerId;

        private String answerText;

        public DescriptiveAnswer build() {
            return new DescriptiveAnswer(answerId, answerText);
        }

        public Builder withAnswerId(Answer.Id answerId) {
            this.answerId = answerId;
            return this;
        }

        public Builder withAnswerText(String answerText) {
            this.answerText = answerText;
            return this;
        }
    }
}
