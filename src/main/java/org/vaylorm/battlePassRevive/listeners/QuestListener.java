package org.vaylorm.battlePassRevive.listeners;

import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.block.BlockBreakEvent;
import org.vaylorm.battlePassRevive.managers.QuestManager;

public class QuestListener implements Listener {
    private final QuestManager questManager;

    public QuestListener(QuestManager questManager) {
        this.questManager = questManager;
    }

    @EventHandler
    public void onZombieKill(EntityDeathEvent event) {
        if (event.getEntity().getType() == EntityType.ZOMBIE) {
            if (event.getEntity().getKiller() != null) {
                Player player = event.getEntity().getKiller();
                questManager.updateQuestProgress(player, "zombie", 1);
            }
        }
    }

    @EventHandler
    public void onWheatBreak(BlockBreakEvent event) {
        if (event.getBlock().getType() == Material.WHEAT) {
            Player player = event.getPlayer();
            questManager.updateQuestProgress(player, "wheat", 1);
        }
    }
} 