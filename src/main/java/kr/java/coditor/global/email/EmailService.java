package kr.java.coditor.global.email;

public interface EmailService {
    void sendEmail(String to, String subject, String text);
}
