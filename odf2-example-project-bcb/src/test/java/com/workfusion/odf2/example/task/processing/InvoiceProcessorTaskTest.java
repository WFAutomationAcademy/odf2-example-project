package com.workfusion.odf2.example.task.processing;

import java.time.Duration;
import java.util.Arrays;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.workfusion.odf.test.junit.WorkerJUnitConfig;
import com.workfusion.odf.test.launch.InputData;
import com.workfusion.odf2.core.webharvest.TaskVariable;
import com.workfusion.odf2.example.model.Invoice;
import com.workfusion.odf2.example.model.Product;
import com.workfusion.odf2.example.repository.InvoiceRepository;
import com.workfusion.odf2.example.repository.ProductRepository;
import com.workfusion.odf2.junit.BotTaskFactory;
import com.workfusion.odf2.junit.OrmSupport;
import com.workfusion.odf2.multiprocess.model.BusinessEntity;
import com.workfusion.odf2.transaction.model.Transaction;
import com.workfusion.odf2.transaction.repository.TransactionRepository;

import static org.assertj.core.api.Assertions.assertThat;

import static com.workfusion.odf2.multiprocess.model.TransactionStatus.PROCESSING_IN_PROGRESS;

@WorkerJUnitConfig
class InvoiceProcessorTaskTest {

    private TransactionRepository transactionRepository;
    private InvoiceRepository invoiceRepository;
    private ProductRepository productRepository;

    @BeforeEach
    void setUp(OrmSupport ormSupport) throws Exception {
        transactionRepository = ormSupport.getTransactionRepository();
        invoiceRepository = new InvoiceRepository(ormSupport.getConnectionSource());
        productRepository = new ProductRepository(ormSupport.getConnectionSource());

        ormSupport.createTables(Transaction.class, Invoice.class, Product.class);
    }

    @Test
    @DisplayName("should process invoice")
    void shouldProcessInvoice(BotTaskFactory botTaskFactory) {
        // given
        Transaction transaction = transactionRepository.startNewTransaction(PROCESSING_IN_PROGRESS.toString());
        Invoice invoice = createInvoice(transaction);

        // when
        botTaskFactory.fromClass(InvoiceProcessorTask.class)
                .withInputData(createInputData(transaction))
                .withTimeout(Duration.ofSeconds(30))
                .buildAndRun();

        // then
        assertThat(invoiceRepository.findById(invoice.getUuid()))
                .map(BusinessEntity::getStatus)
                .contains("PROCESSED");

        assertThat(productRepository.count()).isZero();
    }

    private Invoice createInvoice(Transaction transaction) {
        Invoice invoice = new Invoice();
        invoice.setTransaction(transaction);
        return invoiceRepository.create(invoice);
    }

    private static InputData createInputData(Transaction transaction) {
        return InputData.of(
                Arrays.asList(TaskVariable.TRANSACTION_ID.toString(), TaskVariable.TRANSACTION_STATUS.toString()),
                Arrays.asList(transaction.getUuid().toString(), transaction.getStatus()));
    }

}
