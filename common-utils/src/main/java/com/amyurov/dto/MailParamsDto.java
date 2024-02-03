package com.amyurov.dto;

import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MailParamsDto {
    private String id;
    private String mailTo;
}
