package org.vaylorm.battlePassRevive;

import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.vaylorm.battlePassRevive.commands.BattlePassCommand;
import org.vaylorm.battlePassRevive.quests.ZombieQuest;
import org.vaylorm.battlePassRevive.quests.WheatQuest;
import org.vaylorm.battlePassRevive.managers.QuestManager;
import org.vaylorm.battlePassRevive.storage.QuestStorage;

public final class BattlePassRevive extends JavaPlugin implements Listener {
    private QuestManager questManager;
    private QuestStorage questStorage;

    @Override
    public void onEnable() {
        // Инициализация хранилища
        questStorage = new QuestStorage(this);
        questStorage.loadData();
        
        // Инициализация менеджера квестов
        questManager = new QuestManager(this, questStorage);
        
        // Создание квестов
        ZombieQuest zombieQuest = new ZombieQuest();
        WheatQuest wheatQuest = new WheatQuest();
        
        // Установка storage для квестов
        zombieQuest.setStorage(questStorage);
        wheatQuest.setStorage(questStorage);
        
        // Регистрация слушателей
        getServer().getPluginManager().registerEvents(zombieQuest, this);
        getServer().getPluginManager().registerEvents(wheatQuest, this);
        getServer().getPluginManager().registerEvents(this, this); // Регистрируем основной класс как слушатель
        
        // Регистрация команд
        BattlePassCommand battlePassCommand = new BattlePassCommand(questManager, questStorage);
        getCommand("battlepass").setExecutor(battlePassCommand);
        getCommand("battlepass").setTabCompleter(battlePassCommand);
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        questManager.checkAvailableQuests(event.getPlayer());
    }

    @Override
    public void onDisable() {
        if (questStorage != null) {
            questStorage.saveData();
        }
    }
}
