package com.workfusion.odf2.example.task.processing;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.workfusion.odf.test.junit.WorkerJUnitConfig;
import com.workfusion.odf.test.launch.InputData;
import com.workfusion.odf.test.launch.OutputData;
import com.workfusion.odf2.core.webharvest.TaskVariable;
import com.workfusion.odf2.example.model.Attachment;
import com.workfusion.odf2.example.model.Email;
import com.workfusion.odf2.example.model.Invoice;
import com.workfusion.odf2.example.repository.AttachmentRepository;
import com.workfusion.odf2.example.repository.EmailRepository;
import com.workfusion.odf2.example.repository.InvoiceRepository;
import com.workfusion.odf2.junit.BotTaskFactory;
import com.workfusion.odf2.junit.OrmSupport;
import com.workfusion.odf2.multiprocess.model.ErrorEntity;
import com.workfusion.odf2.multiprocess.model.TransactionErrorStatus;
import com.workfusion.odf2.multiprocess.model.TransactionStatus;
import com.workfusion.odf2.multiprocess.repository.ErrorRepository;
import com.workfusion.odf2.transaction.model.JoinEntity;
import com.workfusion.odf2.transaction.model.Transaction;
import com.workfusion.odf2.transaction.repository.TransactionRepository;

import static org.assertj.core.api.Assertions.assertThat;

import static com.workfusion.odf2.multiprocess.model.TransactionStatus.PROCESSING_IN_PROGRESS;

@WorkerJUnitConfig
class SplitByEmailAttachmentsTaskTest {

    private TransactionRepository transactionRepository;
    private EmailRepository emailRepository;
    private AttachmentRepository attachmentRepository;
    private InvoiceRepository invoiceRepository;
    private ErrorRepository errorRepository;

    @BeforeEach
    void setUp(OrmSupport ormSupport) throws SQLException {
        transactionRepository = ormSupport.getTransactionRepository();
        emailRepository = new EmailRepository(ormSupport.getConnectionSource());
        attachmentRepository = new AttachmentRepository(ormSupport.getConnectionSource());
        invoiceRepository = new InvoiceRepository(ormSupport.getConnectionSource());
        errorRepository = new ErrorRepository(ormSupport.getConnectionSource());

        ormSupport.createTables(Transaction.class, Email.class, Attachment.class, Invoice.class, ErrorEntity.class, JoinEntity.class);
    }

    @Test
    @DisplayName("should convert email to invoice")
    void shouldConvertEmailToInvoice(BotTaskFactory botTaskFactory) {
        // given
        Transaction transaction = transactionRepository.startNewTransaction(PROCESSING_IN_PROGRESS.toString());
        Email email = createEmail(transaction);
        createAttachment(email, "HTML");
        createAttachment(email, "PDF");

        // when
        OutputData outputData = botTaskFactory.fromClass(SplitByEmailAttachmentsTask.class)
                .withInputData(createInputData(transaction))
                .buildAndRun();

        // then
        assertThat(outputData.getRecordCount()).isEqualTo(2);
        assertThat(outputData.getRecord(0)).containsEntry(TaskVariable.PARENT_TRANSACTION_ID.toString(), transaction.getUuid().toString());
        assertThat(outputData.getRecord(1)).containsEntry(TaskVariable.PARENT_TRANSACTION_ID.toString(), transaction.getUuid().toString());

        assertThat(transactionRepository.count()).isEqualTo(3);

        assertThat(invoiceRepository.findAll())
                .hasSize(2)
                .extracting(Invoice::getOriginalDocumentUrl)
                .containsOnly("http://original.document.link");
    }

    @Test
    @DisplayName("should create error entity if attachment contains fail flag")
    void shouldCreateErrorEntityIfAttachmentContainsFailFlag(BotTaskFactory botTaskFactory) {
        // given
        Transaction transaction = transactionRepository.startNewTransaction(PROCESSING_IN_PROGRESS.toString());
        Email email = createEmail(transaction);
        createAttachment(email, "FAIL");

        // when
        Map<String, String> actualRecord = botTaskFactory.fromClass(SplitByEmailAttachmentsTask.class)
                .withInputData(createInputData(transaction))
                .buildAndRun()
                .getFirstRecord();

        // then
        assertThat(actualRecord).containsEntry(TaskVariable.ERROR_STATUS.toString(), TransactionErrorStatus.HAS_ERROR.toString());

        assertThat(errorRepository.findByTransactionId(transaction.getUuid())).hasSize(1);
    }

    @Test
    @DisplayName("should retry transaction if attachment contains corresponding flag")
    void shouldRetryTransactionIfAttachmentContainsCorrespondingFlag(BotTaskFactory botTaskFactory) {
        // given
        Transaction transaction = transactionRepository.startNewTransaction(PROCESSING_IN_PROGRESS.toString());
        Email email = createEmail(transaction);
        createAttachment(email, "RETRY");

        // when
        Map<String, String> actualRecord = botTaskFactory.fromClass(SplitByEmailAttachmentsTask.class)
                .withInputData(createInputData(transaction))
                .buildAndRun()
                .getFirstRecord();

        // then
        assertThat(actualRecord).isEmpty();

        Optional<Transaction> changedTransaction = transactionRepository.findById(transaction.getUuid());
        assertThat(changedTransaction).isPresent();
        assertThat(changedTransaction.get().getStatus()).isEqualTo(TransactionStatus.INTAKE_COMPLETED.name());


        assertThat(attachmentRepository.findAll())
                .allSatisfy(attachment -> assertThat(attachment.getType()).isNotEqualTo("RETRY"))
                .anySatisfy(attachment -> assertThat(attachment.getType()).isEqualTo("LOG-AND-FORGET"));
    }

    @Test
    @DisplayName("should abort transaction if attachment contains corresponding flag")
    void shouldAbortTransactionIfAttachmentContainsCorrespondingFlag(BotTaskFactory botTaskFactory) {
        // given
        Transaction transaction = transactionRepository.startNewTransaction(PROCESSING_IN_PROGRESS.toString());
        Email email = createEmail(transaction);
        createAttachment(email, "LOG-AND-FORGET");

        // when
        Map<String, String> actualRecord = botTaskFactory.fromClass(SplitByEmailAttachmentsTask.class)
                .withInputData(createInputData(transaction))
                .buildAndRun()
                .getFirstRecord();

        // then
        assertThat(actualRecord)
                .containsEntry(TaskVariable.TRANSACTION_STATUS.toString(), TransactionStatus.ABORTED.name())
                .containsEntry(TaskVariable.ERROR_STATUS.toString(), TransactionStatus.ABORTED.name());

        assertThat(errorRepository.findByTransactionId(transaction.getUuid())).hasSize(0);
        assertThat(emailRepository.findByTransactionId(transaction.getUuid())).hasSize(0);
        assertThat(attachmentRepository.findAll()).hasSize(0);
    }

    private Email createEmail(Transaction transaction) {
        Email email = new Email();
        email.setTransaction(transaction);
        return emailRepository.create(email);
    }

    private Attachment createAttachment(Email email, String type) {
        Attachment attachment = new Attachment();
        attachment.setEmail(email);
        attachment.setType(type);
        attachment.setOriginalDocumentLink("http://original.document.link");
        return attachmentRepository.create(attachment);
    }

    private static InputData createInputData(Transaction transaction) {
        return InputData.of(
                Arrays.asList(TaskVariable.TRANSACTION_ID.toString(), TaskVariable.TRANSACTION_STATUS.toString()),
                Arrays.asList(transaction.getUuid().toString(), transaction.getStatus()));
    }

}
