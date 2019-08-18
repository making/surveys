package am.ik.surveys.answer;

import java.util.Objects;
import java.util.function.Predicate;

public interface AnswerDetail<ID> {

    Answer.Id getAnswerId();

    ID id();

    default Predicate<AnswerDetail<?>> isEqual() {
        return d -> Objects.equals(d.id(), this.id());
    }
}
