package de.stylextv.gs.main;

import de.stylextv.gs.world.WorldUtil;
import net.md_5.bungee.api.ChatColor;

public class Variables {
	
	public static String NAME="GSigns";
	
	public static String COLOR1="§d";
	public static String COLOR2="§5";
	
	public static String PREFIX;
	public static String PREFIX_CONSOLE=NAME+" | ";
	
	public static String AUTHOR;
	public static String VERSION;
	
	public static void loadScheme() {
		if(WorldUtil.getMcVersion() >= WorldUtil.MCVERSION_1_16) {
			COLOR1 = ChatColor.of("#E91E63").toString();
			COLOR2 = ChatColor.of("#89133C").toString();
		}
		PREFIX=COLOR1+"§lGS §8> §7";
	}
	
}
