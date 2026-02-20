package com.workfusion.odf2.example.task.submission;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.workfusion.odf.test.junit.IacDeveloperJUnitConfig;
import com.workfusion.odf.test.launch.InputData;
import com.workfusion.odf2.core.webharvest.TaskVariable;
import com.workfusion.odf2.example.model.Invoice;
import com.workfusion.odf2.example.model.Product;
import com.workfusion.odf2.example.repository.InvoiceRepository;
import com.workfusion.odf2.example.repository.ProductRepository;
import com.workfusion.odf2.junit.BotTaskFactory;
import com.workfusion.odf2.junit.OrmSupport;
import com.workfusion.odf2.multiprocess.model.TransactionStatus;
import com.workfusion.odf2.transaction.model.Transaction;

import static org.assertj.core.api.Assertions.assertThat;

@Disabled("Require IA Cloud Developer")
@IacDeveloperJUnitConfig
class InvoiceSubmissionTaskTest {

    private InvoiceRepository invoiceRepository;
    private ProductRepository productRepository;

    private Transaction transaction;

    @BeforeEach
    void setUp(OrmSupport ormSupport) throws Exception {
        ormSupport.createTables(Transaction.class, Invoice.class, Product.class);

        invoiceRepository = new InvoiceRepository(ormSupport.getConnectionSource());
        productRepository = new ProductRepository(ormSupport.getConnectionSource());

        transaction = ormSupport.getTransactionRepository().startNewTransaction(TransactionStatus.SUBMISSION_IN_PROGRESS.toString());
    }

    @Test
    @DisplayName("should not init robot when no invoices to work with")
    void shouldNotInitRobotWhenNoInvoicesToWorkWith(BotTaskFactory botTaskFactory) {
        // given
        Invoice invoice = createInvoice(0);

        // when
        botTaskFactory.fromClass(InvoiceSubmissionTask.class)
                .withInputData(createInputData(transaction))
                .buildAndRun()
                .getFirstRecord();

        // then
        Invoice actualInvoice = invoiceRepository.findRequiredById(invoice.getUuid());

        assertThat(actualInvoice.getDate()).isNull();
        assertThat(actualInvoice.getPayment()).isNull();
        assertThat(actualInvoice.getStatus()).isNull();
        assertThat(actualInvoice.getExternalLink()).isNull();
    }

    @Test
    @DisplayName("should submit multiple invoices")
    void shouldSubmitMultipleInvoices(BotTaskFactory botTaskFactory) {
        // given
        Invoice firstInvoice = createInvoice(0);
        Invoice secondInvoice = createInvoice(1);
        Invoice thirdInvoice = createInvoice(3);

        // when
        botTaskFactory.fromClass(InvoiceSubmissionTask.class)
                .withInputData(createInputData(transaction))
                .withSecureEntries(cfg -> cfg.withEntry("invoice.plane.credentials", "wf-robot@mail.com", "BotsRock4ever!"))
                .buildAndRun();

        // then
        Invoice actualFirstInvoice = invoiceRepository.findRequiredById(firstInvoice.getUuid());
        assertThat(actualFirstInvoice.getExternalLink()).isNull();

        List<Invoice> otherInvoices = invoiceRepository.findByIds(secondInvoice.getUuid(), thirdInvoice.getUuid());
        assertThat(otherInvoices).hasSize(2);

        assertThat(otherInvoices).extracting(Invoice::getDate).doesNotContainNull();
        assertThat(otherInvoices).extracting(Invoice::getPayment).doesNotContainNull();
        assertThat(otherInvoices).extracting(Invoice::getStatus).doesNotContainNull();
        assertThat(otherInvoices).extracting(Invoice::getExternalLink).doesNotContainNull();
    }

    private Invoice createInvoice(int numberOfProducts) {
        Invoice invoiceEntity = new Invoice();
        invoiceEntity.setTransaction(transaction);
        Invoice invoice = invoiceRepository.create(invoiceEntity);

        List<Product> products = IntStream.range(0, numberOfProducts)
                .mapToObj(i -> createProduct(invoice, i))
                .collect(Collectors.toList());

        invoice.setProducts(products);
        return invoice;
    }

    private Product createProduct(Invoice invoice, int index) {
        Product product = new Product();
        product.setInvoice(invoice);
        product.setName("name" + index);
        product.setDescription("description" + index);
        product.setPrice("100");
        return productRepository.create(product);
    }

    private static InputData createInputData(Transaction transaction) {
        return InputData.of(
                Arrays.asList(TaskVariable.TRANSACTION_ID.toString(), TaskVariable.TRANSACTION_STATUS.toString()),
                Arrays.asList(transaction.getUuid().toString(), transaction.getStatus()));
    }

}
