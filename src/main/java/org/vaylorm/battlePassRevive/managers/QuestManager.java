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
    private static QuestStorage storage;
    private final Map<UUID, Map<String, Quest>> playerQuests;
    private final ZombieQuest zombieQuest;
    private final WheatQuest wheatQuest;
    private static QuestManager instance;

    public QuestManager(BattlePassRevive plugin, QuestStorage questStorage) {
        instance = this;
        this.plugin = plugin;
        QuestManager.storage = questStorage;
        this.playerQuests = new HashMap<>();
        this.zombieQuest = new ZombieQuest();
        this.wheatQuest = new WheatQuest();
        
        // Устанавливаем storage для квестов
        this.zombieQuest.setStorage(questStorage);
        this.wheatQuest.setStorage(questStorage);
        
        // Регистрация слушателей
        plugin.getServer().getPluginManager().registerEvents(zombieQuest, plugin);
        plugin.getServer().getPluginManager().registerEvents(wheatQuest, plugin);
    }

    private static QuestManager getInstance() {
        return instance;
    }

    public void initializePlayerQuests(Player player) {
        UUID playerId = player.getUniqueId();
        if (!playerQuests.containsKey(playerId)) {
            Map<String, Quest> quests = new HashMap<>();
            quests.put("zombie", zombieQuest);
            quests.put("wheat", wheatQuest);
            playerQuests.put(playerId, quests);
            
            plugin.getLogger().info("Инициализация квестов для игрока " + player.getName());
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
                plugin.getLogger().info("Игрок " + player.getName() + " активировал квест " + questId);
                return true;
            } else {
                plugin.getLogger().info("Игрок " + player.getName() + " не смог активировать квест " + questId + 
                    " (Активен: " + quest.isActive() + ", Завершен: " + quest.isCompleted() + ")");
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
            plugin.getLogger().info("Игрок " + player.getName() + " перезапустил квест " + questId);
        }
    }

    public static void saveQuestProgress(Player player) {
        if (storage != null) {
            Map<String, Quest> quests = getInstance().playerQuests.get(player.getUniqueId());
            if (quests != null) {
                storage.savePlayerQuests(player, quests);
            }
        }
    }
} 