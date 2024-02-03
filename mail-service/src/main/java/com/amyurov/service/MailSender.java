package com.amyurov.service;

import com.amyurov.dto.MailParamsDto;

public interface MailSender {
    void sendMail(MailParamsDto mailParams);
}
