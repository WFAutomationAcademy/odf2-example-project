package com.workfusion.odf2.example.service;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.workfusion.odf2.example.model.Attachment;
import com.workfusion.odf2.example.model.Email;
import com.workfusion.odf2.multiprocess.model.DocumentEntity;
import com.workfusion.odf2.service.s3.S3Bucket;
import com.workfusion.odf2.service.s3.S3UploadResult;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.matches;
import static org.mockito.ArgumentMatchers.notNull;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class InMemoryEmailServiceTest {

    private static final Logger logger = LoggerFactory.getLogger(InMemoryEmailServiceTest.class);

    @Mock S3Bucket s3Bucket;
    @Mock S3UploadResult s3UploadResult;

    @BeforeEach
    void setUp() {
        when(s3Bucket.put(any(), any())).thenReturn(s3UploadResult);
    }

    @Test
    @DisplayName("should read emails with attachments")
    void shouldReadEmailsWithAttachments() {
        // given
        InMemoryEmailService emailService = new InMemoryEmailService(s3Bucket, logger);

        // when
        List<Email> actualEmails = emailService.readEmails();

        // then
        assertThat(actualEmails).hasSize(5);

        Set<String> actualTypes = actualEmails.stream()
                .flatMap(email -> email.getAttachments().stream())
                .map(DocumentEntity::getType)
                .collect(Collectors.toSet());

        assertThat(actualTypes).containsOnly("HTML", "XML", "FAIL", "RETRY", "PDF");
    }

    @Test
    @DisplayName("should read PDF file from resources and upload to S3")
    void shouldReadPdfFileFromResourcesAndUploadToS3() {
        // given
        when(s3UploadResult.getDirectUrl()).thenReturn("http://expected.url");

        InMemoryEmailService emailService = new InMemoryEmailService(s3Bucket, logger);

        // when
        List<Email> actualEmails = emailService.readEmails();

        // then
        List<Attachment> pdfAttachments = actualEmails.stream()
                .flatMap(email -> email.getAttachments().stream())
                .filter(attachment -> "PDF".equals(attachment.getType()))
                .collect(Collectors.toList());

        assertThat(pdfAttachments).hasSize(1)
                .extracting(DocumentEntity::getOriginalDocumentLink)
                .containsOnly("http://expected.url");

        verify(s3Bucket, times(1)).put(notNull(), matches("example-attachments/.*.pdf"));
    }

}
