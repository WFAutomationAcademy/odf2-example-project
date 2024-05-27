package com.workfusion.odf2.example.task.processing;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.apache.commons.lang3.StringUtils;

import com.workfusion.odf.test.launch.InputData;
import com.workfusion.odf2.core.orm.OdfEntity;
import com.workfusion.odf2.core.webharvest.TaskVariable;
import com.workfusion.odf2.transaction.model.Transaction;

public class InputDataBuilder {

    private final Map<String, String> values = new LinkedHashMap<>();

    private InputDataBuilder() {
    }

    public static InputDataBuilder getInstance() {
        return new InputDataBuilder();
    }

    public static InputDataBuilder from(Transaction transaction) {
        return getInstance().add(transaction);
    }

    public InputDataBuilder add(Transaction transaction) {
        Optional<Transaction> value = Optional.ofNullable(transaction);

        add(TaskVariable.TRANSACTION_ID.toString(), value.map(OdfEntity::getUuid).map(UUID::toString).orElse(""));
        add(TaskVariable.TRANSACTION_STATUS.toString(), value.map(Transaction::getStatus).orElse(""));

        add(TaskVariable.PARENT_TRANSACTION_ID.toString(), value.flatMap(Transaction::getParentTransaction)
                .map(OdfEntity::getUuid)
                .map(UUID::toString)
                .orElse(""));

        add(TaskVariable.ERROR_STATUS.toString(), value.map(Transaction::getErrorStatus).orElse(""));
        add(TaskVariable.SKIP_UNTIL.toString(), value.flatMap(Transaction::getSkipUntil).orElse(""));

        return this;
    }

    public InputDataBuilder add(TaskVariable variable, String value) {
        add(variable.toString(), value);
        return this;
    }

    public InputDataBuilder add(String header, String value) {
        if (StringUtils.isEmpty(header)) {
            throw new NullPointerException("Header is null or empty");
        }
        values.put(header, value == null ? "" : value);
        return this;
    }

    public InputDataBuilder add(Map<String, String> values) {
        values.forEach(this::add);
        return this;
    }

    public InputData build() {
        return InputData.of(
                new ArrayList<>(values.keySet()),
                new ArrayList<>(values.values()));
    }

}
