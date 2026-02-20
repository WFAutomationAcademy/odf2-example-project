package com.workfusion.odf2.example.model;

import java.util.Collection;
import java.util.Date;

import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.field.ForeignCollectionField;
import com.j256.ormlite.table.DatabaseTable;

import com.workfusion.odf2.multiprocess.model.InputEntity;

@DatabaseTable(tableName = "input")
public class Email extends InputEntity {

    public static final String FROM_COLUMN = "from";
    public static final String TO_COLUMN = "to";
    public static final String SUBJECT_COLUMN = "subject";
    public static final String MESSAGE_COLUMN = "message";
    public static final String RECEIVED_COLUMN = "received";

    @DatabaseField(columnName = FROM_COLUMN, dataType = DataType.STRING)
    private String from;

    @DatabaseField(columnName = TO_COLUMN, dataType = DataType.STRING)
    private String to;

    @DatabaseField(columnName = SUBJECT_COLUMN, dataType = DataType.STRING)
    private String subject;

    @DatabaseField(columnName = MESSAGE_COLUMN, dataType = DataType.STRING)
    private String message;

    @DatabaseField(columnName = RECEIVED_COLUMN, dataType = DataType.DATE)
    private Date received;

    @ForeignCollectionField
    private Collection<Attachment> attachments;

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getTo() {
        return to;
    }

    public void setTo(String to) {
        this.to = to;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Date getReceived() {
        return received;
    }

    public void setReceived(Date received) {
        this.received = received;
    }

    public Collection<Attachment> getAttachments() {
        return attachments;
    }

    public void setAttachments(Collection<Attachment> attachments) {
        this.attachments = attachments;
    }

}
