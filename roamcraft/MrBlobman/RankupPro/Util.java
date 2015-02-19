package roamcraft.MrBlobman.RankupPro;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import org.bukkit.ChatColor;

public class Util {
	public static String formatPercent(double amt){
		if (amt > 100.0){
			amt = 100.0;
		}else if (amt < 0.0){
			amt = 0.0;
		}return String.format("%.2f%%", amt);
	}
	
	public static String formatMoney(double amt){
		//Used for the scoreboard, amt needed stays at 0 if you have more than amt needed
		if (amt <= 0){
			amt = 0;
		}if (amt >= 1000000000000.0){
			return String.format("%.2fT", amt/ 1000000000000.0);
		}else if (amt >= 1000000000.0){
			return String.format("%.2fB", amt/ 1000000000.0);
		}else if (amt >= 1000000.0){
			return String.format("%.2fM", amt/ 1000000.0);
		}else if (amt >= 100000.0){
			return String.format("%.2fK", amt/ 1000.0);
		}else{
			return NumberFormat.getCurrencyInstance(Locale.CANADA).format(amt);
		}
	}
	
	public static ChatColor configToColor(String code){
		if (code.length() == 1){
			return ChatColor.getByChar(code);
		}else{
			return null;
		}
	}
	
	private static int index = 0;
	public static List<ChatColor> colors = Arrays.asList(ChatColor.DARK_RED, ChatColor.RED, ChatColor.GOLD, ChatColor.YELLOW, ChatColor.GREEN, ChatColor.DARK_GREEN, ChatColor.AQUA, ChatColor.DARK_AQUA, ChatColor.BLUE, ChatColor.DARK_PURPLE, ChatColor.LIGHT_PURPLE);
	public static ChatColor randomChatColor(){
		index = index + 1;
		return colors.get(index%11);
	}
	
	// Converts a string with & symbols to the corresponding ChatColor
	public static String colorConverter(String messageUncolored) {
		String coloredMessage = ChatColor.translateAlternateColorCodes('&', messageUncolored);
		return coloredMessage;
	}
	
	// Converts an entire list of strings using the colorConverter method above
	public static List<String> convertMessage(List<String> Messages) {
		List<String> coloredListOfMessages = new ArrayList<String>();
		for (String Line : Messages) {
			coloredListOfMessages.add(colorConverter(Line));
		}
		return coloredListOfMessages;
	}
	
	public static String getProgressBar(double percentage, ChatColor barColor, ChatColor fillColor){
		//we have 48chars to work with - 8 color chars
		//1 bar = 2.5%
		int numColoredBars = (int) (percentage/2.70);
		int numFillBars = 37-numColoredBars;
		String coloredBars = fillColor + "";
		while (numColoredBars > 0){
			coloredBars = coloredBars+"|";
			numColoredBars--;
		}
		String fillBars = barColor + "";
		while (numFillBars > 0){
			fillBars = fillBars+"|";
			numFillBars--;
		}
		return coloredBars + fillBars;
	}
}
