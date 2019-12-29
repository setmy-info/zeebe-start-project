package info.setmy.zeebe.services;

import info.setmy.zeebe.job.OrderProcessJobHanlder;
import io.zeebe.client.ZeebeClient;
import io.zeebe.client.api.response.DeploymentEvent;
import io.zeebe.client.api.response.WorkflowInstanceEvent;
import io.zeebe.client.api.worker.JobWorker;
import java.time.Duration;
import java.util.Scanner;
import javax.inject.Named;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Named("zeebeService")
public class ZeebeService {

    private final Logger log = LogManager.getLogger(this.getClass());

    private final ZeebeClient zeebeClient;

    private final OrderProcessJobHanlder orderProcessJobHanlder;

    public ZeebeService(final ZeebeClient zeebeClient, final OrderProcessJobHanlder orderProcessJobHanlder) {
        this.zeebeClient = zeebeClient;
        this.orderProcessJobHanlder = orderProcessJobHanlder;
    }

    public void init() {
        registerWorkflow();
        //createWorkflowInstance();
    }

    public void handleJobs() {
        log.info("Opening job worker.");
        final JobWorker workerRegistration = zeebeClient
                .newWorker()
                .jobType("collect-money")
                .handler(orderProcessJobHanlder)
                .timeout(Duration.ofSeconds(10))
                .open();
        log.info("Job worker opened and receiving jobs.");
        waitUntilSystemInput(workerRegistration);
    }

    private void waitUntilSystemInput(final JobWorker workerRegistration) {
        try (Scanner scanner = new Scanner(System.in)) {
            while (scanner.hasNextLine()) {
                final String nextLine = scanner.nextLine();
                if (nextLine.contains("exit")) {
                    workerRegistration.close();
                    return;
                } else if (nextLine.contains("order")) {
                    createWorkflowInstance();
                }
            }
        }
    }

    public void close() {
        zeebeClient.close();
        log.info("Closed!");
    }

    private void registerWorkflow() {
        final DeploymentEvent deployment = zeebeClient.newDeployCommand()
                .addResourceFromClasspath("order-process.bpmn")
                .send()
                .join();
        final int version = deployment.getWorkflows().get(0).getVersion();
        log.info("Workflow deployed. Version: {}", version);
    }

    private void createWorkflowInstance() {
        final WorkflowInstanceEvent wfInstance = zeebeClient.newCreateInstanceCommand()
                .bpmnProcessId("order-process")
                .latestVersion()
                .send()
                .join();
        final long workflowInstanceKey = wfInstance.getWorkflowInstanceKey();
        log.info("Workflow instance created. Key: {}", workflowInstanceKey);
    }
}
