package org.vaylorm.battlePassRevive;

import org.bukkit.plugin.java.JavaPlugin;
import org.vaylorm.battlePassRevive.commands.BattlePassCommand;
import org.vaylorm.battlePassRevive.quests.ZombieQuest;
import org.vaylorm.battlePassRevive.quests.WheatQuest;
import org.vaylorm.battlePassRevive.managers.QuestManager;
import org.vaylorm.battlePassRevive.storage.QuestStorage;

public final class BattlePassRevive extends JavaPlugin {
    private QuestManager questManager;
    private QuestStorage questStorage;

    @Override
    public void onEnable() {
        // Инициализация хранилища
        questStorage = new QuestStorage(this);
        questStorage.loadData();
        
        // Инициализация менеджера квестов
        questManager = new QuestManager(this, questStorage);
        
        // Регистрация слушателей
        ZombieQuest zombieQuest = new ZombieQuest();
        WheatQuest wheatQuest = new WheatQuest();
        getServer().getPluginManager().registerEvents(zombieQuest, this);
        getServer().getPluginManager().registerEvents(wheatQuest, this);
        
        // Регистрация команд
        BattlePassCommand battlePassCommand = new BattlePassCommand(questManager);
        getCommand("battlepass").setExecutor(battlePassCommand);
        getCommand("battlepass").setTabCompleter(battlePassCommand);
        
        // Автосохранение каждые 5 минут
        getServer().getScheduler().runTaskTimer(this, () -> {
            if (questStorage != null) {
                questStorage.saveData();
            }
        }, 6000L, 6000L); // 6000 тиков = 5 минут
    }

    @Override
    public void onDisable() {
        if (questStorage != null) {
            questStorage.saveData();
        }
    }
}
