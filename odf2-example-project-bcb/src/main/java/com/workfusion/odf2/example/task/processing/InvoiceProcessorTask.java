package com.workfusion.odf2.example.task.processing;

import java.util.Collection;
import java.util.UUID;
import javax.inject.Inject;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;

import com.workfusion.odf2.compiler.BotTask;
import com.workfusion.odf2.core.cdi.Requires;
import com.workfusion.odf2.example.model.Invoice;
import com.workfusion.odf2.example.model.Product;
import com.workfusion.odf2.example.module.RepositoryModule;
import com.workfusion.odf2.example.repository.InvoiceRepository;
import com.workfusion.odf2.example.repository.ProductRepository;
import com.workfusion.odf2.multiprocess.task.processing.BusinessEntityProcessorTask;

@BotTask
@Requires(RepositoryModule.class)
public class InvoiceProcessorTask implements BusinessEntityProcessorTask<Invoice> {

    private final InvoiceRepository invoiceRepository;
    private final ProductRepository productRepository;
    private final Logger logger;

    @Inject
    public InvoiceProcessorTask(InvoiceRepository invoiceRepository, ProductRepository productRepository, Logger logger) {
        this.invoiceRepository = invoiceRepository;
        this.productRepository = productRepository;
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

            if (StringUtils.isNotEmpty(invoice.getOcrResultUrl())) {
                Product product = createNewProduct(invoice);
                logger.info("Created a new product '{}' with name '{}'", product.getUuid(), product.getName());
            }

            invoice.setStatus("PROCESSED");
        }
    }

    @Override
    public void saveBusinessEntities(Collection<Invoice> invoices) {
        invoices.forEach(invoiceRepository::update);
    }

    private Product createNewProduct(Invoice invoice) {
        ProductXpathReader reader = new ProductXpathReader();
        Product product = reader.fromUrl(invoice.getOcrResultUrl());
        product.setInvoice(invoice);
        return productRepository.create(product);
    }

}
