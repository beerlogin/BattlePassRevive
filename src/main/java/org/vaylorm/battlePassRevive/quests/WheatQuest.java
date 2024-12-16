package org.vaylorm.battlePassRevive.quests;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.ChatColor;
import org.vaylorm.battlePassRevive.managers.QuestManager;

public class WheatQuest extends Quest {
    private int lastProgress = 0;

    public WheatQuest() {
        super("wheat_quest", 1000);
    }

    @EventHandler
    public void onWheatBreak(BlockBreakEvent event) {
        if (storage.isQuestGloballyActive(questId)) {
            // активируем квест
            setActive(true);
            setCurrentProgress(0);
        }
        if (storage == null) {
            return;
        }
        if (event.isCancelled()) {
            return;
        }

        Player player = event.getPlayer();
        if (player == null || !player.isOnline()) {
            return;
        }
        
        // Проверяем возможность обработки прогресса
        if (!canHandleProgress(player)) {
            return;
        }

        // Проверяем, что блок является пшеницей и она полностью выросла
        if (event.getBlock().getType() == Material.WHEAT && 
            ((org.bukkit.block.data.Ageable) event.getBlock().getBlockData()).getAge() == 
            ((org.bukkit.block.data.Ageable) event.getBlock().getBlockData()).getMaximumAge()) {
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
                player.sendMessage(ChatColor.YELLOW + "   Собрано пшеницы: " + 
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
        return "EMERALD:10"; // 10 изумрудов
    }
} 