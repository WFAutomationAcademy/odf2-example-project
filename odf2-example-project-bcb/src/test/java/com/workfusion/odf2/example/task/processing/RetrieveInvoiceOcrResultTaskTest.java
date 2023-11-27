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
import com.workfusion.odf2.transaction.model.Transaction;
import com.workfusion.odf2.transaction.repository.TransactionRepository;

import static org.assertj.core.api.Assertions.assertThat;

import static com.workfusion.odf2.multiprocess.model.TransactionStatus.PROCESSING_IN_PROGRESS;

@WorkerJUnitConfig
class RetrieveInvoiceOcrResultTaskTest {

    private TransactionRepository transactionRepository;
    private PdfPageRepository pdfPageRepository;

    @BeforeEach
    void setUp(OrmSupport ormSupport) throws SQLException {
        transactionRepository = ormSupport.getTransactionRepository();
        pdfPageRepository = new PdfPageRepository(ormSupport.getConnectionSource());

        ormSupport.createTables(Transaction.class, PdfPage.class);
    }

    @Test
    @DisplayName("should retrieve OCR result")
    void shouldRetrieveOcrResult(BotTaskFactory botTaskFactory) {
        // given
        Transaction transaction = transactionRepository.startNewTransaction(PROCESSING_IN_PROGRESS.toString());
        createPdfPage(transaction);

        String metaInfoJson = "{\"ocrXmlUrl\": \"https://expected.ocr.result\"}";

        // when
        Map<String, String> actualRecord = botTaskFactory.fromClass(RetrieveInvoiceOcrResultTask.class)
                .withInputData(createInputData(transaction, metaInfoJson))
                .buildAndRun()
                .getFirstRecord();

        // then
        assertThat(actualRecord).doesNotContainKey("meta_info_json");

        assertThat(pdfPageRepository.findByTransactionId(transaction.getUuid()))
                .hasSize(1)
                .extracting(PdfPage::getOcrResultUrl)
                .containsExactly("https://expected.ocr.result");
    }

    private void createPdfPage(Transaction transaction) {
        PdfPage pdfPage = new PdfPage();
        pdfPage.setTransaction(transaction);
        pdfPageRepository.create(pdfPage);
    }

    private static InputData createInputData(Transaction transaction, String metaInfoJson) {
        return InputData.of(
                Arrays.asList(TaskVariable.TRANSACTION_ID.toString(), TaskVariable.TRANSACTION_STATUS.toString(), "meta_info_json"),
                Arrays.asList(transaction.getUuid().toString(), transaction.getStatus(), metaInfoJson));
    }

}
