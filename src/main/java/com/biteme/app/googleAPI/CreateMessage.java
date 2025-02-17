package com.biteme.app.googleAPI;

import com.google.api.services.gmail.model.Message;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;
import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

public class CreateMessage {

    private CreateMessage() {
        //costruttore privato
    }

    public static Message createMessageWithEmail(MimeMessage emailContent)
            throws MessagingException, IOException {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        emailContent.writeTo(buffer);
        byte[] bytes = buffer.toByteArray();

        // Utilizzo del Base64 nativo di Java
        String encodedEmail = Base64.getUrlEncoder().encodeToString(bytes);
        Message message = new Message();
        message.setRaw(encodedEmail);
        return message;
    }
}