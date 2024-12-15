package org.vaylorm.battlePassRevive.commands;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.Material;
import org.vaylorm.battlePassRevive.managers.QuestManager;
import org.vaylorm.battlePassRevive.quests.Quest;
import org.vaylorm.battlePassRevive.storage.QuestStorage;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.hover.content.Text;

public class BattlePassCommand implements CommandExecutor, TabCompleter {
    private final QuestManager questManager;
    private final QuestStorage storage;

    public BattlePassCommand(QuestManager questManager, QuestStorage storage) {
        this.questManager = questManager;
        this.storage = storage;
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
                String activateQuestId = args[1].toLowerCase();
                if (questManager.activateQuest(player, activateQuestId)) {
                    player.sendMessage(ChatColor.GREEN + "Квест успешно активирован!");
                } else {
                    player.sendMessage(ChatColor.RED + "Невозможно актировать квест! Возможно он уже активен или завершен.");
                }
                break;

            case "progress":
                if (args.length < 2) {
                    player.sendMessage(ChatColor.RED + "Использование: /bp progress <zombie/wheat>");
                    return true;
                }
                String progressQuestId = args[1].toLowerCase();
                if (!progressQuestId.equals("zombie") && !progressQuestId.equals("wheat")) {
                    player.sendMessage(ChatColor.RED + "Неверный ID квеста! Используйте zombie или wheat");
                    return true;
                }
                Quest quest = questManager.getQuest(player, progressQuestId);
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

            case "global":
                if (!sender.hasPermission("battlepass.admin")) {
                    sender.sendMessage(ChatColor.RED + "У вас нет прав для использования этой команды!");
                    return true;
                }
                if (args.length < 3) {
                    sender.sendMessage(ChatColor.RED + "Использование: /bp global <activate/deactivate> <zombie/wheat>");
                    return true;
                }
                
                String action = args[1].toLowerCase();
                String globalQuestId = args[2].toLowerCase();
                
                if (!globalQuestId.equals("zombie") && !globalQuestId.equals("wheat")) {
                    sender.sendMessage(ChatColor.RED + "Неверный ID квеста! Используйте zombie или wheat");
                    return true;
                }
                
                switch (action) {
                    case "activate":
                        storage.setQuestGloballyActive(globalQuestId + "_quest", true);
                        sender.sendMessage(ChatColor.GREEN + "Квест " + globalQuestId + " активирован глобально!");
                        break;
                    case "deactivate":
                        storage.setQuestGloballyActive(globalQuestId + "_quest", false);
                        sender.sendMessage(ChatColor.GREEN + "Квест " + globalQuestId + " деактивирован глобально!");
                        break;
                    default:
                        sender.sendMessage(ChatColor.RED + "Неверное действие! Используйте activate или deactivate");
                }
                break;

            case "completers":
                if (!sender.hasPermission("battlepass.admin")) {
                    sender.sendMessage(ChatColor.RED + "У вас нет прав для использования этой команды!");
                    return true;
                }
                if (args.length < 2) {
                    sender.sendMessage(ChatColor.RED + "Использование: /bp completers <add/remove/clear/list> [quest_id] [player]");
                    return true;
                }

                String completerAction = args[1].toLowerCase();
                
                if (args.length >= 3 && !completerAction.equals("help")) {
                    String questId = args[2].toLowerCase();
                    if (!questId.equals("zombie") && !questId.equals("wheat")) {
                        sender.sendMessage(ChatColor.RED + "Неверный ID квеста! Используйте zombie или wheat");
                        return true;
                    }
                }
                
                switch (completerAction) {
                    case "add":
                        if (args.length < 4) {
                            sender.sendMessage(ChatColor.RED + "Использование: /bp completers add <zombie/wheat> <player>");
                            return true;
                        }
                        String questToAdd = args[2].toLowerCase() + "_quest";
                        Player playerToAdd = Bukkit.getPlayer(args[3]);
                        if (playerToAdd == null) {
                            sender.sendMessage(ChatColor.RED + "Игрок не найден!");
                            return true;
                        }
                        storage.addQuestCompleter(questToAdd, playerToAdd);
                        sender.sendMessage(ChatColor.GREEN + "Игрок " + playerToAdd.getName() + " добавлен в список выполнивших квест " + args[2]);
                        break;

                    case "remove":
                        if (args.length < 4) {
                            sender.sendMessage(ChatColor.RED + "Использование: /bp completers remove <zombie/wheat> <player>");
                            return true;
                        }
                        String questToRemove = args[2].toLowerCase() + "_quest";
                        String playerToRemove = args[3];
                        storage.removeQuestCompleter(questToRemove, playerToRemove);
                        sender.sendMessage(ChatColor.GREEN + "Игрок " + playerToRemove + " удален из списка выполнивших квест " + args[2]);
                        break;

                    case "clear":
                        if (args.length < 3) {
                            sender.sendMessage(ChatColor.RED + "Использование: /bp completers clear <zombie/wheat>");
                            return true;
                        }
                        String questToClear = args[2].toLowerCase() + "_quest";
                        storage.clearQuestCompleters(questToClear);
                        sender.sendMessage(ChatColor.GREEN + "Список выполнивших квест " + args[2] + " очищен!");
                        break;

                    case "list":
                        if (args.length < 3) {
                            sender.sendMessage(ChatColor.RED + "Использование: /bp completers list <zombie/wheat>");
                            return true;
                        }
                        String questToList = args[2].toLowerCase() + "_quest";
                        List<String> completers = storage.getQuestCompleters(questToList);
                        if (completers.isEmpty()) {
                            sender.sendMessage(ChatColor.YELLOW + "Никто еще не выполнил этот квест!");
                        } else {
                            sender.sendMessage(ChatColor.GREEN + "Список выполнивших квест " + args[2] + ":");
                            for (String completer : completers) {
                                sender.sendMessage(ChatColor.GRAY + "- " + ChatColor.WHITE + completer);
                            }
                        }
                        break;

                    default:
                        sender.sendMessage(ChatColor.RED + "Неверное действие! Используйте add, remove, clear или list");
                }
                break;

            case "claim":
                List<String> pendingQuests = storage.getPendingRewards(player);
                if (pendingQuests.isEmpty()) {
                    player.sendMessage(ChatColor.RED + "У вас нет доступных наград!");
                    return true;
                }

                player.sendMessage(ChatColor.GREEN + "Доступные награды:");
                for (String questId : pendingQuests) {
                    List<String> rewards = storage.getQuestRewards(player, questId);
                    for (String reward : rewards) {
                        String[] parts = reward.split(":");
                        String type = parts[0];
                        int amount = Integer.parseInt(parts[1]);
                        
                        // Создаем кликабельное сообщение
                        TextComponent message = new TextComponent("  • ");
                        message.setColor(net.md_5.bungee.api.ChatColor.GRAY);
                        
                        TextComponent rewardText = new TextComponent(amount + "x " + type);
                        rewardText.setColor(net.md_5.bungee.api.ChatColor.GOLD);
                        rewardText.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/bp claimreward " + questId));
                        rewardText.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, 
                            new Text("Нажмите, чтобы получить награду!")));
                        
                        message.addExtra(rewardText);
                        player.spigot().sendMessage(message);
                    }
                }
                break;

            case "claimreward":
                if (args.length < 2) {
                    player.sendMessage(ChatColor.RED + "Использование: /bp claimreward <id_квеста>");
                    return true;
                }
                String questToClaim = args[1];
                if (!questToClaim.equals("zombie_quest") && !questToClaim.equals("wheat_quest")) {
                    player.sendMessage(ChatColor.RED + "Неверный ID квеста!");
                    return true;
                }
                List<String> rewards = storage.getQuestRewards(player, questToClaim);
                if (rewards.isEmpty()) {
                    player.sendMessage(ChatColor.RED + "Награда не найдена!");
                    return true;
                }

                for (String reward : rewards) {
                    String[] parts = reward.split(":");
                    Material material = Material.valueOf(parts[0]);
                    int amount = Integer.parseInt(parts[1]);
                    
                    ItemStack item = new ItemStack(material, amount);
                    HashMap<Integer, ItemStack> leftover = player.getInventory().addItem(item);
                    
                    if (!leftover.isEmpty()) {
                        player.getWorld().dropItemNaturally(player.getLocation(), item);
                        player.sendMessage(ChatColor.YELLOW + "Инвентарь полон! Предметы выброшены на землю.");
                    } else {
                        player.sendMessage(ChatColor.GREEN + "Вы получили награду: " + amount + "x " + material.name());
                    }
                }
                
                storage.removeReward(player, questToClaim);
                player.playSound(player.getLocation(), "entity.player.levelup", 1.0f, 1.0f);
                break;

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
            completions.addAll(Arrays.asList("help", "progress", "quests", "activate", "restart", "claim"));
            // Добавляем админские команды
            if (sender.hasPermission("battlepass.admin")) {
                completions.addAll(Arrays.asList("setprogress", "global", "completers"));
            }
            return filterCompletions(completions, args[0]);
        }
        
        if (sender.hasPermission("battlepass.admin")) {
            if (args[0].equalsIgnoreCase("global") && args.length == 2) {
                completions.addAll(Arrays.asList("activate", "deactivate"));
                return filterCompletions(completions, args[1]);
            }
            
            if (args[0].equalsIgnoreCase("global") && args.length == 3) {
                completions.addAll(Arrays.asList("zombie", "wheat"));
                return filterCompletions(completions, args[2]);
            }
            
            if (args[0].equalsIgnoreCase("completers") && args.length == 2) {
                completions.addAll(Arrays.asList("add", "remove", "clear", "list"));
                return filterCompletions(completions, args[1]);
            }
            
            if (args[0].equalsIgnoreCase("completers") && args.length == 3) {
                completions.addAll(Arrays.asList("zombie", "wheat"));
                return filterCompletions(completions, args[2]);
            }
            
            if (args[0].equalsIgnoreCase("completers") && args.length == 4 && 
                (args[1].equalsIgnoreCase("add") || args[1].equalsIgnoreCase("remove"))) {
                Bukkit.getOnlinePlayers().forEach(player -> completions.add(player.getName()));
                return filterCompletions(completions, args[3]);
            }
        }
        
        // Подсказки для обычных команд
        if (args.length == 2) {
            if (args[0].equalsIgnoreCase("activate") || 
                args[0].equalsIgnoreCase("progress") ||
                args[0].equalsIgnoreCase("restart")) {
                completions.addAll(Arrays.asList("zombie", "wheat"));
                return filterCompletions(completions, args[1]);
            }
            // Подсказки для setprogress
            if (args[0].equalsIgnoreCase("setprogress") && sender.hasPermission("battlepass.admin")) {
                Bukkit.getOnlinePlayers().forEach(player -> completions.add(player.getName()));
                return filterCompletions(completions, args[1]);
            }
        }
        
        // Подсказки для claim
        if (args[0].equalsIgnoreCase("claim") && args.length == 2) {
            List<String> pendingQuests = storage.getPendingRewards((Player)sender);
            return filterCompletions(pendingQuests, args[1]);
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
        player.sendMessage(ChatColor.GREEN + "❄ ════════ " + ChatColor.RED + "Новогодний BattlePass" + ChatColor.GREEN + " ════════ ❄");
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
        player.sendMessage(ChatColor.RED + "💎 " + ChatColor.YELLOW + "/bp claim " + 
            ChatColor.WHITE + "- Посмотреть доступные награды");
        
        if (player.hasPermission("battlepass.admin")) {
            player.sendMessage("");
            player.sendMessage(ChatColor.RED + "❄ Админские команды:");
            player.sendMessage(ChatColor.RED + "⚡ " + ChatColor.RED + "/bp setprogress <игрок> <zombie/wheat> <количество> " + 
                ChatColor.WHITE + "- Установить прогресс квеста");
            player.sendMessage(ChatColor.RED + "⚡ " + ChatColor.RED + "/bp global <activate/deactivate> <zombie/wheat> " + 
                ChatColor.WHITE + "- Управление глобальными квестами");
            player.sendMessage(ChatColor.RED + "⚡ " + ChatColor.RED + "/bp completers <add/remove/clear/list> <zombie/wheat> [player] " + 
                ChatColor.WHITE + "- Управление списком выполнивших");
        }
        
        player.sendMessage("");
        player.sendMessage(ChatColor.GREEN + "❄ ═════════════════════════════════════════ ❄");
    }

    private void showAvailableQuests(Player player) {
        Quest zombieQuest = questManager.getQuest(player, "zombie");
        Quest wheatQuest = questManager.getQuest(player, "wheat");

        // Проверяем, выполнены ли оа квеста
        if ((zombieQuest != null && zombieQuest.isCompleted()) && 
            (wheatQuest != null && wheatQuest.isCompleted())) {
            player.sendMessage(ChatColor.GREEN + "❄ ═════════ " + ChatColor.RED + "Новогодние Квесты" + ChatColor.GREEN + " ═════════ ❄");
            player.sendMessage("");
            player.sendMessage(ChatColor.YELLOW + "Все квесты уже завершены!");
            player.sendMessage("");
            player.sendMessage(ChatColor.GREEN + "❄ ══════════════════════════════ ❄");
            return;
        }

        player.sendMessage(ChatColor.GREEN + "❄ ���════════ " + ChatColor.RED + "Новогодние Квесты" + ChatColor.GREEN + " ═══════════ ❄");
        player.sendMessage("");
        
        // Показы��ем только невыполненные квесты
        if (zombieQuest == null || !zombieQuest.isCompleted()) {
            player.sendMessage(ChatColor.RED + "🧟 " + ChatColor.YELLOW + "Охота на Снежных Зомби: " + 
                ChatColor.WHITE + "Победите 60 зомби в снежную ночь");
            if (zombieQuest != null) {
                String status = getQuestStatus(zombieQuest);
                player.sendMessage(ChatColor.RED + "☃ " + ChatColor.YELLOW + "Прогресс охоты на зомби: " + status);
            }
            player.sendMessage("");
        }
        
        if (wheatQuest == null || !wheatQuest.isCompleted()) {
            player.sendMessage(ChatColor.RED + "🌾 " + ChatColor.YELLOW + "Морозостойкая Пшеница: " + 
                ChatColor.WHITE + "Соберите 1000 пшеницы в зимнюю стужу");
            if (wheatQuest != null) {
                String status = getQuestStatus(wheatQuest);
                player.sendMessage(ChatColor.RED + "❄ " + ChatColor.YELLOW + "Прогресс сбора пшеницы: " + status);
            }
            player.sendMessage("");
        }
        
        player.sendMessage(ChatColor.GREEN + "❄ ══════════════════════════════ ❄");
    }

    private String getQuestStatus(Quest quest) {
        if (quest.isCompleted()) {
            return ChatColor.GREEN + "Выполнен";
        } else if (quest.isActive()) {
            return ChatColor.GOLD + "А��тивен " + ChatColor.WHITE + "(" + 
                   quest.getCurrentProgress() + "/" + quest.getTargetProgress() + ")";
        } else {
            return ChatColor.RED + "Не активирован";
        }
    }
} 