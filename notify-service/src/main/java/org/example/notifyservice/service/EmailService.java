package org.example.notifyservice.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class EmailService {

    private final JavaMailSender mailSender;
    private final String from;

    public EmailService(
            JavaMailSender mailSender,
            @Value("${notification.mail.from}") String from) {
        this.mailSender = mailSender;
        this.from = from;
    }

    public void sendBookingCreatedEmail(String recipient) {
        if (recipient == null || recipient.isBlank()){
            throw new IllegalArgumentException("Không được bỏ trống email người nhận");
        }

        SimpleMailMessage simpleMailMessage = new SimpleMailMessage();

        simpleMailMessage.setFrom(from);
        simpleMailMessage.setTo(recipient);
        simpleMailMessage.setText("bạn đã đặt vé thành công");

        mailSender.send(simpleMailMessage);
    }
}
