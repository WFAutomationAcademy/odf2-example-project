package com.workfusion.odf2.example.rpa;

import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

public class NavigationBar {

    @FindBy(xpath = "//a[contains(@class,'logout')]")
    private WebElement logoutButton;

    @FindBy(xpath = "//*[@id='ip-navbar-collapse']//li/a//span[text()='Invoices']/parent::*")
    private WebElement invoicesMenu;

    @FindBy(xpath = "//*[@id='ip-navbar-collapse']//li/ul/li/a[text()='Create Invoice']")
    private WebElement createInvoiceMenuItem;

    public void toCreateInvoicePage() {
        invoicesMenu.click();
        createInvoiceMenuItem.click();
    }

    public void logout() {
        try {
            logoutButton.click();
        } catch (StaleElementReferenceException ignored) {
            // Due to InvoicePlane specifics, sometimes logout button is removed from DOM structure. In this case, we would need to retry the click.
            // see also: https://stackoverflow.com/questions/18225997/stale-element-reference-element-is-not-attached-to-the-page-document
            logoutButton.click();
        }
    }

}
