package com.workfusion.odf2.example.repository;

import java.sql.SQLException;
import java.util.List;
import java.util.UUID;

import com.j256.ormlite.support.ConnectionSource;

import com.workfusion.odf2.core.OdfFrameworkException;
import com.workfusion.odf2.core.orm.OrmLiteRepository;
import com.workfusion.odf2.example.model.Product;

public class ProductRepository extends OrmLiteRepository<Product> {

    public ProductRepository(ConnectionSource connectionSource) throws SQLException {
        super(connectionSource, Product.class);
    }

    public List<Product> findByInvoiceId(UUID invoiceUuid) {
        try {
            return dao.queryBuilder()
                    .where()
                    .eq(Product.INVOICE_COLUMN, invoiceUuid)
                    .query();
        } catch (SQLException e) {
            throw new OdfFrameworkException(e);
        }
    }

}
