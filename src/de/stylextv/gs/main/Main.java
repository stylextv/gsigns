package de.stylextv.gs.main;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.util.concurrent.Callable;

import org.bukkit.Bukkit;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import de.stylextv.gs.bstats.Metrics;
import de.stylextv.gs.command.CommandGS;
import de.stylextv.gs.command.CommandGSigns;
import de.stylextv.gs.command.CommandGamemodeSigns;
import de.stylextv.gs.command.MainTabCompleter;
import de.stylextv.gs.event.EventPlayerInteract;
import de.stylextv.gs.event.EventPlayerJoinQuit;
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
		
		enableBStats();
		checkAutoUpdaterResult();
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
		pm.registerEvents(new EventPlayerJoinQuit(), plugin);
	}
	private void enableBStats() {
		Metrics metrics = new Metrics(this);
		metrics.addCustomChart(new Metrics.SingleLineChart("global_signs", new Callable<Integer>() {
	        @Override
	        public Integer call() throws Exception {
	            return WorldUtil.getTotalAmountOfFrames();
	        }
	    }));
	}
	private void startAutoUpdater() {
		try {
			double d=Double.valueOf(Vars.VERSION);
			int i=(int)(d*10)+1;
			String fileUrl=i/10+"."+i%10;
			URL url = new URL("https://github.com/StylexTV/GSigns/raw/master/version/"+fileUrl+".jar");
			ReadableByteChannel rbc = Channels.newChannel(url.openStream());
			FileOutputStream fos = new FileOutputStream(plugin.getFile());
			fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
			fos.close();
			
			BufferedWriter writer = new BufferedWriter(new FileWriter(plugin.getDataFolder().getPath()+"/au-result"));
		    writer.close();
		} catch(Exception ex) {}
	}
	private void checkAutoUpdaterResult() {
		File f=new File(plugin.getDataFolder().getPath()+"/au-result");
		if(f.exists()) {
			f.delete();
			new BukkitRunnable() {
				@Override
				public void run() {
					Bukkit.getConsoleSender().sendMessage(Vars.PREFIX_CONSOLE+"A new update has been §ainstalled§r. Version: "+Vars.VERSION);
					Bukkit.getConsoleSender().sendMessage(Vars.PREFIX_CONSOLE+"The changelog can be found here:");
					Bukkit.getConsoleSender().sendMessage(Vars.PREFIX_CONSOLE+"https://www.spigotmc.org/resources/g-signs-a-unique-map-signs-plugin-for-lobbies.73693/updates");
				}
			}.runTaskLaterAsynchronously(plugin, 5);
		}
	}
	
	@Override
	public void onDisable() {
		WorldUtil.onDisable();
		startAutoUpdater();
	}
	
}
