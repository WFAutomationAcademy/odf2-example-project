package com.workfusion.odf2.example.task.intake;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.workfusion.odf.test.junit.WorkerJUnitConfig;
import com.workfusion.odf.test.launch.BotTaskUnit;
import com.workfusion.odf.test.s3.S3MockClient;
import com.workfusion.odf.test.s3.S3MockObject;
import com.workfusion.odf2.core.webharvest.TaskVariable;
import com.workfusion.odf2.example.model.Attachment;
import com.workfusion.odf2.example.model.Email;
import com.workfusion.odf2.junit.BotTaskFactory;
import com.workfusion.odf2.junit.OrmSupport;
import com.workfusion.odf2.multiprocess.model.TransactionStatus;
import com.workfusion.odf2.transaction.model.Transaction;
import com.workfusion.odf2.transaction.repository.TransactionRepository;
import com.workfusion.odf2.transaction.task.monitor.MonitorConfigurationEntity;
import com.workfusion.odf2.transaction.task.monitor.MonitorStateEntity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import static com.workfusion.odf2.transaction.task.monitor.AbstractMonitorTaskRunner.MONITOR_REPEAT_VARIABLE;

@WorkerJUnitConfig
class EmailMonitorTaskTest {

    @BeforeEach
    void setUp(OrmSupport ormSupport, S3MockClient s3MockClient) {
        ormSupport.createTables(Transaction.class, Email.class, Attachment.class,
                MonitorStateEntity.class, MonitorConfigurationEntity.class);

        s3MockClient.createBucket("test-bucket");
    }

    @Test
    @DisplayName("should get new emails from external service")
    void shouldGetNewEmailsFromExternalService(BotTaskFactory botTaskFactory, OrmSupport ormSupport, S3MockClient s3MockClient) {
        // given
        BotTaskUnit botTaskUnit = botTaskFactory.fromClass(EmailMonitorTask.class)
                .withDigitalWorkerConfigurationJson("{\"example.attachments.bucket.name\": \"test-bucket\"}");

        // when
        List<Map<String, String>> stepRecords = botTaskUnit.buildAndRun().getRecords();

        // then
        Map<Boolean, List<Map<String, String>>> partitions = stepRecords.stream()
                .collect(Collectors.partitioningBy(record -> Objects.equals(record.get(MONITOR_REPEAT_VARIABLE), "true")));

        List<Map<String, String>> loopRecords = partitions.get(true);
        List<Map<String, String>> transactionRecords = partitions.get(false);

        assertAll("Assert monitor task output", () -> {
            assertThat(loopRecords).hasSize(1);
            assertThat(transactionRecords).hasSize(5);

            transactionRecords.forEach(record -> assertThat(record)
                    .containsKey(TaskVariable.TRANSACTION_ID.toString())
                    .containsEntry(TaskVariable.TRANSACTION_STATUS.toString(), TransactionStatus.INTAKE_IN_PROGRESS.toString()));
        });

        assertAll("Assert database state after monitor task execution", () -> {
            TransactionRepository transactionRepository = ormSupport.getTransactionRepository();
            assertThat(transactionRepository.count()).isEqualTo(5);

            List<Transaction> actualTransactions = transactionRepository.findAll();
            assertThat(actualTransactions).extracting(Transaction::getStatus).containsOnly(TransactionStatus.INTAKE_IN_PROGRESS.toString());

            assertThat(ormSupport.getRepository(Email.class).count()).isEqualTo(5);
            assertThat(ormSupport.getRepository(Attachment.class).count()).isEqualTo(9);
        });

        assertThat(s3MockClient.listObjects("test-bucket"))
                .hasSize(1)
                .extracting(S3MockObject::getKey)
                .allMatch(s -> s.matches("example-attachments/.*.pdf"));
    }

}
