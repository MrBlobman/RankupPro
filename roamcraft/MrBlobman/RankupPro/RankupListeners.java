package roamcraft.MrBlobman.RankupPro;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class RankupListeners implements Listener{
	
	@EventHandler
	public void onJoin(PlayerJoinEvent event){
		if (ScoreboardManagement.allScoreboards.containsKey(event.getPlayer())){
			ScoreboardManagement.allScoreboards.get(event.getPlayer()).cancelScoreboardTask();
		}
		ScoreboardManagement.allScoreboards.put(event.getPlayer(), new RankupScoreboard(event.getPlayer()));
	}
	
	@EventHandler
	public void onQuit(PlayerQuitEvent event){
		if (ScoreboardManagement.allScoreboards.containsKey(event.getPlayer())){
			ScoreboardManagement.allScoreboards.get(event.getPlayer()).cancelScoreboardTask();
			ScoreboardManagement.allScoreboards.remove(event.getPlayer());
		}
	}
}
