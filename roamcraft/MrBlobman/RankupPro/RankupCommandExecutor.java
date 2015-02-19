package roamcraft.MrBlobman.RankupPro;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.configuration.Configuration;
import org.bukkit.entity.Player;

public class RankupCommandExecutor implements CommandExecutor{
	private Rankup plugin;
	public RankupCommandExecutor(Rankup instance){
		this.plugin = instance;
	}
	
	public static String getNextGroup(Player player, Configuration config){
		String nextGroup = null;
		List<String> RankLadder = new ArrayList<String>();
		for (String key : config.getConfigurationSection("Ranks").getKeys(false)){
			RankLadder.add(key);
		}
		if (RankLadder.isEmpty()){
			player.sendMessage(RankupMessages.header+ ChatColor.DARK_RED+ " There are no ranks defined in the list in the config!");
		}
		for (String rank : RankLadder){
			if (!Rankup.perms.playerInGroup(null, Bukkit.getOfflinePlayer(player.getUniqueId()), rank)){
				nextGroup = rank;
				return nextGroup;
			}
		}
		return nextGroup;
	}public static String getCurrentGroup(Player player, Configuration config){
		String currentGroup = null;
		List<String> RankLadder = new ArrayList<String>();
		for (String key : config.getConfigurationSection("Ranks").getKeys(false)){
			RankLadder.add(key);
		}
		if (RankLadder.isEmpty()){
			player.sendMessage(RankupMessages.header+ ChatColor.DARK_RED+ " There are no ranks defined in the list in the config!");
		}
		for (String rank : RankLadder){
			if (!Rankup.perms.playerInGroup(null, Bukkit.getOfflinePlayer(player.getUniqueId()), rank)){
				if (RankLadder.indexOf(rank) == 0){
					return null;
				}else{
					currentGroup = RankLadder.get(RankLadder.indexOf(rank) - 1);
					return currentGroup;
				}
			}
		}if (currentGroup == null && Rankup.perms.playerInGroup(null, Bukkit.getOfflinePlayer(player.getUniqueId()), RankLadder.get(RankLadder.size() - 1))){
			return RankLadder.get(RankLadder.size() - 1);
		}else{
			return null;
		}
	}public static Double calcDiscount(Configuration config, UUID uuid, double rankupPrice){
		double maxDiscount = 0;
		for (String perm : config.getConfigurationSection("Discounts").getKeys(false)){
			if (Rankup.perms.playerInGroup(null, Bukkit.getOfflinePlayer(uuid), perm)){
				double getDiscount = Double.parseDouble(config.getString("Discounts."+perm).replaceAll("%", ""));
				if (maxDiscount < getDiscount){
					maxDiscount = getDiscount;
				}
			}
		}
		rankupPrice = rankupPrice - (rankupPrice * (maxDiscount/100));
		return rankupPrice;
	}public static String getBestDiscount(Configuration config, UUID uuid){
		double maxDiscount = 0;
		String discount = null;
		for (String perm : config.getConfigurationSection("Discounts").getKeys(false)){
			if (Rankup.perms.playerInGroup(null, Bukkit.getOfflinePlayer(uuid), perm)){
				double getDiscount = Double.parseDouble(config.getString("Discounts."+perm).replaceAll("%", ""));
				if (maxDiscount < getDiscount){
					maxDiscount = getDiscount;
					discount = config.getString("Discounts."+perm);
				}
			}
		}if (discount == null){
			return "0%";
		}else{
			return discount;
		}
	}
	
