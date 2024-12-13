package org.vaylorm.battlePassRevive.quests;

import org.bukkit.event.Listener;
import org.bukkit.entity.Player;

public abstract class Quest implements Listener {
    protected String questId;
    protected int currentProgress;
    protected int targetProgress;
    protected boolean isActive;
    protected boolean isCompleted;

    public Quest(String questId, int targetProgress) {
        this.questId = questId;
        this.targetProgress = targetProgress;
        this.currentProgress = 0;
        this.isActive = false;
        this.isCompleted = false;
    }

    public String getQuestId() {
        return questId;
    }

    public int getCurrentProgress() {
        return currentProgress;
    }

    public void setCurrentProgress(int progress) {
        this.currentProgress = progress;
        if (this.currentProgress >= targetProgress && !this.isCompleted) {
            this.isCompleted = true;
            giveReward();
        }
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        this.isActive = active;
    }

    public boolean isCompleted() {
        return isCompleted;
    }

    public int getTargetProgress() {
        return targetProgress;
    }

    public abstract void handleProgress(Player player);

    protected abstract void giveReward();

    public void resetProgress() {
        this.currentProgress = 0;
        this.isCompleted = false;
    }
} 