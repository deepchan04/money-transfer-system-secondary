package com.training.mts.dto;

import com.training.mts.model.ScratchCard;
import java.time.LocalDateTime;

public class ScratchCardDTO {
    private Long id;
    private boolean scratched;
    private String title;
    private String description;
    private String couponCode;
    private LocalDateTime createdAt;
    private LocalDateTime scratchedAt;

    public ScratchCardDTO() {
    }

    public ScratchCardDTO(ScratchCard card) {
        this.id = card.getId();
        this.scratched = card.isScratched();
        this.createdAt = card.getCreatedAt();
        this.scratchedAt = card.getScratchedAt();
        if (card.isScratched()) {
            this.title = card.getTitle();
            this.description = card.getDescription();
            this.couponCode = card.getCouponCode();
        } else {
            // Masked for unscratched cards
            this.title = "Scratch Card";
            this.description = "Scratch this card to reveal your reward!";
            this.couponCode = "LOCKED";
        }
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public boolean isScratched() {
        return scratched;
    }

    public void setScratched(boolean scratched) {
        this.scratched = scratched;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCouponCode() {
        return couponCode;
    }

    public void setCouponCode(String couponCode) {
        this.couponCode = couponCode;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getScratchedAt() {
        return scratchedAt;
    }

    public void setScratchedAt(LocalDateTime scratchedAt) {
        this.scratchedAt = scratchedAt;
    }
}
