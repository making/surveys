package am.ik.surveys.questionchoice.web;

import am.ik.surveys.questionchoice.QuestionChoice;
import am.ik.surveys.questionchoice.QuestionChoiceRepository;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

@Component
public class QuestionChoiceHandler {

    private final QuestionChoiceRepository questionChoiceRepository;

    public QuestionChoiceHandler(QuestionChoiceRepository questionChoiceRepository) {
        this.questionChoiceRepository = questionChoiceRepository;
    }

    public RouterFunction<ServerResponse> routes() {
        return RouterFunctions.route()
            .GET("/question_choices/{questionChoiceId}", this::getQuestionChoice)
            .DELETE("/question_choices/{questionChoiceId}", this::deleteQuestionChoice)
            .build();
    }

    Mono<ServerResponse> getQuestionChoice(ServerRequest req) {
        final QuestionChoice.Id questionChoiceId = QuestionChoice.Id.valueOf(req.pathVariable("questionChoiceId"));
        final Mono<QuestionChoice> questionChoiceMono = this.questionChoiceRepository.findById(questionChoiceId);
        return questionChoiceMono
            .flatMap(questionChoice -> ServerResponse.ok().body(questionChoice))
            .switchIfEmpty(Mono.defer(() -> ServerResponse.notFound().build()));
    }

    Mono<ServerResponse> deleteQuestionChoice(ServerRequest req) {
        final QuestionChoice.Id questionChoiceId = QuestionChoice.Id.valueOf(req.pathVariable("questionChoiceId"));
        final Mono<Void> questionChoiceMono = this.questionChoiceRepository.deleteById(questionChoiceId);
        return questionChoiceMono.then(ServerResponse.noContent().build());
    }
}
