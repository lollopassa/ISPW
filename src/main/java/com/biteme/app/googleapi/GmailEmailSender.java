package com.biteme.app.googleapi;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.gmail.Gmail;
import com.google.api.services.gmail.model.Message;
import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import java.io.IOException;


public class GmailEmailSender {

    private GmailEmailSender() {
            }

    private static final String APPLICATION_NAME = "BiteMeApp";
    private static final HttpTransport HTTP_TRANSPORT = new NetHttpTransport();
    private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();


    public static void sendEmail(String accessToken, String from, String to, String subject, String bodyText)
            throws MessagingException, IOException {

                Credential credential = new GoogleCredential().setAccessToken(accessToken);
        Gmail service = new Gmail.Builder(HTTP_TRANSPORT, JSON_FACTORY, credential)
                .setApplicationName(APPLICATION_NAME)
                .build();

                MimeMessage email = CreateEmail.createEmail(to, from, subject, bodyText);
                Message message = CreateMessage.createMessageWithEmail(email);

                service.users().messages().send("me", message).execute();
    }
}
