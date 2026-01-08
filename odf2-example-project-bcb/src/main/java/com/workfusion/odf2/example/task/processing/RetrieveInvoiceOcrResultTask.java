package com.workfusion.odf2.example.task.processing;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import javax.inject.Inject;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;

import com.workfusion.odf2.compiler.BotTask;
import com.workfusion.odf2.core.cdi.Requires;
import com.workfusion.odf2.core.task.TaskInput;
import com.workfusion.odf2.core.webharvest.TaskVariable;
import com.workfusion.odf2.example.errorhandlig.PdfPageProcessingException;
import com.workfusion.odf2.example.model.PdfPage;
import com.workfusion.odf2.example.module.RepositoryModule;
import com.workfusion.odf2.example.repository.PdfPageRepository;
import com.workfusion.odf2.transaction.CurrentTransaction;
import com.workfusion.odf2.transaction.TransactionResult;
import com.workfusion.odf2.transaction.task.transactional.TransactionalTask;

@BotTask
@Requires(RepositoryModule.class)
public class RetrieveInvoiceOcrResultTask implements TransactionalTask {

    private final PdfPageRepository pdfPageRepository;
    private final TaskInput taskInput;
    private final Logger logger;

    @Inject
    public RetrieveInvoiceOcrResultTask(PdfPageRepository pdfPageRepository, TaskInput taskInput, Logger logger) {
        this.pdfPageRepository = pdfPageRepository;
        this.taskInput = taskInput;
        this.logger = logger;
    }

    @Override
    public void run(CurrentTransaction currentTransaction, TransactionResult result) {
        String metaInfoJson = taskInput.getVariable("meta_info_json").orElseThrow(() -> new PdfPageProcessingException("Meta info not found in task input."));

        Map<String, String> metaInfo = readJsonToMap(metaInfoJson);
        String ocrXmlUrl = metaInfo.get("ocrXmlUrl");

        String transactionId = taskInput.getRequiredVariable(TaskVariable.TRANSACTION_ID);

        PdfPage pdfPage = pdfPageRepository.findFirstByTransactionId(UUID.fromString(transactionId));
        pdfPage.setOcrResultUrl(ocrXmlUrl);
        pdfPageRepository.update(pdfPage);

        logger.info("OCR result received for pdfPage '{}'", pdfPage.getUuid());

        result.withoutColumn("ocr_result")// clean up output data from OCR Bridge fields
                .withoutColumn("task_id")
                .withoutColumn("meta_info_json")
                .withoutColumn("_sys_ocr_process_time");
    }

    private static Map<String, String> readJsonToMap(String json) {
        try {
            return new ObjectMapper().readValue(json, new TypeReference<HashMap<String, String>>() {});
        } catch (JsonProcessingException e) {
            throw new IllegalStateException(e);
        }
    }

}
