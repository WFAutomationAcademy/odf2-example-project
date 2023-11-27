package com.workfusion.odf2.example.task.processing;

import java.util.Collection;
import java.util.UUID;
import javax.inject.Inject;

import org.slf4j.Logger;

import com.workfusion.odf2.compiler.BotTask;
import com.workfusion.odf2.core.cdi.Requires;
import com.workfusion.odf2.core.task.output.TaskRunnerOutput;
import com.workfusion.odf2.example.model.Invoice;
import com.workfusion.odf2.example.module.RepositoryModule;
import com.workfusion.odf2.example.repository.InvoiceRepository;
import com.workfusion.odf2.multiprocess.task.processing.BusinessEntityProcessorTask;

@BotTask
@Requires(RepositoryModule.class)
public class InvoiceProcessorTask implements BusinessEntityProcessorTask<Invoice> {

    private final InvoiceRepository invoiceRepository;
    private final Logger logger;

    @Inject
    public InvoiceProcessorTask(InvoiceRepository invoiceRepository, Logger logger) {
        this.invoiceRepository = invoiceRepository;
        this.logger = logger;
    }

    @Override
    public Collection<Invoice> findBusinessEntities(UUID transactionId) {
        return invoiceRepository.findByTransactionId(transactionId);
    }

    @Override
    public void processBusinessEntities(Collection<Invoice> invoices) {
        for (Invoice invoice : invoices) {
            logger.info("Processing invoice '{}'", invoice.getUuid());

            invoice.setStatus("PROCESSED");
        }
    }

    @Override
    public void saveBusinessEntities(Collection<Invoice> invoices) {
        invoices.forEach(invoiceRepository::update);
    }

    @Override
    public void afterRunning(TaskRunnerOutput result) {
        result.removeColumn("is_pdf_document");
    }

    @Override
    public void insteadOfRunning(TaskRunnerOutput result) {
        result.removeColumn("is_pdf_document");
    }

}
