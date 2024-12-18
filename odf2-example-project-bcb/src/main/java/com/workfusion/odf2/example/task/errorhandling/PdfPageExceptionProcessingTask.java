package com.workfusion.odf2.example.task.errorhandling;

import javax.inject.Inject;

import org.slf4j.Logger;

import com.workfusion.odf2.compiler.BotTask;
import com.workfusion.odf2.core.cdi.Requires;
import com.workfusion.odf2.example.model.PdfPage;
import com.workfusion.odf2.example.module.RepositoryModule;
import com.workfusion.odf2.example.repository.PdfPageRepository;
import com.workfusion.odf2.multiprocess.model.ErrorEntity;
import com.workfusion.odf2.multiprocess.model.TransactionStatus;
import com.workfusion.odf2.multiprocess.task.errorhandling.ErrorHandlingTask;
import com.workfusion.odf2.transaction.model.Transaction;

@BotTask
@Requires(RepositoryModule.class)
public class PdfPageExceptionProcessingTask implements ErrorHandlingTask {

    private final PdfPageRepository pdfPageRepository;
    private final Logger logger;

    @Inject
    public PdfPageExceptionProcessingTask(PdfPageRepository pdfPageRepository, Logger logger) {
        this.pdfPageRepository = pdfPageRepository;
        this.logger = logger;
    }

    @Override
    public boolean isNeedManualErrorHandling(ErrorEntity errorEntity) {
        return !errorEntity.getShortDescription().equalsIgnoreCase("Page has incorrect link.");
    }

    @Override
    public void automationHandling(Transaction transaction) {
        logger.info("Handling error in transaction '{}'", transaction.getUuid());

        PdfPage pdfPage = pdfPageRepository.findFirstByTransactionId(transaction.getUuid());
        pdfPage.setOriginalDocumentLink(pdfPage.getOriginalDocumentLink().replace("_fake", ""));
        pdfPageRepository.update(pdfPage);
    }

    @Override
    public TransactionStatus updateTransactionStatus() {
        return TransactionStatus.INTAKE_COMPLETED;
    }

    @Override
    public String updateSkipUntilValue() {
        return "PreparePdfPageToOcrTask";
    }

}
