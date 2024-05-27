package com.workfusion.odf2.example.task.submission;

import java.util.Collection;
import java.util.Random;
import java.util.UUID;
import javax.inject.Inject;

import org.slf4j.Logger;

import com.workfusion.odf2.compiler.BotTask;
import com.workfusion.odf2.core.cdi.Requires;
import com.workfusion.odf2.example.model.Invoice;
import com.workfusion.odf2.example.module.RepositoryModule;
import com.workfusion.odf2.example.repository.InvoiceRepository;
import com.workfusion.odf2.multiprocess.task.processing.BusinessEntityProcessorTask;

@BotTask
@Requires(RepositoryModule.class)
public class InvoiceSubmissionTask implements BusinessEntityProcessorTask<Invoice> {

    private final InvoiceRepository invoiceRepository;
    private final Logger logger;

    @Inject
    public InvoiceSubmissionTask(InvoiceRepository invoiceRepository, Logger logger) {
        this.invoiceRepository = invoiceRepository;
        this.logger = logger;
    }

    @Override
    public Collection<Invoice> findBusinessEntities(UUID transactionId) {
        return invoiceRepository.findAll(transactionId);
    }

    @Override
    public void processBusinessEntities(Collection<Invoice> invoices) {
        invoices.forEach(this::submitToDataMart);
    }

    @Override
    public void saveBusinessEntities(Collection<Invoice> businessEntities) {
        // do nothing
    }

    private void submitToDataMart(Invoice invoice) {
        try {
            logger.info("Submitting invoice '{}' to Data mart", invoice.getUuid());
            Thread.sleep(new Random().nextInt(100));
        } catch (InterruptedException ignored) {
            Thread.currentThread().interrupt();
        }
    }

}
