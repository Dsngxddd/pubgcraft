package net.cengiz1.pubgcraft.manager;

import net.cengiz1.pubgcraft.Pubgcraft;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.WorldBorder;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.*;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class GameManager {
    private final Pubgcraft plugin;
    private final List<Player> lobbyPlayers = new ArrayList<>();
    private boolean gameInProgress = false;
    private final LootChestManager lootChestManager;
    private WorldBorder worldBorder;
    private ThreadLocalRandom random;

    public GameManager(Pubgcraft plugin) {
        this.plugin = plugin;
        this.lootChestManager = new LootChestManager(plugin);
    }

    private void startGame() {
        gameInProgress = true;
        plugin.getServer().broadcastMessage("Battle Royale başlıyor!");


        int chestCount = plugin.getGameConfig().getInt("loot.chest_count");
        lootChestManager.spawnRandomLootChests(chestCount);

        setupWorldBorder();

        for (Player player : lobbyPlayers) {
            setupScoreboard(player);
            player.teleport(getRandomLocation());
        }

        startBorderShrinking();
    }
    private void setupWorldBorder() {
        World world = plugin.getServer().getWorld(plugin.getGameConfig().getString("world.name"));
        worldBorder = world.getWorldBorder();
        worldBorder.setCenter(world.getSpawnLocation()); 
        worldBorder.setSize(plugin.getGameConfig().getInt("world.initial_border_size"));
        worldBorder.setDamageAmount(2); 
    }

    // Alanı belirli aralıklarla daralt
    private void startBorderShrinking() {
        new BukkitRunnable() {
            @Override
            public void run() {
                if (!gameInProgress) {
                    cancel();
                    return;
                }

                double currentSize = worldBorder.getSize();
                double shrinkAmount = plugin.getGameConfig().getInt("world.shrink_amount");
                double minimumSize = plugin.getGameConfig().getInt("world.minimum_border_size");

                if (currentSize <= minimumSize) {
                    plugin.getServer().broadcastMessage("Alan artık daha fazla daralmayacak!");
                    cancel();
                    return;
                }

                double newSize = Math.max(currentSize - shrinkAmount, minimumSize);
                worldBorder.setSize(newSize, plugin.getGameConfig().getInt("world.shrink_interval")); 
                plugin.getServer().broadcastMessage("Alan " + newSize + "x" + newSize + " boyutuna daralıyor!");
            }
        }.runTaskTimer(plugin, 0, plugin.getGameConfig().getInt("world.shrink_interval") * 20); 
    }

    private Location getRandomLocation() {
        World world = plugin.getServer().getWorld(plugin.getGameConfig().getString("world.name"));
        double radius = plugin.getGameConfig().getInt("world.spawn_radius");
        double x = random.nextDouble() * radius - (radius / 2);
        double z = random.nextDouble() * radius - (radius / 2);
        Location location = new Location(world, x, world.getHighestBlockYAt((int)x, (int)z), z);
        return location;
    }

    private void setupScoreboard(Player player) {
        ScoreboardManager manager = Bukkit.getScoreboardManager();
        Scoreboard board = manager.getNewScoreboard();
        Objective objective = board.registerNewObjective("game", "dummy", plugin.getGameConfig().getString("game.scoreboard_title"));
        objective.setDisplaySlot(DisplaySlot.SIDEBAR);
        Score playersLeft = objective.getScore("Kalan Oyuncu:");
        playersLeft.setScore(lobbyPlayers.size());
        Score borderSize = objective.getScore("Alan Boyutu:");
        borderSize.setScore((int) worldBorder.getSize());
        player.setScoreboard(board);
    }

    public void updateScoreboard() {
        for (Player player : lobbyPlayers) {
            Scoreboard board = player.getScoreboard();
            Objective objective = board.getObjective(DisplaySlot.SIDEBAR);

            if (objective != null) {
                Score playersLeft = objective.getScore("Kalan Oyuncu:");
                playersLeft.setScore(lobbyPlayers.size());

                Score borderSize = objective.getScore("Alan Boyutu:");
                borderSize.setScore((int) worldBorder.getSize());
            }
        }
    }

    public void onPlayerDeath(Player player) {
        lobbyPlayers.remove(player);
        updateScoreboard();
        player.sendMessage("Elendiniz!");

        if (lobbyPlayers.size() == 1) {
            Player winner = lobbyPlayers.get(0);
            plugin.getServer().broadcastMessage(winner.getName() + " kazandı!");
            endGame();
        }
    }

    private void endGame() {
        gameInProgress = false;
        worldBorder.reset();
        for (Player player : plugin.getServer().getOnlinePlayers()) {
            player.teleport(plugin.getServer().getWorld("world").getSpawnLocation());
        }

        lobbyPlayers.clear();
    }

    public void addPlayerToLobby(Player player) {
        if (gameInProgress) {
            player.sendMessage("Oyun zaten başlamış durumda, lobiye katılamazsınız.");
            return;
        }

        if (lobbyPlayers.contains(player)) {
            player.sendMessage("Zaten lobiye katıldınız.");
            return;
        }

        lobbyPlayers.add(player);
        player.sendMessage("Lobiye katıldınız! Oyunun başlamasını bekleyin.");

        int minPlayers = plugin.getGameConfig().getInt("game.min_players");
        if (lobbyPlayers.size() >= minPlayers) {
            startCountdown();
        }
    }
    private void startCountdown() {
        plugin.getServer().broadcastMessage("Yeterli oyuncu sayısına ulaşıldı! Oyun 10 saniye içinde başlayacak...");

        new BukkitRunnable() {
            int countdown = plugin.getGameConfig().getInt("game.countdown");

            @Override
            public void run() {
                if (countdown <= 0) {
                    startGame();
                    cancel();
                } else {
                    plugin.getServer().broadcastMessage("Oyun " + countdown + " saniye içinde başlıyor!");
                    countdown--;
                }
            }
        }.runTaskTimer(plugin, 0, 20); 
    }

}
