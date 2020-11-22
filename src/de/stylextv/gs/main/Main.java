package de.stylextv.gs.main;

import java.io.File;
import java.util.concurrent.Callable;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.ServicePriority;
import org.bukkit.plugin.java.JavaPlugin;

import de.stylextv.gs.api.GSignsAPI;
import de.stylextv.gs.api.PublicGSignsAPI;
import de.stylextv.gs.command.CommandHandler;
import de.stylextv.gs.command.CommandListener;
import de.stylextv.gs.command.MainTabCompleter;
import de.stylextv.gs.config.ConfigManager;
import de.stylextv.gs.event.EventGui;
import de.stylextv.gs.event.EventItemFrame;
import de.stylextv.gs.event.EventMap;
import de.stylextv.gs.event.EventPlayerInteract;
import de.stylextv.gs.event.EventPlayerJoinQuit;
import de.stylextv.gs.gui.GuiManager;
import de.stylextv.gs.service.AutoUpdater;
import de.stylextv.gs.service.Metrics;
import de.stylextv.gs.world.WorldUtil;

public class Main extends JavaPlugin {
	
	private static Main plugin;
	public static Main getPlugin() {
		return plugin;
	}
	
	private AutoUpdater autoUpdater;
	
	@Override
	public void onEnable() {
		register();
		
		WorldUtil.onEnable();
		Variables.loadScheme();
		CommandHandler.create();
		
		enableAPI();
		enableMetrics();
		enableAutoUpdater();
	}
	private void register() {
		plugin=this;
		Variables.AUTHOR=plugin.getDescription().getAuthors().get(0);
		Variables.VERSION=plugin.getDescription().getVersion();
		
		ConfigManager.onEnable();
		
		MainTabCompleter tabCompleter=new MainTabCompleter();
		getCommand("gs").setExecutor(new CommandListener("gs"));
		getCommand("gs").setTabCompleter(tabCompleter);
		getCommand("gsigns").setExecutor(new CommandListener("gsigns"));
		getCommand("gsigns").setTabCompleter(tabCompleter);
		getCommand("gamemodesigns").setExecutor(new CommandListener("gamemodesigns"));
		getCommand("gamemodesigns").setTabCompleter(tabCompleter);
		
		PluginManager pm=Bukkit.getPluginManager();
		pm.registerEvents(new EventPlayerInteract(), plugin);
		pm.registerEvents(new EventPlayerJoinQuit(), plugin);
		pm.registerEvents(new EventItemFrame(), plugin);
		pm.registerEvents(new EventMap(), plugin);
		pm.registerEvents(new EventGui(), plugin);
	}
	private void enableAPI() {
		getServer().getServicesManager().register(GSignsAPI.class, new PublicGSignsAPI(), this, ServicePriority.Normal);
	}
	private void enableMetrics() {
		Metrics metrics = new Metrics(this, 9419);
		metrics.addCustomChart(new Metrics.SingleLineChart("global_signs", new Callable<Integer>() {
	        @Override
	        public Integer call() throws Exception {
	            return WorldUtil.getTotalAmountOfFrames();
	        }
	    }));
	}
	private void enableAutoUpdater() {
		autoUpdater = new AutoUpdater(plugin);
		autoUpdater.checkAutoUpdater();
	}
	
	@Override
	public void onDisable() {
		GuiManager.onDisable();
		WorldUtil.onDisable();
		autoUpdater.startAutoUpdater();
	}
	
	public void runAutoUpdater(Player p) {
		autoUpdater.runAutoUpdater(p);
	}
	
	public File getPluginFile() {
		return getFile();
	}
	
}
