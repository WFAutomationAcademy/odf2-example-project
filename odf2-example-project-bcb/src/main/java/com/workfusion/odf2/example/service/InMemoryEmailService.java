package com.workfusion.odf2.example.service;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.UUID;

import com.amazonaws.util.IOUtils;
import org.slf4j.Logger;

import com.workfusion.odf2.example.model.Attachment;
import com.workfusion.odf2.example.model.Email;
import com.workfusion.odf2.service.s3.S3Bucket;

public class InMemoryEmailService implements EmailService {

    private final S3Bucket s3Bucket;
    private final Logger logger;

    public InMemoryEmailService(S3Bucket s3Bucket, Logger logger) {
        this.s3Bucket = s3Bucket;
        this.logger = logger;
    }

    @Override
    public List<Email> readEmails() {
        return Arrays.asList(
                newEmail("HTML"),
                newEmail("XML"),
                newEmail("FAIL"),
                newEmail("RETRY"),
                newEmailWithPdfAttachment());
    }

    @Override
    public void markAsRead(Email email) {
        try {
            // emulate email server delay
            Thread.sleep(new Random().nextInt(500) + 100);
        } catch (InterruptedException ignored) {
            Thread.currentThread().interrupt();
        }

        logger.info("Email '{}' marked as read", email.getUuid());
    }

    /**
     * Creates new email filled with random data.
     * <p>
     * Each created email contains 2 attachments with provided {@code attachmentType}.
     *
     * @param attachmentType type to be associated with linked attachments
     * @return newly created {@link Email} object
     */
    private Email newEmail(String attachmentType) {
        return new EmailBuilder()
                .randomData()
                .attachment(newAttachmentFromType(attachmentType))
                .attachment(newAttachmentFromType(attachmentType))
                .build();
    }

    /**
     * Creates new email with PDF attachment using 'resources/invoice_example.pdf' file.
     * <p>
     * This method uploads the PDF file to S3 server prior to creating {@link Email} object.
     *
     * @return newly created {@link Email} object
     */
    private Email newEmailWithPdfAttachment() {
        byte[] pdfContent = readResource("/multi_page_invoice.pdf");
        String s3Key = String.format("example-attachments/%s.pdf", UUID.randomUUID());
        String pdfFileUrl = s3Bucket.put(pdfContent, s3Key).getDirectUrl();

        return new EmailBuilder()
                .randomData()
                .attachment(new AttachmentBuilder()
                        .type("PDF")
                        .documentLink(pdfFileUrl)
                        .build())
                .build();
    }

    private static Attachment newAttachmentFromType(String type) {
        return new AttachmentBuilder()
                .type(type)
                .randomContent()
                .build();
    }

    private static byte[] readResource(String name) {
        try (InputStream inputStream = InMemoryEmailService.class.getResourceAsStream(name)) {
            Objects.requireNonNull(inputStream, String.format("Unable to find '%s' resource", name));
            return IOUtils.toByteArray(inputStream);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

}
