package de.stylextv.gs.player;

import java.awt.image.BufferedImage;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitRunnable;

import de.stylextv.gs.decode.BetterGifDecoder.GifImage;
import de.stylextv.gs.image.ImageGenerator;
import de.stylextv.gs.main.Main;
import de.stylextv.gs.main.Vars;
import de.stylextv.gs.world.Direction;
import de.stylextv.gs.world.WorldUtil;

public class PlayerManager {
	
	private static int MAX_STONE_SEARCH=20;
	
	private static Direction[] directions=new Direction[]{new Direction(1, 0, 0),new Direction(0, 0, 1),new Direction(-1, 0, 0),new Direction(0, 0, -1),new Direction(0, -1, 0),new Direction(0, 1, 0)};
	
	private static ConcurrentHashMap<Player, Order> playerTasks=new ConcurrentHashMap<Player, Order>();
	
	public static void startPlacingPhase(Player p, Order order) {
		playerTasks.put(p, order);
		p.sendMessage(Vars.PREFIX+"Your sign was created §asuccessfully§7. Please click with the left mouse button on one of the §estone blocks§7 to place it.");
	}
	public static void cancelPlacingPhase(Player p) {
		Order o=playerTasks.remove(p);
		if(o!=null) p.sendMessage(Vars.PREFIX+"The placement process has been §ccanceled§7.");
		else p.sendMessage(Vars.PREFIX+"You are currently not in a placement §cprocess§7.");
	}
	
	public static void onPlayerQuit(PlayerQuitEvent e) {
		Player p=e.getPlayer();
		WorldUtil.removeAllDrewEntries(p);
		ConnectionManager.removePlayer(p);
	}
	
