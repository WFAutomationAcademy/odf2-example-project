package com.workfusion.odf2.example.task.intake;

import com.workfusion.odf2.example.model.Attachment;
import com.workfusion.odf2.example.model.Email;
import com.workfusion.odf2.example.module.RepositoryModule;
import com.workfusion.odf2.example.module.ServicesModule;
import com.workfusion.odf2.example.repository.AttachmentRepository;
import com.workfusion.odf2.example.repository.EmailRepository;
import com.workfusion.odf2.example.service.EmailService;

import java.util.Collection;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.inject.Inject;

import com.workfusion.odf2.compiler.BotTask;
import com.workfusion.odf2.core.cdi.Requires;
import com.workfusion.odf2.multiprocess.task.intake.InputMonitorTask;

@BotTask
@Requires({ServicesModule.class, RepositoryModule.class})
public class EmailMonitorTask implements InputMonitorTask<Email> {

    private final EmailRepository emailRepository;
    private final AttachmentRepository attachmentRepository;
    private final EmailService emailService;

    @Inject
    public EmailMonitorTask(EmailRepository emailRepository, AttachmentRepository attachmentRepository, EmailService emailService) {
        this.emailRepository = emailRepository;
        this.attachmentRepository = attachmentRepository;
        this.emailService = emailService;
    }

    @Override
    public Collection<Email> queryInputEntities() {
        return emailService.readEmails();
    }

    @Override
    public void saveInputEntity(Email email) {
        emailRepository.create(email);
        try (final Stream<Attachment> attachmentStream = email.getAttachments().stream()) {
            attachmentRepository.createAll(attachmentStream.collect(Collectors.toList()));
        }
    }

}
