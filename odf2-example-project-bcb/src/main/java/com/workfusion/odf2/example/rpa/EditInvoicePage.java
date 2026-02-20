package com.workfusion.odf2.example.rpa;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.ui.Select;

import com.workfusion.odf2.example.model.Invoice;
import com.workfusion.odf2.example.model.Product;

public class EditInvoicePage {

    public static final DateTimeFormatter DATE_FORMAT = DateTimeFormat.forPattern("MM/dd/yyyy");

    @FindBy(id = "invoice_number")
    private WebElement invoiceNumber;

    @FindBy(id = "invoice_date_created")
    private WebElement invoiceDate;

    @FindBy(id = "payment_method")
    private WebElement invoicePayment;

    @FindBy(id = "invoice_status_id")
    private WebElement invoiceStatus;

    @FindBy(id = "btn_save_invoice")
    private WebElement saveButton;

    @FindBy(xpath = "//a[contains(@class, 'btn_add_row')]")
    private WebElement addRowButton;

    @FindBy(id = "item_table")
    private WebElement itemTable;

    public void fillAndSaveDraftInvoice(Invoice invoice) {
        fillProductsInfo(new ArrayList<>(invoice.getProducts()));

        invoice.setNumber(invoiceNumber.getAttribute("value"));
        invoice.setDate(DateTime.parse(invoiceDate.getAttribute("value"), DATE_FORMAT).toDate());

        Select selectPayment = new Select(invoicePayment);
        selectPayment.selectByIndex(1);
        invoice.setPayment(selectPayment.getFirstSelectedOption().getText());

        Select selectStatus = new Select(invoiceStatus);
        invoice.setStatus(selectStatus.getFirstSelectedOption().getText());

        String invoiceId = itemTable.findElement(By.name("invoice_id")).getAttribute("value");
        invoice.setExternalLink(String.format("%s/index.php/invoices/generate_pdf/%s", InvoicePlaneRobot.INVOICE_PLANE_URL, invoiceId));

        saveButton.click();
    }

    private void fillProductsInfo(List<Product> products) {
        ListIterator<Product> iterator = products.listIterator();
        while (iterator.hasNext()) {
            String xpathExpression = String.format("//table/tbody[%d]", iterator.nextIndex() + 2);
            WebElement row = itemTable.findElement(By.xpath(xpathExpression));
            fillProductRow(row, iterator.next());

            if (iterator.hasNext()) {
                addRowButton.click();
            }
        }
    }

    private void fillProductRow(WebElement row, Product product) {
        WebElement name = row.findElement(By.name("item_name"));
        name.sendKeys(product.getName());

        WebElement quantity = row.findElement(By.name("item_quantity"));
        quantity.sendKeys("1");

        WebElement price = row.findElement(By.name("item_price"));
        price.sendKeys(product.getPrice());

        WebElement description = row.findElement(By.name("item_description"));
        description.sendKeys(product.getDescription());
    }

}
