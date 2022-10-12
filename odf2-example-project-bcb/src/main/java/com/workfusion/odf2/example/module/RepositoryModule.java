package com.workfusion.odf2.example.module;

import com.workfusion.odf2.example.repository.AttachmentRepository;
import com.workfusion.odf2.example.repository.EmailRepository;
import com.workfusion.odf2.example.repository.InvoiceRepository;

import java.sql.SQLException;
import javax.inject.Singleton;

import com.j256.ormlite.support.ConnectionSource;
import org.codejargon.feather.Provides;

import com.workfusion.odf2.core.cdi.OdfModule;

public class RepositoryModule implements OdfModule {

    @Provides
    @Singleton
    public InvoiceRepository invoiceRepository(ConnectionSource connectionSource) throws SQLException {
        return new InvoiceRepository(connectionSource);
    }

    @Provides
    @Singleton
    public EmailRepository emailRepository(ConnectionSource connectionSource) throws SQLException {
        return new EmailRepository(connectionSource);
    }

    @Provides
    @Singleton
    public AttachmentRepository attachmentRepository(ConnectionSource connectionSource) throws SQLException {
        return new AttachmentRepository(connectionSource);
    }

}
