package me.mystc.simplehungergames;

import me.mystc.simplehungergames.Commands.*;
import me.mystc.simplehungergames.Files.ChestsFile;
import me.mystc.simplehungergames.Files.LocationsFile;
import org.bukkit.*;
import org.bukkit.block.BlockState;
import org.bukkit.block.Chest;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryInteractEvent;
import org.bukkit.event.player.*;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;

public final class SimpleHungerGames extends JavaPlugin implements Listener {
    // Var
    public static boolean preGame = false;
    public static boolean isGame = false;
    public static boolean isAdmin = false;
    public static Player admin = null;
    public static ArrayList<UUID> inGame = new ArrayList<>();
    public static ArrayList<UUID> deadGame = new ArrayList<>();
    public static Inventory chestLoot = null;
    public static Inventory chestOptions = null;
    public static Inventory chestLists = null;
    public static Inventory chestDisplay = null;

    @Override
    public void onEnable() {
        // Plugin startup logic
        LocationsFile.setup();
        LocationsFile.save();

        ChestsFile.setup();
        ChestsFile.save();

        getConfig().options().copyDefaults(true);
        saveConfig();

        setupInventory();

        getCommand("setlocation").setExecutor(new SetLocationCommand());
        getCommand("play").setExecutor(new PlayCommand());
        getCommand("freeze").setExecutor(new FreezeCommand());
        getCommand("locationreload").setExecutor(new LocationsReload());
        getCommand("end").setExecutor(new EndGame());
        getCommand("chest").setExecutor(new ChestCommand());
        getCommand("chestrefill").setExecutor(new ResetChests());
        getCommand("debug").setExecutor(new DebugCommand());

        Bukkit.getPluginManager().registerEvents(this, this);

        //TODO LOOT
        //TODO Admin Abilities
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        for(Player p : Bukkit.getOnlinePlayers()) {
            SimpleHungerGames.setLobby(p);
            p.sendMessage(ChatColor.translateAlternateColorCodes('&', "&7GG, game&c ended."));
        }

        SimpleHungerGames.inGame.clear();
        SimpleHungerGames.deadGame.clear();
        SimpleHungerGames.admin = null;
        SimpleHungerGames.isAdmin = false;
        SimpleHungerGames.preGame = false;
        SimpleHungerGames.isGame = false;
    }

    // Events
    @EventHandler
    void onDeath(PlayerDeathEvent e) {
        if(e.getEntity().hasPermission("hg.admin") && isAdmin) {
            e.setKeepInventory(true);
            return;
        }
        if(!isGame) return;
        if(!inGame.contains(e.getEntity().getUniqueId())) return;

        Player p = e.getEntity();
        EntityDamageEvent dc = p.getLastDamageCause();
        inGame.remove(p.getUniqueId());
        deadGame.add(p.getUniqueId());

        if(inGame.size() <= 1) {
            Player winner = Bukkit.getPlayer(inGame.get(0));
            for(Player player : Bukkit.getOnlinePlayers()) {
                player.sendTitle(ChatColor.translateAlternateColorCodes('&', "&a" + winner.getName() + " wins!"), ChatColor.translateAlternateColorCodes('&', "&cgame over."), 10, 40, 10);
            }
            Bukkit.dispatchCommand(Bukkit.getServer().getConsoleSender(), "end");
        }

        if(dc.getCause().equals(EntityDamageEvent.DamageCause.ENTITY_ATTACK)) {
            e.setDeathMessage(ChatColor.translateAlternateColorCodes('&', "&4" + p.getDisplayName() + "&7 has died to &c" + p.getKiller().getDisplayName() + "&a ggwp!"));
        } else {
            e.setDeathMessage(ChatColor.translateAlternateColorCodes('&', "&4" + p.getDisplayName() + "&7 has died!" + "&a ggwp!"));
        }

    }

    @EventHandler
    void onRespawn(PlayerRespawnEvent e) {
        if(e.getPlayer().hasPermission("hg.admin") && isAdmin) return;
        if(!isGame) return;
        if(!deadGame.contains(e.getPlayer().getUniqueId())) return;

        Player p = e.getPlayer();
        e.setRespawnLocation(new Location(Bukkit.getWorld("world"), getConfig().getIntegerList("lobby-location").get(0), getConfig().getIntegerList("lobby-location").get(1), getConfig().getIntegerList("lobby-location").get(2)));
        setSpectator(p);
    }

