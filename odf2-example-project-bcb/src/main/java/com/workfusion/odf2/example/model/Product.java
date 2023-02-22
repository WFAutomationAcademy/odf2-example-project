package com.workfusion.odf2.example.model;

import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import com.workfusion.odf2.core.orm.OdfEntity;

@DatabaseTable(tableName = "product")
public class Product extends OdfEntity {

    public static final String INVOICE_COLUMN = "invoice_uuid";
    public static final String NAME_COLUMN = "name";
    public static final String DESCRIPTION_COLUMN = "description";
    public static final String PRICE_COLUMN = "price";

    @DatabaseField(columnName = INVOICE_COLUMN, foreign = true)
    private Invoice invoice;

    @DatabaseField(columnName = NAME_COLUMN, dataType = DataType.STRING)
    private String name;

    @DatabaseField(columnName = DESCRIPTION_COLUMN, dataType = DataType.STRING)
    private String description;

    @DatabaseField(columnName = PRICE_COLUMN, dataType = DataType.STRING)
    private String price;

    public Invoice getInvoice() {
        return invoice;
    }

    public void setInvoice(Invoice invoice) {
        this.invoice = invoice;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }

}
