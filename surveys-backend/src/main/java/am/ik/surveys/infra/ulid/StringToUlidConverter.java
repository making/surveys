package am.ik.surveys.infra.ulid;

import de.huxhorn.sulky.ulid.ULID;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class StringToUlidConverter implements Converter<String, ULID.Value> {

    @Override
    public ULID.Value convert(String text) {
        return text == null ? null : ULID.parseULID(text);
    }
}
