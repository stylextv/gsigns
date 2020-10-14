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
import de.stylextv.gs.command.CommandGS;
import de.stylextv.gs.command.CommandGSigns;
import de.stylextv.gs.command.CommandGamemodeSigns;
import de.stylextv.gs.command.CommandHandler;
import de.stylextv.gs.command.MainTabCompleter;
import de.stylextv.gs.event.EventItemFrame;
import de.stylextv.gs.event.EventMap;
import de.stylextv.gs.event.EventPlayerInteract;
import de.stylextv.gs.event.EventPlayerJoinQuit;
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
		CommandHandler.create();
		
		enableAPI();
		enableMetrics();
		enableAutoUpdater();
	}
	private void register() {
		plugin=this;
		Variables.AUTHOR=plugin.getDescription().getAuthors().get(0);
		Variables.VERSION=plugin.getDescription().getVersion();
		
		MainTabCompleter tabCompleter=new MainTabCompleter();
		getCommand("gs").setExecutor(new CommandGS());
		getCommand("gs").setTabCompleter(tabCompleter);
		getCommand("gsigns").setExecutor(new CommandGSigns());
		getCommand("gsigns").setTabCompleter(tabCompleter);
		getCommand("gamemodesigns").setExecutor(new CommandGamemodeSigns());
		getCommand("gamemodesigns").setTabCompleter(tabCompleter);
		
		PluginManager pm=Bukkit.getPluginManager();
		pm.registerEvents(new EventPlayerInteract(), plugin);
		pm.registerEvents(new EventPlayerJoinQuit(), plugin);
		pm.registerEvents(new EventItemFrame(), plugin);
		pm.registerEvents(new EventMap(), plugin);
	}
	private void enableAPI() {
		getServer().getServicesManager().register(GSignsAPI.class, new PublicGSignsAPI(), this, ServicePriority.Normal);
	}
	private void enableMetrics() {
		Metrics metrics = new Metrics(this);
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
