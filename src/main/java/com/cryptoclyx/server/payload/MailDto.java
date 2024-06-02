package com.cryptoclyx.server.payload;

import lombok.Getter;
import lombok.Setter;

import java.io.File;

@Getter
@Setter
public class MailDto {

    private String mailFrom;

    private String mailTo;

    private String mailCc;

    private String mailBcc;

    private String mailSubject;

    private String mailContent;

    private String templateName;

    private String contentType;

    private File iCal;

    public MailDto() {
        this.contentType = "text/html";
    }
}
