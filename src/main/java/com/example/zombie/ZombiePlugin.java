package com.example.zombie;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.attribute.Attribute;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.entity.Zombie;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

public class ZombiePlugin extends JavaPlugin implements Listener {

    private final Map<UUID, Location> baseLocations = new HashMap<>();
    private final Map<UUID, Long> tpCooldown = new HashMap<>();

    @Override
    public void onEnable() {
        getServer().getPluginManager().registerEvents(this, this);
        getLogger().info("Zombie Plugin 활성화됨!");
    }

    @Override
    public void onDisable() {
        getLogger().info("Zombie Plugin 비활성화됨!");
    }

    // /기본템 명령어
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (label.equalsIgnoreCase("기본템")) {
            if (sender instanceof Player) {
                Player p = (Player) sender;
                giveStarterKit(p);
                return true;
            }
        }
        return false;
    }

    private void giveStarterKit(Player p) {
        // 철갑옷 풀세트
        ItemStack helmet = makeItem(Material.IRON_HELMET, "철 투구", Enchantment.PROTECTION_ENVIRONMENTAL, 2, Enchantment.DURABILITY, 10);
        ItemStack chest = makeItem(Material.IRON_CHESTPLATE, "철 흉갑", Enchantment.PROTECTION_ENVIRONMENTAL, 2, Enchantment.DURABILITY, 10);
        ItemStack legs = makeItem(Material.IRON_LEGGINGS, "철 각반", Enchantment.PROTECTION_ENVIRONMENTAL, 2, Enchantment.DURABILITY, 10);
        ItemStack boots = makeItem(Material.IRON_BOOTS, "철 신발", Enchantment.PROTECTION_ENVIRONMENTAL, 2, Enchantment.DURABILITY, 10);

        p.getInventory().setHelmet(helmet);
        p.getInventory().setChestplate(chest);
        p.getInventory().setLeggings(legs);
        p.getInventory().setBoots(boots);

        // 무기 & 도구
        p.getInventory().addItem(makeItem(Material.IRON_SWORD, "날카로운 철검", Enchantment.DAMAGE_ALL, 2, Enchantment.DURABILITY, 10));
        p.getInventory().addItem(makeItem(Material.IRON_PICKAXE, "광부의 곡괭이", Enchantment.DIG_SPEED, 4, Enchantment.DURABILITY, 20));
        p.getInventory().addItem(makeItem(Material.IRON_SHOVEL, "튼튼한 삽", Enchantment.DIG_SPEED, 3, Enchantment.DURABILITY, 50));
        p.getInventory().addItem(makeItem(Material.IRON_HOE, "초고속 괭이", Enchantment.DIG_SPEED, 5));
        p.getInventory().addItem(makeItem(Material.IRON_AXE, "나무꾼의 도끼", Enchantment.DIG_SPEED, 3));

        // 활 & 화살
        p.getInventory().addItem(makeItem(Material.BOW, "강력한 활", Enchantment.ARROW_INFINITE, 1, Enchantment.ARROW_DAMAGE, 4));
        p.getInventory().addItem(new ItemStack(Material.SPECTRAL_ARROW, 64));

        // 기타 아이템
        p.getInventory().addItem(new ItemStack(Material.ENCHANTING_TABLE, 2));
        p.getInventory().addItem(new ItemStack(Material.BOOKSHELF, 64));
        p.getInventory().addItem(new ItemStack(Material.LAPIS_LAZULI, 64));
        p.getInventory().addItem(new ItemStack(Material.COOKED_BEEF, 64));

        // 베이스 시계
        p.getInventory().addItem(makeBaseClock());

        // 체력, 레벨, 효과
        p.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(100.0);
        p.setHealth(100.0);
        p.setLevel(100);
        p.addPotionEffect(new PotionEffect(PotionEffectType.HEAL, 60, 99));

        p.sendMessage(ChatColor.GREEN + "기본템이 지급되었습니다!");
    }

    private ItemStack makeItem(Material mat, String name, Object... enchants) {
        ItemStack item = new ItemStack(mat, 1);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(ChatColor.YELLOW + name);
        for (int i = 0; i < enchants.length; i += 2) {
            Enchantment ench = (Enchantment) enchants[i];
            int level = (int) enchants[i + 1];
            meta.addEnchant(ench, level, true);
        }
        meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        item.setItemMeta(meta);
        return item;
    }

    private ItemStack makeBaseClock() {
        ItemStack clock = new ItemStack(Material.CLOCK, 1);
        ItemMeta meta = clock.getItemMeta();
        meta.setDisplayName(ChatColor.AQUA + "베이스 시계");
        clock.setItemMeta(meta);
        return clock;
    }

    // 시계 클릭 → GUI 열기
    @EventHandler
    public void onClockUse(PlayerInteractEvent e) {
        if (e.getItem() != null && e.getItem().getType() == Material.CLOCK &&
                e.getItem().getItemMeta().getDisplayName().contains("베이스 시계")) {
            Player p = e.getPlayer();
            Inventory gui = Bukkit.createInventory(null, 9, "베이스 메뉴");

            ItemStack setBase = new ItemStack(Material.PAPER);
            ItemMeta setMeta = setBase.getItemMeta();
            setMeta.setDisplayName(ChatColor.GREEN + "베이스 설정");
            setBase.setItemMeta(setMeta);

            ItemStack tpBase = new ItemStack(Material.PAPER);
            ItemMeta tpMeta = tpBase.getItemMeta();
            tpMeta.setDisplayName(ChatColor.AQUA + "베이스 순간이동");
            tpBase.setItemMeta(tpMeta);

            gui.setItem(3, setBase);
            gui.setItem(5, tpBase);

            p.openInventory(gui);
        }
    }

    // GUI 클릭 이벤트
    @EventHandler
    public void onInventoryClick(InventoryClickEvent e) {
        if (e.getView().getTitle().equals("베이스 메뉴")) {
            e.setCancelled(true);
            if (e.getCurrentItem() == null) return;

            Player p = (Player) e.getWhoClicked();
            String name = e.getCurrentItem().getItemMeta().getDisplayName();

            if (name.contains("베이스 설정")) {
                baseLocations.put(p.getUniqueId(), p.getLocation());
                p.playSound(p.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1f, 1f);
                p.sendMessage(ChatColor.GREEN + "베이스가 설정되었습니다!");
            } else if (name.contains("베이스 순간이동")) {
                if (!baseLocations.containsKey(p.getUniqueId())) {
                    p.sendMessage(ChatColor.RED + "먼저 베이스를 설정하세요!");
                    return;
                }
                long now = System.currentTimeMillis();
                if (tpCooldown.containsKey(p.getUniqueId()) && now - tpCooldown.get(p.getUniqueId()) < 5 * 60 * 1000) {
                    p.sendMessage(ChatColor.RED + "아직 쿨타임입니다!");
                    return;
                }
                p.sendMessage(ChatColor.YELLOW + "5초 후 베이스로 순간이동합니다...");
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        p.teleport(baseLocations.get(p.getUniqueId()));
                        p.sendMessage(ChatColor.AQUA + "베이스로 이동했습니다!");
                        tpCooldown.put(p.getUniqueId(), System.currentTimeMillis());
                    }
                }.runTaskLater(this, 20 * 5);
            }
        }
    }

    // 좀비 강화
    @EventHandler
    public void onZombieSpawn(CreatureSpawnEvent e) {
        if (e.getEntity() instanceof Zombie z) {
            z.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(200.0);
            z.setHealth(200.0);
        }
    }

    // 낙사 피해 무효
    @EventHandler
    public void onFall(EntityDamageEvent e) {
        if (e.getCause() == EntityDamageEvent.DamageCause.FALL) {
            e.setCancelled(true);
        }
    }
}
