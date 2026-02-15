package com.training.mts.dto;



import java.util.List;

public class TransactionHistoryResponse {
    List<TransactionDTO> credits;
    List<TransactionDTO> debits;

    public TransactionHistoryResponse(List<TransactionDTO> credits, List<TransactionDTO> debits) {
        this.credits = credits;
        this.debits = debits;
    }

    public List<TransactionDTO> getCredits() {
        return credits;
    }

    public void setCredits(List<TransactionDTO> credits) {
        this.credits = credits;
    }

    public List<TransactionDTO> getDebits() {
        return debits;
    }

    public void setDebits(List<TransactionDTO> debits) {
        this.debits = debits;
    }
}
