package org.vaylorm.battlePassRevive.quests;

import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.ChatColor;
import org.vaylorm.battlePassRevive.managers.QuestManager;

public class ZombieQuest extends Quest {
    private int lastProgress = 0;

    public ZombieQuest() {
        super("zombie_quest", 60);
    }

    @EventHandler
    public void onZombieKill(EntityDeathEvent event) {
        if (storage == null) {
            return;
        }
        
        if (event.getEntity().getType() == EntityType.ZOMBIE && event.getEntity().getKiller() != null) {
            Player player = event.getEntity().getKiller();
            if (player == null || !player.isOnline()) {
                return;
            }
            if (!canHandleProgress(player)) {
                return;
            }
            handleProgress(player);
        }
    }

    @Override
    public void handleProgress(Player player) {
        if (isActive && !isCompleted) {
            this.lastPlayer = player;
            int newProgress = getCurrentProgress() + 1;
            setCurrentProgress(newProgress);

            int progressPercentage = (newProgress * 100) / targetProgress;
            
            // Показываем прогресс каждые 10%
            if (progressPercentage % 10 == 0 && progressPercentage > 0 && progressPercentage != lastProgress) {
                if (progressPercentage == 100) return;
                player.sendMessage("");
                player.sendMessage(ChatColor.WHITE + "❄ ═══════════════════ ❄");
                player.sendMessage(ChatColor.GOLD + "   ⭐ Прогресс квеста ⭐");
                player.sendMessage(ChatColor.YELLOW + "   Убито зомби: " + 
                    ChatColor.WHITE + progressPercentage + "% " +
                    ChatColor.GRAY + "(" + newProgress + "/" + targetProgress + ")");
                player.sendMessage("");
                
                // Сохраняем прогресс при достижении каждых 10%
                QuestManager.saveQuestProgress(player);
                lastProgress = progressPercentage;
            }
        }
    }

    @Override
    protected String getRewardType() {
        return "DIAMOND:5"; // 5 алмазов
    }

    @Override
    protected boolean canHandleProgress(Player player) {
        if (!super.canHandleProgress(player)) {
            return false;
        }
        
        if (!isActive || isCompleted) {
            return false;
        }
        
        return true;
    }
} 