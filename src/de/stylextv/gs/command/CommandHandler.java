package de.stylextv.gs.command;

import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import de.stylextv.gs.gui.GuiManager;
import de.stylextv.gs.main.Main;
import de.stylextv.gs.main.Variables;
import de.stylextv.gs.permission.PermissionUtil;
import de.stylextv.gs.player.CodeParser;
import de.stylextv.gs.player.Order;
import de.stylextv.gs.player.PlayerManager;
import de.stylextv.gs.world.BetterSign;
import de.stylextv.gs.world.WorldUtil;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;

public class CommandHandler {
	
	private static final TextComponent TEXTCOMP_SPACE = new TextComponent(" ");
	
	private static String UI_TEXT_LINE;
	private static String UI_TEXT_ARROW_LEFT;
	private static String UI_TEXT_ARROW_RIGHT;
	
	private static CopyOnWriteArrayList<String> oldFiles=null;
	
	public static void create() {
		if(oldFiles==null) {
			String[] files=WorldUtil.getLocalImagesFolder().list();
			if(files!=null) {
				oldFiles=new CopyOnWriteArrayList<String>();
				for(String s:files) oldFiles.add(s);
			}
		}
		
		if(WorldUtil.getMcVersion()<WorldUtil.MCVERSION_1_13) {
			UI_TEXT_LINE="---------------------------------";
			UI_TEXT_ARROW_LEFT="◀";
			UI_TEXT_ARROW_RIGHT="▶";
		} else {
			UI_TEXT_LINE = "⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯";
			UI_TEXT_ARROW_LEFT="←";
			UI_TEXT_ARROW_RIGHT="→";
		}
	}
	
