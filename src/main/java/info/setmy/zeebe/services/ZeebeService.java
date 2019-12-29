package info.setmy.zeebe.services;

import info.setmy.zeebe.config.ZeebeProperties;
import info.setmy.zeebe.handlers.CollectMoneyHanlder;
import info.setmy.zeebe.handlers.FetchItemsHanlder;
import info.setmy.zeebe.handlers.OrderPlacedHanlder;
import info.setmy.zeebe.handlers.ShipParcelHanlder;
import info.setmy.zeebe.handlers.ZeebeHandler;
import io.zeebe.client.ZeebeClient;
import io.zeebe.client.ZeebeClientBuilder;
import io.zeebe.client.api.response.DeploymentEvent;
import io.zeebe.client.api.response.Workflow;
import io.zeebe.client.api.response.WorkflowInstanceEvent;
import io.zeebe.client.api.worker.JobHandler;
import io.zeebe.client.api.worker.JobWorker;
import java.time.Duration;
import java.util.Scanner;
import javax.inject.Named;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Named("zeebeService")
public class ZeebeService {

    private final Logger log = LogManager.getLogger(this.getClass());

    private final ZeebeProperties zeebeProperties;

    private ZeebeClient client;

    private final CollectMoneyHanlder collectMoneyHanlder;
    private final FetchItemsHanlder fetchItemsHanlder;
    private final ShipParcelHanlder shipParcelHanlder;
    private final OrderPlacedHanlder orderPlacedHanlder;
    private Workflow workFlow;

    public ZeebeService(
            final OrderPlacedHanlder orderPlacedHanlder,
            final CollectMoneyHanlder collectMoneyHanlder,
            final FetchItemsHanlder fetchItemsHanlder,
            final ShipParcelHanlder shipParcelHanlder,
            final ZeebeProperties zeebeProperties
    ) {
        this.orderPlacedHanlder = orderPlacedHanlder;
        this.collectMoneyHanlder = collectMoneyHanlder;
        this.fetchItemsHanlder = fetchItemsHanlder;
        this.shipParcelHanlder = shipParcelHanlder;
        this.zeebeProperties = zeebeProperties;
    }

    public void execute() {
        initClient();
        //registerWorkflow();
        //createWorkflowInstance();
        handleJobs();
        close();
    }

    private void initClient() {
        final ZeebeClientBuilder builder = ZeebeClient.newClientBuilder().brokerContactPoint(zeebeProperties.getHost() + ":" + zeebeProperties.getPort()).usePlaintext();
        client = builder.build();
        log.info("Connected to: {}", zeebeProperties);
    }

    private void registerWorkflow() {
        final DeploymentEvent deployment = client.newDeployCommand()
                .addResourceFromClasspath("order-process.bpmn")
                .send()
                .join();
        workFlow = deployment.getWorkflows().get(0);
        log.info(
                "Workflow deployed. resourceName: {}, version: {}, bpmnProcessId: {}, workflowKey: {}",
                workFlow.getResourceName(),
                workFlow.getVersion(),
                workFlow.getBpmnProcessId(),
                workFlow.getWorkflowKey()
        );
    }

    private void createWorkflowInstance() {
        final WorkflowInstanceEvent workflowInstance = client.newCreateInstanceCommand()
                .bpmnProcessId(workFlow.getBpmnProcessId())
                .latestVersion()
                .send()
                .join();
        log.info("Workflow instance created. workflowInstanceKey: {}, bpmnProcessId: {}, version: {}, workflowKey: {}",
                workflowInstance.getWorkflowInstanceKey(),
                workflowInstance.getBpmnProcessId(),
                workflowInstance.getVersion(),
                workflowInstance.getWorkflowKey()
        );
    }

    private void handleJobs() {
        final JobWorker orderPlacedJobWorker = handle(orderPlacedHanlder);
        final JobWorker collectMoneyJobWorker = handle(collectMoneyHanlder);
        final JobWorker fetchItemsJobWorker = handle(fetchItemsHanlder);
        final JobWorker shipParcelJobWorker = handle(shipParcelHanlder);
        log.info("Job worker opened and receiving jobs.");
        waitUntilSystemInput();
        orderPlacedJobWorker.close();
        collectMoneyJobWorker.close();
        fetchItemsJobWorker.close();
        shipParcelJobWorker.close();
    }

    private JobWorker handle(final JobHandler handler) {
        final String taskName = ((ZeebeHandler) handler).getTaskName();
        log.info("Opening job worker: {}", taskName);
        final JobWorker jobWorker = client
                .newWorker()
                .jobType(taskName)
                .handler(handler)
                .timeout(Duration.ofSeconds(10))
                .open();
        return jobWorker;
    }

    private void waitUntilSystemInput() {
        try (Scanner scanner = new Scanner(System.in)) {
            while (scanner.hasNextLine()) {
                final String nextLine = scanner.nextLine();
                if (nextLine.contains("exit")) {
                    return;
                }
            }
        }
    }

    private void close() {
        client.close();
        log.info("Closed!");
    }
}
