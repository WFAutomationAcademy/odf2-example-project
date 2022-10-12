package com.workfusion.odf2.example.task.errorhandling;

import java.util.Random;
import javax.inject.Inject;

import org.slf4j.Logger;

import com.workfusion.odf2.compiler.BotTask;
import com.workfusion.odf2.multiprocess.model.ErrorEntity;
import com.workfusion.odf2.multiprocess.model.TransactionStatus;
import com.workfusion.odf2.multiprocess.task.errorhandling.ErrorHandlingTask;
import com.workfusion.odf2.transaction.model.Transaction;

@BotTask
public class ExceptionProcessorTask implements ErrorHandlingTask {

    private final Logger logger;

    @Inject
    public ExceptionProcessorTask(Logger logger) {
        this.logger = logger;
    }

    @Override
    public boolean isNeedManualErrorHandling(ErrorEntity errorEntity) {
        // To simplify, we randomly send some records to manual task and the rest are going to be automatically handled.
        return new Random().nextBoolean();
    }

    @Override
    public void automationHandling(Transaction transaction) {
        logger.info("Handling error in transaction '{}'", transaction.getUuid());
    }

    @Override
    public TransactionStatus updateTransactionStatus() {
        return TransactionStatus.ABORTED;
    }

}