	public static Map<Player, RankupIconMenu> menus = new HashMap<Player, RankupIconMenu>();
	public static List<RankupIconMenu> allMenus = new ArrayList<RankupIconMenu>();
	public static Map<Player, String[]> groups = new HashMap<Player, String[]>();
	
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args){
		Configuration config = plugin.getConfig();
		if (sender instanceof Player){
			final Player player = (Player)sender;
			String playerName = player.getName();
			if (commandLabel.equalsIgnoreCase("rankup") || commandLabel.equalsIgnoreCase("ru") || commandLabel.equalsIgnoreCase("rank") || commandLabel.equalsIgnoreCase("buyrank")){
				//Player typed /rankup
				if (args.length == 0){
					if (getNextGroup(player, config) != null){
						double rankupPrice = plugin.getConfig().getDouble("Ranks."+getNextGroup(player, config)+".RankPrice");
						double bal = Rankup.econ.getBalance(Bukkit.getOfflinePlayer(player.getUniqueId()));
						if (plugin.getConfig().getBoolean("DiscountsEnabled")){
							rankupPrice = calcDiscount(plugin.getConfig(), player.getUniqueId(), rankupPrice);
						}
						//Player has enough money to rankup
						if (bal >= rankupPrice){
							//Take the rankup price from their bank, add them to the next group, tell them they ranked up
							player.sendMessage(RankupMessages.rankedup(plugin.getConfig(), playerName, getNextGroup(player, config), rankupPrice));
							if (!plugin.getConfig().getString("Messages.playerRankedUpBroadcast").isEmpty()){
								RankupMessages.broadcastPlayerRankedUp(plugin.getConfig(), playerName, plugin.getConfig().getString("Ranks."+getNextGroup(player, config)+".RankPrefix"));
							}Rankup.econ.withdrawPlayer(Bukkit.getOfflinePlayer(player.getUniqueId()), rankupPrice);
							String previousRank = getCurrentGroup(player, config);
							Rankup.perms.playerAddGroup(null, Bukkit.getOfflinePlayer(player.getUniqueId()),getNextGroup(player, config));
							if (!Rankup.perms.playerInGroup(null, Bukkit.getOfflinePlayer(player.getUniqueId()), previousRank)){
								Rankup.perms.playerAddGroup(null, Bukkit.getOfflinePlayer(player.getUniqueId()), previousRank);
							}
						//Player does NOT have enough to rankup
						}else if(bal < rankupPrice){
							//Tell the player how much more money they need to rankup
							player.sendMessage(RankupMessages.needMoreMoneyToRankup(plugin.getConfig(), playerName, getNextGroup(player, config), bal, rankupPrice));
						}else{
							player.sendMessage("Something is terribly wrong");
						}
					}
					else if (getNextGroup(player, config) == null){
						player.sendMessage(RankupMessages.header+ ChatColor.AQUA+ "You are already the top rank! Congrats!");
					}return true;
				}
				else if (args.length == 1){
					if (args[0].equalsIgnoreCase("info")){
				        //Player typed /rankup info
						if (getNextGroup(player, config) != null){
								//Give them info about the next rank
								RankupMessages.tellPlayerNextRankInfo(plugin.getConfig(), getNextGroup(player, config), player);
								return true;
						}
						else if (getNextGroup(player, config) == null){
							player.sendMessage(RankupMessages.header+ ChatColor.AQUA+ "You are already the top rank! Congrats!");
							return true;
						}
					}else if (args[0].equalsIgnoreCase("toggledisplay") || args[0].equalsIgnoreCase("td")){
						if (ScoreboardManagement.allScoreboards.containsKey(player)){
							ScoreboardManagement.allScoreboards.get(player).toggleBoard();
						}
						return true;
					}else if (args[0].equalsIgnoreCase("reload") && player.isOp()){
						plugin.reloadConfig();
						if (!allMenus.isEmpty()){
							for (RankupIconMenu menu : allMenus){
								menu.destroy();
							}
							allMenus.clear();
						}
						menus.clear();
						groups.clear();
						player.sendMessage(RankupMessages.header+ChatColor.DARK_PURPLE+"Rankup has been reloaded.");
					}else if (args[0].equalsIgnoreCase("help")){
						player.sendMessage(RankupMessages.header+ChatColor.DARK_PURPLE+"Rankup help!");
						player.sendMessage(ChatColor.YELLOW+"/rankup"+ChatColor.DARK_PURPLE+" Adds you to the next rank permitting you can afford it.");
						player.sendMessage(ChatColor.YELLOW+"/rankup info"+ChatColor.DARK_PURPLE+" Tells you the information about the next rank that your server owner has set.");
						player.sendMessage(ChatColor.YELLOW+"/rankup info <rankname>"+ChatColor.DARK_PURPLE+" Tells you info about a specific rank.");
						if (config.getBoolean("ScoreboardEnabled")){
							player.sendMessage(ChatColor.YELLOW+"/rankup toggledisplay"+ChatColor.DARK_PURPLE+" Toggles the scoreboard on the right of your screen on or off.");
						}player.sendMessage(ChatColor.YELLOW+"/ranks"+ChatColor.DARK_PURPLE+" Opens a gui displaying the ranks and thier prices.");
					}return true;
				}
				else if (args.length == 2){
					//Player typed /rankup info <rankname>
					if (args[0].equalsIgnoreCase("info")){
						boolean correctRank = false;
						for (String key : plugin.getConfig().getConfigurationSection("Ranks").getKeys(false)){
							if (key.equalsIgnoreCase(args[1])){
								if (plugin.getConfig().getString("Ranks."+args[1]) != null){
									RankupMessages.tellPlayerNextRankInfo(plugin.getConfig(), args[1], player);
									correctRank = true;
									break;
								}else if (plugin.getConfig().getString("Ranks."+args[1].toUpperCase()) != null){
									RankupMessages.tellPlayerNextRankInfo(plugin.getConfig(), args[1].toUpperCase(), player);
									correctRank = true;
									break;
								}
							}
						}
						if (!correctRank){
							String ranks = "";
							for (String key : plugin.getConfig().getConfigurationSection("Ranks").getKeys(false)){
								ranks = ranks+key+", ";
							}ranks = ranks.substring(0, ranks.length() - 2);
							player.sendMessage(RankupMessages.header+ChatColor.AQUA+"Please specify one of these valid ranks "+ranks+".");
						}
					return true;
					}
				}
			}else if (commandLabel.equalsIgnoreCase("ranks")){
				if (!menus.containsKey(player)){
					RankupIconMenu menu = RankupIconMenu.createMenu(config, plugin, player);
					String[] playersGroups = Rankup.perms.getGroups();
					menus.put(player, menu);
					groups.put(player, playersGroups);
					menus.get(player).open(player);
				}else{
					if (!groups.get(player).equals(Rankup.perms.getGroups())){
						allMenus.remove(allMenus.indexOf(menus.get(player)));
						menus.get(player).destroy();
						RankupIconMenu newMenu = RankupIconMenu.createMenu(config, plugin, player);
						String[] playersGroups = Rankup.perms.getGroups();
						menus.put(player, newMenu);
						groups.put(player, playersGroups);
						menus.get(player).open(player);
					}else{
						menus.get(player).open(player);
					}
				}
				return true;
			}
		}else if (sender instanceof ConsoleCommandSender){
			if (commandLabel.equalsIgnoreCase("rankup") && (args.length == 1) && (args[0].equalsIgnoreCase("reload"))){
				plugin.reloadConfig();
				if (!allMenus.isEmpty()){
					for (RankupIconMenu menu : allMenus){
						menu.destroy();
					}
					allMenus.clear();
				}
				menus.clear();
				groups.clear();
				sender.sendMessage(RankupMessages.header+ChatColor.DARK_PURPLE+"Rankup has been reloaded.");
			}return true;
		}
		return false;
	}
}
