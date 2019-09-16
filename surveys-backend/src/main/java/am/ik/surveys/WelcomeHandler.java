package am.ik.surveys;

import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Controller;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

@Controller
public class WelcomeHandler {

    public RouterFunction<ServerResponse> routes() {
        return RouterFunctions.route()
            .GET("/", this::showHtml)
            .GET("/_/**", this::showHtml)
            .build();
    }

    public Mono<ServerResponse> showHtml(ServerRequest req) {
        return ServerResponse.ok().bodyValue(new ClassPathResource("META-INF/resources/index.html"));
    }
}
