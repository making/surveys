package am.ik.surveys;

import java.util.function.Consumer;

import am.ik.surveys.config.SecurityConfig;
import am.ik.surveys.infra.sql.SqlSupplier;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.util.StdDateFormat;
import io.r2dbc.spi.ConnectionFactories;
import io.r2dbc.spi.ConnectionFactory;
import org.springframework.security.crypto.password.NoOpPasswordEncoder;
import reactor.core.publisher.Mono;

import org.springframework.core.io.ClassPathResource;
import org.springframework.data.r2dbc.connectionfactory.R2dbcTransactionManager;
import org.springframework.data.r2dbc.connectionfactory.init.ScriptUtils;
import org.springframework.data.r2dbc.core.DatabaseClient;
import org.springframework.http.HttpHeaders;
import org.springframework.http.codec.ServerCodecConfigurer;
import org.springframework.http.codec.json.Jackson2JsonDecoder;
import org.springframework.http.codec.json.Jackson2JsonEncoder;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.restdocs.RestDocumentationContextProvider;
import org.springframework.security.authentication.UserDetailsRepositoryReactiveAuthenticationManager;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.core.userdetails.MapReactiveUserDetailsService;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.WebFilterChainProxy;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.transaction.reactive.TransactionalOperator;
import org.springframework.web.reactive.function.server.HandlerStrategies;
import org.springframework.web.reactive.function.server.RouterFunction;

import static org.springframework.restdocs.cli.CliDocumentation.curlRequest;
import static org.springframework.restdocs.http.HttpDocumentation.httpRequest;
import static org.springframework.restdocs.http.HttpDocumentation.httpResponse;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.modifyUris;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.prettyPrint;
import static org.springframework.restdocs.webtestclient.WebTestClientRestDocumentation.documentationConfiguration;

public class TestUtils {
    static final SecurityConfig securityConfig = new SecurityConfig();

    @SuppressWarnings("deprecated")
    public static WebTestClient.Builder webTestClient(RouterFunction<?> routerFunction, RestDocumentationContextProvider restDocumentation) {
		final UserDetailsRepositoryReactiveAuthenticationManager manager = new UserDetailsRepositoryReactiveAuthenticationManager(
				new MapReactiveUserDetailsService(User.withUsername("admin").password("admin").roles("ADMIN").build()));
		manager.setPasswordEncoder(NoOpPasswordEncoder.getInstance());
		final ServerHttpSecurity http = ServerHttpSecurity.http().authenticationManager(manager);
		final SecurityWebFilterChain securityWebFilterChain = securityConfig.springWebFilterChain(http);
	    return WebTestClient.bindToRouterFunction(routerFunction)
            .webFilter(new WebFilterChainProxy(securityWebFilterChain))
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
                .withRequestDefaults(modifyUris()
                        .scheme("https")
                        .host("surveys.ik.am")
                        .removePort()
                    , prettyPrint())
                .withResponseDefaults(prettyPrint())
                .and()
                .snippets().withDefaults(httpRequest(), httpResponse(), curlRequest()));
    }

	public static Consumer<HttpHeaders> basicAdmin() {
		return basicAdmin("admin");
	}

	public static Consumer<HttpHeaders> basicAdmin(String password) {
		return httpHeaders -> httpHeaders.setBasicAuth("admin", password);
	}

    public static ConnectionFactory connectionFactory() {
        return ConnectionFactories.get("r2dbc:h2:mem:///test?options=DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE");
    }

    public static DatabaseClient databaseClient(ConnectionFactory connectionFactory) {
        initializeDatabase(connectionFactory).block();
        return DatabaseClient.builder()
            .connectionFactory(connectionFactory)
            .build();
    }

    public static TransactionalOperator transactionalOperator(ConnectionFactory connectionFactory) {
        return TransactionalOperator.create(new R2dbcTransactionManager(connectionFactory));
    }

    public static SqlSupplier sqlSupplier() {
        return new SqlSupplier();
    }

    public static Mono<Void> initializeDatabase(ConnectionFactory connectionFactory) {
        return Mono.from(connectionFactory.create())
            .flatMap(connection -> ScriptUtils.executeSqlScript(connection, new ClassPathResource("schema.sql"))
                .then(ScriptUtils.executeSqlScript(connection, new ClassPathResource("reset.sql"))));
    }
}
