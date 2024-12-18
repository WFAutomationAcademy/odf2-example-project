package com.workfusion.odf2.example.rpa;

import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

public class LoginPage {

    @FindBy(id = "email")
    private WebElement emailField;

    @FindBy(id = "password")
    private WebElement passwordField;

    @FindBy(xpath = "//input[@type='submit']")
    private WebElement submit;

    @FindBy(xpath = "//*[self::div[@id='main-area'] or self::div[@id='login']/div[contains(@class,'alert')]]")
    private WebElement loginFailed;

    @FindBy(xpath = "//div[@id='login']/div[contains(@class,'alert')]")
    private WebElement loginFailedMessage;

    public void login(String email, String password) {
        sendKeys(emailField, email);
        sendKeys(passwordField, password);

        submit.click();

        String actualId = loginFailed.getAttribute("id");
        if (!"main-area".equalsIgnoreCase(actualId)) {
            throw new IllegalStateException(String.format("%s%nUser id: %s", loginFailedMessage.getText(), email));
        }
    }

    private static void sendKeys(WebElement element, String value) {
        element.clear();
        element.sendKeys(value);
    }

}
