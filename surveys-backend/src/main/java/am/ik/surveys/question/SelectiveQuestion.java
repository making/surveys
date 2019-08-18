package am.ik.surveys.question;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;

/**
 * 選択式設問
 */
@JsonDeserialize(builder = SelectiveQuestion.Builder.class)
public class SelectiveQuestion extends Question {

    /**
     * 選択可能数
     */
    private final int maxChoices;

    public SelectiveQuestion(Id questionId, String questionText, int maxChoices) {
        super(questionId, questionText);
        this.maxChoices = maxChoices;
    }

    public int getMaxChoices() {
        return maxChoices;
    }

    @JsonPOJOBuilder
    public static class Builder {

        private Id questionId;

        private String questionText;

        private int maxChoices;

        public SelectiveQuestion build() {
            return new SelectiveQuestion(questionId, questionText, maxChoices);
        }

        public Builder withQuestionId(Id questionId) {
            this.questionId = questionId;
            return this;
        }

        public Builder withQuestionText(String questionText) {
            this.questionText = questionText;
            return this;
        }

        public Builder withMaxChoices(int maxChoices) {
            this.maxChoices = maxChoices;
            return this;
        }
    }
}
