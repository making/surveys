package am.ik.surveys.answer;

import am.ik.surveys.question.Question;
import am.ik.surveys.survey.Survey;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import de.huxhorn.sulky.ulid.ULID;

import java.util.Objects;

/**
 * 回答
 */
@JsonDeserialize(builder = Answer.Builder.class)
public class Answer {

    /**
     * 回答ID
     */
    private final Answer.Id answerId;

    /**
     * アンケートID
     */
    private final Survey.Id surveyId;

    /**
     * 設問ID
     */
    private final Question.Id questionId;

    /**
     * 回答者ID
     */
    private final Respondent.Id respondentId;

    public Answer(Id answerId, Survey.Id surveyId, Question.Id questionId, Respondent.Id respondentId) {
        this.answerId = answerId;
        this.surveyId = surveyId;
        this.questionId = questionId;
        this.respondentId = respondentId;
    }

    public Id getAnswerId() {
        return answerId;
    }

    public Survey.Id getSurveyId() {
        return surveyId;
    }

    public Question.Id getQuestionId() {
        return questionId;
    }

    public Respondent.Id getRespondentId() {
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
        Answer answer = (Answer) o;
        return answerId.equals(answer.answerId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(answerId);
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

        private Id answerId;

        private Question.Id questionId;

        private Respondent.Id respondentId;

        private Survey.Id surveyId;

        public Answer build() {
            return new Answer(answerId, surveyId, questionId, respondentId);
        }

        public Builder withAnswerId(Id answerId) {
            this.answerId = answerId;
            return this;
        }

        public Builder withQuestionId(Question.Id questionId) {
            this.questionId = questionId;
            return this;
        }

        public Builder withRespondentId(Respondent.Id respondentId) {
            this.respondentId = respondentId;
            return this;
        }

        public Builder withSurveyId(Survey.Id surveyId) {
            this.surveyId = surveyId;
            return this;
        }
    }
}
