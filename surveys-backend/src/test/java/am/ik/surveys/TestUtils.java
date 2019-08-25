package am.ik.surveys;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.util.StdDateFormat;
import org.springframework.http.codec.ServerCodecConfigurer;
import org.springframework.http.codec.json.Jackson2JsonDecoder;
import org.springframework.http.codec.json.Jackson2JsonEncoder;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.restdocs.RestDocumentationContextProvider;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.server.HandlerStrategies;
import org.springframework.web.reactive.function.server.RouterFunction;

import static org.springframework.restdocs.cli.CliDocumentation.curlRequest;
import static org.springframework.restdocs.http.HttpDocumentation.httpRequest;
import static org.springframework.restdocs.http.HttpDocumentation.httpResponse;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.prettyPrint;
import static org.springframework.restdocs.webtestclient.WebTestClientRestDocumentation.documentationConfiguration;

public class TestUtils {

    public static WebTestClient.Builder webTestClient(RouterFunction<?> routerFunction, RestDocumentationContextProvider restDocumentation) {
        return WebTestClient.bindToRouterFunction(routerFunction)
            .handlerStrategies(HandlerStrategies.builder()
                .codecs(configure -> {
                    configure.registerDefaults(true);
                    ServerCodecConfigurer.ServerDefaultCodecs defaults = configure
                        .defaultCodecs();
                    ObjectMapper objectMapper = Jackson2ObjectMapperBuilder.json()
                        .dateFormat(new StdDateFormat())
                        .propertyNamingStrategy(PropertyNamingStrategy.SNAKE_CASE)
                        .build();
                    defaults.jackson2JsonEncoder(new Jackson2JsonEncoder(objectMapper));
                    defaults.jackson2JsonDecoder(new Jackson2JsonDecoder(objectMapper));
                })
                .build())
            .configureClient()
            .filter(documentationConfiguration(restDocumentation)
                .operationPreprocessors()
                .withRequestDefaults(prettyPrint())
                .withResponseDefaults(prettyPrint())
                .and()
                .snippets().withDefaults(httpRequest(), httpResponse(), curlRequest()));
    }
}
