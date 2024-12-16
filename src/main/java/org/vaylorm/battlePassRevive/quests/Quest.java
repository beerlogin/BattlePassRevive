package org.vaylorm.battlePassRevive.quests;

import org.bukkit.event.Listener;
import org.bukkit.entity.Player;
import org.vaylorm.battlePassRevive.managers.QuestManager;
import org.vaylorm.battlePassRevive.storage.QuestStorage;
import org.bukkit.ChatColor;
import org.bukkit.Particle;

public abstract class Quest implements Listener {
    protected String questId;
    protected int currentProgress;
    protected int targetProgress;
    protected boolean isActive;
    protected boolean isCompleted;
    protected QuestStorage storage;
    protected Player lastPlayer;

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
            if (lastPlayer != null) {
                QuestManager.saveQuestProgress(lastPlayer);
                markAsCompleted(lastPlayer);
                saveReward();
            }
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

    public void resetProgress() {
        this.currentProgress = 0;
        this.isCompleted = false;
    }

    protected boolean canHandleProgress(Player player) {
        if (player == null || !player.isOnline()) {
            return false;
        }

        if (storage == null) {
            return false;
        }
        
        if (!storage.isQuestGloballyActive(questId)) {
            storage.getPlugin().getLogger().fine("Квест " + questId + " не активен глобально");
            setCurrentProgress(targetProgress);
            return false;
        }
        
        if (storage.hasPlayerCompletedGlobalQuest(questId, player)) {
            storage.getPlugin().getLogger().fine("Игрок " + player.getName() + " уже выполнил квест " + questId);
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

    protected abstract String getRewardType();

    protected void saveReward() {
        if (storage == null) {
            return;
        }
        if (lastPlayer != null && lastPlayer.isOnline()) {
            storage.addPendingReward(lastPlayer, questId, getRewardType());
            lastPlayer.sendMessage("");
            lastPlayer.sendMessage(ChatColor.WHITE + "❄ ═══════════════════════ ❄");
            lastPlayer.sendMessage("");
            lastPlayer.sendMessage(ChatColor.AQUA + "   🎄 С завершением квеста! 🎄");
            lastPlayer.sendMessage(ChatColor.WHITE + "Используйте /bp claim чтобы получить награду");
            lastPlayer.sendMessage("");
            lastPlayer.sendMessage(ChatColor.WHITE + "❄ ═══════════════════════ ❄");
            lastPlayer.sendMessage("");
            
            // Эффекты завершения
            for (int i = 0; i < 3; i++) {
                lastPlayer.getWorld().spawnParticle(Particle.TOTEM, lastPlayer.getLocation().add(0, i * 0.5, 0), 50, 0.5, 0.1, 0.5, 0.1);
            }
            lastPlayer.playSound(lastPlayer.getLocation(), "entity.player.levelup", 1.0f, 1.0f);
        }
    }
} 