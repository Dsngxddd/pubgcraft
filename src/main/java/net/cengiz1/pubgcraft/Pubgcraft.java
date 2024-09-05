package net.cengiz1.pubgcraft;

import net.cengiz1.pubgcraft.command.JoinCommand;
import net.cengiz1.pubgcraft.listener.PlayerJoinListener;
import net.cengiz1.pubgcraft.manager.GameManager;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.plugin.java.JavaPlugin;

public class Pubgcraft extends JavaPlugin {
    private GameManager gameManager;

    @Override
    public void onEnable() {
        saveDefaultConfig(); 
        gameManager = new GameManager(this);
        getServer().getPluginManager().registerEvents(new PlayerJoinListener(gameManager), this);
        getCommand("join").setExecutor(new JoinCommand(gameManager));
    }

    @Override
    public void onDisable() {
		gameManager.endgame()
    }

    public ConfigurationSection getGameConfig() {
        return getConfig();
    }
}