	public static void onPlayerInteract(PlayerInteractEvent e) {
		if(e.getAction()==Action.LEFT_CLICK_BLOCK) {
			Block b=e.getClickedBlock();
			if(b.getType().equals(Material.STONE)) {
				Player p=e.getPlayer();
				Order order=playerTasks.get(p);
				if(order!=null) {
					Location top=b.getLocation();
					Location bottom=b.getLocation().clone();
					int maxI=0;
					while(top.getBlock().getRelative(BlockFace.UP).getType().equals(Material.STONE)) {
						top.add(0,1,0);
						maxI++;
						if(maxI>MAX_STONE_SEARCH) break;
					}
					maxI=0;
					while(bottom.getBlock().getRelative(BlockFace.DOWN).getType().equals(Material.STONE)) {
						bottom.add(0,-1,0);
						maxI++;
						if(maxI>MAX_STONE_SEARCH) break;
					}
					if(top.getBlock().getRelative(BlockFace.NORTH).getType().equals(Material.STONE)||top.getBlock().getRelative(BlockFace.SOUTH).getType().equals(Material.STONE)) {
						maxI=0;
						while(top.getBlock().getRelative(BlockFace.NORTH).getType().equals(Material.STONE)) {
							top.add(0,0,-1);
							maxI++;
							if(maxI>MAX_STONE_SEARCH) break;
						}
						maxI=0;
						while(bottom.getBlock().getRelative(BlockFace.SOUTH).getType().equals(Material.STONE)) {
							bottom.add(0,0,1);
							maxI++;
							if(maxI>MAX_STONE_SEARCH) break;
						}
					}
					if(top.getBlock().getRelative(BlockFace.EAST).getType().equals(Material.STONE)||top.getBlock().getRelative(BlockFace.WEST).getType().equals(Material.STONE)) {
						maxI=0;
						while(top.getBlock().getRelative(BlockFace.EAST).getType().equals(Material.STONE)) {
							top.add(1,0,0);
							maxI++;
							if(maxI>MAX_STONE_SEARCH) break;
						}
						maxI=0;
						while(bottom.getBlock().getRelative(BlockFace.WEST).getType().equals(Material.STONE)) {
							bottom.add(-1,0,0);
							maxI++;
							if(maxI>MAX_STONE_SEARCH) break;
						}
					}
					boolean placed=false;
					for(Direction dir:directions) {
						boolean valid=true;
						if(dir.getX()!=0) {
							valid=top.getBlockX()==bottom.getBlockX();
						} else if(dir.getZ()!=0) {
							valid=top.getBlockZ()==bottom.getBlockZ();
						} else valid=top.getBlockY()==bottom.getBlockY();
						
						if(valid) {
							boolean save=true;
							for(int x=bottom.getBlockX(); x<=top.getBlockX(); x++) {
								for(int y=bottom.getBlockY(); y<=top.getBlockY(); y++) {
									if(!save) break;
									for(int z=top.getBlockZ(); z<=top.getBlockZ(); z++) {
										Block block=top.getWorld().getBlockAt(x+dir.getX(), y+dir.getY(), z+dir.getZ());
										if(!(block.getType().isSolid()&&!block.getType().equals(Material.STONE)&&top.getWorld().getBlockAt(x, y, z).getType().equals(Material.STONE))) {
											save=false;
											break;
										}
									}
								}
							}
							if(save) {
								if(dir.getX()!=0) {
									int minZ;
									int maxZ;
									BlockFace face;
									minZ=Math.min(top.getBlockZ(),bottom.getBlockZ());
									maxZ=Math.max(top.getBlockZ(),bottom.getBlockZ());
									if(dir.getX()==-1) {
										face=BlockFace.EAST;
									} else {
										face=BlockFace.WEST;
									}
									int imgHeight=top.getBlockY()-bottom.getBlockY()+1;
									int imgWidth=maxZ-minZ+1;
									
									if(order.getBackgroundGif()!=null) {
										new BukkitRunnable() {
											@Override
											public void run() {
												GifImage gif=order.getBackgroundGif();
												int amount=gif.getFrameCount();
												BufferedImage frames[]=new BufferedImage[amount];
												for(int i=0; i<amount; i++) {
													frames[i]=ImageGenerator.generate(order,imgWidth,imgHeight,i);
												}
												
												long startTime=System.currentTimeMillis();
												int delay=gif.getDelay(0);
												for(int z=minZ; z<=maxZ; z++) {
													for(int y=top.getBlockY(); y>=bottom.getBlockY(); y--) {
														Location loc=new Location(top.getWorld(), top.getBlockX(), y, z);
														int imgY=top.getBlockY()-y;
														int imgX;
														if(dir.getX()==-1) imgX=maxZ-z;
														else imgX=z-minZ;
														BufferedImage[] individualFrames=new BufferedImage[amount];
														for(int i=0; i<amount; i++) {
															individualFrames[i]=frames[i].getSubimage(imgX*128, imgY*128, 128, 128);
														}
														WorldUtil.spawnItemFrame(loc, individualFrames,delay,startTime, face);
													}
												}
												System.gc();
											}
										}.runTaskAsynchronously(Main.getPlugin());
									} else {
										BufferedImage image=ImageGenerator.generate(order,imgWidth,imgHeight);
										
										for(int z=minZ; z<=maxZ; z++) {
											for(int y=top.getBlockY(); y>=bottom.getBlockY(); y--) {
												Location loc=new Location(top.getWorld(), top.getBlockX(), y, z);
												loc.getBlock().setType(Material.AIR);
												int imgY=top.getBlockY()-y;
												int imgX;
												if(dir.getX()==-1) imgX=maxZ-z;
												else imgX=z-minZ;
												WorldUtil.spawnItemFrame(loc, image.getSubimage(imgX*128, imgY*128, 128, 128), face);
											}
										}
									}
								} else if(dir.getZ()!=0) {
									int minX;
									int maxX;
									BlockFace face;
									maxX=Math.max(top.getBlockX(),bottom.getBlockX());
									minX=Math.min(top.getBlockX(),bottom.getBlockX());
									if(dir.getZ()==-1) {
										face=BlockFace.SOUTH;
									} else {
										face=BlockFace.NORTH;
									}
									int imgHeight=top.getBlockY()-bottom.getBlockY()+1;
									int imgWidth=maxX-minX+1;
									
									if(order.getBackgroundGif()!=null) {
										new BukkitRunnable() {
											@Override
											public void run() {
												GifImage gif=order.getBackgroundGif();
												int amount=gif.getFrameCount();
												BufferedImage frames[]=new BufferedImage[amount];
												for(int i=0; i<amount; i++) {
													frames[i]=ImageGenerator.generate(order,imgWidth,imgHeight,i);
												}
												
												long startTime=System.currentTimeMillis();
												int delay=gif.getDelay(0);
												for(int x=minX; x<=maxX; x++) {
													for(int y=top.getBlockY(); y>=bottom.getBlockY(); y--) {
														Location loc=new Location(top.getWorld(), x, y, top.getBlockZ());
														int imgY=top.getBlockY()-y;
														int imgX;
														if(dir.getZ()==-1) imgX=x-minX;
														else imgX=maxX-x;
														BufferedImage[] individualFrames=new BufferedImage[amount];
														for(int i=0; i<amount; i++) {
															individualFrames[i]=frames[i].getSubimage(imgX*128, imgY*128, 128, 128);
														}
														WorldUtil.spawnItemFrame(loc, individualFrames,delay,startTime, face);
													}
												}
												System.gc();
											}
										}.runTaskAsynchronously(Main.getPlugin());
									} else {
										BufferedImage image=ImageGenerator.generate(order,imgWidth,imgHeight);
										
										for(int x=minX; x<=maxX; x++) {
											for(int y=top.getBlockY(); y>=bottom.getBlockY(); y--) {
												Location loc=new Location(top.getWorld(), x, y, top.getBlockZ());
												loc.getBlock().setType(Material.AIR);
												int imgY=top.getBlockY()-y;
												int imgX;
												if(dir.getZ()==-1) imgX=x-minX;
												else imgX=maxX-x;
												WorldUtil.spawnItemFrame(loc, image.getSubimage(imgX*128, imgY*128, 128, 128), face);
											}
										}
									}
								} else {
									if(WorldUtil.getMcVersion()<WorldUtil.MCVERSION_1_13) {
										placed=true;
										p.sendMessage(Vars.PREFIX+"Item frames can only be placed on the §cfloor/ceiling§7 when using minecraft version §c1.13§7 or higher.");
										playerTasks.remove(p);
										break;
									}
									
									int minX;
									int maxX;
									int minZ;
									int maxZ;
									BlockFace face;
									maxX=Math.max(top.getBlockX(),bottom.getBlockX());
									minX=Math.min(top.getBlockX(),bottom.getBlockX());
									maxZ=Math.max(top.getBlockZ(),bottom.getBlockZ());
									minZ=Math.min(top.getBlockZ(),bottom.getBlockZ());
									if(dir.getY()==-1) {
										face=BlockFace.UP;
									} else {
										face=BlockFace.DOWN;
									}
									int h=maxZ-minZ+1;
									int w=maxX-minX+1;
									float playerYaw=p.getLocation().getYaw()+45;
									while(playerYaw<-180) playerYaw+=360;
									while(playerYaw>180) playerYaw-=360;
									playerYaw+=180;
									int rot=(int)playerYaw/90;
									if(face==BlockFace.DOWN&&rot%2==1) {
										rot+=2;
									}
									if(rot%2==1) {
										int temp=h;
										h=w;
										w=temp;
									}
									final int imgWidth=w;
									final int imgHeight=h;
									final int imgRotation=rot%4;
									
									if(order.getBackgroundGif()!=null) {
										new BukkitRunnable() {
											@Override
											public void run() {
												GifImage gif=order.getBackgroundGif();
												int amount=gif.getFrameCount();
												BufferedImage frames[]=new BufferedImage[amount];
												for(int i=0; i<amount; i++) {
													BufferedImage image=ImageGenerator.generate(order,imgWidth,imgHeight,i);
													frames[i]=image;
												}
												
												long startTime=System.currentTimeMillis();
												int delay=gif.getDelay(0);
												for(int x=minX; x<=maxX; x++) {
													for(int z=minZ; z<=maxZ; z++) {
														Location loc=new Location(top.getWorld(), x, top.getBlockY(), z);
														int imgY;
														if(dir.getY()==-1) imgY=z-minZ;
														else imgY=maxZ-z;
														int imgX=x-minX;
														if(imgRotation%2==1) {
															int temp=imgY;
															imgY=imgX;
															imgX=temp;
														}
														if(imgRotation==2) {
															imgX=(imgWidth-1)-imgX;
															imgY=(imgHeight-1)-imgY;
														} else if(imgRotation==1) {
															imgY=(imgHeight-1)-imgY;
														} else if(imgRotation==3) {
															imgX=(imgWidth-1)-imgX;
														}
														BufferedImage[] individualFrames=new BufferedImage[amount];
														for(int i=0; i<amount; i++) {
															individualFrames[i]=ImageGenerator.rotateImage(frames[i].getSubimage(imgX*128, imgY*128, 128, 128), imgRotation*90);
														}
														WorldUtil.spawnItemFrame(loc, individualFrames,delay,startTime, face);
													}
												}
												System.gc();
											}
										}.runTaskAsynchronously(Main.getPlugin());
									} else {
										BufferedImage image=ImageGenerator.generate(order,imgWidth,imgHeight);
										
										for(int x=minX; x<=maxX; x++) {
											for(int z=minZ; z<=maxZ; z++) {
												Location loc=new Location(top.getWorld(), x, top.getBlockY(), z);
												loc.getBlock().setType(Material.AIR);
												int imgY;
												if(dir.getY()==-1) imgY=z-minZ;
												else imgY=maxZ-z;
												int imgX=x-minX;
												if(imgRotation%2==1) {
													int temp=imgY;
													imgY=imgX;
													imgX=temp;
												}
												if(imgRotation==2) {
													imgX=(imgWidth-1)-imgX;
													imgY=(imgHeight-1)-imgY;
												} else if(imgRotation==1) {
													imgY=(imgHeight-1)-imgY;
												} else if(imgRotation==3) {
													imgX=(imgWidth-1)-imgX;
												}
												WorldUtil.spawnItemFrame(loc, ImageGenerator.rotateImage(image.getSubimage(imgX*128, imgY*128, 128, 128), imgRotation*90), face);
											}
										}
									}
								}
								
								placed=true;
								if(order.getBackgroundGif()!=null) p.sendMessage(Vars.PREFIX+"Please §ewait§7 until the sign has been placed. This may take a bit.");
								else p.sendMessage(Vars.PREFIX+"Your sign has been §aplaced§7 successfully.");
								playerTasks.remove(p);
								break;
							}
						}
					}
					if(!placed) p.sendMessage(Vars.PREFIX+"There must be §csolid §7blocks to hang a sign.");
					e.setCancelled(true);
				}
			}
		}
	}
	
}
