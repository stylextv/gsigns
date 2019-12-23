package de.stylextv.gs.main;

import java.awt.Font;
import java.awt.GraphicsEnvironment;

import org.bukkit.Bukkit;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import de.stylextv.gs.command.CommandGS;
import de.stylextv.gs.command.CommandGSigns;
import de.stylextv.gs.command.CommandGamemodeSigns;
import de.stylextv.gs.command.MainTabCompleter;
import de.stylextv.gs.event.EventPlayerInteract;
import de.stylextv.gs.player.PlayerManager;
import de.stylextv.gs.world.WorldUtil;

public class Main extends JavaPlugin {
	
	private static Main plugin;
	public static Main getPlugin() {
		return plugin;
	}
	
	@Override
	public void onEnable() {
		register();
		
		WorldUtil.onEnable();
		PlayerManager.init();
	}
	private void register() {
		plugin=this;
		Vars.AUTHOR=plugin.getDescription().getAuthors().get(0);
		Vars.VERSION=plugin.getDescription().getVersion();
		
		MainTabCompleter tabCompleter=new MainTabCompleter();
		getCommand("gs").setExecutor(new CommandGS());
		getCommand("gs").setTabCompleter(tabCompleter);
		getCommand("gsigns").setExecutor(new CommandGSigns());
		getCommand("gsigns").setTabCompleter(tabCompleter);
		getCommand("gamemodesigns").setExecutor(new CommandGamemodeSigns());
		getCommand("gamemodesigns").setTabCompleter(tabCompleter);
		
		PluginManager pm=Bukkit.getPluginManager();
		pm.registerEvents(new EventPlayerInteract(), plugin);
		
		try {
		    GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
		    ge.registerFont(Font.createFont(Font.TRUETYPE_FONT, Main.class.getClassLoader().getResourceAsStream("assets/fonts/Raleway-Bold.ttf")));
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	
	@Override
	public void onDisable() {
		WorldUtil.onDisable();
	}
	
}
