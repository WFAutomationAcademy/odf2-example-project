package com.workfusion.odf2.example.task.processing;

import java.util.Collections;
import javax.inject.Inject;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;

import com.workfusion.odf2.compiler.BotTask;
import com.workfusion.odf2.core.cdi.Requires;
import com.workfusion.odf2.core.task.AdHocTask;
import com.workfusion.odf2.core.task.TaskInput;
import com.workfusion.odf2.core.task.output.TaskRunnerOutput;
import com.workfusion.odf2.example.model.Invoice;
import com.workfusion.odf2.example.module.RepositoryModule;
import com.workfusion.odf2.example.repository.InvoiceRepository;
import com.workfusion.odf2.transaction.CurrentTransaction;

@BotTask
@Requires(RepositoryModule.class)
public class SubmitInvoiceToOcrTask implements AdHocTask {

    private final CurrentTransaction transaction;
    private final InvoiceRepository invoiceRepository;
    private final Logger logger;

    private Invoice invoice;

    @Inject
    public SubmitInvoiceToOcrTask(CurrentTransaction transaction, InvoiceRepository invoiceRepository, Logger logger) {
        this.transaction = transaction;
        this.invoiceRepository = invoiceRepository;
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
    public TaskRunnerOutput run(TaskInput taskInput) {
        logger.info("Sending invoice '{}' to OCR Bridge", invoice.getUuid());

        return taskInput.asResult()
                .withColumn("original_document_url", invoice.getOriginalDocumentUrl());
    }

}
