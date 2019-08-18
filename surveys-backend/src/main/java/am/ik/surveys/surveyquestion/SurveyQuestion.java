package am.ik.surveys.surveyquestion;

import am.ik.surveys.question.Question;
import am.ik.surveys.survey.Survey;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;

import java.util.Objects;

/**
 * アンケート設問
 */
@JsonDeserialize(builder = SurveyQuestion.Builder.class)
public class SurveyQuestion {

    /**
     * アンケート設問ID
     */
    private final SurveyQuestion.Id surveyQuestionId;

    /**
     * 回答必須
     */
    private final boolean required;

    public SurveyQuestion(Id surveyQuestionId, boolean required) {
        this.surveyQuestionId = Objects.requireNonNull(surveyQuestionId);
        this.required = required;
    }

    public SurveyQuestion(Survey.Id surveyId, Question.Id questionId, boolean required) {
        this(new SurveyQuestion.Id(surveyId, questionId), required);
    }

    public Survey.Id getSurveyId() {
        return this.surveyQuestionId.surveyId;
    }

    public Question.Id getQuestionId() {
        return this.surveyQuestionId.questionId;
    }

    public boolean isRequired() {
        return required;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        SurveyQuestion that = (SurveyQuestion) o;
        return surveyQuestionId.equals(that.surveyQuestionId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(surveyQuestionId);
    }

    public static class Id {

        /**
         * アンケートID
         */
        private final Survey.Id surveyId;

        /**
         * 設問ID
         */
        private final Question.Id questionId;

        public Id(Survey.Id surveyId, Question.Id questionId) {
            this.surveyId = Objects.requireNonNull(surveyId);
            this.questionId = Objects.requireNonNull(questionId);
        }

        public Survey.Id getSurveyId() {
            return surveyId;
        }

        public Question.Id getQuestionId() {
            return questionId;
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
            return surveyId.equals(id.surveyId) &&
                questionId.equals(id.questionId);
        }

        @Override
        public int hashCode() {
            return Objects.hash(surveyId, questionId);
        }
    }

    @JsonPOJOBuilder
    public static class Builder {

        private Question.Id questionId;

        private boolean required = true;

        private Survey.Id surveyId;

        public SurveyQuestion build() {
            return new SurveyQuestion(surveyId, questionId, required);
        }

        public Builder withQuestionId(Question.Id questionId) {
            this.questionId = questionId;
            return this;
        }

        public Builder withRequired(boolean required) {
            this.required = required;
            return this;
        }

        public Builder withSurveyId(Survey.Id surveyId) {
            this.surveyId = surveyId;
            return this;
        }
    }
}
