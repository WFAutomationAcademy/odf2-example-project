package com.workfusion.odf2.example.task.processing;

import java.net.URL;
import java.util.Objects;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;

import com.workfusion.odf.test.junit.WorkerJUnitConfig;
import com.workfusion.odf.test.launch.OutputData;
import com.workfusion.odf2.core.webharvest.TaskVariable;
import com.workfusion.odf2.example.model.Invoice;
import com.workfusion.odf2.example.model.PdfPage;
import com.workfusion.odf2.example.model.Product;
import com.workfusion.odf2.example.repository.InvoiceRepository;
import com.workfusion.odf2.example.repository.PdfPageRepository;
import com.workfusion.odf2.junit.BotTaskFactory;
import com.workfusion.odf2.junit.OrmSupport;
import com.workfusion.odf2.multiprocess.model.TransactionStatus;
import com.workfusion.odf2.transaction.model.SplitStatus;
import com.workfusion.odf2.transaction.model.Transaction;
import com.workfusion.odf2.transaction.repository.TransactionRepository;

import static org.assertj.core.api.Assertions.assertThat;

@WorkerJUnitConfig
class JoinPdfPagesToInvoiceWithProductsTaskTest {

    private static TransactionRepository transactionRepository;
    private static InvoiceRepository invoiceRepository;
    private static PdfPageRepository pdfPageRepository;
    private static Transaction parentTransaction;
    private static Transaction firstChildTransaction;
    private static Transaction secondChildTransaction;

    @BeforeAll
    static void setUp(OrmSupport ormSupport) throws Exception {
        transactionRepository = ormSupport.getTransactionRepository();
        invoiceRepository = new InvoiceRepository(ormSupport.getConnectionSource());
        pdfPageRepository = new PdfPageRepository(ormSupport.getConnectionSource());

        ormSupport.createTables(Transaction.class, Invoice.class, Product.class, PdfPage.class);

        parentTransaction = createTransaction(TransactionStatus.PROCESSING_IN_PROGRESS);
        firstChildTransaction = createChildTransaction(TransactionStatus.PROCESSING_IN_PROGRESS, parentTransaction);
        secondChildTransaction = createChildTransaction(TransactionStatus.PROCESSING_IN_PROGRESS, parentTransaction);

        createInvoice(parentTransaction, "PDF", "link");
    }

    @Test
    @Order(1)
    @DisplayName("should join first pdf page")
    void shouldJoinFirstPdfPage(BotTaskFactory botTaskFactory) {
        // given
        createPdfPage(firstChildTransaction, getResourceUrl("test-ocr-result.xml"));

        // when
        OutputData outputData = botTaskFactory.fromClass(JoinPdfPagesToInvoiceWithProductsTask.class)
                .withInputData(InputDataBuilder.from(firstChildTransaction).build())
                .buildAndRun();

        // then
        assertThat(outputData.getFirstRecord()).doesNotContainKeys(TaskVariable.TRANSACTION_ID.toString(), "original_document_url");
        assertThat(transactionRepository.findByParentTransaction(parentTransaction).stream().filter(transaction -> SplitStatus.JOINED.toString().equals(transaction.getSplitStatus())))
                .hasSize(1);
    }

    @Test
    @Order(2)
    @DisplayName("should finish join")
    void shouldFinishJoin(BotTaskFactory botTaskFactory) {
        // given
        createPdfPage(secondChildTransaction, getResourceUrl("test-ocr-result.xml"));

        // when
        OutputData outputData = botTaskFactory.fromClass(JoinPdfPagesToInvoiceWithProductsTask.class)
                .withInputData(InputDataBuilder.from(secondChildTransaction).build())
                .buildAndRun();

        // then
        assertThat(outputData.getFirstRecord().get("original_document_url")).isEqualTo("link");
        assertThat(transactionRepository.findByParentTransaction(parentTransaction)).extracting(Transaction::getSplitStatus).containsOnly(SplitStatus.JOINED.toString());
        assertThat(invoiceRepository.findFirstByTransactionId(parentTransaction.getUuid()).getProducts()).hasSize(20);
    }

    private String getResourceUrl(String name) {
        URL resource = getClass().getClassLoader().getResource(name);
        Objects.requireNonNull(resource, String.format("Unable to find '%s' resource", resource));
        return resource.toString();
    }

    private static Transaction createTransaction(TransactionStatus transactionStatus) {
        return createChildTransaction(transactionStatus, null);
    }

    private static Transaction createChildTransaction(TransactionStatus status, Transaction parentTransaction) {
        Transaction transaction = transactionRepository.startNewTransaction(status.toString());
        transaction.setParentTransaction(parentTransaction);
        return transactionRepository.update(transaction);
    }

    private static void createInvoice(Transaction transaction, String type, String documentUrl) {
        Invoice invoice = new Invoice();
        invoice.setTransaction(transaction);
        invoice.setType(type);
        invoice.setOriginalDocumentUrl(documentUrl);
        invoiceRepository.create(invoice);
    }

    private static void createPdfPage(Transaction transaction, String ocrResultUrl) {
        PdfPage pdfPage = new PdfPage();
        pdfPage.setTransaction(transaction);
        pdfPage.setOcrResultUrl(ocrResultUrl);
        pdfPageRepository.create(pdfPage);
    }

}