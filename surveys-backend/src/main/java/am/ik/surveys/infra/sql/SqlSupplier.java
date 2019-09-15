package am.ik.surveys.infra.sql;

import org.springframework.core.io.ClassPathResource;
import org.springframework.lang.NonNull;
import org.springframework.util.StreamUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Supplier;

import static java.util.regex.Pattern.quote;

public class SqlSupplier {

    private final ConcurrentMap<String, String> cache = new ConcurrentHashMap<>();

    private final String prefix;

    public SqlSupplier() {
        this("");
    }

    public SqlSupplier(@NonNull String prefix) {
        this.prefix = (!prefix.isEmpty() && !prefix.endsWith("/")) ? prefix + "/" : prefix;
    }

    public Supplier<String> file(String path) {
        return () -> cache.computeIfAbsent(prefix + path, key -> {
            final ClassPathResource resource = new ClassPathResource(key);
            try (InputStream inputStream = resource.getInputStream()) {
                final String sql = StreamUtils.copyToString(inputStream, StandardCharsets.UTF_8);
                return sql.replaceFirst(quote(" "), String.format(" /* path=%s */%n ", path));
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        });
    }
}
