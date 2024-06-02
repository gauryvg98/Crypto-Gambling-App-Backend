package com.cryptoclyx.server.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.scheduling.annotation.EnableAsync;

import java.util.Properties;

@Configuration
@EnableAsync
public class EmailSenderConfiguration {

    @Value("${spring.mail.username}")
    private String USER_NAME;

    @Value("${spring.mail.password}")
    private String PASSWORD;

    @Value("${spring.mail.port}")
    private int PORT;

    @Value("${spring.mail.host}")
    private String HOST;

    @Value("${spring.mail.transport.protocol}")
    private String PROTOCOL;

    @Value("${spring.mail.properties.mail.smtp.auth}")
    private String SMTP_AUTH;

    @Value("${spring.mail.properties.mail.smtp.starttls.enable}")
    private String START_TTLS_ENABLE;

    @Value("${spring.mail.debug}")
    private String MAIL_DEBUG;

    @Bean
    public JavaMailSender getJavaMailSender() {
        JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
        mailSender.setHost(HOST);
        mailSender.setPort(PORT);
        mailSender.setProtocol(PROTOCOL);

        mailSender.setUsername(USER_NAME);
        mailSender.setPassword(PASSWORD);

        Properties props = mailSender.getJavaMailProperties();
        props.put("mail.transport.protocol", PROTOCOL);

        props.put("mail.smtp.auth", SMTP_AUTH);
        props.put("mail.smtp.starttls.enable", START_TTLS_ENABLE);
        props.put("mail.debug", MAIL_DEBUG);

        return mailSender;
    }
}
