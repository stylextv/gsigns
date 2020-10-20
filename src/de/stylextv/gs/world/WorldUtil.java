package de.stylextv.gs.world;

import java.io.File;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.Player;
import org.bukkit.event.server.MapInitializeEvent;
import org.bukkit.scheduler.BukkitRunnable;

import de.stylextv.gs.gui.GuiManager;
import de.stylextv.gs.main.Main;
import de.stylextv.gs.main.Variables;
import de.stylextv.gs.packet.PacketListener;
import de.stylextv.gs.player.ConnectionManager;

public class WorldUtil {
	
	public static final int MCVERSION_1_8=0;
	public static final int MCVERSION_1_9=1;
	public static final int MCVERSION_1_10=2;
	public static final int MCVERSION_1_11=3;
	public static final int MCVERSION_1_12=4;
	public static final int MCVERSION_1_13=5;
	public static final int MCVERSION_1_14=6;
	public static final int MCVERSION_1_15=7;
	public static final int MCVERSION_1_16=8;
	
	private static final DecimalFormat DECIMAL_FORMAT = new DecimalFormat("#.##", new DecimalFormatSymbols(Locale.ENGLISH));
	
	private static final File SIGN_FOLDER = new File("plugins/GSigns/signs");
	private static final File LOCAL_IMAGES_FOLDER = new File("plugins/GSigns/images");
	
	private static MapManager mapManager;
	private static int mcVersion=MCVERSION_1_14;
	
	private static CopyOnWriteArrayList<BetterSign> signs=new CopyOnWriteArrayList<BetterSign>();
	
	private static boolean inFramesUpdate;
	
	public static void onEnable() {
		String version=Bukkit.getServer().getVersion();
		mcVersion=Integer.valueOf(version.split("MC: 1\\.")[1].split("\\.")[0])-8;
		if(!(mcVersion>=MCVERSION_1_8&&mcVersion<=MCVERSION_1_16)) {
			Bukkit.getConsoleSender().sendMessage(Variables.PREFIX_CONSOLE+"The server-version (§c"+version+"§r) you are running is not supported by this plugin!");
		}
		
		LOCAL_IMAGES_FOLDER.mkdirs();
		
		mapManager = new MapManager();
		mapManager.searchForVanillaMaps();
		
		PacketListener packetListener = new PacketListener();
		packetListener.start();
		
		new BukkitRunnable() {
			@Override
			public void run() {
				long currentTime=System.currentTimeMillis();
				int loaded=0;
				if(SIGN_FOLDER.exists()) for(File f:SIGN_FOLDER.listFiles()) {
					try {
						String name=f.getName();
						if(name.endsWith(".gsign")) {
							BetterSign sign=BetterSign.loadSign(f, currentTime);
							sign.setFile(f);
							
							signs.add(sign);
							GuiManager.onSignsListChange();
						}
						loaded++;
					} catch(Exception ex) {
						Bukkit.getConsoleSender().sendMessage(Variables.PREFIX_CONSOLE+"Deleted old/corrupted file: §c"+f.getName());
						ex.printStackTrace();
						f.delete();
					}
				}
				Bukkit.getConsoleSender().sendMessage(Variables.PREFIX_CONSOLE+"Succesfully loaded §"+(loaded == 0 ? "e" : "a")+loaded+"§r signs in "+DECIMAL_FORMAT.format((System.currentTimeMillis()-currentTime)/1000.0)+"s.");
			}
		}.runTaskLater(Main.getPlugin(), 2);
		new BukkitRunnable() {
			@Override
			public void run() {
				if(!inFramesUpdate) {
					inFramesUpdate=true;
					
					ConnectionManager.update();
					long currentTime=System.currentTimeMillis();
					for(BetterSign sign:signs) {
						if(sign.update(currentTime)) {
							sign.deleteFile();
							signs.remove(sign);
							GuiManager.onSignsListChange();
							GuiManager.removeSignMenu(sign);
						}
					}
					
					inFramesUpdate=false;
				}
			}
		}.runTaskTimerAsynchronously(Main.getPlugin(), 0, 0);
	}
	public static void onDisable() {
		SIGN_FOLDER.mkdirs();
		try {
			for(BetterSign sign:signs) {
				if(!sign.isDead()) {
					if(sign.getFile() == null) sign.save();
				} else sign.deleteFile();
			}
		} catch(Exception ex) {
			ex.printStackTrace();
		}
	}
	
	public static void removeAllDrewEntries(Player p) {
		for(BetterSign sign:signs) {
			sign.removePlayer(p);
		}
	}
	
