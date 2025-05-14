package com.workfusion.odf2.example;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

import com.workfusion.odf2.core.webharvest.TaskVariable;
import com.workfusion.odf2.multiprocess.model.TransactionStatus;
import com.workfusion.odf2.spoke.Odf2BusinessProcessSuccessfulResult;
import com.workfusion.odf2.transaction.model.Transaction;
import com.workfusion.spoke.Await;
import com.workfusion.spoke.ControlTower;
import com.workfusion.spoke.bp.BusinessProcessSuccessfulResult;
import com.workfusion.spoke.bundle.AssetBundle;
import com.workfusion.spoke.dw.DigitalWorkerVariation;
import com.workfusion.spoke.mock.ConfigurationPayload;
import com.workfusion.spoke.mock.MockStep;

class ExampleProjectSpokeAgent {

    private final ControlTower controlTower;
    private final ConfigurationPayload configurationPayload;
    private final String projectVersion;
    private final AssetBundle assetBundle;

    private DigitalWorkerVariation digitalWorker;

    ExampleProjectSpokeAgent(ControlTower controlTower, ConfigurationPayload configurationPayload) {
        this.controlTower = Objects.requireNonNull(controlTower);
        this.configurationPayload = Objects.requireNonNull(configurationPayload);

        final ProjectProperties properties = new ProjectProperties();
        projectVersion = properties.getProjectVersion();
        assetBundle = prepareAssetBundle(properties.getBundlePath());
    }

    void importDigitalWorker(String variationName) {
        if (digitalWorker == null) {
            digitalWorker = controlTower.importDigitalWorker(assetBundle);
            digitalWorker.rename(String.format("%s-%s", variationName, LocalDateTime.now()));
        }
    }

    List<Transaction> runBpAndExpectSuccessful(String bpName) {
        Objects.requireNonNull(digitalWorker, "Digital Worker must be imported first");

        return digitalWorker.getBusinessProcessByZipName(namedBusinessProcess(bpName))
                .run()
                .waitFor(Await.atMost(Duration.ofMinutes(3)).checkingEvery(Duration.ofSeconds(5)))
                .untilFinished()
                .expectSuccessful()
                .convertTo(Odf2BusinessProcessSuccessfulResult.class)
                .getTransactions(assetBundle);
    }

    CompletableFuture<BusinessProcessSuccessfulResult> runBpAndExpectSuccessAsync(String bpName) {
        Objects.requireNonNull(digitalWorker, "Digital Worker must be imported first");

        return digitalWorker.getBusinessProcessByZipName(namedBusinessProcess(bpName))
                .runAndExpectSuccessAsync(Await.atMost(Duration.ofMinutes(6)).checkingEvery(Duration.ofSeconds(5)));
    }

    void cleanup() {
        digitalWorker.delete();
    }

    private AssetBundle prepareAssetBundle(String bundlePath) {
        final MockStep.BotTaskBuilder exceptionHandlingTaskMock = MockStep.named("Manual exception handling")
                .botTaskBuilder()
                .exportSingleColumn(TaskVariable.NEW_TRANSACTION_STATUS.toString(), TransactionStatus.COMPLETED.toString());

        return AssetBundle.fromFile(bundlePath)
                .withBusinessProcess(namedBusinessProcess("Error_Handling"), exceptionHandlingTaskMock)
                .with(configurationPayload);
    }

    private String namedBusinessProcess(String bpName) {
        return String.format("%s_v_%s.zip", bpName, projectVersion);
    }

}
