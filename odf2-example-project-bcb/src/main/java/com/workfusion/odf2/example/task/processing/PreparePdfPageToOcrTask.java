package com.workfusion.odf2.example.task.processing;

import javax.inject.Inject;

import com.workfusion.odf2.compiler.BotTask;
import com.workfusion.odf2.core.cdi.Requires;
import com.workfusion.odf2.example.errorhandling.PdfPageProcessingException;
import com.workfusion.odf2.example.module.RepositoryModule;
import com.workfusion.odf2.example.repository.PdfPageRepository;
import com.workfusion.odf2.transaction.CurrentTransaction;
import com.workfusion.odf2.transaction.TransactionResult;
import com.workfusion.odf2.transaction.task.transactional.TransactionalTask;

@BotTask
@Requires(RepositoryModule.class)
public class PreparePdfPageToOcrTask implements TransactionalTask {

    private final PdfPageRepository pdfPageRepository;

    @Inject
    public PreparePdfPageToOcrTask(PdfPageRepository pdfPageRepository) {this.pdfPageRepository = pdfPageRepository;}

    @Override
    public void run(CurrentTransaction currentTransaction, TransactionResult result) {
        String originalDocumentLink = pdfPageRepository.findFirstByTransactionId(currentTransaction.getRequiredId()).getOriginalDocumentLink();

        if (originalDocumentLink.contains("_fake")) {
            throw new PdfPageProcessingException("Page has incorrect link.");
        }

        result.withColumn("original_document_url", originalDocumentLink);
    }

}
