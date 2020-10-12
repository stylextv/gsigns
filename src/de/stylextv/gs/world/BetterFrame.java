package de.stylextv.gs.world;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.MapMeta;
import org.bukkit.map.MapRenderer;
import org.bukkit.map.MapView;
import org.bukkit.scheduler.BukkitRunnable;

import de.stylextv.gs.main.Main;
import de.stylextv.gs.packet.Reflections;
import de.stylextv.gs.packet.Reflections.ConstructorInvoker;
import de.stylextv.gs.packet.TinyProtocol;
import de.stylextv.gs.player.ConnectionManager;
import de.stylextv.gs.render.BetterMapRenderer;

public class BetterFrame {
	
	private static final int VIEW_DISTANCE_SQ=32*32;
	private static final ArrayList<Object> EMPTY_ICONLIST=new ArrayList<Object>();
	
    private static final Class<Object> packetClass = Reflections.getUntypedClass("{nms}.Packet");
    private static final Class<Object> packetPlayOutEntityMetadataClass = Reflections.getUntypedClass("{nms}.PacketPlayOutEntityMetadata");
    private static Class<Object> packetPlayOutMapClass;
    private static final Class<Object> dataWatcherClass = Reflections.getUntypedClass("{nms}.DataWatcher");
    private static final ConstructorInvoker packetPlayOutEntityMetadataConstructor = Reflections.getConstructor(packetPlayOutEntityMetadataClass, int.class, dataWatcherClass, boolean.class);
    private static ConstructorInvoker packetPlayOutMapConstructor;
	
    private static final Reflections.MethodInvoker sendPacket = Reflections.getMethod("{nms}.PlayerConnection", "sendPacket", packetClass);
    
    private static final Reflections.MethodInvoker getItemFrameHandle = Reflections.getMethod("{obc}.entity.CraftItemFrame", "getHandle");
    private static final Class<Object> entityItemFrameClass = Reflections.getUntypedClass("{nms}.EntityItemFrame");
    private static final Reflections.MethodInvoker getDataWatcher = Reflections.getMethod(entityItemFrameClass, "getDataWatcher");
    private static Class<Object> dataWatcherObjectClass;
    private static Class<Object> dataWatcherSerializerClass;
    private static Class<Object> dataWatcherRegistryClass;
    private static Object dataWatcherRegistryF;
    private static ConstructorInvoker dataWatcherObjectConstructor;
    private static Reflections.MethodInvoker dataWatcherSet;
    private static Reflections.MethodInvoker dataWatcherWatch;
    private static final Class<Object> craftItemStackClass = Reflections.getUntypedClass("{obc}.inventory.CraftItemStack");
    private static final Reflections.MethodInvoker asNMSCopy = Reflections.getMethod(craftItemStackClass, "asNMSCopy", ItemStack.class);
    
    static {
    	if(WorldUtil.getMcVersion() == WorldUtil.MCVERSION_1_8) {
    		dataWatcherWatch = Reflections.getMethod("{nms}.DataWatcher", "watch", int.class, Object.class);
    		packetPlayOutMapClass = Reflections.getUntypedClass("{nms}.PacketPlayOutMap");
    		packetPlayOutMapConstructor = Reflections.getConstructor(packetPlayOutMapClass, int.class, byte.class, Collection.class, byte[].class, int.class, int.class, int.class, int.class);
    	} else {
    		dataWatcherObjectClass = Reflections.getUntypedClass("{nms}.DataWatcherObject");
    		dataWatcherSerializerClass = Reflections.getUntypedClass("{nms}.DataWatcherSerializer");
    		dataWatcherRegistryClass = Reflections.getUntypedClass("{nms}.DataWatcherRegistry");
    	    dataWatcherRegistryF = Reflections.getField(dataWatcherRegistryClass, "f", dataWatcherSerializerClass).get(dataWatcherRegistryClass);
    	    dataWatcherObjectConstructor = Reflections.getConstructor(dataWatcherObjectClass, int.class, dataWatcherSerializerClass);
    	    dataWatcherSet = Reflections.getMethod("{nms}.DataWatcher", "set", dataWatcherObjectClass, Object.class);
    	}
    }
    
	private ItemFrame itemFrame;
	private Object[] packets;
	private Object[] mapPackets;
	private MapView[] views;
	
