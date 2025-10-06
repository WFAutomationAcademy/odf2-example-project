package com.workfusion.odf2.example.repository;

import java.sql.SQLException;

import com.j256.ormlite.support.ConnectionSource;

import com.workfusion.odf2.example.model.PdfPage;
import com.workfusion.odf2.transaction.repository.TransactionalEntityRepository;

public class PdfPageRepository extends TransactionalEntityRepository<PdfPage> {

    public PdfPageRepository(ConnectionSource connectionSource) throws SQLException {
        super(connectionSource, PdfPage.class);
    }

}
