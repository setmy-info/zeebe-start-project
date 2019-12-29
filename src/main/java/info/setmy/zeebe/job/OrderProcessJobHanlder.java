package info.setmy.zeebe.job;

import info.setmy.zeebe.model.Order;
import io.zeebe.client.api.response.ActivatedJob;
import io.zeebe.client.api.worker.JobClient;
import io.zeebe.client.api.worker.JobHandler;
import javax.inject.Named;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Named("orderProcessJobHanlder")
public class OrderProcessJobHanlder implements JobHandler {

    private final Logger log = LogManager.getLogger(this.getClass());

    @Override
    public void handle(final JobClient jobCloent, final ActivatedJob job) throws Exception {
        log.info(job);
        final Order order = job.getVariablesAsType(Order.class);
        log.info("Job executed for order: {}", order);
        jobCloent.newCompleteCommand(job.getKey()).send().join();
    }
}
