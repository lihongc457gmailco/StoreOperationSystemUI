package com.mycompany.storeoperationsystemgui;

import jakarta.mail.*;
import jakarta.mail.internet.*;
import java.io.File;
import java.io.IOException;
import java.util.Properties;

public class EmailService {

    // UPDATE THESE WITH YOUR DETAILS
    private static final String SENDER_EMAIL = "lihongc457@gmail.com";
    private static final String SENDER_PASSWORD = "tles beqd zihj kwae"; // Use App Password
    private static final String RECIPIENT_EMAIL = "25005770@siswa.um.edu.my";

    public static void sendDailyReport(String date, double totalSales, File reportFile) {
        
        Properties prop = new Properties();
        prop.put("mail.smtp.host", "smtp.gmail.com");
        prop.put("mail.smtp.port", "465");
        prop.put("mail.smtp.auth", "true");
        prop.put("mail.smtp.socketFactory.port", "465");
        prop.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");

        // *** FIX IS HERE: Use 'jakarta.mail.Session' explicitly ***
        jakarta.mail.Session session = jakarta.mail.Session.getInstance(prop, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(SENDER_EMAIL, SENDER_PASSWORD);
            }
        });

        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(SENDER_EMAIL));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(RECIPIENT_EMAIL));
            message.setSubject("Daily Sales Report: " + date);

            // 1. Create the Body Text
            MimeBodyPart textBodyPart = new MimeBodyPart();
            String emailContent = "Hello,\n\n"
                    + "Here is the automated sales report for " + date + ".\n"
                    + "--------------------------------\n"
                    + "Total Sales: RM " + String.format("%.2f", totalSales) + "\n"
                    + "--------------------------------\n\n"
                    + "The full detailed report is attached.";
            textBodyPart.setText(emailContent);

            // 2. Create the Attachment
            MimeBodyPart attachmentBodyPart = new MimeBodyPart();
            if (reportFile != null && reportFile.exists()) {
                attachmentBodyPart.attachFile(reportFile);
            } else {
                attachmentBodyPart.setText("\n[Error: Report file not found]");
            }

            // 3. Combine them
            Multipart multipart = new MimeMultipart();
            multipart.addBodyPart(textBodyPart);
            if (reportFile != null && reportFile.exists()) {
                multipart.addBodyPart(attachmentBodyPart);
            }

            message.setContent(multipart);

            // 4. Send
            Transport.send(message);
            System.out.println("Email sent successfully to " + RECIPIENT_EMAIL);

        } catch (MessagingException | IOException e) {
            e.printStackTrace();
        }
    }
}