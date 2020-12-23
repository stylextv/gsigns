package de.stylextv.gs.service;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.util.zip.Inflater;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import de.stylextv.gs.lang.LanguageManager;
import de.stylextv.gs.main.Main;
import de.stylextv.gs.main.Variables;
import de.stylextv.gs.world.WorldUtil;

public class AutoUpdater {
	
	private static String CHAT_LINE;
	static {
		if(WorldUtil.getMcVersion()<WorldUtil.MCVERSION_1_13) {
			CHAT_LINE = "§8§m----------------------------------------";
		} else {
			CHAT_LINE = "§8§m⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯";
		}
	}
	
	private Main plugin;
	
	private String updateRequest;
	private boolean inUpdateCheck;
	
	public AutoUpdater(Main plugin) {
		this.plugin = plugin;
	}
	
	public void startAutoUpdater() {
		if(updateRequest!=null) try {
			URL url = new URL("https://github.com/StylexTV/GSigns/raw/master/version/"+updateRequest+".dat");
			
			ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		    try {
		        byte[] chunk = new byte[4096];
		        int bytesRead;
		        InputStream stream = url.openStream();
		        
		        while ((bytesRead = stream.read(chunk)) > 0) {
		            outputStream.write(chunk, 0, bytesRead);
		        }
		        
		    } catch (IOException ex) {
		        ex.printStackTrace();
		    }
		    byte[] bytes = outputStream.toByteArray();
		    
		    Inflater inflater = new Inflater();
			inflater.setInput(bytes);
			FileOutputStream fos = new FileOutputStream(plugin.getPluginFile());
			byte[] buffer = new byte[bytes.length*2];
			while(!inflater.finished()) {
				int count = inflater.inflate(buffer);
				fos.write(buffer, 0, count);
			}
			fos.close();
			inflater.end();
			
			BufferedWriter writer = new BufferedWriter(new FileWriter("plugins/GSigns/au-result"));
			writer.write(Variables.VERSION);
		    writer.close();
		} catch(Exception ex) {ex.printStackTrace();}
	}
	public void checkAutoUpdater() {
		File f=new File("plugins/GSigns/au-result");
		if(f.exists()) {
			new BukkitRunnable() {
				@Override
				public void run() {
					String past=null;
					try {
						BufferedReader reader = new BufferedReader(new FileReader(f));
						past=reader.readLine();
						reader.close();
					} catch (IOException ex) {ex.printStackTrace();}
					f.delete();
					
					final String pastF=past;
					new BukkitRunnable() {
						@Override
						public void run() {
							Bukkit.getConsoleSender().sendMessage(Variables.PREFIX_CONSOLE+LanguageManager.parseMsg("trans.autoupdate.console.installed1", pastF, Variables.VERSION));
							Bukkit.getConsoleSender().sendMessage(Variables.PREFIX_CONSOLE+LanguageManager.parseMsg("trans.autoupdate.console.installed2"));
							Bukkit.getConsoleSender().sendMessage(Variables.PREFIX_CONSOLE+"https://www.spigotmc.org/resources/85704/updates");
						}
					}.runTask(plugin);
				}
			}.runTaskLaterAsynchronously(plugin, 5);
		}
		
		new BukkitRunnable() {
			@Override
			public void run() {
				try {
					int currentVersion=(int) (Double.valueOf(Variables.VERSION)*10);
					int future=1;
					String found=null;
					while(future<100) {
						int i=currentVersion+future;
						try {
							String fileUrl=i/10+"."+i%10;
							URL url = new URL("https://github.com/StylexTV/GSigns/raw/master/version/"+fileUrl+".dat");
							ReadableByteChannel rbc = Channels.newChannel(url.openStream());
							rbc.close();
							found=fileUrl;
						} catch(Exception ex) {
							break;
						}
					    
					    future++;
					}
					
					if(found!=null) {
						final String foundF=found;
						new BukkitRunnable() {
							@Override
							public void run() {
								Bukkit.getConsoleSender().sendMessage(Variables.PREFIX_CONSOLE+LanguageManager.parseMsg("trans.autoupdate.console.found1", foundF));
								Bukkit.getConsoleSender().sendMessage(Variables.PREFIX_CONSOLE+LanguageManager.parseMsg("trans.autoupdate.console.found2"));
							}
						}.runTask(plugin);
					}
				} catch(Exception ex) {
					new BukkitRunnable() {
						@Override
						public void run() {
							Bukkit.getConsoleSender().sendMessage(Variables.PREFIX_CONSOLE+LanguageManager.parseMsg("trans.autoupdate.console.error.unknown"));
							ex.printStackTrace();
						}
					}.runTask(plugin);
				}
			}
		}.runTaskLaterAsynchronously(plugin, 5);
	}
	
	public void runAutoUpdater(Player p) {
		if(updateRequest!=null) {
			p.sendMessage(Variables.PREFIX+LanguageManager.parseMsg("trans.autoupdate.error.alreadyfound1", updateRequest));
			p.sendMessage(Variables.PREFIX+LanguageManager.parseMsg("trans.autoupdate.error.alreadyfound2"));
		} else if(inUpdateCheck) {
			p.sendMessage(Variables.PREFIX+LanguageManager.parseMsg("trans.autoupdate.error.alreadysearching"));
		} else {
			inUpdateCheck=true;
			p.sendMessage(Variables.PREFIX+LanguageManager.parseMsg("trans.autoupdate.begin"));
			BukkitTask runnable=new BukkitRunnable() {
				@Override
				public void run() {
					p.sendMessage(Variables.PREFIX+"§7...");
				}
			}.runTaskTimerAsynchronously(plugin, 120, 120);
			new BukkitRunnable() {
				@Override
				public void run() {
					try {
						int currentVersion=(int) (Double.valueOf(Variables.VERSION)*10);
						int future=1;
						String found=null;
						boolean noConnection=false;
						while(future<100) {
							int i=currentVersion+future;
							try {
								String fileUrl=i/10+"."+i%10;
								URL url = new URL("https://github.com/StylexTV/GSigns/raw/master/version/"+fileUrl+".dat");
								ReadableByteChannel rbc = Channels.newChannel(url.openStream());
								rbc.close();
								found=fileUrl;
							} catch(Exception ex) {
								if(!(ex instanceof FileNotFoundException)) {
									noConnection=true;
								}
								break;
							}
						    
						    future++;
						}
						
						if(found!=null) {
							updateRequest=found;
							p.sendMessage(
									Variables.PREFIX+CHAT_LINE+"§r\n"+
									Variables.PREFIX+LanguageManager.parseMsg("trans.autoupdate.result.found1", found)+"§r\n"+
									Variables.PREFIX+LanguageManager.parseMsg("trans.autoupdate.result.found2")+"§r\n"+
									Variables.PREFIX+LanguageManager.parseMsg("trans.autoupdate.result.found3")+"§r\n"+
									Variables.PREFIX+CHAT_LINE
							);
						} else if(noConnection) {
							p.sendMessage(Variables.PREFIX+LanguageManager.parseMsg("trans.autoupdate.error.noconnection"));
						} else {
							p.sendMessage(Variables.PREFIX+LanguageManager.parseMsg("trans.autoupdate.result.noupdate", "§6"+Variables.NAME+"§7"));
						}
					} catch(Exception ex) {
						p.sendMessage(Variables.PREFIX+LanguageManager.parseMsg("trans.autoupdate.error.unknown"));
						ex.printStackTrace();
					}
					inUpdateCheck=false;
					runnable.cancel();
				}
			}.runTaskAsynchronously(plugin);
		}
	}
	
}
