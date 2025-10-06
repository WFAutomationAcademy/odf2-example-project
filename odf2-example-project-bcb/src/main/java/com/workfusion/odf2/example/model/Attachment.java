package com.workfusion.odf2.example.model;

import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import com.workfusion.odf2.multiprocess.model.DocumentEntity;

@DatabaseTable(tableName = "document")
public class Attachment extends DocumentEntity {

    public static final String INPUT_UUID_COLUMN = "input_uuid";
    public static final String CONTENT_COLUMN = "content";
    public static final String SIZE_COLUMN = "size";

    @DatabaseField(columnName = INPUT_UUID_COLUMN, foreign = true, canBeNull = false)
    private Email email;

    @DatabaseField(columnName = CONTENT_COLUMN, dataType = DataType.STRING)
    private String content;

    @DatabaseField(columnName = SIZE_COLUMN, dataType = DataType.INTEGER)
    private int size;

    public Email getEmail() {
        return email;
    }

    public void setEmail(Email email) {
        this.email = email;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

}
