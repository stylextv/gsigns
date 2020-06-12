package de.stylextv.gs.world;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.BlockFace;
import org.bukkit.craftbukkit.v1_12_R1.entity.CraftItemFrame;
import org.bukkit.craftbukkit.v1_12_R1.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_12_R1.inventory.CraftItemStack;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.map.MapRenderer;
import org.bukkit.map.MapView;
import org.bukkit.scheduler.BukkitRunnable;

import de.stylextv.gs.main.Main;
import de.stylextv.gs.player.ConnectionManager;
import net.minecraft.server.v1_12_R1.PlayerConnection;
import net.minecraft.server.v1_12_R1.DataWatcher;
import net.minecraft.server.v1_12_R1.DataWatcherObject;
import net.minecraft.server.v1_12_R1.DataWatcherRegistry;
import net.minecraft.server.v1_12_R1.EntityItemFrame;
import net.minecraft.server.v1_12_R1.PacketPlayOutEntityMetadata;

public class BetterFrame112 extends BetterFrame {
	
	private ItemFrame itemFrame;
	private PacketPlayOutEntityMetadata[] packets;
	private MapView[] views;
	
	private ArrayList<Player> playersSentTo=new ArrayList<Player>();
	private ArrayList<Player> playersInRadius=new ArrayList<Player>();
	private int stillRefreshCooldown;
	
	private UUID signUid;
	private long startTime;
	private int currentItemIndex=-1;
	private int[] delays;
	private int totalTime=0;
	
