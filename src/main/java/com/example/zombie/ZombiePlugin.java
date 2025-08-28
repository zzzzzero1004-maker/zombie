package com.zombie;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Sound;
import org.bukkit.attribute.Attribute;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Zombie;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.*;

public class ZombiePlugin extends JavaPlugin implements Listener {

    private final Map<UUID, Long> baseCooldown = new HashMap<>();
    private final Map<UUID, org.bukkit.Location> baseLocations = new HashMap<>();

    @Override
    public void onEnable() {
        Bukkit.getPluginManager().registerEvents(this, this);

        // ========== 커스텀 레시피 등록 ==========
        registerRecipes();

        getLogger().info("ZombiePlugin 활성화됨!");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (cmd.getName().equalsIgnoreCase("기본템")) {
            if (sender instanceof Player) {
                Player player = (Player) sender;
                giveStarterKit(player);
                return true;
            }
        }
        return false;
    }

    // 기본템 지급
    private void giveStarterKit(Player player) {
        // 철 갑옷
        ItemStack helmet = enchItem(Material.IRON_HELMET, Map.of(Enchantment.PROTECTION_ENVIRONMENTAL, 2, Enchantment.DURABILITY, 10));
        ItemStack chest = enchItem(Material.IRON_CHESTPLATE, Map.of(Enchantment.PROTECTION_ENVIRONMENTAL, 2, Enchantment.DURABILITY, 10));
        ItemStack legs = enchItem(Material.IRON_LEGGINGS, Map.of(Enchantment.PROTECTION_ENVIRONMENTAL, 2, Enchantment.DURABILITY, 10));
        ItemStack boots = enchItem(Material.IRON_BOOTS, Map.of(Enchantment.PROTECTION_ENVIRONMENTAL, 2, Enchantment.DURABILITY, 10));

        player.getInventory().addItem(helmet, chest, legs, boots);

        // 무기 & 도구
        player.getInventory().addItem(enchItem(Material.IRON_SWORD, Map.of(Enchantment.DAMAGE_ALL, 2, Enchantment.DURABILITY, 10)));
        player.getInventory().addItem(enchItem(Material.IRON_PICKAXE, Map.of(Enchantment.DIG_SPEED, 4, Enchantment.DURABILITY, 20)));
        player.getInventory().addItem(enchItem(Material.IRON_SHOVEL, Map.of(Enchantment.DIG_SPEED, 3, Enchantment.DURABILITY, 50)));
        player.getInventory().addItem(enchItem(Material.IRON_HOE, Map.of(Enchantment.DIG_SPEED, 5)));
        player.getInventory().addItem(enchItem(Material.IRON_AXE, Map.of(Enchantment.DIG_SPEED, 3)));

        // 활 & 화살
        player.getInventory().addItem(enchItem(Material.BOW, Map.of(Enchantment.ARROW_INFINITE, 1, Enchantment.ARROW_DAMAGE, 4)));
        player.getInventory().addItem(new ItemStack(Material.SPECTRAL_ARROW, 64));

        // 기타 아이템
        player.getInventory().addItem(new ItemStack(Material.ENCHANTING_TABLE, 2));
        player.getInventory().addItem(new ItemStack(Material.BOOKSHELF, 64));
        player.getInventory().addItem(new ItemStack(Material.LAPIS_LAZULI, 64));
        player.getInventory().addItem(new ItemStack(Material.COOKED_BEEF, 64));

        // 베이스 시계
        ItemStack clock = new ItemStack(Material.CLOCK);
        ItemMeta cm = clock.getItemMeta();
        cm.setDisplayName("§6베이스 시계");
        clock.setItemMeta(cm);
        player.getInventory().addItem(clock);

        // HP, 레벨, 버프
        player.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(100.0);
        player.setHealth(100.0);
        player.setLevel(100);
        player.addPotionEffect(new PotionEffect(PotionEffectType.HEAL, 60, 100));

        player.sendMessage("§a기본템이 지급되었습니다!");
    }

    private ItemStack enchItem(Material mat, Map<Enchantment, Integer> enchants) {
        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        enchants.forEach((enchant, lvl) -> meta.addEnchant(enchant, lvl, true));
        item.setItemMeta(meta);
        return item;
    }

