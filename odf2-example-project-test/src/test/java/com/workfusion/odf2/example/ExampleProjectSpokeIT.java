package com.workfusion.odf2.example;

import java.io.File;
import java.net.URL;
import java.util.List;
import java.util.Objects;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.workfusion.odf2.multiprocess.model.TransactionStatus;
import com.workfusion.odf2.transaction.model.Transaction;
import com.workfusion.spoke.ControlTower;
import com.workfusion.spoke.configuration.Configuration;
import com.workfusion.spoke.mock.ConfigurationPayload;

import static org.assertj.core.api.Assertions.assertThat;

class ExampleProjectSpokeIT {

    @Test
    @DisplayName("should run all BPs and wait until completed successfully")
    void shouldRunAllBPsAndWaitUntilCompletedSuccessfully() {
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

        // then run Processing Business Process
        List<Transaction> processingResult = spokeAgent.runBpAndExpectSuccessful("Data_Processing");

        assertThat(processingResult).isNotEmpty()
                .extracting(Transaction::getStatus)
                .containsAnyOf(TransactionStatus.PROCESSING_COMPLETED.toString(), TransactionStatus.ABORTED.toString());

        // then run Submission Business Process
        List<Transaction> submissionResult = spokeAgent.runBpAndExpectSuccessful("Data_Submission");

        assertThat(submissionResult).isNotEmpty()
                .extracting(Transaction::getStatus)
                .containsOnly(TransactionStatus.COMPLETED.toString());

        // then run Error Handling Business Process
        List<Transaction> errorHandlingResult = spokeAgent.runBpAndExpectSuccessful("Error_Handling");

        assertThat(errorHandlingResult).isNotEmpty()
                .extracting(Transaction::getStatus)
                .containsAnyOf(TransactionStatus.COMPLETED.toString(), TransactionStatus.ABORTED.toString());

        // then cleanup test artifacts
        spokeAgent.cleanup();
    }

    private File getResource(String name) {
        URL resource = getClass().getClassLoader().getResource(name);
        Objects.requireNonNull(resource, String.format("Unable to find '%s' resource", name));
        return new File(resource.getFile());
    }

}
