package com.workfusion.odf2.example;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.time.Duration;
import java.util.Collection;
import java.util.Properties;

import com.google.common.collect.Table;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.workfusion.odf2.core.webharvest.TaskVariable;
import com.workfusion.odf2.multiprocess.model.TransactionStatus;
import com.workfusion.spoke.Await;
import com.workfusion.spoke.ControlTower;
import com.workfusion.spoke.bp.BusinessProcessRun;
import com.workfusion.spoke.bp.BusinessProcessSuccessfulResult;
import com.workfusion.spoke.bp.Conditions;
import com.workfusion.spoke.bundle.AssetBundle;
import com.workfusion.spoke.configuration.Configuration;

import static org.assertj.core.api.Assertions.assertThat;

class DataIntakeBusinessProcessIT {

    private String projectVersion;
    private String bundlePath;

    @BeforeEach
    void setUp() {
        Properties projectProperties = readProjectProperties();
        projectVersion = projectProperties.getProperty("project.version");
        String packageModuleName = projectProperties.getProperty("package.module.name");
        bundlePath = String.format("../%s/target/%s-%s.zip", packageModuleName, packageModuleName, projectVersion);
    }

    @Test
    @DisplayName("should run Data Intake BP")
    void shouldRunDataIntakeBp() {
        // given
        ControlTower controlTower = ControlTower.createConfigured(Configuration.fromFileInUserHome("spoke-it.properties")
                .orElse(Configuration.fromResource("/instance.properties")));

        // when
        BusinessProcessRun businessProcessRun = controlTower.importAssetBundle(AssetBundle.fromFile(bundlePath))
                .getBusinessProcessByZipName(String.format("Data_Intake_v_%s.zip", projectVersion))
                .run();

        // then wait for some data appear at 'Final Results' step
        businessProcessRun.waitFor(Await.atMost(Duration.ofMinutes(5)).checkingEvery(Duration.ofSeconds(15)))
                .untilStep("Final Results")
                .hasPendingSubmissions(Conditions.exactly(0));

        // then stop the BP
        businessProcessRun.stop();

        // then wait for BP to complete
        BusinessProcessSuccessfulResult result = businessProcessRun
                .waitFor(Await.atMost(Duration.ofMinutes(1)).checkingEvery(Duration.ofSeconds(5)))
                .untilFinished()
                .expectSuccessful();

        // then assert the output data
        Table<Integer, String, String> outputData = result.getOutputDataAsCSV().asTable();
        Collection<String> actualStatusData = outputData.column(TaskVariable.TRANSACTION_STATUS.toString()).values();

        assertThat(actualStatusData)
                .isNotEmpty()
                .containsOnly(TransactionStatus.INTAKE_COMPLETED.toString());
    }

    private Properties readProjectProperties() {
        try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream("project.properties")) {
            Properties properties = new Properties();
            properties.load(inputStream);
            return properties;
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

}
