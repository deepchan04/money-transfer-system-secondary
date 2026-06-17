package com.training.mts.dto;

import com.training.mts.enums.TransactionType;
import com.training.mts.model.Transaction;

import java.time.LocalDateTime;

public class TransactionDTO {
    private String payerVpaId;
    private String payeeVpaId;
    private Double amount;
    private String status;
    private String failureReason;
    private LocalDateTime transactionTime;
    private Integer points;

    public Integer getPoints(){return points;}
    public void setPoints(Integer points){this.points = points;}

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

    public TransactionDTO(Transaction t) {
        this.payerVpaId = t.getPayer().getVpa().getVpaId();
        this.payeeVpaId = t.getPayee().getVpa().getVpaId();
        this.amount = t.getAmount();
        this.status = t.getStatus().name();
        this.failureReason = t.getFailureReason();
        this.transactionTime = t.getTransactionTime();
        this.transactionType = t.getTransactionType();
        this.note = t.getNote();
        this.points = t.getPoints();
    }

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

    public Double getAmount() {
        return amount;
    }

    public void setAmount(Double amount) {
        this.amount = amount;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getFailureReason() {
        return failureReason;
    }

    public void setFailureReason(String failureReason) {
        this.failureReason = failureReason;
    }

    public LocalDateTime getTransactionTime() {
        return transactionTime;
    }

    public void setTransactionTime(LocalDateTime transactionTime) {
        this.transactionTime = transactionTime;
    }

}
