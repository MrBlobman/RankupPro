package roamcraft.MrBlobman.RankupPro;

import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.permission.Permission;

import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

public class Rankup extends JavaPlugin{
	public static Rankup plugin;
	public static Economy econ = null;
	public static Permission perms = null;
	
	public void onDisable(){
		destroyMenus();
	}
	
	public void onEnable(){
		Rankup.plugin = this;
		PluginDescriptionFile pdfFile = getDescription();
		this.getLogger().info(pdfFile.getName() + " Version  " + pdfFile.getVersion() + " has been enabled!");
		this.saveDefaultConfig();
		if (!setupEconomy() ) {
			getLogger().severe(String.format("[%s] - Disabled due to no Vault dependency found!", getDescription().getName()));
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
		setupPermissions();
		registerCmds();
		new ScoreboardManagement();
		this.getServer().getPluginManager().registerEvents(new RankupListeners(), this);
		ifReload();
	}
	
	private boolean setupEconomy() {
        if (getServer().getPluginManager().getPlugin("Vault") == null) {
            return false;
        }
        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            return false;
        }
        econ = rsp.getProvider();
        return econ != null;
    }
	
	private boolean setupPermissions() {
        RegisteredServiceProvider<Permission> rsp = getServer().getServicesManager().getRegistration(Permission.class);
        perms = rsp.getProvider();
        return perms != null;
    }
	
	private void registerCmds(){
		getCommand("rankup").setExecutor(new RankupCommandExecutor(this));
		getCommand("ru").setExecutor(new RankupCommandExecutor(this));
		getCommand("rank").setExecutor(new RankupCommandExecutor(this));
		getCommand("buyrank").setExecutor(new RankupCommandExecutor(this));
		getCommand("ranks").setExecutor(new RankupCommandExecutor(this));
	}
	
	private void ifReload(){
		if(!RankupCommandExecutor.allMenus.isEmpty()){
			for (RankupIconMenu menu : RankupCommandExecutor.allMenus){
				menu.destroy();
			}RankupCommandExecutor.allMenus.clear();
		}
	}
	private void destroyMenus(){
		for (Player player : RankupCommandExecutor.menus.keySet()){
			RankupCommandExecutor.menus.get(player).destroy();
			RankupCommandExecutor.menus.remove(player);
		}RankupCommandExecutor.groups.clear();
	}
	
}