    // 베이스 시계
    @EventHandler
    public void onUseClock(PlayerInteractEvent e) {
        Player p = e.getPlayer();
        ItemStack item = e.getItem();
        if (item != null && item.getType() == Material.CLOCK && item.getItemMeta().hasDisplayName()
                && item.getItemMeta().getDisplayName().equals("§6베이스 시계")) {
            Inventory inv = Bukkit.createInventory(null, 9, "베이스 메뉴");

            ItemStack setBase = new ItemStack(Material.PAPER);
            ItemMeta sm = setBase.getItemMeta();
            sm.setDisplayName("§a베이스 설정");
            setBase.setItemMeta(sm);

            ItemStack tpBase = new ItemStack(Material.PAPER);
            ItemMeta tm = tpBase.getItemMeta();
            tm.setDisplayName("§b베이스 순간이동");
            tpBase.setItemMeta(tm);

            inv.setItem(3, setBase);
            inv.setItem(5, tpBase);

            p.openInventory(inv);
        }
    }

    @EventHandler
    public void onClickBaseMenu(InventoryClickEvent e) {
        if (e.getView().getTitle().equals("베이스 메뉴")) {
            e.setCancelled(true);
            Player p = (Player) e.getWhoClicked();
            ItemStack clicked = e.getCurrentItem();
            if (clicked == null || !clicked.hasItemMeta()) return;

            String name = clicked.getItemMeta().getDisplayName();
            if (name.equals("§a베이스 설정")) {
                baseLocations.put(p.getUniqueId(), p.getLocation());
                p.sendMessage("§a베이스가 설정되었습니다!");
                p.closeInventory();
            } else if (name.equals("§b베이스 순간이동")) {
                if (!baseLocations.containsKey(p.getUniqueId())) {
                    p.sendMessage("§c먼저 베이스를 설정하세요!");
                    return;
                }
                long now = System.currentTimeMillis();
                if (baseCooldown.containsKey(p.getUniqueId()) && now - baseCooldown.get(p.getUniqueId()) < 300000) {
                    p.sendMessage("§c쿨타임이 남아있습니다!");
                    return;
                }
                baseCooldown.put(p.getUniqueId(), now);
                p.sendMessage("§e5초 후 베이스로 이동합니다...");
                Bukkit.getScheduler().runTaskLater(this, () -> {
                    p.teleport(baseLocations.get(p.getUniqueId()));
                    p.playSound(p.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1, 1);
                    p.sendMessage("§a베이스로 이동했습니다!");
                }, 100);
                p.closeInventory();
            }
        }
    }

    // 좀비 강화
    @EventHandler
    public void onZombieSpawn(CreatureSpawnEvent e) {
        if (e.getEntityType() == EntityType.ZOMBIE) {
            Zombie z = (Zombie) e.getEntity();
            z.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(200.0);
            z.setHealth(200.0);
        }
    }

    @EventHandler
    public void onFallDamage(EntityDamageEvent e) {
        if (e.getEntityType() == EntityType.ZOMBIE && e.getCause() == EntityDamageEvent.DamageCause.FALL) {
            e.setCancelled(true);
        }
    }

    // 레시피 등록
    private void registerRecipes() {
        // OP 네더라이트 검
        ItemStack opSword = new ItemStack(Material.NETHERITE_SWORD);
        ItemMeta swordMeta = opSword.getItemMeta();
        swordMeta.addEnchant(Enchantment.DAMAGE_ALL, 20, true);
        swordMeta.addEnchant(Enchantment.FIRE_ASPECT, 10, true);
        swordMeta.addEnchant(Enchantment.DAMAGE_UNDEAD, 20, true);
        swordMeta.addEnchant(Enchantment.DAMAGE_ARTHROPODS, 20, true);
        swordMeta.addEnchant(Enchantment.SWEEPING_EDGE, 20, true);
        swordMeta.addEnchant(Enchantment.DURABILITY, 100, true);
        swordMeta.addEnchant(Enchantment.MENDING, 1, true);
        opSword.setItemMeta(swordMeta);

        NamespacedKey swordKey = new NamespacedKey(this, "op_netherite_sword");
        ShapedRecipe swordRecipe = new ShapedRecipe(swordKey, opSword);
        swordRecipe.shape("N", "N", "S");
        swordRecipe.setIngredient('N', Material.NETHERITE_INGOT);
        swordRecipe.setIngredient('S', Material.STICK);
        Bukkit.addRecipe(swordRecipe);

        // 불사의 토템
        ItemStack totem = new ItemStack(Material.TOTEM_OF_UNDYING);
        NamespacedKey totemKey = new NamespacedKey(this, "custom_totem");
        ShapedRecipe totemRecipe = new ShapedRecipe(totemKey, totem);
        totemRecipe.shape("GEG");
        totemRecipe.setIngredient('G', Material.GOLD_INGOT);
        totemRecipe.setIngredient('E', Material.EMERALD);
        Bukkit.addRecipe(totemRecipe);
    }
}
