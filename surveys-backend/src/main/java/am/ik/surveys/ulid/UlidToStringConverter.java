package am.ik.surveys.ulid;

import de.huxhorn.sulky.ulid.ULID;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class UlidToStringConverter implements Converter<ULID.Value, String> {

    @Override
    public String convert(ULID.Value value) {
        return value == null ? null : value.toString();
    }
}
