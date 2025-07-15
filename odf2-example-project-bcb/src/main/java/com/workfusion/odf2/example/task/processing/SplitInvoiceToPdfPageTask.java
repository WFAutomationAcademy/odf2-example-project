package com.workfusion.odf2.example.task.processing;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Random;
import java.util.stream.Stream;
import javax.inject.Inject;

import org.apache.pdfbox.multipdf.Splitter;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.slf4j.Logger;

import com.workfusion.odf2.compiler.BotTask;
import com.workfusion.odf2.core.cdi.Requires;
import com.workfusion.odf2.core.settings.Configuration;
import com.workfusion.odf2.core.task.TaskInput;
import com.workfusion.odf2.example.model.Invoice;
import com.workfusion.odf2.example.model.PdfPage;
import com.workfusion.odf2.example.module.RepositoryModule;
import com.workfusion.odf2.example.repository.InvoiceRepository;
import com.workfusion.odf2.example.repository.PdfPageRepository;
import com.workfusion.odf2.service.s3.S3Bucket;
import com.workfusion.odf2.service.s3.S3Module;
import com.workfusion.odf2.service.s3.S3Service;
import com.workfusion.odf2.transaction.CurrentTransaction;
import com.workfusion.odf2.transaction.TransactionBuilder;
import com.workfusion.odf2.transaction.TransactionResult;
import com.workfusion.odf2.transaction.model.Transaction;
import com.workfusion.odf2.transaction.repository.TransactionRepository;
import com.workfusion.odf2.transaction.task.split.AbstractSplitTransactionTask;

@BotTask
@Requires({RepositoryModule.class, S3Module.class})
public class SplitInvoiceToPdfPageTask extends AbstractSplitTransactionTask {

    private final InvoiceRepository invoiceRepository;
    private final PdfPageRepository pdfPageRepository;

    private final S3Service s3Service;
    private final S3Bucket bucket;

    private final Logger logger;

    @Inject
    public SplitInvoiceToPdfPageTask(CurrentTransaction currentTransaction, TransactionRepository transactionRepository, TransactionBuilder transactionBuilder, TaskInput taskInput,
            InvoiceRepository invoiceRepository, PdfPageRepository pdfPageRepository, S3Service s3Service, Logger logger, Configuration configuration) {
        super(currentTransaction, transactionRepository, transactionBuilder, taskInput);
        this.invoiceRepository = invoiceRepository;
        this.pdfPageRepository = pdfPageRepository;
        this.s3Service = s3Service;
        this.bucket = s3Service.getBucket(configuration.getRequiredProperty("['example.attachments.bucket.name']"));
        this.logger = logger;
    }

    @Override
    public Collection<TransactionResult> run(CurrentTransaction currentTransaction) {
        return invoiceRepository.findByTransactionId(currentTransaction.getRequiredId()).stream()
                .flatMap(this::splitInvoice)
                .map(this::createChildTransaction)
                .collect(resultCollector());
    }

    public Stream<PdfPage> splitInvoice(Invoice entity) {
        List<PdfPage> pages = splitDocumentIntoPages(entity.getOriginalDocumentUrl());

        int fakePageIndex = new Random().nextInt(pages.size());

        PdfPage page = pages.get(fakePageIndex);
        page.setOriginalDocumentLink(page.getOriginalDocumentLink() + "_fake");

        return pages.stream();
    }

    private Transaction createChildTransaction(PdfPage pdfPage) {
        final Transaction transaction = createChildTransaction();
        pdfPage.setTransaction(transaction);
        pdfPageRepository.createOrUpdate(pdfPage);
        return transaction;
    }

    private List<PdfPage> splitDocumentIntoPages(String documentLink) {
        String fileName = documentLink.substring(documentLink.lastIndexOf("/") + 1, documentLink.lastIndexOf(".") - 1);
        List<PdfPage> pages = new ArrayList<>();

        try (PDDocument document = loadDocument(documentLink)) {
            List<PDDocument> pagedDocuments = splitDocument(document);

            for (int i = 0; i < pagedDocuments.size(); i++) {
                String s3Key = String.format("example-attachments/%s-%s.pdf", fileName, i + 1);
                PdfPage page = createPdfPage(s3Key, pagedDocuments.get(i));
                pages.add(page);
            }

            return pages;
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private PDDocument loadDocument(String docLink) throws IOException {
        byte[] docContent = s3Service.getObjectByUrl(docLink);
        try (InputStream inputStream = new ByteArrayInputStream(docContent)) {
            return PDDocument.load(inputStream);
        }
    }

    private PdfPage createPdfPage(String s3Key, PDDocument document) throws IOException {
        logger.info("Uploading '{}' document", s3Key);
        String s3Url = bucket.put(toByteArray(document), s3Key).getDirectUrl();

        PdfPage page = new PdfPage();
        page.setOriginalDocumentLink(s3Url);
        return page;
    }

    private static List<PDDocument> splitDocument(PDDocument document) throws IOException {
        Splitter splitting = new Splitter();
        splitting.setSplitAtPage(1);
        return splitting.split(document);
    }

    private static byte[] toByteArray(PDDocument document) throws IOException {
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            document.save(outputStream);
            document.close();
            return outputStream.toByteArray();
        }
    }

}
