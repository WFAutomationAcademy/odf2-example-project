package com.workfusion.odf2.example.module;

import javax.inject.Singleton;

import org.codejargon.feather.Provides;
import org.slf4j.Logger;

import com.workfusion.odf2.commons.OdfDefault;
import com.workfusion.odf2.core.cdi.OdfModule;
import com.workfusion.odf2.core.cdi.Requires;
import com.workfusion.odf2.core.settings.Configuration;
import com.workfusion.odf2.example.service.EmailService;
import com.workfusion.odf2.example.service.InMemoryEmailService;
import com.workfusion.odf2.service.s3.S3Bucket;
import com.workfusion.odf2.service.s3.S3Module;
import com.workfusion.odf2.service.s3.S3Service;

@Requires(S3Module.class)
public class ServicesModule implements OdfModule {

    @Provides
    @Singleton
    public EmailService emailService(@OdfDefault S3Service s3Service, Configuration configuration, Logger logger) {
        final S3Bucket s3Bucket = s3Service.getBucket(configuration.getRequiredProperty("example.attachments.bucket.name"));
        return new InMemoryEmailService(s3Bucket, logger);
    }

}