	public static BetterSign createSign() {
		return new BetterSign(randomSignUid());
	}
	public static void registerSign(BetterSign sign) {
		signs.add(sign);
		GuiManager.onSignsListChange();
	}
	public static void spawnItemFrame(BetterSign sign, Location loc, byte[] image, BlockFace direction) {
		sign.addFrame(new BetterFrame(sign, loc, direction, new byte[][]{image}, 0, null));
	}
	public static void spawnItemFrame(BetterSign sign, Location loc, byte[][] images, int[] delays, long startTime, BlockFace direction) {
		int[] delaysCopy=new int[delays.length];
		
		int compactedFrameIndex=0;
		byte[] currentHead=null;
		for(int i=0; i<images.length; i++) {
			if(currentHead==null) {
				byte[] frame0=images[i];
				if(i+1==images.length) {
					images[compactedFrameIndex]=frame0;
					delaysCopy[compactedFrameIndex]=delays[i];
					compactedFrameIndex++;
				} else {
					byte[] frame1=images[i+1];
					boolean equal=true;
					for(int j=0; j<frame0.length; j++) {
						if(frame0[j]!=frame1[j]) {
							equal=false;
							break;
						}
					}
					if(equal) {
						currentHead=frame0;
						images[compactedFrameIndex]=frame0;
						delaysCopy[compactedFrameIndex]=delays[i]+delays[i+1];
						i++;
					} else {
						images[compactedFrameIndex]=frame0;
						delaysCopy[compactedFrameIndex]=delays[i];
						compactedFrameIndex++;
					}
				}
			} else {
				byte[] frame0=images[i];
				boolean equal=true;
				for(int j=0; j<frame0.length; j++) {
					if(frame0[j]!=currentHead[j]) {
						equal=false;
						break;
					}
				}
				if(equal) {
					delaysCopy[compactedFrameIndex]=delaysCopy[compactedFrameIndex]+delays[i];
				} else {
					currentHead=null;
					compactedFrameIndex++;
					i--;
				}
			}
		}
		if(currentHead!=null) compactedFrameIndex++;
		
		boolean b=compactedFrameIndex!=1;
		if(compactedFrameIndex != images.length) {
			byte[][] newFrames=new byte[compactedFrameIndex][];
			int[] newDelays=null;
			if(b) newDelays=new int[compactedFrameIndex];
			for(int i=0; i<compactedFrameIndex; i++) {
				newFrames[i]=images[i];
				if(b) newDelays[i]=delaysCopy[i];
			}
			
			images=newFrames;
			delays=newDelays;
		}
		
		int[] delaysF=delays;
		byte[][] imagesF=images;
		new BukkitRunnable() {
			@Override
			public void run() {
				loc.getBlock().setType(Material.AIR);
				
				sign.addFrame(new BetterFrame(sign, loc, direction, imagesF, b?startTime:0, delaysF));
			}
		}.runTask(Main.getPlugin());
	}
	
	public static UUID randomSignUid() {
		UUID uid=UUID.randomUUID();
		while(true) {
			boolean exists=false;
			for(BetterSign sign:signs) {
				if(sign.getUid().compareTo(uid)==0) {
					exists=true;
					break;
				}
			}
			
			if(exists) {
				uid=UUID.randomUUID();
			} else break;
		}
		return uid;
	}
	public static BetterFrame getFrame(ItemFrame itemFrame) {
		int entityId = itemFrame.getEntityId();
		for(BetterSign sign:signs) {
			BetterFrame frame=sign.getFrame(entityId);
			if(frame!=null) return frame;
		}
		return null;
	}
	public static BetterFrame getFrame(Location loc, BlockFace facing) {
		for(BetterSign sign:signs) {
			BetterFrame frame=sign.getFrame(loc, facing);
			if(frame!=null) return frame;
		}
		return null;
	}
	public static BetterSign getSign(UUID uid) {
		for(BetterSign sign:signs) {
			if(sign.getUid().compareTo(uid)==0) return sign;
		}
		return null;
	}
	public static boolean removeSign(UUID uid) {
		for(BetterSign sign:signs) {
			if(sign.getUid().compareTo(uid)==0) {
				sign.removeItemFrames();
				sign.deleteFile();
				signs.remove(sign);
				GuiManager.onSignsListChange();
				GuiManager.removeSignMenu(sign);
				return true;
			}
		}
		return false;
	}
	public static void removeSign(BetterSign sign) {
		sign.removeItemFrames();
		sign.deleteFile();
		signs.remove(sign);
		GuiManager.onSignsListChange();
		GuiManager.removeSignMenu(sign);
	}
	public static boolean isFrame(int entityId) {
		for(BetterSign sign:signs) {
			if(sign.isFrame(entityId)) return true;
		}
		return false;
	}
	
	public static void onMapInitialize(MapInitializeEvent e) {
		mapManager.onMapInitialize(e);
	}
	public static Set<Short> getOccupiedIdsFor(OfflinePlayer p) {
		Set<Short> ids = new HashSet<>();
		for(BetterSign sign:signs) {
			sign.getOccupiedIdsFor(p, ids);
		}
		return ids;
	}
	public static boolean isIdUsedBy(OfflinePlayer p, short id) {
		if(id > MapManager.FORCED_OFFSET) {
			for(BetterSign sign:signs) {
				if(sign.isIdUsedBy(p, id)) return true;
			}
		}
		return false;
	}
	
	public static MapManager getMapManager() {
		return mapManager;
	}
	public static CopyOnWriteArrayList<BetterSign> getSigns() {
		return signs;
	}
	public static File getSignFolder() {
		return SIGN_FOLDER;
	}
	public static File getLocalImagesFolder() {
		return LOCAL_IMAGES_FOLDER;
	}
	public static int getTotalAmountOfFrames() {
		int i=0;
		for(BetterSign sign:signs) {
			i+=sign.getAmountOfFrames();
		}
		return i;
	}
	public static int getMcVersion() {
		return mcVersion;
	}
	
}
