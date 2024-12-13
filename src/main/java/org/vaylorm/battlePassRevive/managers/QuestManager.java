package org.vaylorm.battlePassRevive.managers;

import org.bukkit.entity.Player;
import org.vaylorm.battlePassRevive.BattlePassRevive;
import org.vaylorm.battlePassRevive.quests.Quest;
import org.vaylorm.battlePassRevive.quests.WheatQuest;
import org.vaylorm.battlePassRevive.quests.ZombieQuest;
import org.vaylorm.battlePassRevive.storage.QuestStorage;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class QuestManager {
    @SuppressWarnings("unused")
    private final BattlePassRevive plugin;
    private final QuestStorage storage;
    private final Map<UUID, Map<String, Quest>> playerQuests;
    private final ZombieQuest zombieQuest;
    private final WheatQuest wheatQuest;

    public QuestManager(BattlePassRevive plugin, QuestStorage storage) {
        this.plugin = plugin;
        this.storage = storage;
        this.playerQuests = new HashMap<>();
        this.zombieQuest = new ZombieQuest();
        this.wheatQuest = new WheatQuest();
        
        // Регистрация слушателей
        plugin.getServer().getPluginManager().registerEvents(zombieQuest, plugin);
        plugin.getServer().getPluginManager().registerEvents(wheatQuest, plugin);
    }

    public void initializePlayerQuests(Player player) {
        UUID playerId = player.getUniqueId();
        if (!playerQuests.containsKey(playerId)) {
            Map<String, Quest> quests = new HashMap<>();
            quests.put("zombie", zombieQuest);
            quests.put("wheat", wheatQuest);
            playerQuests.put(playerId, quests);
            
            // Загрузка сохраненного прогресса
            storage.loadPlayerQuests(player, quests);
        }
    }

    public boolean activateQuest(Player player, String questId) {
        initializePlayerQuests(player);
        UUID playerId = player.getUniqueId();
        Map<String, Quest> quests = playerQuests.get(playerId);
        if (quests != null && quests.containsKey(questId)) {
            Quest quest = quests.get(questId);
            if (!quest.isActive() && !quest.isCompleted()) {
                quest.setActive(true);
                storage.savePlayerQuests(player, quests);
                return true;
            }
        }
        return false;
    }

    public void updateQuestProgress(Player player, String questId, int progress) {
        initializePlayerQuests(player);
        UUID playerId = player.getUniqueId();
        Map<String, Quest> quests = playerQuests.get(playerId);
        if (quests != null && quests.containsKey(questId)) {
            Quest quest = quests.get(questId);
            if (quest.isActive() && !quest.isCompleted()) {
                quest.setCurrentProgress(quest.getCurrentProgress() + progress);
                storage.savePlayerQuests(player, quests);
            }
        }
    }

    public Quest getQuest(Player player, String questId) {
        initializePlayerQuests(player);
        Map<String, Quest> quests = playerQuests.get(player.getUniqueId());
        return quests != null ? quests.get(questId) : null;
    }

    public void restartQuest(Player player, String questId) {
        initializePlayerQuests(player);
        Map<String, Quest> quests = playerQuests.get(player.getUniqueId());
        if (quests != null && quests.containsKey(questId)) {
            Quest quest = quests.get(questId);
            quest.resetProgress();
            storage.savePlayerQuests(player, quests);
        }
    }
} 