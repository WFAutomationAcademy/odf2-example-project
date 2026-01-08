package com.workfusion.odf2.example.task.submission;

import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import javax.inject.Inject;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;

import com.workfusion.bot.service.SecureEntryDTO;
import com.workfusion.odf2.compiler.BotTask;
import com.workfusion.odf2.core.cdi.Requires;
import com.workfusion.odf2.core.webharvest.rpa.RpaRunner;
import com.workfusion.odf2.example.model.Invoice;
import com.workfusion.odf2.example.module.RepositoryModule;
import com.workfusion.odf2.example.module.RpaModule;
import com.workfusion.odf2.example.repository.InvoiceRepository;
import com.workfusion.odf2.example.rpa.InvoicePlaneRobot;
import com.workfusion.odf2.multiprocess.task.processing.BusinessEntityProcessorTask;
import com.workfusion.odf2.service.ControlTowerServicesModule;
import com.workfusion.odf2.service.vault.SecretsVaultService;

@BotTask(requireRpa = true)
@Requires({ControlTowerServicesModule.class, RepositoryModule.class, RpaModule.class})
public class InvoiceSubmissionTask implements BusinessEntityProcessorTask<Invoice> {

    private final InvoiceRepository invoiceRepository;
    private final SecretsVaultService secretsVault;
    private final RpaRunner rpaRunner;
    private final Logger logger;

    @Inject
    public InvoiceSubmissionTask(InvoiceRepository invoiceRepository,
            SecretsVaultService secretsVault, RpaRunner rpaRunner, Logger logger) {
        this.invoiceRepository = invoiceRepository;
        this.secretsVault = secretsVault;
        this.rpaRunner = rpaRunner;
        this.logger = logger;
    }

    @Override
    public Collection<Invoice> findBusinessEntities(UUID transactionId) {
        return invoiceRepository.findByTransactionId(transactionId).stream()
                .filter(Invoice::hasProducts)
                .collect(Collectors.toList());
    }

    @Override
    public void processBusinessEntities(Collection<Invoice> invoices) {
        if (invoices.isEmpty()) {
            return;
        }

        SecureEntryDTO credentials = secretsVault.getEntry("invoice.plane.credentials");

        rpaRunner.execute(driver -> {
            InvoicePlaneRobot robot = new InvoicePlaneRobot(driver, credentials);
            for (Invoice invoice : invoices) {
                robot.processInvoice(invoice);
                logger.info("Invoice '{}' successfully processed by InvoicePlaneRobot", invoice.getUuid());
            }
        });
    }

    @Override
    public void saveBusinessEntities(Collection<Invoice> invoices) {
        List<Invoice> invoicesToUpdate = invoices.stream()
                .filter(invoice -> StringUtils.isNotEmpty(invoice.getExternalLink()))
                .collect(Collectors.toList());

        invoicesToUpdate.forEach(invoiceRepository::update);
    }

}
