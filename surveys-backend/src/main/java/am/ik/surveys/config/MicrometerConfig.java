package am.ik.surveys.config;

import org.springframework.boot.actuate.autoconfigure.metrics.MeterRegistryCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.micrometer.core.instrument.config.MeterFilter;

@Configuration
public class MicrometerConfig {
    @Bean
    public MeterRegistryCustomizer meterRegistryCustomizer() {
        return registry -> registry.config() //
            .meterFilter(MeterFilter.deny(id -> {
                String uri = id.getTag("uri");
                return uri != null && uri.startsWith("/actuator");
            }));
    }
}