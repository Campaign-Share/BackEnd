package com.cs.webservice.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;

import java.util.Properties;

@Configuration
public class GmailConfig {
    @Value("${spring.mail.host}")
    private String MAIL_HOST;

    @Value("${spring.mail.port}")
    private int MAIL_PORT;

    @Value("${spring.mail.username}")
    private String MAIL_USERNAME;

    @Value("${spring.mail.password}")
    private String MAIL_PASSWORD;

    @Bean
    public JavaMailSender javaMailSender() {
        JavaMailSenderImpl javaMailSender = new JavaMailSenderImpl();

        javaMailSender.setHost(MAIL_HOST);
        javaMailSender.setPort(MAIL_PORT);
        javaMailSender.setUsername(MAIL_USERNAME);
        javaMailSender.setPassword(MAIL_PASSWORD);

        Properties props = javaMailSender.getJavaMailProperties();
        props.put("mail.transport.protocol", "smtp");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        javaMailSender.setJavaMailProperties(props);

        return javaMailSender;
    }
}