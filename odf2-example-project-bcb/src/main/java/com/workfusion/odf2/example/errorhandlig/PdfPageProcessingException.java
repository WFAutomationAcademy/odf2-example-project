package com.workfusion.odf2.example.errorhandlig;

public class PdfPageProcessingException extends RuntimeException {

    public PdfPageProcessingException(String message) {
        super(message);
    }

    public PdfPageProcessingException(String message, Exception exception) { super(message, exception);}
}
