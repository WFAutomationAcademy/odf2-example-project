package com.workfusion.odf2.example.service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Random;

import org.apache.commons.lang3.RandomStringUtils;

import com.workfusion.odf2.example.model.Attachment;
import com.workfusion.odf2.example.model.Email;

class EmailBuilder {

    private String from;
    private String to;
    private String subject;
    private String message;
    private Date receivedDate;
    private final List<Attachment> attachments = new ArrayList<>();

    EmailBuilder from(String from) {
        this.from = from;
        return this;
    }

    EmailBuilder to(String to) {
        this.to = to;
        return this;
    }

    EmailBuilder subject(String subject) {
        this.subject = subject;
        return this;
    }

    EmailBuilder message(String message) {
        this.message = message;
        return this;
    }

    EmailBuilder receivedDate(Date receivedDate) {
        this.receivedDate = receivedDate;
        return this;
    }

    EmailBuilder randomData() {
        return from(randomEmail())
                .to(randomEmail())
                .subject(randomString(8))
                .message(randomString(10))
                .receivedDate(randomDate());
    }

    EmailBuilder attachment(Attachment attachment) {
        attachments.add(Objects.requireNonNull(attachment));
        return this;
    }

    Email build() {
        Email email = new Email();
        email.setFrom(from);
        email.setTo(to);
        email.setSubject(subject);
        email.setMessage(message);
        email.setReceived(receivedDate);

        if (!attachments.isEmpty()) {
            email.setAttachments(attachments);
            attachments.forEach(attachment -> attachment.setEmail(email));
        }

        return email;
    }

    private static String randomEmail() {
        return String.format("%s@example-email.com", randomString(5));
    }

    private static String randomString(int length) {
        return RandomStringUtils.random(length, true, false);
    }

    private static Date randomDate() {
        int shift = new Random().nextInt(10);
        Instant dayInPast = Instant.now().minus(shift, ChronoUnit.DAYS);
        return Date.from(dayInPast);
    }

}