	private HashMap<Player, Integer> playersSentProgress=new HashMap<Player, Integer>();
	private ArrayList<Player> playersInRadius=new ArrayList<Player>();
	private int stillRefreshCooldown;
	
	private UUID signUid;
	private long startTime;
	private int currentItemIndex=-1;
	private int[] delays;
	private int totalTime=0;
	
	@SuppressWarnings("deprecation")
	public BetterFrame(UUID signUid, Location loc, BlockFace dir, MapRenderer[] mapRenderers, long startTime, int[] delays) {
		this.signUid=signUid;
		this.packets=new Object[mapRenderers.length];
		this.views=new MapView[mapRenderers.length];
		this.startTime=startTime;
		this.delays=delays;
		if(delays!=null) for(int i:delays) totalTime+=i;
		
		if(WorldUtil.getMcVersion() > WorldUtil.MCVERSION_1_12) {
			World w=loc.getWorld();
			loc.add(0, 0, -1);
			BlockData backup=null;
			Block b=loc.getBlock();
			if(!b.getType().isSolid()) {
				backup=b.getBlockData();
				b.setType(Material.COBBLESTONE);
			}
			loc.add(0, 0, 1);
			itemFrame=(ItemFrame) w.spawnEntity(loc, EntityType.ITEM_FRAME);
			itemFrame.setFacingDirection(dir);
			if(backup!=null) b.setBlockData(backup);
			
			Object itemFrameEntity = getItemFrameHandle.invoke(itemFrame);
			Object dataWatcher=getDataWatcher.invoke(itemFrameEntity);
			for(int i=0; i<views.length; i++) {
				MapView view = Bukkit.createMap(w);
				views[i]=view;
				view.getRenderers().clear();
				for(MapRenderer r:view.getRenderers()) view.removeRenderer(r);
				view.addRenderer(mapRenderers[i]);
				
				ItemStack item = new ItemStack(Material.FILLED_MAP, 1);
				MapMeta meta=(MapMeta) item.getItemMeta();
				meta.setMapView(view);
				item.setItemMeta(meta);
				dataWatcherSet.invoke(dataWatcher, dataWatcherObjectConstructor.invoke(WorldUtil.getMcVersion()==WorldUtil.MCVERSION_1_13?6:7, dataWatcherRegistryF), asNMSCopy.invoke(craftItemStackClass, item));
				packets[i]=packetPlayOutEntityMetadataConstructor.invoke(itemFrame.getEntityId(), dataWatcher, false);
			}
			itemFrame.setItem(null);
		} else {
			World w=loc.getWorld();
			itemFrame=(ItemFrame) w.spawnEntity(loc, EntityType.ITEM_FRAME);
			itemFrame.setFacingDirection(dir);
			
			Object itemFrameEntity = getItemFrameHandle.invoke(itemFrame);
			Object dataWatcher=getDataWatcher.invoke(itemFrameEntity);
			if(WorldUtil.getMcVersion() == WorldUtil.MCVERSION_1_8) {
				this.mapPackets=new Object[mapRenderers.length];
				for(int i=0; i<views.length; i++) {
					MapRenderer renderer=mapRenderers[i];
					
					MapView view = Bukkit.createMap(w);
					views[i]=view;
					short id=0;
					try {
						id=(short) view.getClass().getMethod("getId").invoke(view);
					} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException | SecurityException | NoSuchMethodException ex) {ex.printStackTrace();}
					view.getRenderers().clear();
					for(MapRenderer r:view.getRenderers()) view.removeRenderer(r);
					view.addRenderer(renderer);
					
					ItemStack item = new ItemStack(Material.MAP, 1, id);
					dataWatcherWatch.invoke(dataWatcher, 8, asNMSCopy.invoke(craftItemStackClass, item));
					packets[i]=packetPlayOutEntityMetadataConstructor.invoke(itemFrame.getEntityId(), dataWatcher, false);
					mapPackets[i]=packetPlayOutMapConstructor.invoke(id, (byte) 0, EMPTY_ICONLIST, ((BetterMapRenderer)renderer).getData(), 0, 0, 128, 128);
				}
			} else {
				for(int i=0; i<views.length; i++) {
					MapView view = Bukkit.createMap(w);
					views[i]=view;
					short id=0;
					try {
						id=(short) view.getClass().getMethod("getId").invoke(view);
					} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException | SecurityException | NoSuchMethodException ex) {ex.printStackTrace();}
					view.getRenderers().clear();
					for(MapRenderer r:view.getRenderers()) view.removeRenderer(r);
					view.addRenderer(mapRenderers[i]);
					
					ItemStack item = new ItemStack(Material.MAP, 1, id);
					dataWatcherSet.invoke(dataWatcher, dataWatcherObjectConstructor.invoke(6, dataWatcherRegistryF), asNMSCopy.invoke(craftItemStackClass, item));
					packets[i]=packetPlayOutEntityMetadataConstructor.invoke(itemFrame.getEntityId(), dataWatcher, false);
				}
			}
			itemFrame.setItem(null);
		}
	}
	@SuppressWarnings("deprecation")
	public BetterFrame(UUID signUid, int[] mapIds, Location loc, BlockFace dir, MapRenderer[] mapRenderers, long startTime, int[] delays) {
		this.signUid=signUid;
		this.packets=new Object[mapRenderers.length];
		this.views=new MapView[mapRenderers.length];
		this.startTime=startTime;
		this.delays=delays;
		if(delays!=null) for(int i:delays) totalTime+=i;
		
		if(WorldUtil.getMcVersion() > WorldUtil.MCVERSION_1_12) {
			World w=loc.getWorld();
			loc.add(0, 0, -1);
			BlockData backup=null;
			Block b=loc.getBlock();
			if(!b.getType().isSolid()) {
				backup=b.getBlockData();
				b.setType(Material.COBBLESTONE);
			}
			loc.add(0, 0, 1);
			itemFrame=(ItemFrame) w.spawnEntity(loc, EntityType.ITEM_FRAME);
			itemFrame.setFacingDirection(dir);
			if(backup!=null) b.setBlockData(backup);

			Object itemFrameEntity = getItemFrameHandle.invoke(itemFrame);
			Object dataWatcher=getDataWatcher.invoke(itemFrameEntity);
			for(int i=0; i<views.length; i++) {
				MapView view=Bukkit.getMap(mapIds[i]);
				views[i]=view;
				view.getRenderers().clear();
				for(MapRenderer r:view.getRenderers()) view.removeRenderer(r);
				view.addRenderer(mapRenderers[i]);
				
				ItemStack item = new ItemStack(Material.FILLED_MAP, 1);
				MapMeta meta=(MapMeta) item.getItemMeta();
				meta.setMapView(view);
				item.setItemMeta(meta);
				dataWatcherSet.invoke(dataWatcher, dataWatcherObjectConstructor.invoke(WorldUtil.getMcVersion()==WorldUtil.MCVERSION_1_13?6:7, dataWatcherRegistryF), asNMSCopy.invoke(craftItemStackClass, item));
				packets[i]=packetPlayOutEntityMetadataConstructor.invoke(itemFrame.getEntityId(), dataWatcher, false);
			}
			itemFrame.setItem(null);
		} else {
			World w=loc.getWorld();
			itemFrame=(ItemFrame) w.spawnEntity(loc, EntityType.ITEM_FRAME);
			itemFrame.setFacingDirection(dir);
			
			Object itemFrameEntity = getItemFrameHandle.invoke(itemFrame);
			Object dataWatcher=getDataWatcher.invoke(itemFrameEntity);
			if(WorldUtil.getMcVersion() == WorldUtil.MCVERSION_1_8) {
				this.mapPackets=new Object[mapRenderers.length];
				for(int i=0; i<views.length; i++) {
					MapRenderer renderer=mapRenderers[i];
					int id=mapIds[i];
					
					try {
						MapView view=(MapView) Bukkit.class.getMethod("getMap", short.class).invoke(Bukkit.class, (short)id);
						views[i]=view;
						
						view.getRenderers().clear();
						for(MapRenderer r:view.getRenderers()) view.removeRenderer(r);
						view.addRenderer(renderer);
						
					} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException | SecurityException | NoSuchMethodException ex) {ex.printStackTrace();}
					
					ItemStack item = new ItemStack(Material.MAP, 1, (short)id);
					dataWatcherWatch.invoke(dataWatcher, 8, asNMSCopy.invoke(craftItemStackClass, item));
					packets[i]=packetPlayOutEntityMetadataConstructor.invoke(itemFrame.getEntityId(), dataWatcher, false);
					mapPackets[i]=packetPlayOutMapConstructor.invoke(id, (byte) 0, EMPTY_ICONLIST, ((BetterMapRenderer)renderer).getData(), 0, 0, 128, 128);
				}
			} else {
				for(int i=0; i<views.length; i++) {
					int id=mapIds[i];
					try {
						MapView view=(MapView) Bukkit.class.getMethod("getMap", short.class).invoke(Bukkit.class, (short)id);
						views[i]=view;
						
						view.getRenderers().clear();
						for(MapRenderer r:view.getRenderers()) view.removeRenderer(r);
						view.addRenderer(mapRenderers[i]);
						
					} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException | SecurityException | NoSuchMethodException ex) {ex.printStackTrace();}
					
					ItemStack item = new ItemStack(Material.MAP, 1, (short) id);
					dataWatcherSet.invoke(dataWatcher, dataWatcherObjectConstructor.invoke(6, dataWatcherRegistryF), asNMSCopy.invoke(craftItemStackClass, item));
					packets[i]=packetPlayOutEntityMetadataConstructor.invoke(itemFrame.getEntityId(), dataWatcher, false);
				}
			}
			itemFrame.setItem(null);
		}
	}
	@SuppressWarnings("deprecation")
	public BetterFrame(UUID signUid, int[] mapIds, ItemFrame itemFrame, MapRenderer[] mapRenderers, long startTime, int[] delays) {
		this.signUid=signUid;
		this.packets=new Object[mapRenderers.length];
		this.views=new MapView[mapRenderers.length];
		this.startTime=startTime;
		this.delays=delays;
		if(delays!=null) for(int i:delays) totalTime+=i;
		this.itemFrame=itemFrame;
		
		if(WorldUtil.getMcVersion() > WorldUtil.MCVERSION_1_12) {
			Object itemFrameEntity = getItemFrameHandle.invoke(itemFrame);
			Object dataWatcher=getDataWatcher.invoke(itemFrameEntity);
			for(int i=0; i<views.length; i++) {
				MapView view=Bukkit.getMap(mapIds[i]);
				views[i]=view;
				view.getRenderers().clear();
				for(MapRenderer r:view.getRenderers()) view.removeRenderer(r);
				view.addRenderer(mapRenderers[i]);
				
				ItemStack item = new ItemStack(Material.FILLED_MAP, 1);
				MapMeta meta=(MapMeta) item.getItemMeta();
				meta.setMapView(view);
				item.setItemMeta(meta);
				dataWatcherSet.invoke(dataWatcher, dataWatcherObjectConstructor.invoke(WorldUtil.getMcVersion()==WorldUtil.MCVERSION_1_13?6:7, dataWatcherRegistryF), asNMSCopy.invoke(craftItemStackClass, item));
				packets[i]=packetPlayOutEntityMetadataConstructor.invoke(itemFrame.getEntityId(), dataWatcher, false);
			}
			itemFrame.setItem(null);
		} else {
			Object itemFrameEntity = getItemFrameHandle.invoke(itemFrame);
			Object dataWatcher=getDataWatcher.invoke(itemFrameEntity);
			if(WorldUtil.getMcVersion() == WorldUtil.MCVERSION_1_8) {
				this.mapPackets=new Object[mapRenderers.length];
				for(int i=0; i<views.length; i++) {
					MapRenderer renderer=mapRenderers[i];
					int id=mapIds[i];
					
					try {
						MapView view=(MapView) Bukkit.class.getMethod("getMap", short.class).invoke(Bukkit.class, (short)id);
						views[i]=view;
						
						view.getRenderers().clear();
						for(MapRenderer r:view.getRenderers()) view.removeRenderer(r);
						view.addRenderer(renderer);
						
					} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException | SecurityException | NoSuchMethodException ex) {ex.printStackTrace();}
					
					ItemStack item = new ItemStack(Material.MAP, 1, (short)id);
					dataWatcherWatch.invoke(dataWatcher, 8, asNMSCopy.invoke(craftItemStackClass, item));
					packets[i]=packetPlayOutEntityMetadataConstructor.invoke(itemFrame.getEntityId(), dataWatcher, false);
					mapPackets[i]=packetPlayOutMapConstructor.invoke(id, (byte) 0, EMPTY_ICONLIST, ((BetterMapRenderer)renderer).getData(), 0, 0, 128, 128);
				}
			} else {
				for(int i=0; i<views.length; i++) {
					int id=mapIds[i];
					try {
						MapView view=(MapView) Bukkit.class.getMethod("getMap", short.class).invoke(Bukkit.class, (short)id);
						views[i]=view;
						
						view.getRenderers().clear();
						for(MapRenderer r:view.getRenderers()) view.removeRenderer(r);
						view.addRenderer(mapRenderers[i]);
						
					} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException | SecurityException | NoSuchMethodException ex) {ex.printStackTrace();}
					
					ItemStack item = new ItemStack(Material.MAP, 1, (short) id);
					dataWatcherSet.invoke(dataWatcher, dataWatcherObjectConstructor.invoke(6, dataWatcherRegistryF), asNMSCopy.invoke(craftItemStackClass, item));
					packets[i]=packetPlayOutEntityMetadataConstructor.invoke(itemFrame.getEntityId(), dataWatcher, false);
				}
			}
			itemFrame.setItem(null);
		}
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
				
				Object packet=packets[currentItemIndex];
				for(Player all:Bukkit.getOnlinePlayers()) {
					if(all.getWorld()==itemFrame.getWorld()) {
						Location loc1=all.getLocation();
						Location loc2=itemFrame.getLocation();
						double dis=(loc1.getX()-loc2.getX())*(loc1.getX()-loc2.getX()) + (loc1.getZ()-loc2.getZ())*(loc1.getZ()-loc2.getZ());
						if(dis<BetterFrame.VIEW_DISTANCE_SQ) {
							sendContent(all);
			        		Object connection = TinyProtocol.getConnection.get(TinyProtocol.getPlayerHandle.invoke(all));
					        sendPacket.invoke(connection, packet);
						}
					} else removePlayer(all);
				}
				
			} else if(a==1) {
				
				stillRefreshCooldown--;
				if(stillRefreshCooldown<=0) stillRefreshCooldown=10;
				
				Object packet=packets[0];
				for(Player all:Bukkit.getOnlinePlayers()) {
					if(all.getWorld()==itemFrame.getWorld()) {
						Location loc1=all.getLocation();
						Location loc2=itemFrame.getLocation();
						double dis=(loc1.getX()-loc2.getX())*(loc1.getX()-loc2.getX()) + (loc1.getZ()-loc2.getZ())*(loc1.getZ()-loc2.getZ());
						if(dis<BetterFrame.VIEW_DISTANCE_SQ) {
							sendContent(all);
							if(!playersInRadius.contains(all)) {
								playersInRadius.add(all);
								
				        		Object connection = TinyProtocol.getConnection.get(TinyProtocol.getPlayerHandle.invoke(all));
						        sendPacket.invoke(connection, packet);
							} else if(stillRefreshCooldown==1) {
				        		Object connection = TinyProtocol.getConnection.get(TinyProtocol.getPlayerHandle.invoke(all));
						        sendPacket.invoke(connection, packet);
							}
						} else {
							playersInRadius.remove(all);
						}
					} else removePlayer(all);
				}
				
			}
		}
		return false;
	}
	
	public void removePlayer(Player p) {
		playersSentProgress.remove(p);
		playersInRadius.remove(p);
	}
	public void sendContent(Player p) {
		Integer got=playersSentProgress.get(p);
		if(got==null) got=0;
		
		int allowedToSend;
		if(got!=views.length&&(allowedToSend=ConnectionManager.canSend(p,views.length-got))!=0) {
			playersSentProgress.put(p, got+allowedToSend);
			int gotF=got;
			int allowedToSendF=allowedToSend;
			new BukkitRunnable() {
				@Override
				public void run() {
					if(WorldUtil.getMcVersion() == WorldUtil.MCVERSION_1_8) {
		        		Object connection = TinyProtocol.getConnection.get(TinyProtocol.getPlayerHandle.invoke(p));
						for(int i=0; i<allowedToSendF; i++) {
					        sendPacket.invoke(connection, mapPackets[gotF+i]);
						}
					} else {
						for(int i=0; i<allowedToSendF; i++) {
							p.sendMap(views[gotF+i]);
						}
					}
				}
			}.runTaskAsynchronously(Main.getPlugin());
		}
	}
	
	public boolean isDead() {
		return itemFrame.isDead();
	}
	public ItemFrame getItemFrame() {
		return itemFrame;
	}
	public BlockFace getFacing() {
		return itemFrame.getFacing();
	}
	public Location getLocation() {
		return itemFrame.getLocation();
	}
	
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