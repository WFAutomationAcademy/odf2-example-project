package com.workfusion.odf2.example.model;

import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import com.workfusion.odf2.transaction.model.OdfTransactionalEntity;

@DatabaseTable(tableName = "pdf_page")
public class PdfPage extends OdfTransactionalEntity {

    public static final String ORIGINAL_DOCUMENT_LINK_COLUMN = "original_document_link";

    public static final String OCR_RESULT_URL = "ocr_result_url";

    @DatabaseField(columnName = ORIGINAL_DOCUMENT_LINK_COLUMN, dataType = DataType.STRING)
    private String originalDocumentLink;

    @DatabaseField(columnName = OCR_RESULT_URL, dataType = DataType.STRING)
    private String ocrResultUrl;

    public String getOriginalDocumentLink() {
        return originalDocumentLink;
    }

    public void setOriginalDocumentLink(String originalDocumentLink) {
        this.originalDocumentLink = originalDocumentLink;
    }

    public String getOcrResultUrl() {
        return ocrResultUrl;
    }

    public void setOcrResultUrl(String ocrResultUrl) {
        this.ocrResultUrl = ocrResultUrl;
    }

}
