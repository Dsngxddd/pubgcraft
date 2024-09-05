package net.cengiz1.pubgcraft.manager;

import net.cengiz1.pubgcraft.Pubgcraft;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Chest;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Random;

public class LootChestManager {
    private final Pubgcraft plugin;
    private final Random random = new Random();
    private final List<ItemStack> lootItems = new ArrayList<>();

    public LootChestManager(Pubgcraft plugin) {
        this.plugin = plugin;

    }

    public void createLootChest(Location location) {
        location.getBlock().setType(Material.CHEST);
        Chest chest = (Chest) location.getBlock().getState()
        chest.getInventory().addItem(generateRandomLoot());
    }

    private ItemStack[] generateRandomLoot() {
        List<ItemStack> loot = new ArrayList<>();
        loot.add(new ItemStack(Material.IRON_SWORD)); 
        loot.add(new ItemStack(Material.GOLDEN_APPLE, random.nextInt(3) + 1));
        loot.add(new ItemStack(Material.IRON_CHESTPLATE)); 
        loot.add(new ItemStack(Material.BOW));
        loot.add(new ItemStack(Material.ARROW, random.nextInt(10) + 5)); 

        return loot.toArray(new ItemStack[0]);
    }

    public void spawnRandomLootChests(int chestCount) {
        World world = plugin.getServer().getWorld(plugin.getGameConfig().getString("world.name"));
        int radius = plugin.getGameConfig().getInt("world.spawn_radius"); 

        for (int i = 0; i < chestCount; i++) {
            double x = random.nextDouble() * radius - (radius / 2);
            double z = random.nextDouble() * radius - (radius / 2);
            Location randomLocation = new Location(world, x, world.getHighestBlockYAt((int)x, (int)z), z);
            createLootChest(randomLocation);
        }
    }


}
