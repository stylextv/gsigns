package de.stylextv.gs.world;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.BlockFace;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftItemFrame;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_8_R3.inventory.CraftItemStack;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.map.MapRenderer;
import org.bukkit.map.MapView;
import org.bukkit.scheduler.BukkitRunnable;

import de.stylextv.gs.main.Main;
import de.stylextv.gs.player.ConnectionManager;
import net.minecraft.server.v1_8_R3.DataWatcher;
import net.minecraft.server.v1_8_R3.EntityItemFrame;
import net.minecraft.server.v1_8_R3.PacketPlayOutEntityMetadata;
import net.minecraft.server.v1_8_R3.PlayerConnection;

public class BetterFrame18 implements BetterFrame {
	
	private ItemFrame itemFrame;
	private PacketPlayOutEntityMetadata[] packets;
	private MapView[] views;
	
	private ArrayList<Player> playersSentTo=new ArrayList<Player>();
	private ArrayList<Player> playersInRadius=new ArrayList<Player>();
	
	private long startTime;
	private int currentItemIndex=-1;
	private int delay;
	
	@SuppressWarnings({ "deprecation" })
	public BetterFrame18(Location loc, BlockFace dir, MapRenderer[] mapRenderers, long startTime, int delay) {
		this.packets=new PacketPlayOutEntityMetadata[mapRenderers.length];
		this.views=new MapView[mapRenderers.length];
		this.startTime=startTime;
		this.delay=delay;
		
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
			
			ItemStack item = new ItemStack(Material.MAP, 1, (short)id);
			dataWatcher.a(5, CraftItemStack.asNMSCopy(item));
			packets[i]=new PacketPlayOutEntityMetadata(itemFrame.getEntityId(), dataWatcher, false);
		}
		itemFrame.setItem(null);
	}
	@SuppressWarnings({ "deprecation" })
	public BetterFrame18(int[] mapIds, Location loc, BlockFace dir, MapRenderer[] mapRenderers, long startTime, int delay) {
		this.packets=new PacketPlayOutEntityMetadata[mapRenderers.length];
		this.views=new MapView[mapRenderers.length];
		this.startTime=startTime;
		this.delay=delay;
		
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
			
			ItemStack item = new ItemStack(Material.MAP, 1, (short)id);
			dataWatcher.a(5, CraftItemStack.asNMSCopy(item));
			packets[i]=new PacketPlayOutEntityMetadata(itemFrame.getEntityId(), dataWatcher, false);
		}
		itemFrame.setItem(null);
	}
	public BetterFrame18(ItemFrame itemFrame) {
		this.itemFrame=itemFrame;
	}
	
	public boolean update(long currentTime) {
		if(itemFrame.isDead()) return true;
		if(packets!=null) {
			int prevFrame=currentItemIndex;
			
			int a=packets.length;
			if(a>1) {
				int totalTime=delay*a;
				long msIntoGif=currentTime-startTime;
				double d=(msIntoGif%totalTime)/(double)totalTime;
				currentItemIndex=(int) (a*d);
			} else currentItemIndex=0;
			
			if(currentItemIndex!=prevFrame) {
				
				PacketPlayOutEntityMetadata packet=packets[currentItemIndex];
				for(Player all:Bukkit.getOnlinePlayers()) {
					if(all.getWorld()==itemFrame.getWorld()) {
						double dis=all.getLocation().distanceSquared(itemFrame.getLocation());
						if(dis<BetterFrame.VIEW_DISTANCE_SQ) {
							sendContent(all);
					        PlayerConnection connection = ((CraftPlayer) all).getHandle().playerConnection;
					        connection.sendPacket(packet);
						} else if(dis>BetterFrame.VIEW_DISTANCE_SQ*2) removePlayer(all);
					} else removePlayer(all);
				}
				
			} else if(a==1) {
				
				PacketPlayOutEntityMetadata packet=packets[0];
				for(Player all:Bukkit.getOnlinePlayers()) {
					if(all.getWorld()==itemFrame.getWorld()) {
						double dis=all.getLocation().distanceSquared(itemFrame.getLocation());
						if(dis<BetterFrame.VIEW_DISTANCE_SQ) {
							if(!playersInRadius.contains(all)) {
								playersInRadius.add(all);
								
								sendContent(all);
						        PlayerConnection connection = ((CraftPlayer) all).getHandle().playerConnection;
						        connection.sendPacket(packet);
							}
						} else if(dis>BetterFrame.VIEW_DISTANCE_SQ*2) removePlayer(all);
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
		if(!playersSentTo.contains(p)) {
			if(views.length==1||ConnectionManager.canSend(p)) {
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
	}
	
	public void remove() {
		itemFrame.remove();
	}
	public boolean isDead() {
		return itemFrame.isDead();
	}
	public BlockFace getFacing() {
		return itemFrame.getFacing();
	}
	public Location getLocation() {
		return itemFrame.getLocation();
	}
	
	public int getCurrentItemIndex() {
		return currentItemIndex;
	}
	public void setCurrentItemIndex(int currentItemIndex) {
		this.currentItemIndex = currentItemIndex;
	}
	public int getDelay() {
		return delay;
	}
	
	public MapView[] getMapViews() {
		return views;
	}
	
}
