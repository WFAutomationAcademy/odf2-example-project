package com.workfusion.odf2.example.repository;

import java.sql.SQLException;

import com.j256.ormlite.support.ConnectionSource;

import com.workfusion.odf2.example.model.Email;
import com.workfusion.odf2.transaction.repository.TransactionalEntityRepository;

public class EmailRepository extends TransactionalEntityRepository<Email> {

    public EmailRepository(ConnectionSource connectionSource) throws SQLException {
        super(connectionSource, Email.class);
    }

}
