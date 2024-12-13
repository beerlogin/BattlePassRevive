package org.vaylorm.battlePassRevive.storage;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.vaylorm.battlePassRevive.BattlePassRevive;
import org.vaylorm.battlePassRevive.quests.Quest;

import java.io.File;
import java.io.IOException;
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
                // Создаем базовую структуру
                questConfig = new YamlConfiguration();
                questConfig.createSection("players");
                questConfig.save(questFile);
            } catch (IOException e) {
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
        String playerPath = "players." + player.getUniqueId();
        for (Quest quest : quests.values()) {
            String questPath = playerPath + "." + quest.getQuestId();
            questConfig.set(questPath + ".progress", quest.getCurrentProgress());
            questConfig.set(questPath + ".active", quest.isActive());
        }
        saveData();
    }
} 