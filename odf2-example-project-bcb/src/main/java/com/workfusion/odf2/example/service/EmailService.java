package com.workfusion.odf2.example.service;

import com.workfusion.odf2.example.model.Email;

import java.util.List;

public interface EmailService {

    List<Email> readEmails();

    void markAsRead(Email email);

}
