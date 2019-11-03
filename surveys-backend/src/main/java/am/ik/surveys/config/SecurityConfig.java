package am.ik.surveys.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;

@Configuration
public class SecurityConfig {

	@Bean
	public SecurityWebFilterChain springWebFilterChain(ServerHttpSecurity http) {
		return http //
				.httpBasic() //
				.and() //
				.formLogin() //
				.and() //
				.authorizeExchange(authorization -> authorization //
						.pathMatchers(HttpMethod.OPTIONS, "/**").permitAll() //
						.pathMatchers(HttpMethod.GET, "/surveys/*", "/questions/*", "/questions/*/question_choices",
								"/question_choices", "/docs/**", "/webjars/**", "/actuator/health", "/actuator/info",
								"/actuator/prometheus", "/static/**", "/favicon.ico", "/manifest.json")
						.permitAll() //
						.pathMatchers(HttpMethod.POST, "/surveys/*/answers").permitAll() //
						.pathMatchers("/", "/index.html", "/_/**", "actuator/**").hasRole("ADMIN") //
						.anyExchange().authenticated()) //
				.csrf(csrf -> csrf.disable()) //
				.build();
	}
}
