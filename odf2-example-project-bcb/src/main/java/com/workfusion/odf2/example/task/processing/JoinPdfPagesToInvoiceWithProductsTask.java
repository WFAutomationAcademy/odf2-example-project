package com.workfusion.odf2.example.task.processing;

import java.util.Collection;
import java.util.List;

import javax.inject.Inject;

import org.slf4j.Logger;

import com.workfusion.odf2.compiler.BotTask;
import com.workfusion.odf2.core.OdfFrameworkException;
import com.workfusion.odf2.core.cdi.Requires;
import com.workfusion.odf2.core.task.TaskInput;
import com.workfusion.odf2.core.task.output.SingleResult;
import com.workfusion.odf2.core.time.OdfTime;
import com.workfusion.odf2.example.model.Invoice;
import com.workfusion.odf2.example.model.PdfPage;
import com.workfusion.odf2.example.model.Product;
import com.workfusion.odf2.example.module.RepositoryModule;
import com.workfusion.odf2.example.repository.InvoiceRepository;
import com.workfusion.odf2.example.repository.PdfPageRepository;
import com.workfusion.odf2.example.repository.ProductRepository;
import com.workfusion.odf2.service.ControlTowerServicesModule;
import com.workfusion.odf2.service.pool.PoolObjectFactory;
import com.workfusion.odf2.transaction.CurrentTransaction;
import com.workfusion.odf2.transaction.model.Transaction;
import com.workfusion.odf2.transaction.repository.TransactionRepository;
import com.workfusion.odf2.transaction.task.join.AbstractJoinByParentTransactionTask;

@BotTask
@Requires({RepositoryModule.class, ControlTowerServicesModule.class})
public class JoinPdfPagesToInvoiceWithProductsTask extends AbstractJoinByParentTransactionTask {

    private final PdfPageRepository pdfPageRepository;
    private final InvoiceRepository invoiceRepository;
    private final ProductRepository productRepository;
    private final Logger logger;

    @Inject
    protected JoinPdfPagesToInvoiceWithProductsTask(TaskInput taskInput, CurrentTransaction currentTransaction, TransactionRepository transactionRepository,
            PoolObjectFactory poolObjectFactory, OdfTime odfTime, PdfPageRepository pdfPageRepository, InvoiceRepository invoiceRepository, ProductRepository productRepository, Logger logger) {
        super(taskInput, currentTransaction, transactionRepository, poolObjectFactory, odfTime);
        this.pdfPageRepository = pdfPageRepository;
        this.invoiceRepository = invoiceRepository;
        this.productRepository = productRepository;
        this.logger = logger;
    }

    @Override
    protected SingleResult run(Collection<Transaction> childTransactions, Transaction parentTransaction) {
        final Invoice invoice = invoiceRepository.findByTransactionId(currentTransaction.getRequiredParentId()).stream()
                .findFirst()
                .orElseThrow(() -> new OdfFrameworkException("Invoice not found."));

        childTransactions.stream().flatMap(transaction -> pdfPageRepository.findByTransactionId(transaction.getUuid()).stream())
                .forEach(pdfPage -> createNewProductsFromPdfPage(pdfPage, invoice));

        invoice.setTransaction(parentTransaction);
        invoiceRepository.update(invoice);

        return new SingleResult(taskInput).withColumn("original_document_url", invoice.getOriginalDocumentUrl());
    }

    private void createNewProductsFromPdfPage(PdfPage pdfPage, Invoice invoice) {
        ProductXpathReader reader = new ProductXpathReader();
        List<Product> products = reader.fromUrl(pdfPage.getOcrResultUrl());
        products.forEach(product -> {
            product.setInvoice(invoice);
            Product productFromRepository = productRepository.create(product);
            logger.info("Created a new product '{}' with name '{}'", productFromRepository.getUuid(), productFromRepository.getName());
        });
    }

}
