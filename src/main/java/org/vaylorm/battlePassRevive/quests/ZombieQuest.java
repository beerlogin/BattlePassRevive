package org.vaylorm.battlePassRevive.quests;

import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.ChatColor;
import org.vaylorm.battlePassRevive.managers.QuestManager;

public class ZombieQuest extends Quest {
    private Player lastPlayer;
    private int lastProgress = 0;

    public ZombieQuest() {
        super("zombie_quest", 10);
    }

    @EventHandler
    public void onZombieKill(EntityDeathEvent event) {
        if (event.getEntity().getType() == EntityType.ZOMBIE && event.getEntity().getKiller() != null) {
            handleProgress(event.getEntity().getKiller());
        }
    }

    @Override
    public void handleProgress(Player player) {
        if (isActive && !isCompleted) {
            this.lastPlayer = player;
            int newProgress = getCurrentProgress() + 1;
            setCurrentProgress(newProgress);
            lastProgress = newProgress;

            // Показываем прогресс каждые 10%
            int progressPercentage = (newProgress * 100) / targetProgress;
            if (progressPercentage == lastProgress) return;
            if (lastProgress == 0) lastProgress = progressPercentage;

            if (progressPercentage % 10 == 0 && progressPercentage > 0 && progressPercentage != lastProgress) {
                if (progressPercentage == 100) return;
                player.sendMessage("");
                player.sendMessage(ChatColor.WHITE + "❄ ═══════════════════ ❄");
                player.sendMessage("");
                player.sendMessage(ChatColor.GOLD + "   ⭐ Прогресс квеста ⭐");
                player.sendMessage(ChatColor.YELLOW + "   Убито зомби: " + 
                    ChatColor.WHITE + progressPercentage + "% " +
                    ChatColor.GRAY + "(" + newProgress + "/" + targetProgress + ")");
                player.sendMessage("");
                player.sendMessage(ChatColor.WHITE + "❄ ═══════════════════ ❄");
                player.sendMessage("");
                
                // Сохраняем прогресс при достижении каждых 10%
                QuestManager.saveQuestProgress(player);
            }
        }
    }

    @Override
    protected void giveReward() {
        if (lastPlayer != null && lastPlayer.isOnline()) {
            lastPlayer.getInventory().addItem(new ItemStack(Material.DIAMOND, 5));
            lastPlayer.sendMessage("");
            lastPlayer.sendMessage(ChatColor.WHITE + "❄ ═══════════════════════ ❄");
            lastPlayer.sendMessage("");
            lastPlayer.sendMessage(ChatColor.AQUA + "   🎄 С завершением квеста! 🎄");
            lastPlayer.sendMessage(ChatColor.WHITE + "Вы убили всех необходимых зомби");
            lastPlayer.sendMessage("");
            lastPlayer.sendMessage(ChatColor.AQUA + "   Ваш подарок:");
            lastPlayer.sendMessage(ChatColor.WHITE + "   ❆ 5 алмазов ❆");
            lastPlayer.sendMessage("");
            lastPlayer.sendMessage(ChatColor.WHITE + "❄ ═══════════════════════ ❄");
            lastPlayer.sendMessage("");

            lastPlayer.sendTitle(ChatColor.AQUA + "🎄 С завершением квеста! 🎄", ChatColor.WHITE + "Вы убили всех необходимых зомби", 10, 20, 10);
            // Создаем красивый эффект вокруг игрока
            for (int i = 0; i < 3; i++) {
                lastPlayer.getWorld().spawnParticle(Particle.TOTEM, lastPlayer.getLocation().add(0, i * 0.5, 0), 50, 0.5, 0.1, 0.5, 0.1);
                lastPlayer.getWorld().spawnParticle(Particle.VILLAGER_HAPPY, lastPlayer.getLocation().add(0, i * 0.5 + 3, 0), 15, 0.5, 0.1, 0.5, 0);
            }
            // Спираль вверх вокруг игрока из партиклов
            for (int i = 0; i < 10; i++) {
                lastPlayer.getWorld().spawnParticle(Particle.FIREWORKS_SPARK, lastPlayer.getLocation().add(0, i * 0.5, 0), 10, 0.5, 0.1, 0.5, 0.1);
            }

            // Воспроизводим звук получения награды
            lastPlayer.playSound(lastPlayer.getLocation(), "entity.player.levelup", 1.0f, 1.0f);
        }
    }
} 