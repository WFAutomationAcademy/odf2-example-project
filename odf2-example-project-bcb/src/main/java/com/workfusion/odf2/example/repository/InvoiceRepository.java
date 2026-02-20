package com.workfusion.odf2.example.repository;

import java.sql.SQLException;
import java.util.List;
import java.util.UUID;

import com.j256.ormlite.support.ConnectionSource;

import com.workfusion.odf2.core.OdfFrameworkException;
import com.workfusion.odf2.core.orm.OdfEntity;
import com.workfusion.odf2.example.model.Invoice;
import com.workfusion.odf2.transaction.repository.TransactionalEntityRepository;

public class InvoiceRepository extends TransactionalEntityRepository<Invoice> {

    public InvoiceRepository(ConnectionSource connectionSource) throws SQLException {
        super(connectionSource, Invoice.class);
    }

    public Invoice findRequiredById(UUID id) {
        return findById(id).orElseThrow(() -> new OdfFrameworkException(String.format("Invoice '%s' does not exist", id)));
    }

    public List<Invoice> findByIds(UUID... ids) {
        try {
            return dao.queryBuilder()
                    .where()
                    .in(OdfEntity.UUID_COLUMN, (Object[]) ids)
                    .query();
        } catch (SQLException e) {
            throw new OdfFrameworkException(e);
        }
    }

}
