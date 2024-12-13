package org.vaylorm.battlePassRevive.quests;

import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.ChatColor;

public class WheatQuest extends Quest {
    private Player lastPlayer;

    public WheatQuest() {
        super("wheat_quest", 1000);
    }

    @EventHandler
    public void onWheatBreak(BlockBreakEvent event) {
        if (event.getBlock().getType() == Material.WHEAT) {
            handleProgress(event.getPlayer());
        }
    }

    @Override
    public void handleProgress(Player player) {
        if (isActive && !isCompleted) {
            this.lastPlayer = player;
            setCurrentProgress(getCurrentProgress() + 1);
        }
    }

    @Override
    protected void giveReward() {
        if (lastPlayer != null && lastPlayer.isOnline()) {
            lastPlayer.getInventory().addItem(new ItemStack(Material.EMERALD, 10));
            lastPlayer.sendMessage("");
            lastPlayer.sendMessage(ChatColor.WHITE + "❄ ═══════════════════════ ❄");
            lastPlayer.sendMessage("");
            lastPlayer.sendMessage(ChatColor.AQUA + "   🎄 С завершением квеста! 🎄");
            lastPlayer.sendMessage(ChatColor.WHITE + "Вы собрали всю необходимую пшеницу");
            lastPlayer.sendMessage("");
            lastPlayer.sendMessage(ChatColor.AQUA + "   Ваш подарок:");
            lastPlayer.sendMessage(ChatColor.WHITE + "   ❆ 10 изумрудов ❆");
            lastPlayer.sendMessage("");
            lastPlayer.sendMessage(ChatColor.WHITE + "❄ ═══════════════════════ ❄");
            lastPlayer.sendMessage("");

            lastPlayer.sendTitle(ChatColor.AQUA + "🎄 С завершением квеста! 🎄", ChatColor.WHITE + "Вы собрали всю необходимую пшеницу", 10, 20, 10);
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