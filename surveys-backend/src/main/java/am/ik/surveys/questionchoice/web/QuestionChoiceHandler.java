package am.ik.surveys.questionchoice.web;

import am.ik.surveys.answer.AnswerDetailRepository;
import am.ik.surveys.questionchoice.QuestionChoice;
import am.ik.surveys.questionchoice.QuestionChoiceRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.reactive.TransactionalOperator;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

@Component
public class QuestionChoiceHandler {

    private final QuestionChoiceRepository questionChoiceRepository;

    private final AnswerDetailRepository answerDetailRepository;

    private final TransactionalOperator transactionalOperator;

    public QuestionChoiceHandler(QuestionChoiceRepository questionChoiceRepository, AnswerDetailRepository answerDetailRepository, TransactionalOperator transactionalOperator) {
        this.questionChoiceRepository = questionChoiceRepository;
        this.answerDetailRepository = answerDetailRepository;
        this.transactionalOperator = transactionalOperator;
    }

    public RouterFunction<ServerResponse> routes() {
        return RouterFunctions.route()
            .GET("/question_choices/{question_choice_id}", this::getQuestionChoice)
            .DELETE("/question_choices/{question_choice_id}", this::deleteQuestionChoice)
            .build();
    }

    private Mono<ServerResponse> getQuestionChoice(ServerRequest req) {
        final QuestionChoice.Id questionChoiceId = QuestionChoice.Id.valueOf(req.pathVariable("question_choice_id"));
        final Mono<QuestionChoice> questionChoiceMono = this.questionChoiceRepository.findById(questionChoiceId);
        return questionChoiceMono
            .flatMap(questionChoice -> ServerResponse.ok().bodyValue(questionChoice))
            .switchIfEmpty(Mono.defer(() -> ServerResponse.notFound().build()));
    }

    private Mono<ServerResponse> deleteQuestionChoice(ServerRequest req) {
        final QuestionChoice.Id questionChoiceId = QuestionChoice.Id.valueOf(req.pathVariable("question_choice_id"));
        final Mono<Void> deleteAnswerDelete = this.answerDetailRepository.deleteByQuestionChoiceId(questionChoiceId);
        final Mono<Void> deleteQuestionChoice = this.questionChoiceRepository.deleteById(questionChoiceId);
        return deleteAnswerDelete
            .then(deleteQuestionChoice)
            .as(transactionalOperator::transactional)
            .then(ServerResponse.noContent().build());
    }
}
