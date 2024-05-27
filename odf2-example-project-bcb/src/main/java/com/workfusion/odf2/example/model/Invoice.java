package com.workfusion.odf2.example.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;

import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.field.ForeignCollectionField;
import com.j256.ormlite.table.DatabaseTable;

import com.workfusion.odf2.multiprocess.model.BusinessEntity;

@DatabaseTable(tableName = "business_entity")
public class Invoice extends BusinessEntity {

    public static final String AMOUNT_COLUMN = "amount";
    public static final String DATE_COLUMN = "date";
    public static final String ORIGINAL_DOCUMENT_URL = "original_document_url";

    @DatabaseField(columnName = AMOUNT_COLUMN, dataType = DataType.INTEGER)
    private int amount;

    @DatabaseField(columnName = DATE_COLUMN, dataType = DataType.DATE)
    private Date date;

    @DatabaseField(columnName = ORIGINAL_DOCUMENT_URL, dataType = DataType.STRING)
    private String originalDocumentUrl;

    @ForeignCollectionField
    private Collection<Product> products = new ArrayList<>();

    public int getAmount() {
        return amount;
    }

    public void setAmount(int amount) {
        this.amount = amount;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public String getOriginalDocumentUrl() {
        return originalDocumentUrl;
    }

    public void setOriginalDocumentUrl(String originalDocumentUrl) {
        this.originalDocumentUrl = originalDocumentUrl;
    }

    public Collection<Product> getProducts() { return products; }

    public void setProducts(Collection<Product> products) { this.products = products; }

    public boolean hasProducts() {
        return products != null && !products.isEmpty();
    }

}
