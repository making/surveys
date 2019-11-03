package am.ik.surveys.config;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.stereotype.Component;

import brave.Tracer;
import io.r2dbc.pool.ConnectionPool;
import io.r2dbc.proxy.ProxyConnectionFactory;

@Component
public class ConnectionFactoryBeanPostProcessor implements BeanPostProcessor {
    private final Tracer tracer;

    public ConnectionFactoryBeanPostProcessor(Tracer tracer) {
        this.tracer = tracer;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        if (bean instanceof ConnectionPool) {
            ConnectionPool connectionPool = (ConnectionPool) bean;
            return ProxyConnectionFactory.builder(connectionPool) //
                .listener(new TracingExecutionListener(tracer)) //
                .build();
        }
        return bean;
    }
}
