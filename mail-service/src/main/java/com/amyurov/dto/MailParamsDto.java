package com.amyurov.dto;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@RequiredArgsConstructor
public class MailParamsDto {
    private String id;
    private String mailTo;
}
