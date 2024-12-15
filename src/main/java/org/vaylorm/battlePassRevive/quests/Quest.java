package org.vaylorm.battlePassRevive.quests;

import org.bukkit.event.Listener;
import org.bukkit.entity.Player;
import org.vaylorm.battlePassRevive.storage.QuestStorage;

public abstract class Quest implements Listener {
    protected String questId;
    protected int currentProgress;
    protected int targetProgress;
    protected boolean isActive;
    protected boolean isCompleted;
    protected QuestStorage storage;

    public Quest(String questId, int targetProgress) {
        this.questId = questId;
        this.targetProgress = targetProgress;
        this.currentProgress = 0;
        this.isActive = false;
        this.isCompleted = false;
    }

    public void setStorage(QuestStorage storage) {
        this.storage = storage;
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
            storage.getPlugin().getLogger().info("Квест " + questId + " выполнен!");
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

    protected boolean canHandleProgress(Player player) {
        if (storage == null || player == null) {
            storage.getPlugin().getLogger().warning("Ошибка обработки прогресса: storage или player равны null");
            return false;
        }
        
        if (!storage.isQuestGloballyActive(questId)) {
            storage.getPlugin().getLogger().fine("Квест " + questId + " не активен глобально");
            return false;
        }
        
        if (storage.hasPlayerCompletedGlobalQuest(questId, player)) {
            storage.getPlugin().getLogger().fine("Игрок " + player.getName() + " уже выполнил квест " + questId);
            return false;
        }
        
        if (!player.isOnline()) {
            storage.getPlugin().getLogger().warning("Попытка обработать прогресс для оффлайн игрока: " + player.getName());
            return false;
        }
        
        return true;
    }

    protected void markAsCompleted(Player player) {
        if (storage != null && player != null && player.isOnline()) {
            storage.addQuestCompleter(questId, player);
            storage.getPlugin().getLogger().info("Игрок " + player.getName() + " завершил квест " + questId);
        } else {
            storage.getPlugin().getLogger().warning("Не удалось отметить квест как выполненный для игрока " + 
                (player != null ? player.getName() : "null"));
        }
    }
} 