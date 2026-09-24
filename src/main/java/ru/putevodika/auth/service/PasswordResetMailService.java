package ru.putevodika.auth.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;
import ru.putevodika.auth.config.PasswordResetProperties;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.web.util.HtmlUtils;

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

        try {
            MimeMessage message =
                    mailSender.createMimeMessage();

            MimeMessageHelper helper =
                    new MimeMessageHelper(
                            message,
                            StandardCharsets.UTF_8.name()
                    );

            helper.setFrom(properties.mailFrom());
            helper.setTo(email);
            helper.setSubject(
                    "Путеводика: Восстановление пароля"
            );

            helper.setText(
                    buildResetEmailHtml(resetUrl),
                    true
            );

            mailSender.send(message);

        } catch (MessagingException | MailException exception) {
            log.error(
                    "Не удалось отправить письмо типа password-reset на {}",
                    email,
                    exception
            );
        }
    }

    private String buildResetEmailHtml(
            String resetUrl
    ) {
        String safeUrl =
                HtmlUtils.htmlEscape(resetUrl);

        return """
            Для восстановления пароля перейдите по ссылке ниже:<br><br>
            <a href="%s">Восстановить пароль</a><br><br>
            Если вы не запрашивали восстановление пароля, просто проигнорируйте это письмо.<br><br>
            С заботой,<br>
            Команда Путеводики<br>
            <pre>{\\__/}
            ( • .•)
            / > ❤️</pre>
            Это письмо сгенерировано автоматически. Не отвечайте на него.
            """
                .formatted(safeUrl);
    }

    @Async("passwordResetExecutor")
    public void sendPasswordChangedNotice(
            String email
    ) {
        SimpleMailMessage message =
                new SimpleMailMessage();

        message.setFrom(
                properties.mailFrom()
        );
        message.setTo(email);
        message.setSubject(
                "Пароль изменён — Путеводика"
        );
        message.setText(
                """
                Пароль вашей учетной записи Путеводики был изменён.

                Если вы не выполняли это действие, обратитесь в поддержку проекта.
                """
        );

        sendSafely(
                message,
                email,
                "password-changed"
        );
    }

    private void sendSafely(
            SimpleMailMessage message,
            String email,
            String type
    ) {
        try {
            mailSender.send(message);
        } catch (MailException exception) {
            log.error(
                    "Не удалось отправить письмо типа {} на {}",
                    type,
                    email,
                    exception
            );
        }
    }
}
