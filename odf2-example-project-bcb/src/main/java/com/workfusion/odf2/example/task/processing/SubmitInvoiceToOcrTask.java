package com.workfusion.odf2.example.task.processing;

import java.util.Collections;
import java.util.UUID;
import javax.inject.Inject;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;

import com.workfusion.odf2.compiler.BotTask;
import com.workfusion.odf2.core.cdi.Requires;
import com.workfusion.odf2.example.model.Invoice;
import com.workfusion.odf2.example.module.RepositoryModule;
import com.workfusion.odf2.example.repository.InvoiceRepository;
import com.workfusion.odf2.service.ControlTowerServicesModule;
import com.workfusion.odf2.service.ocr.OcrConfiguration;
import com.workfusion.odf2.service.ocr.OcrInputDocument;
import com.workfusion.odf2.service.ocr.OcrSdk12;
import com.workfusion.odf2.service.ocr.OcrService;
import com.workfusion.odf2.service.ocr.OcrType;
import com.workfusion.odf2.service.s3.S3Service;
import com.workfusion.odf2.transaction.CurrentTransaction;
import com.workfusion.odf2.transaction.task.ocr.SubmitDocumentToOcrTask;

@BotTask
@Requires({RepositoryModule.class, ControlTowerServicesModule.class})
public class SubmitInvoiceToOcrTask implements SubmitDocumentToOcrTask {

    private final CurrentTransaction transaction;
    private final InvoiceRepository invoiceRepository;
    private final OcrService ocrService;
    private final Logger logger;

    private Invoice invoice;

    @Inject
    public SubmitInvoiceToOcrTask(CurrentTransaction transaction, InvoiceRepository invoiceRepository,
            @OcrSdk12 OcrService ocrService, Logger logger) {
        this.transaction = transaction;
        this.invoiceRepository = invoiceRepository;
        this.ocrService = ocrService;
        this.logger = logger;
    }

    @Override
    public void afterConstruction() {
        invoice = transaction.getId()
                .map(invoiceRepository::findByTransactionId)
                .orElse(Collections.emptyList())
                .stream().findFirst()
                .orElse(null);
    }

    @Override
    public boolean shouldRun() {
        return invoice != null
                && "PDF".equals(invoice.getType())
                && StringUtils.isNotEmpty(invoice.getOriginalDocumentUrl());
    }

    @Override
    public OcrInputDocument getDocument(S3Service s3Service, UUID transactionId) {
        logger.info("Sending invoice '{}' to OCR processing", invoice.getUuid());
        return () -> s3Service.getObjectByUrl(invoice.getOriginalDocumentUrl());
    }

    @Override
    public OcrConfiguration getOcrConfiguration() {
        return new OcrConfiguration(OcrType.TOD);
    }

    @Override
    public OcrService getOcrService() {
        return ocrService;
    }

}
