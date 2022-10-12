package com.workfusion.odf2.example.repository;

import com.workfusion.odf2.example.model.Email;

import java.sql.SQLException;
import java.util.List;
import java.util.UUID;

import com.j256.ormlite.support.ConnectionSource;

import com.workfusion.odf2.core.OdfFrameworkException;
import com.workfusion.odf2.transaction.model.OdfTransactionalEntity;
import com.workfusion.odf2.transaction.repository.TransactionalEntityRepository;

public class EmailRepository extends TransactionalEntityRepository<Email> {

    public EmailRepository(ConnectionSource connectionSource) throws SQLException {
        super(connectionSource, Email.class);
    }

    public List<Email> findAll(UUID transactionId) {
        try {
            return dao.queryForEq(OdfTransactionalEntity.TRANSACTION_UUID_COLUMN, transactionId);
        } catch (SQLException e) {
            throw new OdfFrameworkException(e);
        }
    }

}
