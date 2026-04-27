package com.minlish.service;

/**
 * Created by: IntelliJ IDEA
 * User      : dutv
 * Date      : 29/03/2026
 * Time      : 14:57
 * File      : EmailService
 */

public interface EmailService {

    void sendEmail(String to, String subject, String text);
}
