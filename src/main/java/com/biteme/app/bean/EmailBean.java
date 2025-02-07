package com.biteme.app.bean;

public class EmailBean {
    private String destinatario;
    private String subject;
    private String body;

    public EmailBean() {
    }

    public EmailBean(String destinatario, String subject, String body) {
        this.destinatario = destinatario;
        this.subject = subject;
        this.body = body;
    }

    public String getDestinatario() {
        return destinatario;
    }

    public void setDestinatario(String destinatario) {
        this.destinatario = destinatario;
    }

    public String getSubject() {
        return subject;
    }


    public String getBody() {
        return body;
    }

}
