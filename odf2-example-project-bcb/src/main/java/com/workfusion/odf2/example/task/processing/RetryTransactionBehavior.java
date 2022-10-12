package com.workfusion.odf2.example.task.processing;

import com.workfusion.odf2.example.model.Attachment;
import com.workfusion.odf2.example.model.Email;
import com.workfusion.odf2.example.repository.AttachmentRepository;
import com.workfusion.odf2.example.repository.EmailRepository;

import java.util.List;
import java.util.Objects;
import javax.inject.Inject;

import org.slf4j.Logger;

import com.workfusion.odf2.core.task.output.OdfOutput;
import com.workfusion.odf2.core.task.output.SingleResult;
import com.workfusion.odf2.multiprocess.model.TransactionStatus;
import com.workfusion.odf2.transaction.CurrentTransaction;
import com.workfusion.odf2.transaction.model.Transaction;

public class RetryTransactionBehavior {

    private final Logger logger;

    private final CurrentTransaction currentTransaction;

    private final EmailRepository emailRepository;
    private final AttachmentRepository attachmentRepository;

    @Inject
    public RetryTransactionBehavior(Logger logger, CurrentTransaction currentTransaction, EmailRepository emailRepository, AttachmentRepository attachmentRepository) {
        this.logger = logger;
        this.currentTransaction = currentTransaction;
        this.emailRepository = emailRepository;
        this.attachmentRepository = attachmentRepository;
    }

    public OdfOutput retryTransaction() {
        final Transaction transaction = currentTransaction.get();

        logger.warn("Transaction {} will be retried (this is intended behavior for demonstration purposes)", transaction.getUuid());

        final List<Email> emails = emailRepository.findAll(transaction.getUuid());

        for (Email email : emails) {
            for (Attachment attachment : email.getAttachments()) {
                if (Objects.equals(attachment.getType(), "RETRY")) {
                    attachment.setType("LOG-AND-FORGET"); // so we will not retry it infinitely; also to demonstrate another behavior
                    attachmentRepository.update(attachment);
                }
            }
        }

        transaction.setStatus(TransactionStatus.INTAKE_COMPLETED.name()); // so it will be picked by Processing monitor again
        currentTransaction.updateIfLoaded();

        return new SingleResult(); // empty result, because we do not want to propagate transaction further into BP
    }

}
