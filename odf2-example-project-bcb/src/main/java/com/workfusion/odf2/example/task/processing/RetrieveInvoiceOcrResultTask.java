package com.workfusion.odf2.example.task.processing;

import java.util.UUID;
import javax.inject.Inject;

import org.slf4j.Logger;

import com.workfusion.odf2.compiler.BotTask;
import com.workfusion.odf2.core.cdi.Requires;
import com.workfusion.odf2.core.task.TaskInput;
import com.workfusion.odf2.core.webharvest.TaskVariable;
import com.workfusion.odf2.example.model.Invoice;
import com.workfusion.odf2.example.module.RepositoryModule;
import com.workfusion.odf2.example.repository.InvoiceRepository;
import com.workfusion.odf2.service.ControlTowerServicesModule;
import com.workfusion.odf2.service.ocr.OcrResult;
import com.workfusion.odf2.service.ocr.OcrSdk12;
import com.workfusion.odf2.service.ocr.OcrService;
import com.workfusion.odf2.transaction.TransactionResult;
import com.workfusion.odf2.transaction.task.ocr.ReceiveOcrResultTask;

@BotTask
@Requires({RepositoryModule.class, ControlTowerServicesModule.class})
public class RetrieveInvoiceOcrResultTask implements ReceiveOcrResultTask {

    private final OcrService ocrService;
    private final TaskInput taskInput;
    private final InvoiceRepository invoiceRepository;
    private final Logger logger;

    @Inject
    public RetrieveInvoiceOcrResultTask(@OcrSdk12 OcrService ocrService, TaskInput taskInput, InvoiceRepository invoiceRepository, Logger logger) {
        this.ocrService = ocrService;
        this.taskInput = taskInput;
        this.invoiceRepository = invoiceRepository;
        this.logger = logger;
    }

    @Override
    public boolean shouldRun() {
        return taskInput.getVariable(TaskVariable.OCR_TASK_ID).isPresent();
    }

    @Override
    public OcrService getOcrService() {
        return ocrService;
    }

    @Override
    public void doWithResult(OcrResult ocrResult, UUID transactionId, TransactionResult result) {
        Invoice invoice = invoiceRepository.findFirstByTransactionId(transactionId);
        invoice.setOcrResultUrl(ocrResult.getDocumentXmlLink());
        invoiceRepository.update(invoice);

        logger.info("OCR result received for invoice '{}'", invoice.getUuid());
    }

}
