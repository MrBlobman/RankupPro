package roamcraft.MrBlobman.RankupPro;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.Configuration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;
 



import java.util.Arrays;
 
public class RankupIconMenu implements Listener {
 
    private String name;
    private int size;
    private OptionClickEventHandler handler;
    private Plugin plugin;
    private Player player;
 
    private String[] optionNames;
    private ItemStack[] optionIcons;
 
    public RankupIconMenu(String name, int size, OptionClickEventHandler handler, Plugin plugin) {
        this.name = name;
        this.size = size;
        this.handler = handler;
        this.plugin = plugin;
        this.optionNames = new String[size];
        this.optionIcons = new ItemStack[size];
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }
 
    public RankupIconMenu setOption(int position, ItemStack icon, String name, String... info) {
        optionNames[position] = name;
        optionIcons[position] = setItemNameAndLore(icon, name, info);
        return this;
    }
 
    public void setSpecificTo(Player player) {
        this.player = player;
    }
 
    public boolean isSpecific() {
        return player != null;
    }
 
    public void open(Player player) {
        Inventory inventory = Bukkit.createInventory(player, size, name);
        for (int i = 0; i < optionIcons.length; i++) {
            if (optionIcons[i] != null) {
                inventory.setItem(i, optionIcons[i]);
            }
        }
        player.openInventory(inventory);
    }
 
    public void destroy() {
        HandlerList.unregisterAll(this);
        handler = null;
        plugin = null;
        optionNames = null;
        optionIcons = null;
    }
 
