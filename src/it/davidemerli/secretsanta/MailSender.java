package it.davidemerli.secretsanta;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Properties;

class MailSender {
    private final String USER_NAME;
    private final String PASSWORD;

    MailSender(File credentials) throws IOException {
        String[] split = SettingsReader.getLinesFromFile(credentials).stream()
                .findFirst().orElseThrow(() -> new IOException("No credentials found"))
                .split(":");

        this.USER_NAME = split[0];
        this.PASSWORD = split[1];
    }

    void sendMail(String body, String... to) throws MessagingException {
        String subject = "SECRET SANTA 2.0!";

        System.out.println(String.format("sent mail to: %s", to[0]));

//        sendFromGMail(USER_NAME, PASSWORD, to, subject, body);
    }

    private void sendFromGMail(String from, String pass, String[] to, String subject, String body) throws MessagingException {
        Properties props = System.getProperties();
        String host = "smtp.gmail.com";
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", host);
        props.put("mail.smtp.user", from);
        props.put("mail.smtp.password", pass);
        props.put("mail.smtp.port", "587");
        props.put("mail.smtp.auth", "true");

        Session session = Session.getDefaultInstance(props);
        MimeMessage message = new MimeMessage(session);

        message.setFrom(new InternetAddress(from));

        Arrays.stream(to)
                .forEach(s -> {
                    try {
                        InternetAddress ia = new InternetAddress(s);
                        message.addRecipient(Message.RecipientType.TO, ia);
                    } catch (MessagingException ex) {
                        ex.printStackTrace();
                    }
                });

        message.setSubject(subject);
        message.setText(body);
        Transport transport = session.getTransport("smtp");
        transport.connect(host, from, pass);
        transport.sendMessage(message, message.getAllRecipients());
        transport.close();
    }
}
