package am.ik.surveys.surveyquestion.web;

import am.ik.surveys.question.Question;
import am.ik.surveys.question.QuestionRepository;
import am.ik.surveys.question.web.QuestionResponse;
import am.ik.surveys.question.web.SelectiveQuestionResponse;
import am.ik.surveys.questionchoice.QuestionChoice;
import am.ik.surveys.questionchoice.QuestionChoiceRepository;
import am.ik.surveys.surveyquestion.SurveyQuestion;
import com.fasterxml.jackson.annotation.JsonUnwrapped;
import reactor.core.publisher.Mono;

import java.util.List;

public class SurveyQuestionResponse {

    @JsonUnwrapped
    private SurveyQuestion surveyQuestion;

    @JsonUnwrapped
    private QuestionResponse questionResponse;

    public SurveyQuestionResponse(SurveyQuestion surveyQuestion, QuestionResponse questionResponse) {
        this.surveyQuestion = surveyQuestion;
        this.questionResponse = questionResponse;
    }

    public SurveyQuestion getSurveyQuestion() {
        return surveyQuestion;
    }

    public void setSurveyQuestion(SurveyQuestion surveyQuestion) {
        this.surveyQuestion = surveyQuestion;
    }

    public QuestionResponse getQuestionResponse() {
        return questionResponse;
    }

    public void setQuestionResponse(QuestionResponse questionResponse) {
        this.questionResponse = questionResponse;
    }

    public static Mono<SurveyQuestionResponse> from(SurveyQuestion surveyQuestion, QuestionRepository questionRepository, QuestionChoiceRepository questionChoiceRepository) {
        final Mono<Question> questionMono = questionRepository.findById(surveyQuestion.getQuestionId());
        final Mono<QuestionResponse> questionResponseMono = questionMono.flatMap(question -> QuestionResponse.from(question, questionChoiceRepository));
        return questionResponseMono.map(questionResponse -> new SurveyQuestionResponse(surveyQuestion, questionResponse));
    }

    public static SurveyQuestionResponse create(SurveyQuestion surveyQuestion, Question question, List<QuestionChoice> questionChoices) {
        if (questionChoices != null) {
            return new SurveyQuestionResponse(surveyQuestion, new SelectiveQuestionResponse(question, questionChoices));
        } else {
            return new SurveyQuestionResponse(surveyQuestion, new QuestionResponse(question));
        }
    }
}
