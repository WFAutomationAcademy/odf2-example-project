package com.workfusion.odf2.example;

import java.io.File;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import org.assertj.core.util.Strings;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.workfusion.odf2.core.webharvest.TaskVariable;
import com.workfusion.odf2.multiprocess.model.TransactionStatus;
import com.workfusion.odf2.spoke.Odf2BusinessProcessSuccessfulResult;
import com.workfusion.odf2.transaction.model.Transaction;
import com.workfusion.spoke.ControlTower;
import com.workfusion.spoke.bp.BusinessProcessSuccessfulResult;
import com.workfusion.spoke.configuration.Configuration;
import com.workfusion.spoke.mock.ConfigurationPayload;

import static org.assertj.core.api.Assertions.assertThat;

class ExampleProjectSpokeIT {

    @Test
    @DisplayName("should run all BPs and wait until completed successfully")
    void shouldRunAllBPsAndWaitUntilCompletedSuccessfully() throws Exception {
        // given
        ControlTower controlTower = ControlTower.createConfigured(Configuration.fromFileInUserHome("spoke-it.properties")
                .orElse(Configuration.fromFileSpecifiedInSystemProperty())
                .orElse(Configuration.fromResource("/instance.properties")));

        ExampleProjectSpokeAgent spokeAgent = new ExampleProjectSpokeAgent(controlTower,
                ConfigurationPayload.fromFile(getResource("test_configuration_payload.json")));

        SecretsVaultService secretsVault = new SecretsVaultService(controlTower);
        secretsVault.setInvoicePlaneCredentials();

        // when
        spokeAgent.importDigitalWorker("odf2-example-project-test");

        // then run Intake Business Process
        List<Transaction> intakeResult = spokeAgent.runBpAndExpectSuccessful("Data_Intake");

        assertThat(intakeResult).isNotEmpty()
                .extracting(Transaction::getStatus)
                .containsOnly(TransactionStatus.INTAKE_COMPLETED.toString());

        // then run Processing and Error Handling Business Process
        CompletableFuture<BusinessProcessSuccessfulResult> processingResult = spokeAgent.runBpAndExpectSuccessAsync("Data_Processing");

        CompletableFuture<BusinessProcessSuccessfulResult> errorHandlingResult = spokeAgent.runBpAndExpectSuccessAsync("Error_Handling");

        assertThat(getTransactionalOutput(errorHandlingResult)).isNotEmpty()
                .extracting(input -> input.get(TaskVariable.TRANSACTION_STATUS.toString()))
                .containsAnyOf(TransactionStatus.INTAKE_COMPLETED.toString(), TransactionStatus.COMPLETED.toString());
        assertThat(getTransactionalOutput(errorHandlingResult)).extracting(input -> input.get(TaskVariable.SKIP_UNTIL.toString()))
                .containsAnyOf("PreparePdfPageToOcrTask", "");

        assertThat(getTransactionalOutput(processingResult)).isNotEmpty()
                .extracting(input -> input.get(TaskVariable.TRANSACTION_STATUS.toString()))
                .containsAnyOf(TransactionStatus.PROCESSING_COMPLETED.toString(), TransactionStatus.ABORTED.toString());
        assertThat(getTransactionalOutput(processingResult).stream().filter(stringStringMap -> !Strings.isNullOrEmpty(stringStringMap.get("original_document_url")))).hasSize(1);


        // then run Submission Business Process
        List<Transaction> submissionResult = spokeAgent.runBpAndExpectSuccessful("Data_Submission");

        assertThat(submissionResult).isNotEmpty()
                .extracting(Transaction::getStatus)
                .containsOnly(TransactionStatus.COMPLETED.toString());
        assertThat(submissionResult)
                .extracting(Transaction::getErrorStatus)
                .containsOnlyNulls();

        // then cleanup test artifacts
        spokeAgent.cleanup();
    }

    private File getResource(String name) {
        URL resource = getClass().getClassLoader().getResource(name);
        Objects.requireNonNull(resource, String.format("Unable to find '%s' resource", name));
        return new File(resource.getFile());
    }

    private List<Map<String, String>> getTransactionalOutput(CompletableFuture<BusinessProcessSuccessfulResult> bpResult) throws Exception {
        return bpResult.get()
                .convertTo(Odf2BusinessProcessSuccessfulResult.class)
                .getTransactionalOutput()
                .collect(Collectors.toList());
    }
}
