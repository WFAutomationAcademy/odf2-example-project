package com.workfusion.odf2.example.service;

import java.util.Random;

import org.apache.commons.lang3.RandomStringUtils;

import com.workfusion.odf2.example.model.Attachment;

class AttachmentBuilder {

    private String type;
    private String documentLink;
    private String content;
    private int size;

    AttachmentBuilder type(String type) {
        this.type = type;
        return this;
    }

    AttachmentBuilder documentLink(String documentLink) {
        this.documentLink = documentLink;
        return this;
    }

    AttachmentBuilder content(String content) {
        this.content = content;
        this.size = content.length();
        return this;
    }

    AttachmentBuilder randomContent() {
        int randomSize = new Random().nextInt(10) + 5;
        return content(RandomStringUtils.random(randomSize, true, false));
    }

    Attachment build() {
        Attachment attachment = new Attachment();
        attachment.setType(type);
        attachment.setOriginalDocumentLink(documentLink);
        attachment.setContent(content);
        attachment.setSize(size);
        return attachment;
    }

}
