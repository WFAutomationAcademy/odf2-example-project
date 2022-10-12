package com.workfusion.odf2.example.task.processing;

import com.workfusion.odf2.example.model.Email;
import com.workfusion.odf2.example.repository.AttachmentRepository;
import com.workfusion.odf2.example.repository.EmailRepository;

import java.util.List;
import javax.inject.Inject;

import org.slf4j.Logger;

import com.workfusion.odf2.core.task.output.OdfOutput;
import com.workfusion.odf2.multiprocess.model.TransactionStatus;
import com.workfusion.odf2.transaction.CurrentTransaction;
import com.workfusion.odf2.transaction.model.Transaction;

public class LogAndForgetBehavior {

    private final Logger logger;

    private final CurrentTransaction currentTransaction;

    private final EmailRepository emailRepository;
    private final AttachmentRepository attachmentRepository;

    @Inject
    public LogAndForgetBehavior(Logger logger, CurrentTransaction currentTransaction, EmailRepository emailRepository, AttachmentRepository attachmentRepository) {
        this.logger = logger;
        this.currentTransaction = currentTransaction;
        this.emailRepository = emailRepository;
        this.attachmentRepository = attachmentRepository;
    }

    public OdfOutput abortTransaction() {
        final Transaction transaction = currentTransaction.get();

        logger.warn("Transaction {} was aborted (this is intended behavior for demonstration purposes)", transaction.getUuid());

        final List<Email> emails = emailRepository.findAll(transaction.getUuid());

        for (Email email : emails) {
            attachmentRepository.deleteAll(email.getAttachments());
        }

        emailRepository.deleteAll(emails);

        transaction.setStatus(TransactionStatus.ABORTED.name());
        transaction.setErrorStatus(TransactionStatus.ABORTED.name());
        currentTransaction.updateIfLoaded();

        return currentTransaction.toTaskOutput();
    }

}
