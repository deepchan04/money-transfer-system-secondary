package com.training.mts.dto;

public class RewardStatusDTO {
    private Integer rewardPoints;
    private Integer totalPointsEarned;
    private long unscratchedCount;
    private long scratchedCount;

    public RewardStatusDTO() {
    }

    public RewardStatusDTO(Integer rewardPoints, Integer totalPointsEarned, long unscratchedCount, long scratchedCount) {
        this.rewardPoints = rewardPoints;
        this.totalPointsEarned = totalPointsEarned;
        this.unscratchedCount = unscratchedCount;
        this.scratchedCount = scratchedCount;
    }

    public Integer getRewardPoints() {
        return rewardPoints;
    }

    public void setRewardPoints(Integer rewardPoints) {
        this.rewardPoints = rewardPoints;
    }

    public Integer getTotalPointsEarned() {
        return totalPointsEarned;
    }

    public void setTotalPointsEarned(Integer totalPointsEarned) {
        this.totalPointsEarned = totalPointsEarned;
    }

    public long getUnscratchedCount() {
        return unscratchedCount;
    }

    public void setUnscratchedCount(long unscratchedCount) {
        this.unscratchedCount = unscratchedCount;
    }

    public long getScratchedCount() {
        return scratchedCount;
    }

    public void setScratchedCount(long scratchedCount) {
        this.scratchedCount = scratchedCount;
    }
}
