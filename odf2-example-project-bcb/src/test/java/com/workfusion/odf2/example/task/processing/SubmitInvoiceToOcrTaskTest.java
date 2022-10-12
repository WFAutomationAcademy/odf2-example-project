package com.workfusion.odf2.example.task.processing;

import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.apache.curator.shaded.com.google.common.io.Resources;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import com.workfusion.odf.test.junit.WorkerJUnitConfig;
import com.workfusion.odf.test.launch.InputData;
import com.workfusion.odf.test.launch.OutputData;
import com.workfusion.odf.test.ocr.OcrMock;
import com.workfusion.odf.test.s3.S3MockClient;
import com.workfusion.odf2.core.settings.ConfigEntity;
import com.workfusion.odf2.core.webharvest.TaskVariable;
import com.workfusion.odf2.example.model.Invoice;
import com.workfusion.odf2.example.repository.InvoiceRepository;
import com.workfusion.odf2.junit.BotTaskFactory;
import com.workfusion.odf2.junit.OrmSupport;
import com.workfusion.odf2.transaction.model.Transaction;
import com.workfusion.odf2.transaction.repository.TransactionRepository;

import static org.assertj.core.api.Assertions.assertThat;

import static com.workfusion.odf2.multiprocess.model.TransactionStatus.PROCESSING_IN_PROGRESS;

@WorkerJUnitConfig
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class SubmitInvoiceToOcrTaskTest {

    private static TransactionRepository transactionRepository;
    private static InvoiceRepository invoiceRepository;
    private static OutputData stepData;

    @BeforeAll
    static void beforeAll(OrmSupport ormSupport, OcrMock ocrMock, S3MockClient s3MockClient) throws SQLException {
        transactionRepository = ormSupport.getTransactionRepository();
        invoiceRepository = new InvoiceRepository(ormSupport.getConnectionSource());

        ormSupport.createTables(Transaction.class, Invoice.class, ConfigEntity.class);
        ormSupport.getConfigRepository().create(new ConfigEntity("odf.ocr.s3.bucket", "test-bucket"));
        ormSupport.getConfigRepository().create(new ConfigEntity("odf.ocr.cache.enabled", "false"));

        s3MockClient.createBucket("test-bucket");

        ocrMock.shouldReturn(
                        "Lorem ipsum dolor sit amet",
                        "<document><page height='7' width='13'><charParams content='X', t='T', r='R', b='B', l='L' /></page></document>",
                        "Lorem ipsum dolor sit amet")
                .withPages("page")
                .forAnyImage();
    }

    @Test
    @Order(1)
    @DisplayName("should skip task if invoice type is not PDF")
    void shouldSkipTaskIfInvoiceTypeIsNotPdf(BotTaskFactory botTaskFactory) {
        // given
        Transaction transaction = transactionRepository.startNewTransaction(PROCESSING_IN_PROGRESS.toString());
        createInvoice(transaction, "HTML", "http://test-document.link");

        // when
        Map<String, String> actualRecord = botTaskFactory.fromClass(SubmitInvoiceToOcrTask.class)
                .withInputData(createInputData(transaction))
                .buildAndRun()
                .getFirstRecord();

        // then
        assertThat(actualRecord).doesNotContainKey(TaskVariable.OCR_TASK_ID.toString());
    }

    @Test
    @Order(2)
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
        assertThat(actualRecord).doesNotContainKey(TaskVariable.OCR_TASK_ID.toString());
    }

    @Test
    @Order(3)
    @DisplayName("should send document to OCR")
    void shouldSendDocumentToOcr(BotTaskFactory botTaskFactory, S3MockClient s3MockClient) {
        // given
        URL documentUrl = s3MockClient.putObject("test-bucket", "ocr-input/test-document.pdf", "PDF content");

        Transaction transaction = transactionRepository.startNewTransaction(PROCESSING_IN_PROGRESS.toString());
        createInvoice(transaction, "PDF", documentUrl.toString());

        // when
        stepData = botTaskFactory.fromClass(SubmitInvoiceToOcrTask.class)
                .withInputData(createInputData(transaction))
                .buildAndRun();

        // then
        assertThat(stepData.getFirstRecord()).containsKey(TaskVariable.OCR_TASK_ID.toString());
    }

    @Test
    @Order(4)
    @DisplayName("should retrieve document from OCR")
    void shouldRetrieveDocumentFromOcr(BotTaskFactory botTaskFactory) throws Exception {
        // when
        String transactionId = botTaskFactory.fromClass(RetrieveInvoiceOcrResultTask.class)
                .withInputData(InputData.fromResource(stepData.getFile()))
                .buildAndRun()
                .getFirstRecord()
                .get(TaskVariable.TRANSACTION_ID.toString());

        // then
        List<Invoice> invoices = invoiceRepository.findByTransactionId(UUID.fromString(transactionId));
        assertThat(invoices).hasSize(1);

        String ocrResultUrl = invoices.get(0).getOcrResultUrl();
        assertThat(ocrResultUrl).isNotEmpty();

        String actualContent = Resources.toString(new URL(ocrResultUrl), StandardCharsets.UTF_8);
        assertThat(actualContent).isEqualTo("Lorem ipsum dolor sit amet");
    }

    private static Invoice createInvoice(Transaction transaction, String type, String documentUrl) {
        Invoice invoice = new Invoice();
        invoice.setTransaction(transaction);
        invoice.setType(type);
        invoice.setOriginalDocumentUrl(documentUrl);
        return invoiceRepository.create(invoice);
    }

    private static InputData createInputData(Transaction transaction) {
        return InputData.of(
                Arrays.asList(TaskVariable.TRANSACTION_ID.toString(), TaskVariable.TRANSACTION_STATUS.toString()),
                Arrays.asList(transaction.getUuid().toString(), transaction.getStatus()));
    }

}
