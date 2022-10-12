package com.workfusion.odf2.example.task.intake;

import com.workfusion.odf2.example.model.Email;
import com.workfusion.odf2.example.module.RepositoryModule;
import com.workfusion.odf2.example.module.ServicesModule;
import com.workfusion.odf2.example.repository.EmailRepository;
import com.workfusion.odf2.example.service.EmailService;

import java.util.Collection;
import java.util.UUID;
import javax.inject.Inject;

import com.workfusion.odf2.compiler.BotTask;
import com.workfusion.odf2.core.cdi.Requires;
import com.workfusion.odf2.multiprocess.task.intake.InputEntityProcessorTask;
import com.workfusion.odf2.transaction.StageLoggingService;

@BotTask
@Requires({ServicesModule.class, RepositoryModule.class})
public class MarkEmailsAsReadTask implements InputEntityProcessorTask<Email> {

    private final EmailRepository emailRepository;
    private final EmailService emailService;
    private final StageLoggingService stageLoggingService;

    @Inject
    public MarkEmailsAsReadTask(EmailRepository emailRepository, EmailService emailService, StageLoggingService stageLoggingService) {
        this.emailRepository = emailRepository;
        this.emailService = emailService;
        this.stageLoggingService = stageLoggingService;
    }

    @Override
    public Collection<Email> findInputEntities(UUID transactionId) {
        return emailRepository.findAll(transactionId);
    }

    @Override
    public void processInputEntities(Collection<Email> emails) {
        stageLoggingService.stageStarted("markAsRead");
        emails.forEach(emailService::markAsRead);
        stageLoggingService.stageEnded("markAsRead");
    }

    @Override
    public void saveInputEntities(Collection<Email> emails) {
        // do nothing
    }

}
