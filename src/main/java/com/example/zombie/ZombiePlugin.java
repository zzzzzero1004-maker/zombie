package com.example.zombie;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.CommandExecutor;
import org.bukkit.entity.Player;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

public class ZombiePlugin extends JavaPlugin implements CommandExecutor {

    private final Map<UUID, Location> baseLocations = new HashMap<>();
    private final Map<UUID, Long> teleportCooldown = new HashMap<>();

    @Override
    public void onEnable() {
        getCommand("기본템").setExecutor(this);
        getLogger().info("Zombie 플러그인이 활성화되었습니다!");
    }

    @Override
    public void onDisable() {
        getLogger().info("Zombie 플러그인이 비활성화되었습니다!");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("플레이어만 사용할 수 있습니다!");
            return true;
        }

        Player player = (Player) sender;

        if (command.getName().equalsIgnoreCase("기본템")) {
            giveStarterKit(player);
            return true;
        }

        return false;
    }

    private void giveStarterKit(Player player) {
        // 철 갑옷 풀세트 보호 II, 내구성 X
        ItemStack helmet = enchantedItem(Material.IRON_HELMET, Enchantment.PROTECTION_ENVIRONMENTAL, 2, Enchantment.DURABILITY, 10);
        ItemStack chest = enchantedItem(Material.IRON_CHESTPLATE, Enchantment.PROTECTION_ENVIRONMENTAL, 2, Enchantment.DURABILITY, 10);
        ItemStack legs = enchantedItem(Material.IRON_LEGGINGS, Enchantment.PROTECTION_ENVIRONMENTAL, 2, Enchantment.DURABILITY, 10);
        ItemStack boots = enchantedItem(Material.IRON_BOOTS, Enchantment.PROTECTION_ENVIRONMENTAL, 2, Enchantment.DURABILITY, 10);

        // 무기 & 도구
        ItemStack sword = enchantedItem(Material.IRON_SWORD, Enchantment.DAMAGE_ALL, 2, Enchantment.DURABILITY, 10);
        ItemStack pickaxe = enchantedItem(Material.IRON_PICKAXE, Enchantment.DIG_SPEED, 4, Enchantment.DURABILITY, 20);
        ItemStack shovel = enchantedItem(Material.IRON_SHOVEL, Enchantment.DIG_SPEED, 3, Enchantment.DURABILITY, 50);
        ItemStack hoe = enchantedItem(Material.IRON_HOE, Enchantment.DIG_SPEED, 5);
        ItemStack axe = enchantedItem(Material.IRON_AXE, Enchantment.DIG_SPEED, 3);

        // 활 + 화살
        ItemStack bow = enchantedItem(Material.BOW, Enchantment.ARROW_INFINITE, 1, Enchantment.ARROW_DAMAGE, 4);
        ItemStack arrows = new ItemStack(Material.ARROW, 64);

        // 기타 아이템
        ItemStack enchantTable = new ItemStack(Material.ENCHANTING_TABLE, 2);
        ItemStack bookshelves = new ItemStack(Material.BOOKSHELF, 64);
        ItemStack lapis = new ItemStack(Material.LAPIS_LAZULI, 64);
        ItemStack steak = new ItemStack(Material.COOKED_BEEF, 64);

        // 베이스 시계
        ItemStack baseClock = new ItemStack(Material.CLOCK);
        ItemMeta meta = baseClock.getItemMeta();
        meta.setDisplayName(ChatColor.GREEN + "베이스 시계");
        baseClock.setItemMeta(meta);

        // 인벤토리에 추가
        player.getInventory().addItem(helmet, chest, legs, boots,
                sword, pickaxe, shovel, hoe, axe,
                bow, arrows,
                enchantTable, bookshelves, lapis, steak, baseClock);

        // 체력, 레벨, 즉시 치유 버프
        player.setMaxHealth(100.0);
        player.setHealth(100.0);
        player.setLevel(100);
        player.addPotionEffect(new org.bukkit.potion.PotionEffect(
                org.bukkit.potion.PotionEffectType.HEAL, 60, 100));

        player.sendMessage(ChatColor.AQUA + "[기본템] 아이템과 능력이 지급되었습니다!");
    }

    private ItemStack enchantedItem(Material material, Object... enchants) {
        ItemStack item = new ItemStack(material, 1);
        ItemMeta meta = item.getItemMeta();

        for (int i = 0; i < enchants.length; i += 2) {
            Enchantment ench = (Enchantment) enchants[i];
            int level = (int) enchants[i + 1];
            meta.addEnchant(ench, level, true);
        }

        item.setItemMeta(meta);
        return item;
    }
}
