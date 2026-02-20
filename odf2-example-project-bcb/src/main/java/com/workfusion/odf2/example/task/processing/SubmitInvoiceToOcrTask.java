package com.workfusion.odf2.example.task.processing;

import java.util.Collections;
import javax.inject.Inject;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;

import com.workfusion.odf2.compiler.BotTask;
import com.workfusion.odf2.core.cdi.Requires;
import com.workfusion.odf2.core.task.output.SingleResult;
import com.workfusion.odf2.core.task.output.TaskRunnerOutput;
import com.workfusion.odf2.example.model.Invoice;
import com.workfusion.odf2.example.module.RepositoryModule;
import com.workfusion.odf2.example.repository.InvoiceRepository;
import com.workfusion.odf2.transaction.CurrentTransaction;
import com.workfusion.odf2.transaction.TransactionResult;
import com.workfusion.odf2.transaction.task.transactional.TransactionalTask;

@BotTask
@Requires(RepositoryModule.class)
public class SubmitInvoiceToOcrTask implements TransactionalTask {

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
    public void run(CurrentTransaction currentTransaction, TransactionResult result) {
        logger.info("Sending invoice '{}' to OCR Bridge", invoice.getUuid());

        result.withColumn("is_pdf_document", "true");
    }

    @Override
    public void insteadOfRunning(TaskRunnerOutput result) {
        result.setColumn("is_pdf_document", "false");
    }

    @Override
    public void processSkippedBotTaskResult(SingleResult result) {
        result.withColumn("is_pdf_document", "true");
    }

}
