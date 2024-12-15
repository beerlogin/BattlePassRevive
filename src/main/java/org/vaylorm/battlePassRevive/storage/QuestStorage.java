package org.vaylorm.battlePassRevive.storage;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.vaylorm.battlePassRevive.BattlePassRevive;
import org.vaylorm.battlePassRevive.quests.Quest;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class QuestStorage {
    private final BattlePassRevive plugin;
    private File questFile;
    private FileConfiguration questConfig;

    public QuestStorage(BattlePassRevive plugin) {
        this.plugin = plugin;
        setupStorage();
    }

    private void setupStorage() {
        if (!plugin.getDataFolder().exists()) {
            plugin.getDataFolder().mkdir();
        }
        
        questFile = new File(plugin.getDataFolder(), "quests.yml");
        if (!questFile.exists()) {
            try {
                questFile.createNewFile();
                questConfig = YamlConfiguration.loadConfiguration(questFile);
                questConfig.createSection("players");
                questConfig.createSection("active_quests");
                questConfig.save(questFile);
            } catch (IOException e) {
                plugin.getLogger().severe("Не удалось создать файл quests.yml: " + e.getMessage());
                e.printStackTrace();
            }
        }
        questConfig = YamlConfiguration.loadConfiguration(questFile);
    }

    public void loadData() {
        if (!questFile.exists()) {
            plugin.saveResource("quests.yml", false);
        }
        questConfig = YamlConfiguration.loadConfiguration(questFile);
    }

    public void saveData() {
        try {
            questConfig.save(questFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void loadPlayerQuests(Player player, Map<String, Quest> quests) {
        String playerPath = "players." + player.getUniqueId();
        for (Quest quest : quests.values()) {
            String questPath = playerPath + "." + quest.getQuestId();
            quest.setCurrentProgress(questConfig.getInt(questPath + ".progress", 0));
            quest.setActive(questConfig.getBoolean(questPath + ".active", false));
        }
    }

    public void savePlayerQuests(Player player, Map<String, Quest> quests) {
        if (player == null || quests == null) {
            plugin.getLogger().severe("Попытка сохранить null данные");
            return;
        }
        String playerPath = "players." + player.getUniqueId();
        for (Quest quest : quests.values()) {
            String questPath = playerPath + "." + quest.getQuestId();
            questConfig.set(questPath + ".progress", quest.getCurrentProgress());
            questConfig.set(questPath + ".active", quest.isActive());
            questConfig.set(questPath + ".completed", quest.isCompleted());
        }
        try {
            questConfig.save(questFile);
            plugin.getLogger().info("Сохранен прогресс квестов для игрока " + player.getName());
        } catch (IOException e) {
            plugin.getLogger().severe("Не удалось сохранить прогресс в quests.yml для игрока " + player.getName() + ": " + e.getMessage());
            e.printStackTrace();
        }
    }

    public boolean isQuestGloballyActive(String questId) {
        if (questId == null || (!questId.equals("zombie_quest") && !questId.equals("wheat_quest"))) {
            return false;
        }
        return questConfig.getBoolean("active_quests." + questId + ".active", false);
    }

    public void setQuestGloballyActive(String questId, boolean active) {
        if (questId == null || (!questId.equals("zombie_quest") && !questId.equals("wheat_quest"))) {
            plugin.getLogger().warning("Попытка активировать неверный квест: " + questId);
            return;
        }
        questConfig.set("active_quests." + questId + ".active", active);
        saveData();
        plugin.getLogger().info("Квест " + questId + " " + (active ? "активирован" : "деактивирован") + " глобально");
    }

    public void addQuestCompleter(String questId, Player player) {
        if (questId == null || player == null || 
            (!questId.equals("zombie_quest") && !questId.equals("wheat_quest"))) {
            plugin.getLogger().warning("Попытка добавить неверного игрока или квест: " + player + ", " + questId);
            return;
        }
        List<String> completers = questConfig.getStringList("active_quests." + questId + ".completers");
        if (!completers.contains(player.getName())) {
            completers.add(player.getName());
            questConfig.set("active_quests." + questId + ".completers", completers);
            saveData();
            plugin.getLogger().info("Игрок " + player.getName() + " добавлен в список выполнивших квест " + questId);
        }
    }

    public boolean hasPlayerCompletedGlobalQuest(String questId, Player player) {
        if (questId == null || player == null || 
            (!questId.equals("zombie_quest") && !questId.equals("wheat_quest"))) {
            return false;
        }
        List<String> completers = questConfig.getStringList("active_quests." + questId + ".completers");
        return completers.contains(player.getName());
    }

    public void removeQuestCompleter(String questId, String playerName) {
        if (questId == null || playerName == null || playerName.isEmpty() || 
            (!questId.equals("zombie_quest") && !questId.equals("wheat_quest"))) {
            plugin.getLogger().warning("Попытка удалить неверного игрока или квест: " + playerName + ", " + questId);
            return;
        }
        List<String> completers = questConfig.getStringList("active_quests." + questId + ".completers");
        if (completers.remove(playerName)) {
            questConfig.set("active_quests." + questId + ".completers", completers);
            saveData();
            plugin.getLogger().info("Игрок " + playerName + " удален из списка выполнивших квест " + questId);
        }
    }

    public void clearQuestCompleters(String questId) {
        if (questId == null || (!questId.equals("zombie_quest") && !questId.equals("wheat_quest"))) {
            plugin.getLogger().warning("Попытка очистить список неверного квеста: " + questId);
            return;
        }
        questConfig.set("active_quests." + questId + ".completers", new ArrayList<String>());
        saveData();
        plugin.getLogger().info("Список выполнивших квест " + questId + " очищен");
    }

    public List<String> getQuestCompleters(String questId) {
        if (questId == null || (!questId.equals("zombie_quest") && !questId.equals("wheat_quest"))) {
            return new ArrayList<>();
        }
        return questConfig.getStringList("active_quests." + questId + ".completers");
    }

    public BattlePassRevive getPlugin() {
        return plugin;
    }

    public void addPendingReward(Player player, String questId, String rewardType) {
        String playerPath = "players." + player.getUniqueId() + ".pending_rewards." + questId;
        List<String> rewards = questConfig.getStringList(playerPath);
        if (!rewards.contains(rewardType)) {
            rewards.add(rewardType);
            questConfig.set(playerPath, rewards);
            saveData();
        }
    }

    public List<String> getPendingRewards(Player player) {
        String playerPath = "players." + player.getUniqueId() + ".pending_rewards";
        if (questConfig.getConfigurationSection(playerPath) == null) {
            return new ArrayList<>();
        }
        return new ArrayList<>(questConfig.getConfigurationSection(playerPath).getKeys(false));
    }

    public List<String> getQuestRewards(Player player, String questId) {
        String playerPath = "players." + player.getUniqueId() + ".pending_rewards." + questId;
        return questConfig.getStringList(playerPath);
    }

    public void removeReward(Player player, String questId) {
        String playerPath = "players." + player.getUniqueId() + ".pending_rewards." + questId;
        questConfig.set(playerPath, null);
        saveData();
    }
} 