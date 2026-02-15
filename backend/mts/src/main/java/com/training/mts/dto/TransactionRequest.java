package com.training.mts.dto;

import com.training.mts.enums.TransactionType;

public class TransactionRequest {
    public String getPayerVpaId() {
        return payerVpaId;
    }

    public void setPayerVpaId(String payerVpaId) {
        this.payerVpaId = payerVpaId;
    }

    public String getPayeeVpaId() {
        return payeeVpaId;
    }

    public void setPayeeVpaId(String payeeVpaId) {
        this.payeeVpaId = payeeVpaId;
    }

    private String payerVpaId;
    private String payeeVpaId;

    public String getPayerPwd() {
        return payerPwd;
    }

    public void setPayerPwd(String payerPwd) {
        this.payerPwd = payerPwd;
    }

    private String payerPwd;
    public String getIdempotencyKey() {
        return idempotencyKey;
    }

    public void setIdempotencyKey(String idempotencyKey) {
        this.idempotencyKey = idempotencyKey;
    }

    private String idempotencyKey;
    private Double amount;

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }



    private String note;

    public TransactionType getTransactionType() {
        return transactionType;
    }

    public void setTransactionType(TransactionType transactionType) {
        this.transactionType = transactionType;
    }

    private TransactionType transactionType;

    public Double getAmount() {
        return amount;
    }

    public void setAmount(Double amount) {
        this.amount = amount;
    }
}
