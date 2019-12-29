package info.setmy.zeebe;

import info.setmy.zeebe.services.ZeebeService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 *
 * @author <a href="mailto:imre.tabur@eesti.ee">Imre Tabur</a>
 */
@SpringBootApplication
@EnableScheduling
public class ZeebeApplication implements CommandLineRunner {

    private final ZeebeService zeebeService;

    public ZeebeApplication(final ZeebeService zeebeService) {
        this.zeebeService = zeebeService;
    }

    public static void main(String[] args) {
        final SpringApplication application = new SpringApplication(ZeebeApplication.class);
        application.run(args);
    }

    @Override
    public void run(final String... args) throws Exception {
        zeebeService.init();
        zeebeService.handleJobs();
        zeebeService.close();
    }
}
