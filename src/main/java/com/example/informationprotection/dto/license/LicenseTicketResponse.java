package com.example.informationprotection.dto.license;

public class LicenseTicketResponse {
    private final Ticket ticket;
    private final String signature;

    public LicenseTicketResponse(Ticket ticket, String signature) {
        this.ticket = ticket;
        this.signature = signature;
    }

    public Ticket getTicket() {
        return ticket;
    }

    public String getSignature() {
        return signature;
    }
}
