package am.ik.surveys.answer;

import am.ik.surveys.questionchoice.QuestionChoice;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;

import java.util.Objects;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;

/**
 * 選択回答
 */
@JsonDeserialize(builder = ChosenAnswer.Builder.class)
public class ChosenAnswer implements AnswerDetail<ChosenAnswer.Id> {

    /**
     * 選択回答ID
     */
    private final ChosenAnswer.Id chosenAnswerId;

    /**
     * 回答内容
     */
    @JsonInclude(value = NON_NULL)
    private String answerText;

    public ChosenAnswer(Id chosenAnswerId, String answerText) {
        this.chosenAnswerId = Objects.requireNonNull(chosenAnswerId);
        this.answerText = answerText;
    }

    public ChosenAnswer(Answer.Id answerId, QuestionChoice.Id questionChoiceId, String answerText) {
        this(new Id(answerId, questionChoiceId), answerText);
    }

    public Answer.Id getAnswerId() {
        return this.chosenAnswerId.answerId;
    }

    public String getAnswerText() {
        return answerText;
    }

    public QuestionChoice.Id getquestionChoiceId() {
        return this.chosenAnswerId.questionChoiceId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ChosenAnswer that = (ChosenAnswer) o;
        return chosenAnswerId.equals(that.chosenAnswerId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(chosenAnswerId);
    }

    @Override
    public Id id() {
        return this.chosenAnswerId;
    }

    public static class Id {

        /**
         * 回答ID
         */
        private final Answer.Id answerId;

        /**
         * 設問選択肢ID
         */
        private final QuestionChoice.Id questionChoiceId;

        public Id(Answer.Id answerId, QuestionChoice.Id questionChoiceId) {
            this.answerId = Objects.requireNonNull(answerId);
            this.questionChoiceId = Objects.requireNonNull(questionChoiceId);
        }

        public Answer.Id getAnswerId() {
            return answerId;
        }

        public QuestionChoice.Id getquestionChoiceId() {
            return questionChoiceId;
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
            return answerId.equals(id.answerId) &&
                questionChoiceId.equals(id.questionChoiceId);
        }

        @Override
        public int hashCode() {
            return Objects.hash(answerId, questionChoiceId);
        }
    }

    @JsonPOJOBuilder
    public static class Builder {

        private Answer.Id answerId;

        private String answerText;

        private QuestionChoice.Id questionChoiceId;

        public ChosenAnswer build() {
            return new ChosenAnswer(answerId, questionChoiceId, answerText);
        }

        public Builder withAnswerId(Answer.Id answerId) {
            this.answerId = answerId;
            return this;
        }

        public Builder withAnswerText(String answerText) {
            this.answerText = answerText;
            return this;
        }

        public Builder withQuestionChoiceId(QuestionChoice.Id questionChoiceId) {
            this.questionChoiceId = questionChoiceId;
            return this;
        }
    }
}
