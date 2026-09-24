package ru.putevodika.auth.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.util.HtmlUtils;
import org.springframework.web.util.UriComponentsBuilder;
import ru.putevodika.auth.config.PasswordResetProperties;

import java.nio.charset.StandardCharsets;

@Service
@RequiredArgsConstructor
@Slf4j
public class PasswordResetMailService {

    private final JavaMailSender mailSender;

    private final PasswordResetProperties properties;


    @Async("passwordResetExecutor")
    public void sendResetLink(
            String email,
            String rawToken
    ) {
        String resetUrl =
                UriComponentsBuilder
                        .fromUriString(
                                properties.frontendResetUrl()
                        )
                        .queryParam(
                                "token",
                                rawToken
                        )
                        .build()
                        .toUriString();

        sendHtmlEmail(
                email,
                "Путеводика: Восстановление пароля",
                buildResetEmailHtml(resetUrl),
                "password-reset"
        );
    }


    @Async("passwordResetExecutor")
    public void sendPasswordChangedNotice(
            String email
    ) {
        sendHtmlEmail(
                email,
                "Путеводика: Ваш пароль изменён",
                buildPasswordChangedEmailHtml(),
                "password-changed"
        );
    }


    private String buildResetEmailHtml(
            String resetUrl
    ) {
        String safeUrl =
                HtmlUtils.htmlEscape(resetUrl);

        return """
                <div style="
                    font-family: Arial, sans-serif;
                    font-size: 16px;
                    line-height: 1.5;
                    color: #000000;
                ">

                    <p style="margin: 0 0 0 0;">
                        Для восстановления пароля перейдите по ссылке ниже:
                    </p>

                    <p style="margin: 0 0 0 0;">
                        <a href="%s">
                            Восстановить пароль
                        </a>
                        ← тут ссылка
                    </p>

                    <p style="margin: 0 0 18px 0;">
                        Если вы не запрашивали восстановление пароля,
                        просто игнорируйте это письмо.
                    </p>

                    <p style="margin: 0;">
                        С заботой,<br>
                        Команда Путеводики
                    </p>

                    <div style="
                        font-family: monospace;
                        font-size: 16px;
                        line-height: 1.2;
                        white-space: pre;
                        margin: 4px 0 18px 0;
                    ">{\\__/}
                ( • .•)
                / &gt; ❤️</div>

                    <p style="margin: 0;">
                        <em style="font-style: italic;">
                            Это письмо сгенерировано автоматически. Не отвечайте на него.
                        </em>
                    </p>

                </div>
                """
                .formatted(safeUrl);
    }


    private String buildPasswordChangedEmailHtml() {
        return """
                <div style="
                    font-family: Arial, sans-serif;
                    font-size: 16px;
                    line-height: 1.5;
                    color: #000000;
                ">

                    <p style="margin: 0 0 18px 0;">
                        Пароль вашей учётной записи был успешно изменён.
                    </p>

                    <p style="margin: 0 0 18px 0;">
                        Если вы не меняли пароль, ответьте на это письмо,
                        чтобы связаться с нами.
                    </p>

                    <p style="margin: 0;">
                        С заботой,<br>
                        Команда Путеводики
                    </p>

                    <div style="
                        font-family: monospace;
                        font-size: 16px;
                        line-height: 1.2;
                        white-space: pre;
                        margin: 4px 0 18px 0;
                    ">{\\__/}
                ( • .•)
                / &gt; ❤️</div>

                    <p style="margin: 0;">
                        <em style="font-style: italic;">
                            Это письмо сгенерировано автоматически, отвечать на него не обязательно.
                        </em>
                    </p>

                </div>
                """;
    }


    private void sendHtmlEmail(
            String email,
            String subject,
            String html,
            String type
    ) {
        try {
            MimeMessage message =
                    mailSender.createMimeMessage();

            MimeMessageHelper helper =
                    new MimeMessageHelper(
                            message,
                            false,
                            StandardCharsets.UTF_8.name()
                    );

            helper.setFrom(
                    properties.mailFrom()
            );

            helper.setTo(email);

            helper.setSubject(subject);

            helper.setText(
                    html,
                    true
            );

            mailSender.send(message);

        } catch (MessagingException | MailException exception) {
            log.error(
                    "Не удалось отправить письмо типа {} на {}",
                    type,
                    email,
                    exception
            );
        }
    }
}