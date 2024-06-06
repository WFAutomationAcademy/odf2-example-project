package com.workfusion.odf2.example.task.processing;

import java.net.URL;
import java.time.Duration;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.workfusion.odf.test.junit.WorkerJUnitConfig;
import com.workfusion.odf.test.s3.S3MockClient;
import com.workfusion.odf2.example.model.Invoice;
import com.workfusion.odf2.example.model.PdfPage;
import com.workfusion.odf2.example.repository.InvoiceRepository;
import com.workfusion.odf2.example.repository.PdfPageRepository;
import com.workfusion.odf2.junit.BotTaskFactory;
import com.workfusion.odf2.junit.InputDataBuilder;
import com.workfusion.odf2.junit.OrmSupport;
import com.workfusion.odf2.multiprocess.model.TransactionStatus;
import com.workfusion.odf2.transaction.model.Transaction;

import static org.assertj.core.api.Assertions.assertThat;

@WorkerJUnitConfig
class SplitInvoiceToPdfPageTaskTest {

    private InvoiceRepository invoiceRepository;
    private PdfPageRepository pdfPageRepository;
    private Transaction transaction;

    @BeforeEach
    void setUp(OrmSupport ormSupport, S3MockClient s3MockClient) throws Exception {
        invoiceRepository = new InvoiceRepository(ormSupport.getConnectionSource());
        pdfPageRepository = new PdfPageRepository(ormSupport.getConnectionSource());

        ormSupport.createTables(Transaction.class, Invoice.class, PdfPage.class);

        transaction = ormSupport.getTransactionRepository().startNewTransaction(TransactionStatus.SUBMISSION_IN_PROGRESS.name());

        s3MockClient.createBucket("test-bucket");
    }

    @Test
    @DisplayName("should split invoice to pdf pages")
    void shouldSplitInvoiceToPdfPages(BotTaskFactory botTaskFactory, S3MockClient s3MockClient) {
        // given
        URL pdfUrl = s3MockClient.putObjectFromResource("test-bucket", String.format("example-attachments/%s.pdf", UUID.randomUUID()), "multi_page_invoice.pdf");
        createInvoice(pdfUrl.toString());

        // when
        botTaskFactory.fromClass(SplitInvoiceToPdfPageTask.class)
                .withInputData(InputDataBuilder.from(transaction).build())
                .withDigitalWorkerConfigurationJson("{\"example.attachments.bucket.name\": \"test-bucket\"}")
                .withTimeout(Duration.ofSeconds(30))
                .buildAndRun();

        // then
        assertThat(pdfPageRepository.findAll()).hasSize(3);
        assertThat(pdfPageRepository.findAll().stream().map(PdfPage::getOriginalDocumentLink).filter(link -> link.contains("_fake"))).hasSize(1);
    }

    private void createInvoice(String documentLink) {
        Invoice invoice = new Invoice();
        invoice.setTransaction(transaction);
        invoice.setOriginalDocumentUrl(documentLink);
        invoiceRepository.create(invoice);
    }

}
