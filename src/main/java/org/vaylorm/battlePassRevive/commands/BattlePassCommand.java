package org.vaylorm.battlePassRevive.commands;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.vaylorm.battlePassRevive.managers.QuestManager;
import org.vaylorm.battlePassRevive.quests.Quest;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class BattlePassCommand implements CommandExecutor, TabCompleter {
    private final QuestManager questManager;

    public BattlePassCommand(QuestManager questManager) {
        this.questManager = questManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "Эта команда только для игроков!");
            return true;
        }

        Player player = (Player) sender;

        if (args.length < 1) {
            sendHelp(player);
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "help":
                sendHelp(player);
                break;

            case "activate":
                if (args.length < 2) {
                    player.sendMessage(ChatColor.RED + "Использование: /bp activate <zombie/wheat>");
                    return true;
                }
                String questId = args[1].toLowerCase();
                if (questManager.activateQuest(player, questId)) {
                    player.sendMessage(ChatColor.GREEN + "Квест успешно активирован!");
                } else {
                    player.sendMessage(ChatColor.RED + "Невозможно активировать квест! Возможно он уже активен или завершен.");
                }
                break;

            case "progress":
                if (args.length < 2) {
                    player.sendMessage(ChatColor.RED + "Использование: /bp progress <zombie/wheat>");
                    return true;
                }
                Quest quest = questManager.getQuest(player, args[1].toLowerCase());
                if (quest != null) {
                    player.sendMessage(ChatColor.GOLD + "Прогресс квеста: " + 
                        ChatColor.WHITE + quest.getCurrentProgress() + "/" + quest.getTargetProgress());
                    if (quest.isCompleted()) {
                        player.sendMessage(ChatColor.GREEN + "Квест выполнен!");
                    } else if (quest.isActive()) {
                        player.sendMessage(ChatColor.YELLOW + "Квест активен");
                    } else {
                        player.sendMessage(ChatColor.RED + "Квест не активирован");
                    }
                } else {
                    player.sendMessage(ChatColor.RED + "Квест не найден!");
                }
                break;

            case "quests":
                showAvailableQuests(player);
                break;

            case "setprogress":
                if (!sender.hasPermission("battlepass.admin")) {
                    sender.sendMessage(ChatColor.RED + "У вас нет прав для использования этой команды!");
                    return true;
                }
                if (args.length < 4) {
                    sender.sendMessage(ChatColor.RED + "Использование: /bp setprogress <игрок> <zombie/wheat> <количество>");
                    return true;
                }
                
                Player targetPlayer = Bukkit.getPlayer(args[1]);
                if (targetPlayer == null) {
                    sender.sendMessage(ChatColor.RED + "Игрок не найден!");
                    return true;
                }
                
                String targetQuestId = args[2].toLowerCase();
                int newProgress;
                try {
                    newProgress = Integer.parseInt(args[3]);
                } catch (NumberFormatException e) {
                    sender.sendMessage(ChatColor.RED + "Неверный формат числа!");
                    return true;
                }
                
                Quest targetQuest = questManager.getQuest(targetPlayer, targetQuestId);
                if (targetQuest == null) {
                    sender.sendMessage(ChatColor.RED + "Квест не найден!");
                    return true;
                }
                
                targetQuest.setCurrentProgress(newProgress);
                sender.sendMessage(ChatColor.GREEN + "Прогресс квеста установлен на " + newProgress);
                break;

            case "restart":
                if (args.length < 2) {
                    sender.sendMessage("§cИспользование: /battlepass restart <quest_id>");
                    return true;
                }
                
                questManager.restartQuest(player, args[1]);
                sender.sendMessage("§aКвест " + args[1] + " был успешно перезапущен!");
                return true;

            default:
                sendHelp(player);
                break;
        }

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();
        
        if (args.length == 1) {
            // Основные подкоманды
            completions.addAll(Arrays.asList("help", "progress", "quests", "activate", "restart"));
            return filterCompletions(completions, args[0]);
        }
        
        // Добавляем подсказки для команд, требующих id квеста
        if (args.length == 2) {
            if (args[0].equalsIgnoreCase("activate") || 
                args[0].equalsIgnoreCase("progress") ||
                args[0].equalsIgnoreCase("restart")) {
                completions.addAll(Arrays.asList("zombie", "wheat"));
                return filterCompletions(completions, args[1]);
            }
        }
        
        return completions;
    }
    
    // Вспомогательный метод для фильтрации предложений
    private List<String> filterCompletions(List<String> completions, String partial) {
        String lowercasePartial = partial.toLowerCase();
        return completions.stream()
                .filter(str -> str.toLowerCase().startsWith(lowercasePartial))
                .collect(Collectors.toList());
    }

    private void sendHelp(Player player) {
        player.sendMessage(ChatColor.GREEN + "❄ ═══ " + ChatColor.RED + "Новогодний BattlePass" + ChatColor.GREEN + " ═══ ❄");
        player.sendMessage("");
        player.sendMessage(ChatColor.RED + "☃ " + ChatColor.YELLOW + "/bp help " + 
            ChatColor.WHITE + "- Показать список новогодних команд");
        player.sendMessage(ChatColor.RED + "🎄 " + ChatColor.YELLOW + "/bp quests " + 
            ChatColor.WHITE + "- Посмотреть праздничные квесты");
        player.sendMessage(ChatColor.RED + "🎁 " + ChatColor.YELLOW + "/bp activate <zombie/wheat> " + 
            ChatColor.WHITE + "- Начать выполнение квеста");
        player.sendMessage(ChatColor.RED + "⭐ " + ChatColor.YELLOW + "/bp progress <zombie/wheat> " + 
            ChatColor.WHITE + "- Узнать свой прогресс");
        player.sendMessage(ChatColor.RED + "🔔 " + ChatColor.YELLOW + "/bp restart <zombie/wheat> " + 
            ChatColor.WHITE + "- Перезапустить квест");
        if (player.hasPermission("battlepass.admin")) {
            player.sendMessage(ChatColor.RED + "❄ " + ChatColor.RED + "/bp setprogress <игрок> <zombie/wheat> <количество> " + 
                ChatColor.WHITE + "- Установить прогресс квеста");
        }
        player.sendMessage("");
        player.sendMessage(ChatColor.GREEN + "❄ ═══════════════════════════ ❄");
    }

    private void showAvailableQuests(Player player) {
        player.sendMessage(ChatColor.GOLD + "=== Доступные квесты ===");
        player.sendMessage(ChatColor.YELLOW + "Зомби квест: " + ChatColor.WHITE + "Убейте 10 зомби");
        player.sendMessage(ChatColor.YELLOW + "Пшеничный квест: " + ChatColor.WHITE + "Соберите 1000 пшеницы");
        
        // Показываем статус каждого квеста
        Quest zombieQuest = questManager.getQuest(player, "zombie");
        Quest wheatQuest = questManager.getQuest(player, "wheat");
        
        if (zombieQuest != null) {
            String status = getQuestStatus(zombieQuest);
            player.sendMessage(ChatColor.YELLOW + "Статус зомби квеста: " + status);
        }
        
        if (wheatQuest != null) {
            String status = getQuestStatus(wheatQuest);
            player.sendMessage(ChatColor.YELLOW + "Статус пшеничного квеста: " + status);
        }
    }

    private String getQuestStatus(Quest quest) {
        if (quest.isCompleted()) {
            return ChatColor.GREEN + "Выполнен";
        } else if (quest.isActive()) {
            return ChatColor.GOLD + "Активен " + ChatColor.WHITE + "(" + 
                   quest.getCurrentProgress() + "/" + quest.getTargetProgress() + ")";
        } else {
            return ChatColor.RED + "Не активирован";
        }
    }
} 