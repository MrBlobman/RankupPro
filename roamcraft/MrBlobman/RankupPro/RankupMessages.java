package roamcraft.MrBlobman.RankupPro;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.Configuration;
import org.bukkit.entity.Player;

public class RankupMessages {
	
	public static String header = ChatColor.DARK_RED+"[" + ChatColor.AQUA+ "RCRankup"+ ChatColor.DARK_RED+ "] ";
	
	public static void tellPlayerNextRankInfo(Configuration config, String NextRankName, Player player){
		player.sendMessage(header+ChatColor.AQUA+"Info about "+Util.colorConverter(config.getString("Ranks."+NextRankName+".RankPrefix")));
		for (String line : Util.convertMessage(config.getStringList("Ranks."+NextRankName+".RankInfo"))){
			player.sendMessage(line);
		}
	}
	
	public static void tellPlayerRanksMessages(Configuration config, Player player){
		player.sendMessage(header+ ChatColor.AQUA+ "Ranks and rankup prices.");
		for (String key : config.getConfigurationSection("Ranks").getKeys(false)){
			String rank = Util.colorConverter(config.getString("Ranks."+key+".RankPrefix"));
			String price = ChatColor.AQUA+Util.colorConverter(config.getString("Ranks."+key+".RankPrice"));
			if (rank == null || price == null){
				player.sendMessage(ChatColor.RED+"Missing config settings");
			}else{
				player.sendMessage("     "+rank+ChatColor.DARK_GRAY+" : "+ ChatColor.AQUA+ price);
			}
		}
	}
	
	@SuppressWarnings("deprecation")
	public static void broadcastPlayerRankedUp(Configuration config, String playerName, String nextRankPrefix){
		if (config.getString("Messages.playerRankedUpBroadcast") instanceof String){
			String BCMessage = header+ Util.colorConverter(config.getString("Messages.playerRankedUpBroadcast").replace("%player%", playerName).replace("%rankPrefix%", nextRankPrefix));
			for (Player player : Bukkit.getServer().getOnlinePlayers()){
				if (!player.equals(Bukkit.getPlayer(playerName))){
					player.sendMessage(BCMessage);
				}
			}
		}
	}
	
	public static String needMoreMoneyToRankup(Configuration config, String name, String nextRank, double bal, double rankprice){
		String nextRankPrefix = config.getString("Ranks."+ nextRank+ ".RankPrefix");
		double amountRequired = rankprice - bal;
		String amtReq = Util.formatMoney(amountRequired);
		return header+ Util.colorConverter(config.getString("Messages.needMoreMoneyToRankup").replace("%rankPrefix%", nextRankPrefix).replace("%amtNeeded%", amtReq));
	}
	
	public static String rankedup(Configuration config, String name, String nextRank, double rankprice){
		String nextRankPrefix = config.getString("Ranks."+ nextRank+ ".RankPrefix");
		String amtReq = Util.formatMoney(rankprice);
		String replacedString = Util.colorConverter(config.getString("Messages.rankedUp").replace("%rankPrefix%", nextRankPrefix).replace("%amtNeeded%", amtReq));
		return header+ replacedString;
	}
	
	public static String formatCmd(String cmd, Player player){
		return cmd.replace("%player%", player.getName()).substring(1);
	}
	
	public static String[] formatHasRankLore(Configuration config, String key, Player player){
		List<String> lore = config.getStringList("Ranks."+key+".RanksMenuHasRankLore");
		List<String> newLore = new ArrayList<String>();
		String discount = RankupCommandExecutor.getBestDiscount(config, player.getUniqueId());
		String name = player.getName();
		String price = Util.formatMoney(RankupCommandExecutor.calcDiscount(config, player.getUniqueId(), config.getDouble("Ranks."+key+".RankPrice")));
		for (String line : lore){
			if (line.contains("%discount%")){
				if (!discount.equalsIgnoreCase("0%")){
					String toAdd = Util.colorConverter(line.replace("%playerName%", name).replace("%discount%", discount).replace("%rankPrice%", price));
					newLore.add(toAdd);
				}
			}else{
				String toAdd = Util.colorConverter(line.replace("%playerName%", name).replace("%rankPrice%", price));
				newLore.add(toAdd);
			}
		}
		String[] loreArray = newLore.toArray(new String[newLore.size()]);
		return loreArray;
	}
	public static String[] formatDoesNotHaveRankLore(Configuration config, String key, Player player){
		List<String> lore = config.getStringList("Ranks."+key+".RanksMenuDoesNotHaveRankLore");
		List<String> newLore = new ArrayList<String>();
		String discount = RankupCommandExecutor.getBestDiscount(config, player.getUniqueId());
		String name = player.getName();
		String price = Util.formatMoney(RankupCommandExecutor.calcDiscount(config, player.getUniqueId(), config.getDouble("Ranks."+key+".RankPrice")));
		for (String line : lore){
			if (line.contains("%discount%")){
				if (!discount.equalsIgnoreCase("0%")){
					String toAdd = Util.colorConverter(line.replace("%playerName%", name).replace("%discount%", discount).replace("%rankPrice%", price));
					newLore.add(toAdd);
				}
			}else{
				String toAdd = Util.colorConverter(line.replace("%playerName%", name).replace("%rankPrice%", price));
				newLore.add(toAdd);
			}
		}
		String[] loreArray = newLore.toArray(new String[newLore.size()]);
		return loreArray;
	}
}
