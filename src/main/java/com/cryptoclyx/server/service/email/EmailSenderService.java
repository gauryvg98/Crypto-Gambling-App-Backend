package com.cryptoclyx.server.service.email;


import com.cryptoclyx.server.entity.User;
import com.cryptoclyx.server.payload.MailDto;
import com.cryptoclyx.server.payload.res.AuthTokenResponse;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.extern.log4j.Log4j2;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.runtime.RuntimeConstants;
import org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

@Log4j2
@Service
public class EmailSenderService {

    @Value("${email.changePassword.url}")
    private String changePassworddUrl;

    @Value("${email.confirmation.url}")
    private String emailConfirmationEndpoint;

    @Value("${spring.mail.from}")
    private String MAIL_FROM;

    @Value("${spring.mail.username}")
    private String USER_NAME;

    @Value("${backend.url}")
    private String appUrl;

    @Autowired
    private JavaMailSender emailSender;

    private VelocityEngine velocityEngine;

    private Map<String, Template> templates = new HashMap<>();

    public EmailSenderService() {
        velocityEngine = new VelocityEngine();
        velocityEngine.setProperty(RuntimeConstants.RESOURCE_LOADER, "classpath");
        velocityEngine.setProperty("classpath.resource.loader.class", ClasspathResourceLoader.class.getName());
        velocityEngine.init();
    }

    @Async
    public void sendUserRegistrationMail(User user, AuthTokenResponse token) {
        log.debug("Sending email confirmation link to user: {}", user);

        String emailConfirmationLink = this.emailConfirmationEndpoint.replace("{emailConfirmationUid}", token.getToken());

        final MailDto mail = new MailDto();
        String title;
        mail.setMailFrom(USER_NAME);
        mail.setMailTo(user.getEmail());

        title = "Email Confirmation";
        mail.setMailSubject(title);
        mail.setTemplateName("confirm_registration.vm");

        final Map<String, Object> parameters = new HashMap<>();
        parameters.put("title", title);
        parameters.put("firstName", user.getFirstName());
        parameters.put("lastName", user.getLastName());
        parameters.put("emailConfirmationLink", emailConfirmationLink);

        sendMail(mail, parameters);
    }

    @Async
    public void sendResetPasswordEmail(User user, AuthTokenResponse token) {
        log.debug("Sending reset password link to user: {}", user);

        String passwordResetToken = this.changePassworddUrl.replace("{emailConfirmationUid}", token.getToken());

        final MailDto mail = new MailDto();
        String title;
        mail.setMailFrom(USER_NAME);
        mail.setMailTo(user.getEmail());

        title = "Reset Password Notification";
        mail.setMailSubject(title);
        mail.setTemplateName("reset_password.vm");

        final Map<String, Object> parameters = new HashMap<>();
        parameters.put("title", title);
        parameters.put("firstName", user.getFirstName());
        parameters.put("lastName", user.getLastName());
        parameters.put("passwordResetToken", passwordResetToken);

        sendMail(mail, parameters);
    }

    @Async
    public void sendPasswordChangedEmail(User user) {
        log.debug("Sending change password notification to user: {}", user);

        final MailDto mail = new MailDto();
        String title;
        mail.setMailFrom(USER_NAME);
        mail.setMailTo(user.getEmail());

        title = "Password Changed Notification";
        mail.setMailSubject(title);
        mail.setTemplateName("password_changed.vm");

        final Map<String, Object> parameters = new HashMap<>();
        parameters.put("title", title);
        parameters.put("firstName", user.getFirstName());
        parameters.put("lastName", user.getLastName());

        sendMail(mail, parameters);
    }

    @Async
    public void sendAppWalletLowBalanceEmail(String email, String network, String walletAddress, Long solLamports) {
        log.debug("Sending app solana wallet low balance email");

        final MailDto mail = new MailDto();
        String title;
        mail.setMailFrom(USER_NAME);
        mail.setMailTo(email);

        title = "App Solana Wallet Low Balance";
        mail.setMailSubject(title);
        mail.setTemplateName("low_app_wallet_balance.vm");

        final Map<String, Object> parameters = new HashMap<>();
        parameters.put("title", title);
        parameters.put("network", network);
        parameters.put("wallet_address", walletAddress);
        parameters.put("current_balance", solLamports/1000000000l);

        sendMail(mail, parameters);
    }

    public void sendMail(final MailDto mail, final Map<String, Object> contentParametersMap) {
        try {
            MimeMessage message = createMessage(mail, contentParametersMap);
            emailSender.send(message);
        } catch (Exception e) {
            log.error("Error during mail sending: " + e.getMessage(), e);
        }
    }

    private MimeMessage createMessage(MailDto mail, Map<String, Object> contentParametersMap) throws MessagingException, UnsupportedEncodingException {
        contentParametersMap.put("appUrl", appUrl);
        //String unsubscribeLink = emailHelper.getUnsubscribeLink(mail.getMailTo());
        //contentParametersMap.put("unsubscribeLink", unsubscribeLink);

        log.debug("Sending mail: {}; parameters: {}", mail, contentParametersMap);

        MimeMessage message = emailSender.createMimeMessage();
        //message.addHeader("List-Unsubscribe", "<" + unsubscribeLink + ">");

        MimeMessageHelper messageHelper = new MimeMessageHelper(message, true, "UTF-8");

        messageHelper.setFrom(mail.getMailFrom(), MAIL_FROM);
        messageHelper.setTo(mail.getMailTo());
        messageHelper.setSubject(mail.getMailSubject());

        if(mail.getICal() != null)
            messageHelper.addAttachment("appointment.ics", mail.getICal());

        String name = mail.getTemplateName();

        Template template = templates.get(name);

        if (template == null) {
            template = velocityEngine.getTemplate("email/" + name, "UTF-8");
            templates.put(name, template);
        }

        VelocityContext velocityContext = new VelocityContext(contentParametersMap);

        StringWriter stringWriter = new StringWriter(6000);//approx size for email is 5500 bytes.

        template.merge(velocityContext, stringWriter);

        messageHelper.setText(stringWriter.toString(), true);

        return message;
    }

}
