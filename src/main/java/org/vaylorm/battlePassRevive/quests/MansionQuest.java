package org.vaylorm.battlePassRevive.quests;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.generator.structure.Structure;

public class MansionQuest extends Quest {
    private Location mansionCachedLocation;

    public MansionQuest() {
        super("mansion_quest", 1);
    }

    @EventHandler
    public void onMove(PlayerMoveEvent event) {
        if (storage.isQuestGloballyActive(questId)) {
            // активируем квест
            setActive(true);
            setCurrentProgress(0);
        }
        
        if (storage == null) {
            return;
        }

        if (mansionCachedLocation == null) {
            cacheNearbyMansion(event.getPlayer());
        }

        mansionCachedLocation.setY(event.getPlayer().getLocation().getY());

        if (mansionCachedLocation.distance(event.getFrom()) <= 50) {
            handleProgress(event.getPlayer());
        }
    }

    @Override
    public void handleProgress(Player player) {
        if (isActive && !isCompleted) {
            this.lastPlayer = player;
            setCurrentProgress(currentProgress + 1);
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

        return isActive && !isCompleted;
    }

    // Чтобы не нагружать сервер постоянным использованием World.locateNearestStructure, плагин обновляет позицию самого близкого особняка каждые 5 минут.
    // Результат можно использовать в любое время через this.cachedMansionLocation.
    public void cacheNearbyMansion(Player player) {
        Bukkit.getScheduler().runTaskTimer(
                Bukkit.getPluginManager().getPlugin("BattlePassRevive"),
                task -> this.mansionCachedLocation = player.getWorld().locateNearestStructure(player.getLocation(), Structure.MANSION, 640, false).getLocation(),
                0L,
                300L
        );
    }
} 