package com.amyurov.service.impl;

import com.amyurov.dto.MailParamsDto;
import com.amyurov.service.MailSender;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class MailSenderImpl implements MailSender {
    private final JavaMailSender javaMailSender;

    @Value("${spring.mail.username}")
    private String emailFrom;
    @Value("${service.activation.uri}")
    private String activationServiceUri;
    public MailSenderImpl(JavaMailSender javaMailSender) {
        this.javaMailSender = javaMailSender;
    }

    @Override
    public void sendMail(MailParamsDto mailParams) {
        String subject = "Активация учетной записи";
        String messageBody = getActivationMessageBody(mailParams.getId());
        String emailTo = mailParams.getMailTo();

        SimpleMailMessage mailMessage = new SimpleMailMessage();
        mailMessage.setFrom(emailFrom);
        mailMessage.setTo(emailTo);
        mailMessage.setSubject(subject);
        mailMessage.setText(messageBody);

        javaMailSender.send(mailMessage);
    }

    private String getActivationMessageBody(String id) {
        String message = String.format("Для регистрации перейдите по ссылке:\n" + activationServiceUri);
        return message.replace("{id}", id);
    }
}