    @EventHandler
    void onDamage(EntityDamageByEntityEvent e) {
        if(!isGame) e.setCancelled(true);
        if(!(e.getDamager() instanceof Player)) return;

        Player p = (Player) e.getDamager();
        if(inGame.contains(p.getUniqueId())) return;
        if(deadGame.contains(p.getUniqueId())) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    void onLeave(PlayerQuitEvent e){
        if(e.getPlayer().hasPermission("hg.admin") && isAdmin) return;
        if(!isGame) return;
        if(deadGame.contains(e.getPlayer().getUniqueId())) return;

        inGame.remove(e.getPlayer().getUniqueId());
        deadGame.add(e.getPlayer().getUniqueId());
        setSpectator(e.getPlayer());
    }

    @EventHandler
    void onJoin(PlayerJoinEvent e) {
        if(e.getPlayer().hasPermission("hg.admin")) {
            if(isGame && isAdmin) {
                makeAdmin(e.getPlayer());
                return;
            }
        }
        if(!isGame) return;

        inGame.remove(e.getPlayer().getUniqueId());
        if(!deadGame.contains(e.getPlayer().getUniqueId())) deadGame.add(e.getPlayer().getUniqueId());
        setSpectator(e.getPlayer());
    }

    @EventHandler
    void onMove(final PlayerMoveEvent e) {
        if(!preGame) return;
        if(e.getPlayer().hasPermission("hg.admin") && isAdmin) return;
        final Location from = e.getFrom();
        final Location to = e.getTo();
        if (from.getX() != to.getX() || from.getZ() != to.getZ()) {
            e.setTo(from);
        }
    }

    @EventHandler
    void onBreak(BlockBreakEvent e) {
        if(!preGame) return;
        if(e.getPlayer().hasPermission("hg.admin") && isAdmin) return;
        e.setCancelled(true);
    }

    @EventHandler
    void onPlace(BlockPlaceEvent e) {
        if(!preGame) return;
        if(e.getPlayer().hasPermission("hg.admin") && isAdmin) return;
        e.setCancelled(true);
    }

    @EventHandler
    void onRegen(EntityRegainHealthEvent e) {
        if(e.getRegainReason().equals(EntityRegainHealthEvent.RegainReason.SATIATED) || e.getRegainReason().equals(EntityRegainHealthEvent.RegainReason.REGEN)) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    void onCloseCreateInv(InventoryCloseEvent e) {
        if(e.getInventory().equals(chestLoot)) {
            ItemStack[] inv = e.getInventory().getContents();
            e.getInventory().clear();
            List<String> invs = ChestsFile.get().getStringList("inv");
            if(invs.size() >= 45) {
                e.getPlayer().sendMessage(ChatColor.translateAlternateColorCodes('&', "&cMax Inventories reached"));
                return;
            }
            invs.add(SerializeInventory.itemStackArrayToBase64(inv));
            ChestsFile.get().set("inv", invs);
            ChestsFile.save();
            e.getPlayer().sendMessage(ChatColor.translateAlternateColorCodes('&', "&7Inventory&a saved!"));
        } else if(e.getInventory().equals(chestDisplay)) {
            chestDisplay.clear();
        }
    }

    @EventHandler
    void onInventoryInteract(InventoryClickEvent e) {
        if(!e.getWhoClicked().hasPermission("hg.chest")) return;

        if(e.getInventory().equals(chestOptions)) {
            if(e.getSlot() == 2) {
                // Create list
                Player p = (Player)e.getWhoClicked();
                p.sendMessage(ChatColor.translateAlternateColorCodes('&', "&aCreating inventory.."));
                p.closeInventory();
                p.openInventory(chestLoot);
                e.setCancelled(true);
            } else if(e.getSlot() == 6) {
                // Delete list
                Player p = (Player)e.getWhoClicked();
                setupList();
                p.openInventory(chestLists);
                e.setCancelled(true);
            } else {
                e.setCancelled(true);
            }
        } else if(e.getInventory().equals(chestLists)) {
            try {
                ItemStack item = e.getCurrentItem();
                int slot = e.getSlot();
                if (!item.hasItemMeta()) e.setCancelled(true);
                Player p = (Player) e.getWhoClicked();
                chestDisplay = Bukkit.createInventory(null, 36, "Loot table");
                chestDisplay.setContents(SerializeInventory.itemStackArrayFromBase64(ChestsFile.get().getStringList("inv").get(slot)));

                ItemStack optionDelete = new ItemStack(Material.RED_STAINED_GLASS_PANE);
                ItemMeta deleteMeta = optionDelete.getItemMeta();
                deleteMeta.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&cDelete Loot Table"));
                ArrayList<String> lore = new ArrayList<String>();
                lore.add(String.valueOf(slot));
                deleteMeta.setLore(lore);
                optionDelete.setItemMeta(deleteMeta);

                chestDisplay.setItem(31, optionDelete);

                p.openInventory(chestDisplay);
                e.setCancelled(true);
            } catch (Exception ex) {
                //owww
            }
        } else if(e.getInventory().equals(chestDisplay)) {
            if(e.getSlot() == 31) {
                int slot = Integer.parseInt(e.getCurrentItem().getItemMeta().getLore().get(0));
                List<String> invs = ChestsFile.get().getStringList("inv");
                invs.remove(slot);
                ChestsFile.get().set("inv", invs);
                ChestsFile.save();
                e.getWhoClicked().closeInventory();
                e.getWhoClicked().sendMessage(ChatColor.translateAlternateColorCodes('&', "&cDeleted Inventory"));
                e.setCancelled(true);
            }
        }
    }

    @EventHandler
    void onClick(PlayerInteractEvent e) {
        if(deadGame.contains(e.getPlayer().getUniqueId())) e.setCancelled(true);
    }

    @EventHandler
    void onInteract(PlayerInteractEvent e){
        if(isAdmin && e.getPlayer().equals(admin) && e.getAction().equals(Action.RIGHT_CLICK_BLOCK)) {
            if(e.getItem().getType().equals(Material.MOJANG_BANNER_PATTERN)) {
                Random rand = new Random();
                int coinFlip = rand.nextInt(2);
                if(coinFlip == 1) {
                    for(Player p : Bukkit.getOnlinePlayers()) {
                        p.sendMessage(ChatColor.translateAlternateColorCodes('&', "&7Coin fliped&6 heads"));
                    }
                } else {
                    for(Player p : Bukkit.getOnlinePlayers()) {
                        p.sendMessage(ChatColor.translateAlternateColorCodes('&', "&7Coin fliped&6 tails"));
                    }
                }
            } else if(e.getItem().getType().equals(Material.PHANTOM_MEMBRANE)) {
                World w = Bukkit.getWorld("world");
                w.getWorldBorder().setSize(w.getWorldBorder().getSize()-10, 4);
                for(Player p : Bukkit.getOnlinePlayers()) {
                    p.sendMessage(ChatColor.translateAlternateColorCodes('&', "&cReducing&7 world border by&c 5&7."));
                }
            } else if(e.getItem().getType().equals(Material.MAGMA_CREAM)) {
                World w = Bukkit.getWorld("world");
                w.getWorldBorder().setSize(w.getWorldBorder().getSize()+10, 4);
                for(Player p : Bukkit.getOnlinePlayers()) {
                    p.sendMessage(ChatColor.translateAlternateColorCodes('&', "&aIncreasing&7 world border by&a 5&7."));
                }
            } else if(e.getItem().getType().equals(Material.RABBIT_FOOT)) {
                Player pl = e.getPlayer();
                pl.performCommand("freeze");
            }
        }
    }

    void setSpectator(Player p){
        p.getInventory().clear();
        p.setBedSpawnLocation(new Location(Bukkit.getWorld("world"), Bukkit.getPluginManager().getPlugin("SimpleHungerGames").getConfig().getIntegerList("lobby-location").get(0), Bukkit.getPluginManager().getPlugin("SimpleHungerGames").getConfig().getIntegerList("lobby-location").get(1), Bukkit.getPluginManager().getPlugin("SimpleHungerGames").getConfig().getIntegerList("lobby-location").get(2)), true);
        p.setGameMode(GameMode.ADVENTURE);
        p.setInvisible(true);
        p.setInvulnerable(true);
        p.setAllowFlight(true);
        p.setFlying(true);
        p.setFoodLevel(20);
        p.setSaturation(20);
        p.setHealth(20);
        p.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&c[dead]&f " + p.getName()));
        p.teleport(new Location(Bukkit.getWorld("world"), Bukkit.getPluginManager().getPlugin("SimpleHungerGames").getConfig().getIntegerList("lobby-location").get(0), Bukkit.getPluginManager().getPlugin("SimpleHungerGames").getConfig().getIntegerList("lobby-location").get(1), Bukkit.getPluginManager().getPlugin("SimpleHungerGames").getConfig().getIntegerList("lobby-location").get(2)));
    }

    public static void setGamer(Player p) {
        p.getInventory().clear();
        p.setBedSpawnLocation(new Location(Bukkit.getWorld("world"), Bukkit.getPluginManager().getPlugin("SimpleHungerGames").getConfig().getIntegerList("lobby-location").get(0), Bukkit.getPluginManager().getPlugin("SimpleHungerGames").getConfig().getIntegerList("lobby-location").get(1), Bukkit.getPluginManager().getPlugin("SimpleHungerGames").getConfig().getIntegerList("lobby-location").get(2)), true);
        p.setGameMode(GameMode.SURVIVAL);
        p.setInvisible(false);
        p.setInvulnerable(false);
        p.setAllowFlight(false);
        p.setFlying(false);
        p.setFoodLevel(20);
        p.setSaturation(5);
        p.setHealth(20);
        p.setDisplayName(ChatColor.translateAlternateColorCodes('&', p.getName()));
    }

    public static void setLobby(Player p) {
        p.getInventory().clear();
        p.setBedSpawnLocation(new Location(Bukkit.getWorld("world"), Bukkit.getPluginManager().getPlugin("SimpleHungerGames").getConfig().getIntegerList("lobby-location").get(0), Bukkit.getPluginManager().getPlugin("SimpleHungerGames").getConfig().getIntegerList("lobby-location").get(1), Bukkit.getPluginManager().getPlugin("SimpleHungerGames").getConfig().getIntegerList("lobby-location").get(2)), true);
        p.setGameMode(GameMode.ADVENTURE);
        p.setInvisible(false);
        p.setInvulnerable(true);
        p.setAllowFlight(false);
        p.setFlying(false);
        p.setFoodLevel(20);
        p.setSaturation(20);
        p.setHealth(20);
        p.setDisplayName(ChatColor.translateAlternateColorCodes('&', p.getName()));
        p.teleport(new Location(Bukkit.getWorld("world"), Bukkit.getPluginManager().getPlugin("SimpleHungerGames").getConfig().getIntegerList("lobby-location").get(0), Bukkit.getPluginManager().getPlugin("SimpleHungerGames").getConfig().getIntegerList("lobby-location").get(1), Bukkit.getPluginManager().getPlugin("SimpleHungerGames").getConfig().getIntegerList("lobby-location").get(2)));
    }

    public static void makeAdmin(Player p) {
        p.getInventory().clear();

        ItemStack coin = new ItemStack(Material.MOJANG_BANNER_PATTERN);
        ItemMeta coinMeta = coin.getItemMeta();
        coinMeta.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&6Coin"));
        coin.setItemMeta(coinMeta);

        ItemStack closeBorder = new ItemStack(Material.PHANTOM_MEMBRANE);
        ItemMeta closeBorderMeta = closeBorder.getItemMeta();
        closeBorderMeta.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&cClose Border"));
        closeBorder.setItemMeta(closeBorderMeta);

        ItemStack increaseBorder = new ItemStack(Material.MAGMA_CREAM);
        ItemMeta increaseBorderMeta = closeBorder.getItemMeta();
        increaseBorderMeta.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&bIncrease Border"));
        increaseBorder.setItemMeta(increaseBorderMeta);

        ItemStack pauseGame = new ItemStack(Material.RABBIT_FOOT);
        ItemMeta pauseGameMeta = pauseGame.getItemMeta();
        pauseGameMeta.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&aPause Game"));
        pauseGame.setItemMeta(pauseGameMeta);

        p.getInventory().setContents(new ItemStack[]{coin, closeBorder, increaseBorder, pauseGame});
        p.setGameMode(GameMode.CREATIVE);
        p.setFlying(true);
    }

    public static void resetChests() {
        World world = Bukkit.getWorld("world");

        for(Chunk c : world.getLoadedChunks()){
            for(BlockState b : c.getTileEntities()){
                if(b instanceof Chest) {
                    try {
                        Random rand = new Random();
                        Chest chest = (Chest) b;
                        Inventory ci = chest.getBlockInventory();
                        List<String> invs = ChestsFile.get().getStringList("inv");
                        ItemStack[] inv = SerializeInventory.itemStackArrayFromBase64(invs.get(rand.nextInt(invs.size())));
                        ci.setContents(inv);
                    } catch (Exception e) {
                        //owww
                    }
                }
            }
        }
    }

    void setupInventory(){
        chestLoot = Bukkit.createInventory(null, 27, "Create Loot Table");

        chestOptions = Bukkit.createInventory(null, 9, "Pick one");

        ItemStack optionCreate = new ItemStack(Material.LIME_STAINED_GLASS_PANE);
        ItemMeta createMeta = optionCreate.getItemMeta();
        createMeta.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&aCreate Loot Table"));
        optionCreate.setItemMeta(createMeta);

        ItemStack optionDelete = new ItemStack(Material.RED_STAINED_GLASS_PANE);
        ItemMeta deleteMeta = optionDelete.getItemMeta();
        deleteMeta.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&cDelete Loot Table"));
        optionDelete.setItemMeta(deleteMeta);

        chestOptions.setItem(2, optionCreate);
        chestOptions.setItem(6, optionDelete);
    }

    void setupList() {
        List<String> invs = ChestsFile.get().getStringList("inv");
        int len = invs.size();
        if(len <= 9) {
            //one row
            chestLists = Bukkit.createInventory(null, 9, "Pick a loot table");
            for(int i=0;i<len;i++) {
                ItemStack item = new ItemStack(Material.CHEST);
                ItemMeta itemMeta = item.getItemMeta();
                itemMeta.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&b" + (i+1)));
                item.setItemMeta(itemMeta);
                chestLists.addItem(item);
            }
        } else if(len <= 18) {
            //two rows
            chestLists = Bukkit.createInventory(null, 18, "Pick a loot table");
            for(int i=0;i<len;i++) {
                ItemStack item = new ItemStack(Material.CHEST);
                ItemMeta itemMeta = item.getItemMeta();
                itemMeta.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&b" + (i+1)));
                item.setItemMeta(itemMeta);
                chestLists.addItem(item);
            }
        } else if(len <= 27) {
            //three rows
            chestLists = Bukkit.createInventory(null, 27, "Pick a loot table");
            for(int i=0;i<len;i++) {
                ItemStack item = new ItemStack(Material.CHEST);
                ItemMeta itemMeta = item.getItemMeta();
                itemMeta.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&b" + (i+1)));
                item.setItemMeta(itemMeta);
                chestLists.addItem(item);
            }
        } else if(len <= 36) {
            //four rows
            chestLists = Bukkit.createInventory(null, 36, "Pick a loot table");
            for(int i=0;i<len;i++) {
                ItemStack item = new ItemStack(Material.CHEST);
                ItemMeta itemMeta = item.getItemMeta();
                itemMeta.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&b" + (i+1)));
                item.setItemMeta(itemMeta);
                chestLists.addItem(item);
            }
        } else if(len <= 45) {
            //five rows
            chestLists = Bukkit.createInventory(null, 45, "Pick a loot table");
            for(int i=0;i<len;i++) {
                ItemStack item = new ItemStack(Material.CHEST);
                ItemMeta itemMeta = item.getItemMeta();
                itemMeta.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&b" + (i+1)));
                item.setItemMeta(itemMeta);
                chestLists.addItem(item);
            }
        } else {
            System.out.println("ERROR");
        }


    }
}
