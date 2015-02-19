package roamcraft.MrBlobman.RankupPro;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.Configuration;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

public class RankupScoreboard {
	private List<Team> teams;
	private BukkitTask updateTask;
	private Player player;
	private Scoreboard board;
	
	RankupScoreboard(Player player){
		Scoreboard board = ScoreboardManagement.manager.getNewScoreboard();
		this.board = board;
		this.player = player;
		List<Team> teams = new ArrayList<Team>();
		for (int i = 0; i < 16; i++){
			Team team = board.registerNewTeam("Line_"+String.valueOf(i));
			teams.add(i, team);
		}this.teams = teams;
		Objective rankup1 = board.registerNewObjective("buffer1", "dummy");
		Objective rankup2 = board.registerNewObjective("buffer2", "dummy");
		rankup1.setDisplayName(ScoreboardManagement.HEADER);
		rankup2.setDisplayName(ScoreboardManagement.HEADER);
		rankup1.setDisplaySlot(DisplaySlot.SIDEBAR);
		scheduleTask();
		player.setScoreboard(board);
	}
	
	public void scheduleTask(){
		this.updateTask = Bukkit.getScheduler().runTaskTimer(Rankup.plugin, new Runnable(){
			@Override
			public void run(){
				updateScoreboard();
			}
		}, 0L, ScoreboardManagement.UPDATE_DELAY);
	}
	
	public void cancelScoreboardTask(){
		if (this.updateTask != null){
			this.updateTask.cancel();
			this.updateTask = null;
		}
	}
	
	public void toggleBoard(){
		if (this.updateTask != null){
			//Task is scheduled
			player.sendMessage(ChatColor.RED+"Scoreboard has been turned off.");
			player.setScoreboard(ScoreboardManagement.manager.getNewScoreboard());
			cancelScoreboardTask();
		}else{
			player.sendMessage(ChatColor.GREEN+"Scoreboard has been turned on.");
			scheduleTask();
			player.setScoreboard(board);
		}
	}
	
	public void updateScoreboard(){
		Objective objectiveCurrent = board.getObjective(DisplaySlot.SIDEBAR);
		Objective objective = null; //buffer objective
		//Gets correct objective to set as buffer and clear all scores from it
		if (objectiveCurrent.getName().equalsIgnoreCase("buffer1")){
			objective = board.getObjective("buffer2");
			objective.unregister();
			objective = board.registerNewObjective("buffer2", "dummy");
		}else{
			objective = board.getObjective("buffer1");
			objective.unregister();
			objective = board.registerNewObjective("buffer1", "dummy");
		}objective.setDisplayName(ScoreboardManagement.HEADER);
		int counter = 0;
		for (String line : ScoreboardManagement.LINES){
			line = evlauateVariables(line, this.player);
			String[] teamSplit = ScoreboardManagement.formatScoreboardString(line);
			Team team = teams.get(counter);
			if (teamSplit[0] != null){
				team.setPrefix(teamSplit[0]);
			}else{
				team.setPrefix("");
			}
			if (teamSplit[2] != null){
				team.setSuffix(teamSplit[2]);
			}else{
				team.setSuffix("");
			}String entry = teamSplit[1];
			team.addEntry(entry);
			objective.getScore(entry).setScore(ScoreboardManagement.LINES.size()-counter);
			counter = counter + 1;
		}objective.setDisplaySlot(DisplaySlot.SIDEBAR);
	}

	@SuppressWarnings("deprecation") //Bukkit.getOnlinePlayers now returns Collection<? extends Player> not Player[]
	public String evlauateVariables(String line, Player player){
		Configuration config = Rankup.plugin.getConfig();
		boolean isNotTopRank = (RankupCommandExecutor.getNextGroup(player, config) != null ? true : false);
		boolean isOnRankLadder = (RankupCommandExecutor.getCurrentGroup(player, config) != null ? true : false);
		double playerBal = Rankup.econ.getBalance(player.getPlayer());
		if (line.contains("<currentRank>")){
			String currentRankPrefix = "";
			if (isOnRankLadder){
				currentRankPrefix = Util.colorConverter(config.getString("Ranks."+RankupCommandExecutor.getCurrentGroup(player, config)+".RankPrefix"));
			}
			line = line.replace("<currentRank>", currentRankPrefix);
		}if (line.contains("<nextRank>")){
			String nextRankPrefix = Util.randomChatColor() + "You are Top Rank!";
			if (isNotTopRank){
				nextRankPrefix = Util.colorConverter(config.getString("Ranks."+RankupCommandExecutor.getNextGroup(player, config)+".RankPrefix"));
			}
			line = line.replace("<nextRank>", nextRankPrefix);
		}if (line.contains("<balance>")){
			line = line.replace("<balance>", Util.formatMoney(playerBal));
		}if (line.contains("<nextRankPrice>")){
			line = line.replace("<nextRankPrice>", Util.formatMoney(config.getDouble("Ranks."+RankupCommandExecutor.getNextGroup(player, config)+".RankPrice")));
		}if (line.contains("<costLeftToRankup>")){
			double amtNeeded = 0.0;
			if (isNotTopRank){
				amtNeeded = config.getDouble("Ranks."+RankupCommandExecutor.getNextGroup(player, config)+".RankPrice") - playerBal;
			}
			line = line.replace("<costLeftToRankup>", Util.formatMoney((amtNeeded > 0) ? amtNeeded : 0d));
		}if (line.contains("<%ofRankupCostPlayerHas>") || line.contains("<progressBar>")){
			double rankupCost = 0.0;
			if (isNotTopRank){
				rankupCost = config.getDouble("Ranks."+RankupCommandExecutor.getNextGroup(player, config)+".RankPrice");
			}
			if (rankupCost <= 0){
				if (isNotTopRank){
					if (!line.contains("<progressBar>")){
						line = ChatColor.AQUA+""+ChatColor.BOLD+"/rankup";
					}else{
						line.replace("<progressBar>", Util.getProgressBar(100, ChatColor.AQUA, ChatColor.DARK_RED));
					}
				}else{
					line = line.replace("<%ofRankupCostPlayerHas>", Util.formatPercent(100)).replace("<progressBar>", Util.getProgressBar(100, ChatColor.AQUA, ChatColor.DARK_RED));
				}
			}else{
				double percentageGotten = (playerBal > 0) ? ((playerBal/rankupCost)*100) : 0d;
				if (percentageGotten < 100){
					line = line.replace("<%ofRankupCostPlayerHas>", Util.formatPercent(percentageGotten)).replace("<progressBar>", Util.getProgressBar(percentageGotten, ChatColor.AQUA, ChatColor.DARK_RED));
				}else{
					if (isNotTopRank){
						if (!line.contains("<progressBar>")){
							line = ChatColor.AQUA+""+ChatColor.BOLD+"/rankup";
						}else{
							line.replace("<progressBar>", Util.getProgressBar(100, ChatColor.AQUA, ChatColor.DARK_RED));
						}
					}else{
						line = line.replace("<%ofRankupCostPlayerHas>", Util.formatPercent(100)).replace("<progressBar>", Util.getProgressBar(100, ChatColor.AQUA, ChatColor.DARK_RED));
					}
				}
			}
		}if (line.contains("<playersOnline>")){
			line = line.replace("<playersOnline>", String.valueOf(Bukkit.getOnlinePlayers().length));
		}if (line.contains("<playerName>")){
			line = line.replace("<playerName>", player.getName());
		}return line;
	}
	
}