	@EventHandler(priority = EventPriority.HIGHEST)
    void onInventoryClick(InventoryClickEvent event) {
        if (event.getInventory().getTitle().equals(name) && (player == null || event.getWhoClicked() == player)) {
            event.setCancelled(true);
            if (event.getClick() != ClickType.LEFT)
                return;
            int slot = event.getRawSlot();
            if (slot >= 0 && slot < size && optionNames[slot] != null) {
                Plugin plugin = this.plugin;
                OptionClickEvent e = new OptionClickEvent((Player) event.getWhoClicked(), slot, optionNames[slot], optionIcons[slot]);
                handler.onOptionClick(e);
                ((Player) event.getWhoClicked()).updateInventory();
                if (e.willClose()) {
                    final Player p = (Player) event.getWhoClicked();
                    Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
                        public void run() {
                            p.closeInventory();
                        }
                    });
                }
                if (e.willDestroy()) {
                    destroy();
                }
            }
        }
    }
 
    public interface OptionClickEventHandler {
        public void onOptionClick(OptionClickEvent event);
    }
 
    public class OptionClickEvent {
        private Player player;
        private int position;
        private String name;
        private boolean close;
        private boolean destroy;
        private ItemStack item;
 
        public OptionClickEvent(Player player, int position, String name, ItemStack item) {
            this.player = player;
            this.position = position;
            this.name = name;
            this.close = true;
            this.destroy = false;
            this.item = item;
        }
 
        public Player getPlayer() {
            return player;
        }
 
        public int getPosition() {
            return position;
        }
 
        public String getName() {
            return name;
        }
 
        public boolean willClose() {
            return close;
        }
 
        public boolean willDestroy() {
            return destroy;
        }
 
        public void setWillClose(boolean close) {
            this.close = close;
        }
 
        public void setWillDestroy(boolean destroy) {
            this.destroy = destroy;
        }
 
        public ItemStack getItem() {
            return item;
        }
    }
 
    private ItemStack setItemNameAndLore(ItemStack item, String name, String[] lore) {
        ItemMeta im = item.getItemMeta();
        im.setDisplayName(name);
        im.setLore(Arrays.asList(lore));
        item.setItemMeta(im);
        return item;
    }
    
    public static RankupIconMenu createMenu(final Configuration config, final Plugin plugin, final Player p){
    	final String menuName = ChatColor.DARK_RED+"["+ChatColor.DARK_AQUA+p.getName()+ChatColor.DARK_RED+"]";
		int menuSize = ((int)(config.getConfigurationSection("Ranks").getKeys(false).size()/9) + 1) * 9;
		Material hasRankMat = Material.matchMaterial(config.getString("RanksMenuOptions.HasRankItemType"));
		Short hasRankData = (short) config.getInt("RanksMenuOptions.HasRankItemDataValue");
		Material notRankMat = Material.matchMaterial(config.getString("RanksMenuOptions.DoesNotHaveRankItemType"));
		Short notRankData = (short) config.getInt("RanksMenuOptions.DoesNotHaveRankItemDataValue");
		final RankupIconMenu menu = new RankupIconMenu(menuName, menuSize, new RankupIconMenu.OptionClickEventHandler() {
            @Override
            public void onOptionClick(RankupIconMenu.OptionClickEvent event){
            	Player player = event.getPlayer();
            	for (String key : config.getConfigurationSection("Ranks").getKeys(false)){
            		if (Util.colorConverter(config.getString("Ranks."+key+".RankPrefix")).equalsIgnoreCase(event.getName())){
	            		if (Rankup.perms.playerInGroup(null, Bukkit.getOfflinePlayer(player.getUniqueId()), key)){
            				for (String cmd : config.getStringList("Ranks."+key+".CommandsToRunOnMenuClick")){
	            				if (cmd.startsWith("*")){
	            					if (player.isOp()){
	            						plugin.getServer().dispatchCommand(player, RankupMessages.formatCmd(cmd, player));
	            					}else{
	            						try{
	            						    player.setOp(true);
	            						    plugin.getServer().dispatchCommand(player, RankupMessages.formatCmd(cmd, player));
	            						}catch(Exception e){
	            						    e.printStackTrace();
	            						}finally{
	            						    player.setOp(false);
	            						}
	            					}
	            				}else if(cmd.startsWith("!")){
	            					plugin.getServer().dispatchCommand(plugin.getServer().getConsoleSender(), RankupMessages.formatCmd(cmd, player));
	            				}else if(cmd.startsWith("/")){
	            					plugin.getServer().dispatchCommand(player, RankupMessages.formatCmd(cmd, player));
	            				}else if(cmd.startsWith("@")){
	            					player.sendMessage(RankupMessages.header+Util.colorConverter(RankupMessages.formatCmd(cmd, player)));
	            				}
	            			}
	            		}else{
	            			plugin.getServer().dispatchCommand(player, "rankup info "+key);
	            		}
            		}
            	}
                event.setWillClose(true);
        	}
        }, plugin);
		menu.setSpecificTo(p);
		int slot = 0;
		for (String key : config.getConfigurationSection("Ranks").getKeys(false)){
			if (Rankup.perms.playerInGroup(null, Bukkit.getOfflinePlayer(p.getUniqueId()), key)){
				ItemStack item = new ItemStack(hasRankMat, 1, hasRankData);
				String[] info = RankupMessages.formatHasRankLore(config, key, p);
				menu.setOption(slot, item, Util.colorConverter(config.getString("Ranks."+key+".RankPrefix")), info);
				slot = slot + 1;
			}else{
				ItemStack item = new ItemStack(notRankMat, 1, notRankData);
				String[] info = RankupMessages.formatDoesNotHaveRankLore(config, key, p);
				menu.setOption(slot, item, Util.colorConverter(config.getString("Ranks."+key+".RankPrefix")), info);
				slot = slot + 1;
			}
		}if (RankupCommandExecutor.allMenus.contains(menu)){
			RankupIconMenu menuToReturn = RankupCommandExecutor.allMenus.get(RankupCommandExecutor.allMenus.indexOf(menu));
			menu.destroy();
			return menuToReturn;
		}else{
			RankupCommandExecutor.allMenus.add(menu);
			return menu;
		}
    }
}