	public static boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if(sender instanceof Player) {
			Player p=(Player) sender;
			boolean hasPermList=PermissionUtil.hasListPermission(p);
			boolean hasPermCreate=PermissionUtil.hasCreatePermission(p);
			boolean hasPermRemove=PermissionUtil.hasRemovePermission(p);
			boolean hasPermUpdate=PermissionUtil.hasUpdatePermission(p);
			boolean hasPermPause=PermissionUtil.hasPausePermission(p);
			boolean hasPermGui=PermissionUtil.hasGuiPermission(p);
			boolean hasPermTp=PermissionUtil.hasTeleportPermission(p);
			
			if(hasPermList || hasPermCreate || hasPermRemove || hasPermUpdate || hasPermPause || hasPermGui || hasPermTp) {
				if(args.length == 0) {
					sendHelpSuggestion(p);
				} else {
					
					String subCommand=args[0];
					if(subCommand.equalsIgnoreCase("listfiles")) {
						if(hasPermList) {
							handleListFilesCommand(p, args);
						} else sendNoPermission(p);
					} else if(subCommand.equalsIgnoreCase("listsigns")) {
						if(hasPermList) {
							handleListSignsCommand(p, args);
						} else sendNoPermission(p);
					} else if(subCommand.equalsIgnoreCase("gui")) {
						if(hasPermGui) {
							if(args.length==1) {
								GuiManager.openMainGui(p, 0, 0);
							} else p.sendMessage(Variables.PREFIX+"§7Use §c/gs gui");
						} else sendNoPermission(p);
					} else if(subCommand.equalsIgnoreCase("remove")) {
						if(hasPermRemove) {
							if(args.length==1) {
								PlayerManager.toggleRemovingPhase(p);
							} else if(args.length==2) {
								try {
									UUID uid=UUID.fromString(args[1]);
									if(WorldUtil.removeSign(uid)) {
										p.sendMessage(Variables.PREFIX+"§7The sign was §aremoved §7successfully.");
									} else p.sendMessage(Variables.PREFIX+"§7Could not find a sign with the UUID: §c"+args[1]+"§7.");
								} catch(IllegalArgumentException ex) {
									p.sendMessage(Variables.PREFIX+"§7Please enter a valid §cUUID§7.");
								}
							} else p.sendMessage(Variables.PREFIX+"§7Use §c/gs remove [UUID]");
						} else sendNoPermission(p);
					} else if(subCommand.equalsIgnoreCase("tp")) {
						if(hasPermTp) {
							if(args.length==2) {
								try {
									UUID uid=UUID.fromString(args[1]);
									BetterSign sign = WorldUtil.getSign(uid);
									if(sign!=null) {
										sign.teleport(p);
										p.sendMessage(Variables.PREFIX+"§7You have been §ateleported §7successfully.");
									} else p.sendMessage(Variables.PREFIX+"§7Could not find a sign with the UUID: §c"+args[1]+"§7.");
								} catch(IllegalArgumentException ex) {
									p.sendMessage(Variables.PREFIX+"§7Please enter a valid §cUUID§7.");
								}
							} else p.sendMessage(Variables.PREFIX+"§7Use §c/gs tp (UUID)");
						} else sendNoPermission(p);
					} else if(subCommand.equalsIgnoreCase("play")) {
						if(hasPermPause) {
							if(args.length==2) {
								try {
									UUID uid=UUID.fromString(args[1]);
									BetterSign sign = WorldUtil.getSign(uid);
									if(sign!=null) {
										if(!sign.isGif()) p.sendMessage(Variables.PREFIX+"§7This sign does not contain a §cGIF§7.");
										else if(!sign.isPaused()) p.sendMessage(Variables.PREFIX+"§7The sign is already §cplaying§7.");
										else {
											sign.play();
											p.sendMessage(Variables.PREFIX+"§7The sign is §aplaying §7now.");
										}
									} else p.sendMessage(Variables.PREFIX+"§7Could not find a sign with the UUID: §c"+args[1]+"§7.");
								} catch(IllegalArgumentException ex) {
									p.sendMessage(Variables.PREFIX+"§7Please enter a valid §cUUID§7.");
								}
							} else p.sendMessage(Variables.PREFIX+"§7Use §c/gs play (UUID)");
						} else sendNoPermission(p);
					} else if(subCommand.equalsIgnoreCase("pause")) {
						if(hasPermPause) {
							if(args.length==2) {
								try {
									UUID uid=UUID.fromString(args[1]);
									BetterSign sign = WorldUtil.getSign(uid);
									if(sign!=null) {
										if(!sign.isGif()) p.sendMessage(Variables.PREFIX+"§7This sign does not contain a §cGIF§7.");
										else if(sign.isPaused()) p.sendMessage(Variables.PREFIX+"§7The sign is already §cpaused§7.");
										else {
											sign.pause();
											p.sendMessage(Variables.PREFIX+"§7The sign has now §astopped §7playing.");
										}
									} else p.sendMessage(Variables.PREFIX+"§7Could not find a sign with the UUID: §c"+args[1]+"§7.");
								} catch(IllegalArgumentException ex) {
									p.sendMessage(Variables.PREFIX+"§7Please enter a valid §cUUID§7.");
								}
							} else p.sendMessage(Variables.PREFIX+"§7Use §c/gs pause (UUID)");
						} else sendNoPermission(p);
					} else if(subCommand.equalsIgnoreCase("update")) {
						if(hasPermUpdate) {
							if(args.length==1) {
								Main.getPlugin().runAutoUpdater(p);
							} else p.sendMessage(Variables.PREFIX+"§7Use §c/gs update");
						} else sendNoPermission(p);
					} else if(subCommand.equalsIgnoreCase("create")) {
						if(hasPermCreate) {
							if(args.length==1) {
								sendCreateSuggestion(p);
							} else {
								handleCreateCommand(p, args);
							}
						} else sendNoPermission(p);
					} else if(subCommand.equalsIgnoreCase("cancel")) {
						if(hasPermCreate) {
							if(args.length==1) {
								PlayerManager.cancelPlacingPhase(p);
							} else p.sendMessage(Variables.PREFIX+"§7Use §c/gs cancel");
						} else sendNoPermission(p);
					} else if(subCommand.equalsIgnoreCase("help")) {
						sendHelp(p);
					} else if(subCommand.equalsIgnoreCase("info")) {
						sendInfo(p);
					} else sendHelpSuggestion(p);
					
				}
			} else {
				sendNoPermission(p);
			}
			
		} else sender.sendMessage(Variables.PREFIX_CONSOLE+"§7This command is for §cplayers§r only.");
		return false;
	}
	
	private static void handleListFilesCommand(Player p, String[] args) {
		if(args.length<=2) {
			int page=0;
			if(args.length==2) try {
				page=Integer.valueOf(args[1])-1;
			} catch(NumberFormatException ex) {
				p.sendMessage(Variables.PREFIX+"§7Please enter a valid §cpage number§7.");
				return;
			}
			String[] files=WorldUtil.getLocalImagesFolder().list();
			int length=0;
			if(files!=null) length=files.length;
			int pages=length/11 + (length%11!=0 ? 1 : 0);
			
			if(page<0) page=0;
			else if(page>=pages) {
				page=pages==0 ? 0 : pages-1;
			}
			
			if(length==0) {
				p.sendMessage(Variables.COLOR2+"§m#"+Variables.COLOR1+"§m"+UI_TEXT_LINE+Variables.COLOR2+"§m#");
				p.sendMessage("                       "+Variables.COLOR1+"Files");
				p.sendMessage(" ");
				p.sendMessage("§8- "+Variables.COLOR1+"Page 1 §8>");
				p.sendMessage("    §8- §7§oFolder is empty =(");
				for(int i=0; i<11; i++) p.sendMessage(" ");
				sendPageArrows(p, page, pages, "listfiles");
				p.sendMessage("§e*§7: Newly added");
				p.sendMessage(" ");
				p.sendMessage(Variables.COLOR2+"§m#"+Variables.COLOR1+"§m"+UI_TEXT_LINE+Variables.COLOR2+"§m#");
			} else {
				if(oldFiles==null) {
					oldFiles=new CopyOnWriteArrayList<String>();
					for(String s:files) oldFiles.add(s);
				} else for(String s:oldFiles) {
					boolean remove=true;
					for(String check:files) {
						if(check.equalsIgnoreCase(s)) {
							remove=false;
							break;
						}
					}
					if(remove) oldFiles.remove(s);
				}
				
				int j=page*11+10;
				if(j>=length) j=length-1;
				p.sendMessage(Variables.COLOR2+"§m#"+Variables.COLOR1+"§m"+UI_TEXT_LINE+Variables.COLOR2+"§m#");
				p.sendMessage("                       "+Variables.COLOR1+"Files");
				p.sendMessage(" ");
				p.sendMessage("§8- "+Variables.COLOR1+"Page "+(page+1)+" §8>");
				for(int i=0; i<11; i++) {
					int index=i+page*11;
					if(index<=j) {
						String name=files[index];
						boolean isNew=!oldFiles.contains(name);
						if(isNew) {
							oldFiles.add(name);
						}
						sendFile(p, name, isNew);
					} else p.sendMessage(" ");
				}
				p.sendMessage(" ");
				sendPageArrows(p, page, pages, "listfiles");
				p.sendMessage("§e*§7: Newly added");
				p.sendMessage(" ");
				p.sendMessage(Variables.COLOR2+"§m#"+Variables.COLOR1+"§m"+UI_TEXT_LINE+Variables.COLOR2+"§m#");
			}
		} else p.sendMessage(Variables.PREFIX+"§7Use §c/gs listfiles [Page]");
	}
	private static void handleListSignsCommand(Player p, String[] args) {
		if(args.length<=2) {
			int page=0;
			if(args.length==2) try {
				page=Integer.valueOf(args[1])-1;
			} catch(NumberFormatException ex) {
				p.sendMessage(Variables.PREFIX+"§7Please enter a valid §cpage number§7.");
				return;
			}
			CopyOnWriteArrayList<BetterSign> signs=WorldUtil.getSigns();
			int length=signs.size();
			int pages=length/12 + (length%12!=0 ? 1 : 0);
			
			if(page<0) page=0;
			else if(page>=pages) {
				page=pages==0 ? 0 : pages-1;
			}
			
			if(length==0) {
				p.sendMessage(Variables.COLOR2+"§m#"+Variables.COLOR1+"§m"+UI_TEXT_LINE+Variables.COLOR2+"§m#");
				p.sendMessage("                       "+Variables.COLOR1+"Signs");
				p.sendMessage(" ");
				p.sendMessage("§8- "+Variables.COLOR1+"Page 1 §8>");
				p.sendMessage("    §8- §7§oNo signs created yet =(");
				for(int i=0; i<12; i++) p.sendMessage(" ");
				sendPageArrows(p, page, pages, "listsigns");
				p.sendMessage(" ");
				p.sendMessage(Variables.COLOR2+"§m#"+Variables.COLOR1+"§m"+UI_TEXT_LINE+Variables.COLOR2+"§m#");
			} else {
				int j=page*12+11;
				if(j>=length) j=length-1;
				p.sendMessage(Variables.COLOR2+"§m#"+Variables.COLOR1+"§m"+UI_TEXT_LINE+Variables.COLOR2+"§m#");
				p.sendMessage("                       "+Variables.COLOR1+"Signs");
				p.sendMessage(" ");
				p.sendMessage("§8- "+Variables.COLOR1+"Page "+(page+1)+" §8>");
				for(int i=0; i<12; i++) {
					int index=i+page*12;
					if(index<=j) {
						sendSign(p, signs.get(index));
					} else p.sendMessage(" ");
				}
				p.sendMessage(" ");
				sendPageArrows(p, page, pages, "listsigns");
				p.sendMessage(" ");
				p.sendMessage(Variables.COLOR2+"§m#"+Variables.COLOR1+"§m"+UI_TEXT_LINE+Variables.COLOR2+"§m#");
			}
		} else p.sendMessage(Variables.PREFIX+"§7Use §c/gs listsigns [Page]");
	}
	private static void handleCreateCommand(Player p, String[] args) {
		new BukkitRunnable() {
			@Override
			public void run() {
				String code="";
				for(int i=1; i<args.length; i++) {
					if(i!=1) code=code+" ";
					code=code+args[i];
				}
				Order order=CodeParser.parseCode(code);
				if(order!=null) {
					if(order.getError()!=null) {
						p.sendMessage(Variables.PREFIX+"§7The following value could not be parsed: §c"+order.getError());
					} else {
						PlayerManager.startPlacingPhase(p, order);
					}
				} else p.sendMessage(Variables.PREFIX+"§7The §ccode§7 you provided could not be parsed.");
				
			}
		}.runTaskAsynchronously(Main.getPlugin());
	}
	
	private static void sendFile(Player p, String file, boolean isNew) {
		TextComponent comp=new TextComponent("    ");
		String displayName=file;
		int j=isNew ? 1 : 0;
		if(displayName.length()>30-j) displayName=displayName.substring(0, 27-j)+"...";
		comp.addExtra(
				createClickableComponent("§8- §7"+displayName, "§7Click here to get a §ecommand§7 for this file.", "/gs create {bg-img:"+file+"}", ClickEvent.Action.SUGGEST_COMMAND)
		);
		if(isNew) comp.addExtra(new TextComponent("§e*"));
		p.spigot().sendMessage(comp);
	}
	private static void sendSign(Player p, BetterSign sign) {
		String uuid=sign.getUid().toString();
		String displayName=uuid.substring(0, 4)+"..., "+sign.getWorld().getName()+", "+sign.getSize();
		boolean playButton=sign.isGif();
		int j=playButton ? 12 : 8;
		if(displayName.length()>32-j) displayName=displayName.substring(0, 29-j)+"...";
		TextComponent comp=new TextComponent("    §8- §7"+displayName+" ");
		comp.addExtra(
				createClickableComponent("§b[§b§l↓§b]", "§7Click here to §eteleport§7 to this sign.", "/gs tp "+uuid, ClickEvent.Action.RUN_COMMAND)
		);
		comp.addExtra(TEXTCOMP_SPACE);
		if(playButton) {
			if(sign.isPaused()) {
				comp.addExtra(
						createClickableComponent("§e[§e⏵§e]", "§7Click here to §eplay§7 this sign.", "/gs play "+uuid, ClickEvent.Action.RUN_COMMAND)
				);
			} else {
				comp.addExtra(
						createClickableComponent("§e[§e⏸§e]", "§7Click here to §epause§7 this sign.", "/gs pause "+uuid, ClickEvent.Action.RUN_COMMAND)
				);
			}
			comp.addExtra(TEXTCOMP_SPACE);
		}
		comp.addExtra(
				createClickableComponent("§c[§c§l❌§c]", "§7Click here to §eremove§7 this sign.", "/gs remove "+uuid, ClickEvent.Action.SUGGEST_COMMAND)
		);
		p.spigot().sendMessage(comp);
	}
	private static void sendPageArrows(Player p, int page, int pages, String cmd) {
		TextComponent comp=new TextComponent("    ");
		if(page>0) comp.addExtra(getPageArrow(page-1, true, cmd));
		else comp.addExtra(getPageArrow(true));
		TextComponent line=new TextComponent(" §8| ");
		comp.addExtra(line);
		if(page<pages-1) comp.addExtra(getPageArrow(page+1, false, cmd));
		else comp.addExtra(getPageArrow(false));
		comp.addExtra(line);
		comp.addExtra(
				createClickableComponent("§eRefresh", "§7Click here to refresh the §ecurrent§7 page.", "/gs "+cmd+" "+(page+1), ClickEvent.Action.RUN_COMMAND)
		);
		p.spigot().sendMessage(comp);
	}
	
	private static void sendHelpSuggestion(Player p) {
		p.sendMessage(Variables.PREFIX+"§7Use §e/gs help§7 to get a list of commands.");
	}
	private static void sendCreateSuggestion(Player p) {
		p.sendMessage(Variables.PREFIX+"§7Use §c/gs create (Code)");
	}
	public static void sendNoPermission(Player p) {
		p.sendMessage(Variables.PREFIX+"§7You don't have the right §cpermission§7 to do that.");
	}
	private static void sendHelp(Player p) {
		p.sendMessage(
				Variables.COLOR2+"§m#"+Variables.COLOR1+"§m"+UI_TEXT_LINE+Variables.COLOR2+"§m#"+"§r\n"+
				"                     "+Variables.COLOR1+Variables.NAME+"§r\n \n"+
				"§8- "+Variables.COLOR1+"/gs create (Code) §8> §7Create a sign§e*"+"§r\n"+
				"§8- "+Variables.COLOR1+"/gs remove [UUID] §8> §7Remove a sign§e*"+"§r\n"+
				"§8- "+Variables.COLOR1+"/gs cancel §8> §7Cancel placement§e*"+"§r\n"+
				"§8- "+Variables.COLOR1+"/gs listfiles [Page] §8> §7Lists your images§e*"+"§r\n"+
				"§8- "+Variables.COLOR1+"/gs listsigns [Page] §8> §7Lists your signs§e*"+"§r\n"+
				"§8- "+Variables.COLOR1+"/gs gui §8> §7Open gui§e*"+"§r\n"+
				"§8- "+Variables.COLOR1+"/gs tp (UUID) §8> §7Teleport to a sign§e*"+"§r\n"+
				"§8- "+Variables.COLOR1+"/gs play (UUID) §8> §7Play a sign§e*"+"§r\n"+
				"§8- "+Variables.COLOR1+"/gs pause (UUID) §8> §7Pause a sign§e*"+"§r\n"+
				"§8- "+Variables.COLOR1+"/gs update §8> §7Update plugin§e*"+"§r\n"+
				"§8- "+Variables.COLOR1+"/gs help §8> §7Show help"+"§r\n"+
				"§8- "+Variables.COLOR1+"/gs info §8> §7Show plugin information"+"§r\n \n"+
				"§7(): "+Variables.COLOR1+"Required§7, []: "+Variables.COLOR1+"Optional"+"§r\n"+
				"§e*§7: Needs extra permission to be executed"+"§r\n \n"+
				Variables.COLOR2+"§m#"+Variables.COLOR1+"§m"+UI_TEXT_LINE+Variables.COLOR2+"§m#"
		);
	}
	private static void sendInfo(Player p) {
		p.sendMessage(
				Variables.COLOR2+"§m#"+Variables.COLOR1+"§m"+UI_TEXT_LINE+Variables.COLOR2+"§m#"+"§r\n"+
				"                   "+Variables.COLOR1+"Information"+"§r\n \n"+
				"§7§oYou are using the open source plugin"+"§r\n"+
				"§7§o §e"+Variables.NAME+"§7§o to put images and gifs into"+"§r\n"+
				"§7§o item frames!"+"§r\n \n"+
				"§8- §7Developed by §8> §b"+Variables.AUTHOR+"§r\n"+
				"§8- §7Installed version §8> §b"+Variables.VERSION+"§r\n \n"+
				Variables.COLOR2+"§m#"+Variables.COLOR1+"§m"+UI_TEXT_LINE+Variables.COLOR2+"§m#"
		);
	}
	
	private static TextComponent getPageArrow(int page, boolean dir, String cmd) {
	    return createClickableComponent(dir ? "§b§l"+UI_TEXT_ARROW_LEFT : "§b§l"+UI_TEXT_ARROW_RIGHT, dir ? "§7Click here to view the §eprevious§7 page." : "§7Click here to view the §enext§7 page.", "/gs "+cmd+" "+(page+1), ClickEvent.Action.RUN_COMMAND);
	}
	private static TextComponent getPageArrow(boolean dir) {
		TextComponent comp=new TextComponent(dir ? "§8§l"+UI_TEXT_ARROW_LEFT : "§8§l"+UI_TEXT_ARROW_RIGHT);
		return comp;
	}
	@SuppressWarnings("deprecation")
	private static TextComponent createClickableComponent(String baseText, String hoverText, String clickText, ClickEvent.Action clickAction) {
		TextComponent comp=new TextComponent(baseText);
		if(WorldUtil.getMcVersion()<=WorldUtil.MCVERSION_1_11) {
			comp.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(hoverText).create()));
		} else {
			comp.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(new TextComponent(hoverText)).create()));
		}
		comp.setClickEvent(new ClickEvent(clickAction, clickText));
		return comp;
	}
	
}
