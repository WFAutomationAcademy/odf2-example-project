package com.workfusion.odf2.example.module;

import javax.inject.Singleton;

import org.codejargon.feather.Provides;

import com.workfusion.odf2.core.cdi.OdfModule;
import com.workfusion.odf2.core.cdi.Requires;
import com.workfusion.odf2.core.errorhandling.ErrorHandlingLogic;
import com.workfusion.odf2.core.errorhandling.FailFastErrorHandling;
import com.workfusion.odf2.core.task.TaskInput;
import com.workfusion.odf2.core.time.OdfTime;
import com.workfusion.odf2.example.errorhandling.MultiProcessErrorHandlingLogic;
import com.workfusion.odf2.multiprocess.repository.ErrorRepository;
import com.workfusion.odf2.service.screenshot.RpaExceptionScreenshotUploader;
import com.workfusion.odf2.service.screenshot.RpaScreenshotModule;
import com.workfusion.odf2.transaction.CurrentTransaction;
import com.workfusion.odf2.transaction.TransactionModule;
import com.workfusion.odf2.transaction.repository.JoinRepository;
import com.workfusion.odf2.transaction.repository.TransactionRepository;

@Requires({TransactionModule.class, RpaScreenshotModule.class})
public class ErrorHandlingModule implements OdfModule {

    @Provides
    @Singleton
    public ErrorHandlingLogic multiProcessErrorHandlingLogic(TaskInput taskInput, CurrentTransaction currentTransaction,
            TransactionRepository transactionRepository, JoinRepository joinRepository, ErrorRepository errorRepository,
            FailFastErrorHandling failFastErrorHandling,
            OdfTime odfTime, RpaExceptionScreenshotUploader rpaExceptionScreenshotUploader) {

        return new MultiProcessErrorHandlingLogic(taskInput, currentTransaction, transactionRepository, joinRepository, failFastErrorHandling,
                errorRepository, odfTime, rpaExceptionScreenshotUploader);
    }

}
