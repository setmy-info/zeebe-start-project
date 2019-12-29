package info.setmy.zeebe.beans;

import info.setmy.zeebe.config.ZeebeProperties;
import io.zeebe.client.ZeebeClient;
import io.zeebe.client.ZeebeClientBuilder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 *
 * @author <a href="mailto:imre.tabur@eesti.ee">Imre Tabur</a>
 */
@Configuration
public class BenasConfig {

    private final Logger log = LogManager.getLogger(this.getClass());

    @Bean("zeebeClient")
    public ZeebeClient zeebeClient(final ZeebeProperties zeebeProperties) {
        final ZeebeClientBuilder builder = ZeebeClient.newClientBuilder().brokerContactPoint(zeebeProperties.getHost() + ":" + zeebeProperties.getPort()).usePlaintext();
        final ZeebeClient client = builder.build();
        log.info("Connected to: {}", zeebeProperties);
        return client;
    }
}
