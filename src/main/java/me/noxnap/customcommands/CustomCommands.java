package me.noxnap.customcommands;

import net.md_5.bungee.api.plugin.Plugin;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.ConfigurationProvider;
import net.md_5.bungee.config.YamlConfiguration;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.plugin.Command;
import java.util.List;
import java.util.ArrayList;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.api.ChatColor;

public class CustomCommands extends Plugin {

    private Configuration config;
    private HashMap<String, String> messageContent = new HashMap<>();
    private HashMap<String, String> commands = new HashMap<>();
    private HashMap<String, String> permission = new HashMap<>();
    private List<Command> registeredCommands = new ArrayList<>();

    @Override
    public void onEnable() {
        try {
            if (!getDataFolder().exists()) {
                getDataFolder().mkdir();
            }
            File configFile = new File(getDataFolder(), "commands.yml");
            if (!configFile.exists()) {
                configFile.createNewFile();
            }
            config = ConfigurationProvider.getProvider(YamlConfiguration.class).load(configFile);
            // populate the command and permission hashmap
            for (String command : config.getKeys()) {
                commands.put(command, config.getString(command + ".slash-command"));
                permission.put(command, config.getString(command + ".permission"));
                messageContent.put(command, config.getString(command + ".message-content"));
                String message = config.getString(command + ".message-content");
                message = ChatColor.translateAlternateColorCodes('&', message);
                messageContent.put(command, message);
            }
            // Register commands
            for (Map.Entry<String, String> entry : commands.entrySet()) {
                String cmd = entry.getKey();
                String perm = permission.get(cmd);
                String message = messageContent.get(cmd);
                if (perm == null || perm.isEmpty()) {
                    perm = "customcmds.use";
                }
                CustomCommand customCommand = new CustomCommand(cmd, perm, message);
                getProxy().getPluginManager().registerCommand(this, customCommand);
                getProxy().getPluginManager().registerCommand(this, new ReloadCommand(this));
                registeredCommands.add(customCommand);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public class CustomCommand extends Command {

        private final String message;

        public CustomCommand(String command, String permission, String message) {
            super(command, permission);
            this.message = message;
        }

        @Override
        public void execute(CommandSender sender, String[] args) {
            String cmd = this.getName();
            String message = messageContent.get(cmd);
            message = ChatColor.translateAlternateColorCodes('&', message);
            sender.sendMessage(new TextComponent(message));
        }



    }
    public class ReloadCommand extends Command {
        private final CustomCommands plugin;

        public ReloadCommand(CustomCommands plugin) {
            super("CustomCmd reload", "customcmds.reload");
            this.plugin = plugin;
        }

        @Override
        public void execute(CommandSender sender, String[] args) {
            // Reload the configuration file
            try {
                File configFile = new File(plugin.getDataFolder(), "commands.yml");
                plugin.config = ConfigurationProvider.getProvider(YamlConfiguration.class).load(configFile);
            } catch (IOException e) {
                sender.sendMessage(new TextComponent("An error occurred while reloading the config file."));
                e.printStackTrace();
                return;
            }

            // Unregister all the commands
            for (Command command : plugin.registeredCommands) {
                plugin.getProxy().getPluginManager().unregisterCommand(command);
            }
            plugin.commands.clear();
            plugin.permission.clear();
            plugin.messageContent.clear();
            plugin.registeredCommands.clear();

            // Populate the command, permission, and messageContent hashmaps
            for (String command : plugin.config.getKeys()) {
                plugin.commands.put(command, plugin.config.getString(command + ".slash-command"));
                plugin.permission.put(command, plugin.config.getString(command + ".permission"));
                plugin.messageContent.put(command, plugin.config.getString(command + ".message-content"));
            }

            // Re-register the commands
            for (Map.Entry<String, String> entry : plugin.commands.entrySet()) {
                String cmd = entry.getKey();
                String perm = plugin.permission.get(cmd);
                String message = plugin.messageContent.get(cmd);
                if (perm == null || perm.isEmpty()) {
                    perm = "customcmds.use";
                }
                plugin.getProxy().getPluginManager().registerCommand(this.plugin, new CustomCommand(cmd, perm, message));
            }
            sender.sendMessage(new TextComponent("Custom commands have been reloaded."));
        }
    }
    @Override
    public void onDisable() {
        // Plugin shutdown logic
        // Unregister commands
        for (Command command : registeredCommands) {
            getProxy().getPluginManager().unregisterCommand(command);
        }
        //clear the hashmap
        commands.clear();
        permission.clear();
        registeredCommands.clear();
    }
    }


