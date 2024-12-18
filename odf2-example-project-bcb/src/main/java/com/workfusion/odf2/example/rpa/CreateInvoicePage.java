package com.workfusion.odf2.example.rpa;

import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.ui.Select;

public class CreateInvoicePage {

    @FindBy(id = "invoice_group_id")
    private WebElement invoiceGroupSelect;

    @FindBy(xpath = "//div[@id='create-invoice']//button[@id='invoice_create_confirm']")
    private WebElement invoiceGroupConfirmButton;

    public void createDraftInvoice() {
        Select selectObject = new Select(invoiceGroupSelect);
        selectObject.selectByIndex(1);
        invoiceGroupConfirmButton.click();
    }

}
