package roamcraft.MrBlobman.RankupPro;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.Configuration;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.ScoreboardManager;

public class ScoreboardManagement {
	
	public static String HEADER;
	public static List<String> LINES;
	public static Long UPDATE_DELAY;
	public static ScoreboardManager manager;
	
	public static Map<Player, RankupScoreboard> allScoreboards = new HashMap<Player, RankupScoreboard>();
	
	ScoreboardManagement(){
		ScoreboardManagement.manager = Bukkit.getScoreboardManager();
		Configuration config = Rankup.plugin.getConfig();
		if (config.contains("ScoreboardOptions.UpdateDelay")){
			ScoreboardManagement.UPDATE_DELAY = 20L * config.getLong("ScoreboardOptions.UpdateDelay");
		}else{
			ScoreboardManagement.UPDATE_DELAY = 5L*20L;
		}
		if (config.contains("ScoreboardOptions.Layout.Header")){
			String temp = config.getString("ScoreboardOptions.Layout.Header");
			if (temp.length() > 32){
				temp = temp.substring(0, 32);
			}ScoreboardManagement.HEADER = ChatColor.translateAlternateColorCodes('&', temp);
		}else{
			ScoreboardManagement.HEADER = ChatColor.AQUA+""+ChatColor.BOLD+"[ Info ]";
		}
		if (config.contains("ScoreboardOptions.Layout.Body")){
			ScoreboardManagement.LINES = config.getStringList("ScoreboardOptions.Layout.Body");
		}else{
			ScoreboardManagement.LINES = new ArrayList<String>();
		}
	}
	
	public static String[] formatScoreboardString(String line){
		String[] formattedLine = new String[3];
		if (line.length() > 45){
			line = ChatColor.translateAlternateColorCodes('&', line.substring(0, 45));
		}else{
			line = ChatColor.translateAlternateColorCodes('&', line);
		}
		if (line.length() > 30){
			//We need to use a prefix and suffix
			formattedLine[0] = line.substring(0, 15);
			formattedLine[1] = line.substring(15, 30);
			formattedLine[2] = line.substring(30, line.length());
		}else if (line.length() > 15){
			//We just need a prefix
			formattedLine[0] = line.substring(0, 15);
			formattedLine[1] = line.substring(15, line.length());
			formattedLine[2] = null;
		}else{
			//We dont need a prefix or suffix
			formattedLine[0] = null;
			formattedLine[1] = line;
			formattedLine[2] = null;
		}
		return formattedLine;
	}
}
