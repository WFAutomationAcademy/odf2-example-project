package com.workfusion.odf2.example.errorhandling;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.LoggerFactory;

import com.workfusion.odf2.core.errorhandling.FailFastErrorHandling;
import com.workfusion.odf2.core.task.OdfTask;
import com.workfusion.odf2.core.task.TaskInput;
import com.workfusion.odf2.core.time.OdfTime;
import com.workfusion.odf2.multiprocess.MultiProcessErrorHandling;
import com.workfusion.odf2.multiprocess.repository.ErrorRepository;
import com.workfusion.odf2.transaction.CurrentTransaction;
import com.workfusion.odf2.transaction.model.Transaction;
import com.workfusion.odf2.transaction.repository.JoinRepository;
import com.workfusion.odf2.transaction.repository.TransactionRepository;
public class MultiProcessErrorHandlingLogic extends MultiProcessErrorHandling {
    public MultiProcessErrorHandlingLogic(TaskInput taskInput, CurrentTransaction currentTransaction, TransactionRepository transactionRepository, JoinRepository joinRepository, FailFastErrorHandling failFastErrorHandling, ErrorRepository errorRepository, OdfTime odfTime) {
        super(taskInput, currentTransaction, transactionRepository, joinRepository, failFastErrorHandling, LoggerFactory.getLogger(MultiProcessErrorHandlingLogic.class), errorRepository, odfTime);
    }
    @Override
    protected boolean shouldSetHasErrorStatusForSiblingTransactions(Transaction transaction, Exception e, OdfTask odfTask) {
        return ExceptionUtils.getThrowableList(e).stream().noneMatch(PdfPageProcessingException.class::isInstance);
    }
}