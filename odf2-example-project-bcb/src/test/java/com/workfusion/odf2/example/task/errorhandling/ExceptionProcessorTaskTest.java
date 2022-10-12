package com.workfusion.odf2.example.task.errorhandling;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.workfusion.odf.test.junit.WorkerJUnitConfig;
import com.workfusion.odf.test.launch.InputData;
import com.workfusion.odf2.core.webharvest.TaskVariable;
import com.workfusion.odf2.junit.BotTaskFactory;
import com.workfusion.odf2.junit.OrmSupport;
import com.workfusion.odf2.multiprocess.model.ErrorEntity;
import com.workfusion.odf2.multiprocess.model.ErrorType;
import com.workfusion.odf2.multiprocess.model.TransactionStatus;
import com.workfusion.odf2.multiprocess.repository.ErrorRepository;
import com.workfusion.odf2.transaction.model.Transaction;
import com.workfusion.odf2.transaction.repository.TransactionRepository;

import static org.assertj.core.api.Assertions.assertThat;

import static com.workfusion.odf2.multiprocess.model.TransactionStatus.PROCESSING_IN_PROGRESS;

@WorkerJUnitConfig
class ExceptionProcessorTaskTest {

    private TransactionRepository transactionRepository;
    private ErrorRepository errorRepository;

    @BeforeEach
    void setUp(OrmSupport ormSupport) throws SQLException {
        transactionRepository = ormSupport.getTransactionRepository();
        errorRepository = new ErrorRepository(ormSupport.getConnectionSource());

        ormSupport.createTables(Transaction.class, ErrorEntity.class);
    }

    @Test
    @DisplayName("should set new transaction status to 'aborted'")
    void shouldSetNewTransactionStatusToAborted(BotTaskFactory botTaskFactory) {
        // given
        Transaction transaction = createTransaction(PROCESSING_IN_PROGRESS);
        createErrorEntity(transaction);

        // when
        Map<String, String> actualRecord = botTaskFactory.fromClass(ExceptionProcessorTask.class)
                .withInputData(createInputData(transaction))
                .buildAndRun()
                .getFirstRecord();

        // then
        assertThat(actualRecord).containsEntry(TaskVariable.NEW_TRANSACTION_STATUS.toString(), TransactionStatus.ABORTED.toString());
    }

    private Transaction createTransaction(TransactionStatus status) {
        Transaction transaction = new Transaction();
        transaction.setStatus(status.name());
        return transactionRepository.create(transaction);
    }

    private ErrorEntity createErrorEntity(Transaction transaction) {
        ErrorEntity errorEntity = new ErrorEntity();
        errorEntity.setTransaction(transaction);
        errorEntity.setType(ErrorType.BUSINESS);
        return errorRepository.create(errorEntity);
    }

    private static InputData createInputData(Transaction transaction) {
        return InputData.of(
                Arrays.asList(TaskVariable.TRANSACTION_ID.toString(), TaskVariable.TRANSACTION_STATUS.toString()),
                Arrays.asList(transaction.getUuid().toString(), transaction.getStatus()));
    }

}
