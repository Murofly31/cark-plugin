package org.oyuncozucu.untitled4;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.time.LocalDate;
import java.util.*;

public class CarkManager implements Listener {

    private final Untitled4 plugin;
    private final int CARK_SIZE = 9;
    private List<ItemStack> dailyRewards;
    private LocalDate lastUpdate;
    private final Set<UUID> spunToday = new HashSet<>();
    private final Map<UUID, Inventory> spinningPlayers = new HashMap<>();

    public CarkManager(Untitled4 plugin) {
        this.plugin = plugin;
        updateDailyRewards();
    }

    private void updateDailyRewards() {
        List<Material> allMaterials = Arrays.asList(Material.values());
        Collections.shuffle(allMaterials, new Random(LocalDate.now().toEpochDay()));
        dailyRewards = new ArrayList<>();
        int count = 0;
        for (Material mat : allMaterials) {
            if (mat.isItem() && !mat.isAir()) {
                dailyRewards.add(new ItemStack(mat, 1));
                count++;
                if (count >= CARK_SIZE) break;
            }
        }
        lastUpdate = LocalDate.now();
        spunToday.clear();
    }

    public void openCarkGui(Player player) {
        if (!LocalDate.now().equals(lastUpdate)) {
            updateDailyRewards();
        }
        if (spunToday.contains(player.getUniqueId())) {
            player.sendMessage("§cBugün zaten çarkı çevirdin!");
            return;
        }
        Inventory inv = Bukkit.createInventory(null, CARK_SIZE, "§6Çarkı Çevir!");
        for (int i = 0; i < CARK_SIZE; i++) {
            inv.setItem(i, dailyRewards.get(i));
        }
        player.openInventory(inv);
        spinningPlayers.put(player.getUniqueId(), inv);

        // Animasyon başlat
        new BukkitRunnable() {
            int ticks = 0;
            final int maxTicks = 40; // 2 saniye (20 tick = 1 sn)
            final Random random = new Random();

            @Override
            public void run() {
                if (!player.isOnline() || !spinningPlayers.containsKey(player.getUniqueId())) {
                    spinningPlayers.remove(player.getUniqueId());
                    cancel();
                    return;
                }
                // Slotları rastgele değiştir
                for (int i = 0; i < CARK_SIZE; i++) {
                    Material mat = dailyRewards.get(random.nextInt(dailyRewards.size())).getType();
                    inv.setItem(i, new ItemStack(mat, 1));
                }
                ticks++;
                if (ticks >= maxTicks) {
                    // Animasyon bitti, ödül ver
                    ItemStack reward = dailyRewards.get(random.nextInt(dailyRewards.size())).clone();
                    inv.setItem(4, reward); // Ortadaki slot
                    player.getInventory().addItem(reward);
                    player.sendMessage("§aÇark döndü! Kazandığın ödül: §e" + reward.getType());
                    spunToday.add(player.getUniqueId());
                    spinningPlayers.remove(player.getUniqueId());
                    // 1 sn sonra GUI kapansın
                    new BukkitRunnable() {
                        @Override
                        public void run() {
                            player.closeInventory();
                        }
                    }.runTaskLater(plugin, 20L);
                    cancel();
                }
            }
        }.runTaskTimer(plugin, 0L, 2L); // Her 2 tick'te bir animasyon
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent e) {
        if (e.getView().getTitle().equals("§6Çarkı Çevir!")) {
            e.setCancelled(true);
        }
    }
}