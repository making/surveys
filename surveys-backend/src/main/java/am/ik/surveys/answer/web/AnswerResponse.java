package am.ik.surveys.answer.web;

import am.ik.surveys.answer.Answer;
import am.ik.surveys.answer.AnswerDetail;
import am.ik.surveys.answer.AnswerDetailRepository;
import com.fasterxml.jackson.annotation.JsonUnwrapped;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

public class AnswerResponse {

    @JsonUnwrapped
    private Answer answer;

    private List<AnswerDetail<?>> details;

    public AnswerResponse(Answer answer, List<AnswerDetail<?>> details) {
        this.answer = answer;
        this.details = details;
    }

    public Answer getAnswer() {
        return answer;
    }

    public void setAnswer(Answer answer) {
        this.answer = answer;
    }

    public List<AnswerDetail<?>> getDetails() {
        return details;
    }

    public void setDetails(List<AnswerDetail<?>> details) {
        this.details = details;
    }

    public static Mono<AnswerResponse> from(Answer answer, AnswerDetailRepository answerDetailRepository) {
        final Flux<AnswerDetail<?>> detailFlux = answerDetailRepository.findAllByAnswerId(answer.getAnswerId());
        return detailFlux.collectList().map(details -> new AnswerResponse(answer, details));
    }
}
