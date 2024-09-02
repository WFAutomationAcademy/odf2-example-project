package com.workfusion.odf2.example.task.processing;

import java.time.Instant;
import java.util.Date;
import java.util.Objects;
import java.util.stream.Stream;
import javax.inject.Inject;

import com.workfusion.odf2.compiler.BotTask;
import com.workfusion.odf2.core.cdi.Requires;
import com.workfusion.odf2.core.errorhandling.ErrorHandlingLogic;
import com.workfusion.odf2.core.task.output.OdfOutput;
import com.workfusion.odf2.example.model.Attachment;
import com.workfusion.odf2.example.model.Email;
import com.workfusion.odf2.example.model.Invoice;
import com.workfusion.odf2.example.module.RepositoryModule;
import com.workfusion.odf2.example.repository.EmailRepository;
import com.workfusion.odf2.example.repository.InvoiceRepository;
import com.workfusion.odf2.transaction.repository.TransactionalEntityRepository;
import com.workfusion.odf2.transaction.task.split.SplitTransactionTask;

@BotTask
@Requires(RepositoryModule.class)
public class SplitByEmailAttachmentsTask implements SplitTransactionTask<Email, Invoice> {

    private final EmailRepository emailRepository;
    private final InvoiceRepository invoiceRepository;

    private final LogAndForgetBehavior logAndForgetBehavior;
    private final RetryTransactionBehavior retryTransactionBehavior;

    @Inject
    public SplitByEmailAttachmentsTask(EmailRepository emailRepository, InvoiceRepository invoiceRepository,
            LogAndForgetBehavior logAndForgetBehavior, RetryTransactionBehavior retryTransactionBehavior) {
        this.emailRepository = emailRepository;
        this.invoiceRepository = invoiceRepository;
        this.logAndForgetBehavior = logAndForgetBehavior;
        this.retryTransactionBehavior = retryTransactionBehavior;
    }

    @Override
    public TransactionalEntityRepository<Email> getInputEntityRepository() {
        return emailRepository;
    }

    @Override
    public TransactionalEntityRepository<Invoice> getOutputEntityRepository() {
        return invoiceRepository;
    }

    @Override
    public Stream<Invoice> splitEntity(Email email) {
        return email.getAttachments().stream().map(this::convertToInvoice);
    }

    private Invoice convertToInvoice(Attachment attachment) {
        validateAttachment(attachment);

        Invoice invoice = new Invoice();
        invoice.setAmount(attachment.getSize());
        invoice.setDate(Date.from(Instant.now()));
        invoice.setType(attachment.getType());
        invoice.setOriginalDocumentUrl(attachment.getOriginalDocumentLink());
        return invoice;
    }

    private void validateAttachment(Attachment attachment) {
        if (Objects.equals("FAIL", attachment.getType())) {
            throw new IllegalStateException(String.format(
                    "Email '%s' contains FAIL attachment (this is intended behavior for demonstration purposes)",
                    attachment.getEmail().getUuid()));

        } else if (Objects.equals("LOG-AND-FORGET", attachment.getType())) {
            throw new IllegalStateException(
                    String.format("Email '%s' contains LOG-AND-FORGET attachment", attachment.getEmail().getUuid()));

        } else if (attachment.getType() != null && attachment.getType().startsWith("RETRY")) {
            throw new IllegalStateException(String.format("Email '%s' contains RETRY attachment", attachment.getEmail().getUuid()));
        }
    }

    @Override
    public OdfOutput handleException(ErrorHandlingLogic logic, Exception e) {
        if (e instanceof IllegalStateException && e.getMessage().contains("LOG-AND-FORGET")) {
            return logAndForgetBehavior.abortTransaction();
        }

        if (e instanceof IllegalStateException && e.getMessage().contains("RETRY")) {
            return retryTransactionBehavior.retryTransaction();
        }

        return SplitTransactionTask.super.handleException(logic, e);
    }

}
