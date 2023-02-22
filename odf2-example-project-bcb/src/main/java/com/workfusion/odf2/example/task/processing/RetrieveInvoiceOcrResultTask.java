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
import com.workfusion.odf2.core.task.AdHocTask;
import com.workfusion.odf2.core.task.TaskInput;
import com.workfusion.odf2.core.task.output.TaskRunnerOutput;
import com.workfusion.odf2.core.webharvest.TaskVariable;
import com.workfusion.odf2.example.model.Invoice;
import com.workfusion.odf2.example.module.RepositoryModule;
import com.workfusion.odf2.example.repository.InvoiceRepository;

@BotTask
@Requires(RepositoryModule.class)
public class RetrieveInvoiceOcrResultTask implements AdHocTask {

    private final InvoiceRepository invoiceRepository;
    private final Logger logger;

    @Inject
    public RetrieveInvoiceOcrResultTask(InvoiceRepository invoiceRepository, Logger logger) {
        this.invoiceRepository = invoiceRepository;
        this.logger = logger;
    }

    @Override
    public TaskRunnerOutput run(TaskInput taskInput) {
        Map<String, String> metaInfo = readJsonToMap(taskInput.getRequiredVariable("meta_info_json"));
        String ocrXmlUrl = metaInfo.get("ocrXmlUrl");

        String transactionId = taskInput.getRequiredVariable(TaskVariable.TRANSACTION_ID);

        Invoice invoice = invoiceRepository.findFirstByTransactionId(UUID.fromString(transactionId));
        invoice.setOcrResultUrl(ocrXmlUrl);
        invoiceRepository.update(invoice);

        logger.info("OCR result received for invoice '{}'", invoice.getUuid());

        return taskInput.asResult()
                .withoutColumn("ocr_result") // clean up output data from OCR Bridge fields
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
