package com.workfusion.odf2.example.task.processing;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.workfusion.odf.test.junit.WorkerJUnitConfig;
import com.workfusion.odf.test.launch.InputData;
import com.workfusion.odf2.core.webharvest.TaskVariable;
import com.workfusion.odf2.example.model.PdfPage;
import com.workfusion.odf2.example.repository.PdfPageRepository;
import com.workfusion.odf2.junit.BotTaskFactory;
import com.workfusion.odf2.junit.OrmSupport;
import com.workfusion.odf2.multiprocess.model.ErrorEntity;
import com.workfusion.odf2.multiprocess.repository.ErrorRepository;
import com.workfusion.odf2.transaction.model.JoinEntity;
import com.workfusion.odf2.transaction.model.Transaction;
import com.workfusion.odf2.transaction.repository.TransactionRepository;

import static org.assertj.core.api.Assertions.assertThat;


import static com.workfusion.odf2.multiprocess.model.TransactionErrorStatus.HAS_ERROR;
import static com.workfusion.odf2.multiprocess.model.TransactionStatus.PROCESSING_IN_PROGRESS;

@WorkerJUnitConfig
class PreparePdfPageToOcrTaskTest {

    private TransactionRepository transactionRepository;
    private PdfPageRepository pdfPageRepository;
    private ErrorRepository errorRepository;

    @BeforeEach
    void setUp(OrmSupport ormSupport) throws SQLException {
        transactionRepository = ormSupport.getTransactionRepository();
        pdfPageRepository = new PdfPageRepository(ormSupport.getConnectionSource());
        errorRepository = new ErrorRepository(ormSupport.getConnectionSource());

        ormSupport.createTables(Transaction.class, PdfPage.class, ErrorEntity.class, JoinEntity.class);
    }

    @Test
    @DisplayName("Should add column with original document link")
    void shouldAddColumnWithOriginalDocumentLink(BotTaskFactory botTaskFactory) {
        // given
        Transaction transaction = transactionRepository.startNewTransaction(PROCESSING_IN_PROGRESS.toString());
        createPdfPage(transaction, "link");

        // when
        Map<String, String> actualRecord = botTaskFactory.fromClass(PreparePdfPageToOcrTask.class)
                .withInputData(createInputData(transaction))
                .buildAndRun()
                .getFirstRecord();

        // then
        assertThat(actualRecord).containsEntry("original_document_url", "link");
    }

    @Test
    @DisplayName("should throw exception if link contain fake data")
    void shouldThrowExceptionIfLinkContainFakeData(BotTaskFactory botTaskFactory) {
        // given
        Transaction transaction = transactionRepository.startNewTransaction(PROCESSING_IN_PROGRESS.toString());
        createPdfPage(transaction, "link_fake");

        // when
        Map<String, String> actualRecord = botTaskFactory.fromClass(PreparePdfPageToOcrTask.class)
                .withInputData(createInputData(transaction))
                .buildAndRun()
                .getFirstRecord();

        // then
        assertThat(actualRecord).containsEntry(TaskVariable.ERROR_STATUS.toString(), HAS_ERROR.toString());

        assertThat(errorRepository.findAll()).hasSize(1);
        assertThat(errorRepository.findFirstByTransactionId(transaction.getUuid()).getShortDescription()).isEqualTo("Page has incorrect link.");
    }

    private void createPdfPage(Transaction transaction, String originalDocumentLink) {
        PdfPage pdfPage = new PdfPage();
        pdfPage.setTransaction(transaction);
        pdfPage.setOriginalDocumentLink(originalDocumentLink);
        pdfPageRepository.create(pdfPage);
    }

    private static InputData createInputData(Transaction transaction) {
        return InputData.of(
                Arrays.asList(TaskVariable.TRANSACTION_ID.toString(), TaskVariable.TRANSACTION_STATUS.toString()),
                Arrays.asList(transaction.getUuid().toString(), transaction.getStatus()));
    }
}