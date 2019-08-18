package am.ik.surveys.answer;

import am.ik.surveys.Fixtures;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;

@Repository
public class AnswerDetailRepository {

    public Flux<AnswerDetail<?>> findAllByAnswerId(Answer.Id answerId) {
        return Flux.fromStream(this.answerDetails.stream().filter(a -> Objects.equals(a.getAnswerId(), answerId)));
    }


    public Mono<List<AnswerDetail<?>>> insert(List<AnswerDetail<?>> answerDetails) {
        return Mono.fromCallable(() -> {
            this.answerDetails.addAll(answerDetails);
            return answerDetails;
        });
    }

    public Mono<AnswerDetail> insert(AnswerDetail<?> answerDetail) {
        return Mono.fromCallable(() -> {
            this.answerDetails.add(answerDetail);
            return answerDetail;
        });
    }

    public Mono<AnswerDetail> insert(Mono<AnswerDetail<?>> answerDetailMono) {
        return answerDetailMono.map(answerDetail -> {
            this.answerDetails.add(answerDetail);
            return answerDetail;
        });
    }

    public Mono<Void> delete(AnswerDetail<?> answerDetail) {
        return Mono.fromRunnable(() -> {
            this.answerDetails.removeIf(answerDetail.isEqual());
        });
    }

    private final CopyOnWriteArrayList<AnswerDetail<?>> answerDetails = new CopyOnWriteArrayList<>(Fixtures.answerDetails);
}
