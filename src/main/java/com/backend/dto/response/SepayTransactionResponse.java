package com.backend.dto.response;

import lombok.Data;

import java.util.List;

@Data
public class SepayTransactionResponse {
    private int status;
    private Object error;
    private MessagesResponse messages;
    private List<Transaction> transactions;
}
