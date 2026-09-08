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

        SimpleMailMessage message =
                new SimpleMailMessage();

        message.setFrom(
                properties.mailFrom()
        );
        message.setTo(email);
        message.setSubject(
                "Восстановление доступа — Путеводика"
        );
        message.setText(
                """
                Вы запросили восстановление доступа к Путеводике.

                Перейдите по ссылке, чтобы установить новый пароль:
                %s

                Ссылка действует %d минут и может быть использована только один раз.

                Если вы не запрашивали восстановление пароля, просто проигнорируйте это письмо.
                """
                        .formatted(
                                resetUrl,
                                properties
                                        .tokenTtl()
                                        .toMinutes()
                        )
        );

        sendSafely(
                message,
                email,
                "password-reset"
        );
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
