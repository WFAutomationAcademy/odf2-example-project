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
import com.workfusion.odf2.example.model.Invoice;
import com.workfusion.odf2.example.repository.InvoiceRepository;
import com.workfusion.odf2.junit.BotTaskFactory;
import com.workfusion.odf2.junit.OrmSupport;
import com.workfusion.odf2.multiprocess.model.TransactionErrorStatus;
import com.workfusion.odf2.transaction.model.Transaction;
import com.workfusion.odf2.transaction.repository.TransactionRepository;

import static org.assertj.core.api.Assertions.assertThat;

import static com.workfusion.odf2.multiprocess.model.TransactionErrorStatus.HAS_ERROR;
import static com.workfusion.odf2.multiprocess.model.TransactionStatus.PROCESSING_IN_PROGRESS;

@WorkerJUnitConfig
class SubmitInvoiceToOcrTaskTest {

    private TransactionRepository transactionRepository;
    private InvoiceRepository invoiceRepository;

    @BeforeEach
    void setUp(OrmSupport ormSupport) throws SQLException {
        transactionRepository = ormSupport.getTransactionRepository();
        invoiceRepository = new InvoiceRepository(ormSupport.getConnectionSource());

        ormSupport.createTables(Transaction.class, Invoice.class);
    }

    @Test
    @DisplayName("should skip task if invoice type is not PDF")
    void shouldSkipTaskIfInvoiceTypeIsNotPdf(BotTaskFactory botTaskFactory) {
        // given
        Transaction transaction = transactionRepository.startNewTransaction(PROCESSING_IN_PROGRESS.toString());
        createInvoice(transaction, "HTML", "https://test-document.link");

        // when
        Map<String, String> actualRecord = botTaskFactory.fromClass(SubmitInvoiceToOcrTask.class)
                .withInputData(createInputData(transaction))
                .buildAndRun()
                .getFirstRecord();

        // then
        assertThat(actualRecord.get("is_pdf_document")).isEqualTo("false");
    }

    @Test
    @DisplayName("should skip task if invoice does not contain document url")
    void shouldSkipTaskIfInvoiceDoesNotContainDocumentUrl(BotTaskFactory botTaskFactory) {
        // given
        Transaction transaction = transactionRepository.startNewTransaction(PROCESSING_IN_PROGRESS.toString());
        createInvoice(transaction, "PDF", null);

        // when
        Map<String, String> actualRecord = botTaskFactory.fromClass(SubmitInvoiceToOcrTask.class)
                .withInputData(createInputData(transaction))
                .buildAndRun()
                .getFirstRecord();

        // then
        assertThat(actualRecord.get("is_pdf_document")).isEqualTo("false");
    }

    @Test
    @DisplayName("should send document to OCR")
    void shouldSendDocumentToOcr(BotTaskFactory botTaskFactory) {
        // given
        Transaction transaction = transactionRepository.startNewTransaction(PROCESSING_IN_PROGRESS.toString());
        createInvoice(transaction, "PDF", "https://expected.document.url");

        // when
        Map<String, String> actualRecord = botTaskFactory.fromClass(SubmitInvoiceToOcrTask.class)
                .withInputData(createInputData(transaction))
                .buildAndRun()
                .getFirstRecord();

        // then
        assertThat(actualRecord.get("is_pdf_document")).isEqualTo("true");
    }

    @Test
    @DisplayName("should send document to OCR")
    void shouldSendDocumentToOcr1(BotTaskFactory botTaskFactory) {
        // given
        Transaction transaction = createTransactionWithSkipFields(HAS_ERROR, null);
        createInvoice(transaction, "PDF", "https://expected.document.url");

        // when
        Map<String, String> actualRecord = botTaskFactory.fromClass(SubmitInvoiceToOcrTask.class)
                .withInputData(createInputData(transaction))
                .buildAndRun()
                .getFirstRecord();

        // then
        assertThat(actualRecord.get("is_pdf_document")).isEqualTo("false");
    }

    @Test
    @DisplayName("should send document to OCR")
    void shouldSendDocumentToOcr2(BotTaskFactory botTaskFactory) {
        // given
        Transaction transaction = createTransactionWithSkipFields(HAS_ERROR, "TestTask");
        createInvoice(transaction, "PDF", "https://expected.document.url");

        // when
        Map<String, String> actualRecord = botTaskFactory.fromClass(SubmitInvoiceToOcrTask.class)
                .withInputData(createInputData(transaction))
                .buildAndRun()
                .getFirstRecord();

        // then
        assertThat(actualRecord.get("is_pdf_document")).isEqualTo("true");
    }

    private Transaction createTransactionWithSkipFields(TransactionErrorStatus errorStatus, String skipUntilValue) {
        Transaction transaction = transactionRepository.startNewTransaction(PROCESSING_IN_PROGRESS.toString());

        transaction.setSkipUntil(skipUntilValue);
        transaction.setErrorStatus(errorStatus.toString());

        return transactionRepository.update(transaction);
    }

    private Invoice createInvoice(Transaction transaction, String type, String documentUrl) {
        Invoice invoice = new Invoice();
        invoice.setTransaction(transaction);
        invoice.setType(type);
        invoice.setOriginalDocumentUrl(documentUrl);
        return invoiceRepository.create(invoice);
    }

    private static InputData createInputData(Transaction transaction) {
        return InputData.of(
                Arrays.asList(TaskVariable.TRANSACTION_ID.toString(), TaskVariable.TRANSACTION_STATUS.toString(), TaskVariable.ERROR_STATUS.toString(), TaskVariable.SKIP_UNTIL.toString()),
                Arrays.asList(transaction.getUuid().toString(), transaction.getStatus(), transaction.getErrorStatus(), transaction.getSkipUntil().orElse("")));
    }

}
