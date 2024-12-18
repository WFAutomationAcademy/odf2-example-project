package com.workfusion.odf2.example.rpa;

import java.util.Objects;
import java.util.concurrent.TimeUnit;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.PageFactory;

import com.workfusion.bot.service.SecureEntryDTO;
import com.workfusion.odf2.core.webharvest.rpa.RpaDriver;
import com.workfusion.odf2.example.model.Invoice;
import com.workfusion.rpa.driver.Driver;

public class InvoicePlaneRobot {

    public static final String INVOICE_PLANE_URL = "https://train-invoiceplane.workfusion.com";

    private final Driver driver;
    private final SecureEntryDTO credentials;

    private final LoginPage loginPage;
    private final NavigationBar navigationBar;
    private final CreateInvoicePage createInvoicePage;
    private final EditInvoicePage editInvoicePage;

    public InvoicePlaneRobot(Driver driver, SecureEntryDTO credentials) {
        this.driver = Objects.requireNonNull(driver);
        this.credentials = Objects.requireNonNull(credentials);

        setOptions();

        loginPage = PageFactory.initElements(driver, LoginPage.class);
        navigationBar = PageFactory.initElements(driver, NavigationBar.class);
        createInvoicePage = PageFactory.initElements(driver, CreateInvoicePage.class);
        editInvoicePage = PageFactory.initElements(driver, EditInvoicePage.class);
    }

    public void processInvoice(Invoice invoice) {
        driver.navigate().to(INVOICE_PLANE_URL);

        loginPage.login(credentials.getKey(), credentials.getValue());

        navigationBar.toCreateInvoicePage();
        createInvoicePage.createDraftInvoice();
        editInvoicePage.fillAndSaveDraftInvoice(invoice);

        navigationBar.logout(driver);
    }

    private void setOptions() {
        driver.switchDriver(RpaDriver.CHROME.getName());

        WebDriver.Options options = driver.manage();

        options.timeouts()
                .implicitlyWait(10, TimeUnit.SECONDS)
                .pageLoadTimeout(90, TimeUnit.SECONDS);

        options.deleteAllCookies();
    }

}
