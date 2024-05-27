package com.workfusion.odf2.example.module;

import com.workfusion.odf2.example.repository.AttachmentRepository;
import com.workfusion.odf2.example.repository.EmailRepository;
import com.workfusion.odf2.example.repository.InvoiceRepository;

import java.sql.SQLException;
import javax.inject.Singleton;

import com.j256.ormlite.support.ConnectionSource;
import org.codejargon.feather.Provides;

import com.workfusion.odf2.core.cdi.OdfModule;
import com.workfusion.odf2.example.repository.PdfPageRepository;
import com.workfusion.odf2.example.repository.ProductRepository;

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

    @Provides
    @Singleton
    public ProductRepository productRepository(ConnectionSource connectionSource) throws SQLException {
        return new ProductRepository(connectionSource);
    }

    @Provides
    @Singleton
    public PdfPageRepository pdfPageRepository(ConnectionSource connectionSource) throws SQLException {
        return new PdfPageRepository(connectionSource);
    }

}