	@SuppressWarnings({ "unchecked", "rawtypes", "deprecation" })
	public BetterFrame112(UUID signUid, Location loc, BlockFace dir, MapRenderer[] mapRenderers, long startTime, int[] delays) {
		this.signUid=signUid;
		this.packets=new PacketPlayOutEntityMetadata[mapRenderers.length];
		this.views=new MapView[mapRenderers.length];
		this.startTime=startTime;
		this.delays=delays;
		if(delays!=null) for(int i:delays) totalTime+=i;
		
		World w=loc.getWorld();
		itemFrame=(ItemFrame) w.spawnEntity(loc, EntityType.ITEM_FRAME);
		itemFrame.setFacingDirection(dir);
		
		EntityItemFrame itemFrameEntity=((CraftItemFrame) itemFrame).getHandle();
		DataWatcher dataWatcher=itemFrameEntity.getDataWatcher();
		for(int i=0; i<views.length; i++) {
			MapView view = Bukkit.createMap(w);
			views[i]=view;
			short id=0;
			try {
				id=(short) view.getClass().getMethod("getId").invoke(view);
			} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException | SecurityException | NoSuchMethodException ex) {}
			view.getRenderers().clear();
			for(MapRenderer r:view.getRenderers()) view.removeRenderer(r);
			view.addRenderer(mapRenderers[i]);
			
			ItemStack item = new ItemStack(Material.MAP, 1, id);
			dataWatcher.set(new DataWatcherObject(6, DataWatcherRegistry.f), CraftItemStack.asNMSCopy(item));
			packets[i]=new PacketPlayOutEntityMetadata(itemFrame.getEntityId(), dataWatcher, false);
		}
		itemFrame.setItem(null);
	}
	@SuppressWarnings({ "unchecked", "rawtypes", "deprecation" })
	public BetterFrame112(UUID signUid, int[] mapIds, Location loc, BlockFace dir, MapRenderer[] mapRenderers, long startTime, int[] delays) {
		this.signUid=signUid;
		this.packets=new PacketPlayOutEntityMetadata[mapRenderers.length];
		this.views=new MapView[mapRenderers.length];
		this.startTime=startTime;
		this.delays=delays;
		if(delays!=null) for(int i:delays) totalTime+=i;
		
		World w=loc.getWorld();
		itemFrame=(ItemFrame) w.spawnEntity(loc, EntityType.ITEM_FRAME);
		itemFrame.setFacingDirection(dir);
		
		EntityItemFrame itemFrameEntity=((CraftItemFrame) itemFrame).getHandle();
		DataWatcher dataWatcher=itemFrameEntity.getDataWatcher();
		for(int i=0; i<views.length; i++) {
			int id=mapIds[i];
			try {
				MapView view=(MapView) Bukkit.class.getMethods()[5].invoke(Bukkit.class, (short)id);
				views[i]=view;
				
				view.getRenderers().clear();
				for(MapRenderer r:view.getRenderers()) view.removeRenderer(r);
				view.addRenderer(mapRenderers[i]);
				
			} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException | SecurityException ex) {ex.printStackTrace();}
			
			ItemStack item = new ItemStack(Material.MAP, 1, (short) id);
			dataWatcher.set(new DataWatcherObject(6, DataWatcherRegistry.f), CraftItemStack.asNMSCopy(item));
			packets[i]=new PacketPlayOutEntityMetadata(itemFrame.getEntityId(), dataWatcher, false);
		}
		itemFrame.setItem(null);
	}
	@SuppressWarnings({ "unchecked", "rawtypes", "deprecation" })
	public BetterFrame112(UUID signUid, int[] mapIds, ItemFrame itemFrame, MapRenderer[] mapRenderers, long startTime, int[] delays) {
		this.signUid=signUid;
		this.packets=new PacketPlayOutEntityMetadata[mapRenderers.length];
		this.views=new MapView[mapRenderers.length];
		this.startTime=startTime;
		this.delays=delays;
		if(delays!=null) for(int i:delays) totalTime+=i;
		
		this.itemFrame=itemFrame;
		EntityItemFrame itemFrameEntity=((CraftItemFrame) itemFrame).getHandle();
		DataWatcher dataWatcher=itemFrameEntity.getDataWatcher();
		for(int i=0; i<views.length; i++) {
			int id=mapIds[i];
			try {
				MapView view=(MapView) Bukkit.class.getMethods()[5].invoke(Bukkit.class, (short)id);
				views[i]=view;
				
				view.getRenderers().clear();
				for(MapRenderer r:view.getRenderers()) view.removeRenderer(r);
				view.addRenderer(mapRenderers[i]);
				
			} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException | SecurityException ex) {ex.printStackTrace();}
			
			ItemStack item = new ItemStack(Material.MAP, 1, (short) id);
			dataWatcher.set(new DataWatcherObject(6, DataWatcherRegistry.f), CraftItemStack.asNMSCopy(item));
			packets[i]=new PacketPlayOutEntityMetadata(itemFrame.getEntityId(), dataWatcher, false);
		}
		itemFrame.setItem(null);
	}
	
	public boolean update(long currentTime) {
		if(itemFrame.isDead()) return true;
		if(packets!=null) {
			int prevFrame=currentItemIndex;
			
			int a=packets.length;
			if(a>1) {
				int msIntoGif=(int) ((currentTime-startTime)%totalTime);
				int j=0;
				for(int i=0; i<delays.length; i++) {
					int delay=delays[i];
					if(msIntoGif<j+delay) {
						currentItemIndex=i;
						break;
					}
					j+=delay;
				}
			} else currentItemIndex=0;
			
			if(currentItemIndex!=prevFrame) {
				
				PacketPlayOutEntityMetadata packet=packets[currentItemIndex];
				for(Player all:Bukkit.getOnlinePlayers()) {
					if(all.getWorld()==itemFrame.getWorld()) {
						Location loc1=all.getLocation();
						Location loc2=itemFrame.getLocation();
						double dis=(loc1.getX()-loc2.getX())*(loc1.getX()-loc2.getX()) + (loc1.getZ()-loc2.getZ())*(loc1.getZ()-loc2.getZ());
						if(dis<BetterFrame.VIEW_DISTANCE_SQ) {
							sendContent(all);
					        PlayerConnection connection = ((CraftPlayer) all).getHandle().playerConnection;
					        connection.sendPacket(packet);
						} else if(dis>BetterFrame.CONTENT_RELOAD_DISTANCE_SQ) removePlayer(all);
					} else removePlayer(all);
				}
				
			} else if(a==1) {
				
				stillRefreshCooldown--;
				if(stillRefreshCooldown<=0) stillRefreshCooldown=10;
				
				PacketPlayOutEntityMetadata packet=packets[0];
				for(Player all:Bukkit.getOnlinePlayers()) {
					if(all.getWorld()==itemFrame.getWorld()) {
						Location loc1=all.getLocation();
						Location loc2=itemFrame.getLocation();
						double dis=(loc1.getX()-loc2.getX())*(loc1.getX()-loc2.getX()) + (loc1.getZ()-loc2.getZ())*(loc1.getZ()-loc2.getZ());
						if(dis<BetterFrame.VIEW_DISTANCE_SQ) {
							if(!playersInRadius.contains(all)) {
								playersInRadius.add(all);
								
								sendContent(all);
						        PlayerConnection connection = ((CraftPlayer) all).getHandle().playerConnection;
						        connection.sendPacket(packet);
							} else if(stillRefreshCooldown==1) {
						        PlayerConnection connection = ((CraftPlayer) all).getHandle().playerConnection;
						        connection.sendPacket(packet);
							}
						} else {
							playersInRadius.remove(all);
							if(dis>BetterFrame.CONTENT_RELOAD_DISTANCE_SQ) removePlayer(all);
						}
					} else removePlayer(all);
				}
				
			}
		}
		return false;
	}
	
	public void removePlayer(Player p) {
		playersSentTo.remove(p);
		playersInRadius.remove(p);
	}
	public void sendContent(Player p) {
		if(!playersSentTo.contains(p)&&(views.length==1||ConnectionManager.canSend(p,views.length))) {
			playersSentTo.add(p);
			new BukkitRunnable() {
				@Override
				public void run() {
					for(MapView view:views) {
						p.sendMap(view);
					}
				}
			}.runTaskAsynchronously(Main.getPlugin());
		}
	}
	
	public boolean isDead() {
		return itemFrame.isDead();
	}
	@Override
	public ItemFrame getItemFrame() {
		return itemFrame;
	}
	public BlockFace getFacing() {
		return itemFrame.getFacing();
	}
	public Location getLocation() {
		return itemFrame.getLocation();
	}
	
	@Override
	public UUID getSignUid() {
		return signUid;
	}
	public int getCurrentItemIndex() {
		return currentItemIndex;
	}
	public void setCurrentItemIndex(int currentItemIndex) {
		this.currentItemIndex = currentItemIndex;
	}
	public int getDelay(int index) {
		if(delays==null) return 0;
		return delays[index];
	}
	
	public MapView[] getMapViews() {
		return views;
	}
	
}
