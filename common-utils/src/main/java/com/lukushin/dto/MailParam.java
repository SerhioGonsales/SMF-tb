package com.lukushin.dto;

import lombok.*;

@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class MailParam {
    private String id;
    private String emailTo;
}
