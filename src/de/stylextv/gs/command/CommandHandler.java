package de.stylextv.gs.command;

import java.util.concurrent.CopyOnWriteArrayList;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import de.stylextv.gs.main.Main;
import de.stylextv.gs.main.Variables;
import de.stylextv.gs.permission.PermissionUtil;
import de.stylextv.gs.player.CodeParser;
import de.stylextv.gs.player.Order;
import de.stylextv.gs.player.PlayerManager;
import de.stylextv.gs.world.WorldUtil;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;

public class CommandHandler {
	
	private static CopyOnWriteArrayList<String> oldFiles=null;
	
	public static void create() {
		if(oldFiles==null) {
			String[] files=WorldUtil.getCustomImagesFolder().list();
			if(files!=null) {
				oldFiles=new CopyOnWriteArrayList<String>();
				for(String s:files) oldFiles.add(s);
			}
		}
	}
	
	public static boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if(sender instanceof Player) {
			Player p=(Player) sender;
			boolean hasPermList=PermissionUtil.hasListPermission(p);
			boolean hasPermCreate=PermissionUtil.hasCreatePermission(p);
			boolean hasPermRemove=PermissionUtil.hasRemovePermission(p);
			boolean hasPermUpdate=PermissionUtil.hasUpdatePermission(p);
			if(args.length>=1) {
				String sub=args[0];
				if(sub.equalsIgnoreCase("listfiles")) {
					if(hasPermList) {
						if(args.length<=2) {
							int page=0;
							if(args.length==2) try {
								page=Integer.valueOf(args[1])-1;
							} catch(NumberFormatException ex) {
								p.sendMessage(Variables.PREFIX+"§7Please enter a valid §cpage number§7.");
								return false;
							}
							String[] files=WorldUtil.getCustomImagesFolder().list();
							int length=0;
							if(files!=null) length=files.length;
							int pages=length/11 + (length%11!=0 ? 1 : 0);
							
							if(page<0) page=0;
							else if(page>=pages) {
								page=pages==0 ? 0 : pages-1;
							}
							
							if(length==0) {
								p.sendMessage("§2§m#§a§m⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯§2§m#");
								p.sendMessage("                       §aFiles");
								p.sendMessage("");
								p.sendMessage("§8- §aPage 1 §8>");
								p.sendMessage("    §8- §7§oFolder is empty =(");
								for(int i=0; i<11; i++) p.sendMessage("");
								sendPageArrows(p, page, pages);
								p.sendMessage("§e*§7: Newly added");
								p.sendMessage("");
								p.sendMessage("§2§m#§a§m⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯§2§m#");
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
								p.sendMessage("§2§m#§a§m⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯§2§m#");
								p.sendMessage("                       §aFiles");
								p.sendMessage("");
								p.sendMessage("§8- §aPage "+(page+1)+" §8>");
								for(int i=0; i<11; i++) {
									int index=i+page*11;
									if(index<=j) {
										String name=files[index];
										boolean isNew=!oldFiles.contains(name);
										if(isNew) {
											oldFiles.add(name);
										}
										sendFile(p, name, isNew);
									} else p.sendMessage("");
								}
								p.sendMessage("");
								sendPageArrows(p, page, pages);
								p.sendMessage("§e*§7: Newly added");
								p.sendMessage("");
								p.sendMessage("§2§m#§a§m⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯§2§m#");
							}
						} else p.sendMessage(Variables.PREFIX+"§7Use §c/gs listfiles [Page]");
					} else sendNoPermission(p);
				} else if(sub.equalsIgnoreCase("remove")) {
					if(hasPermRemove) {
						if(args.length==1) {
							PlayerManager.toggleRemovingPhase(p);
						} else p.sendMessage(Variables.PREFIX+"§7Use §c/gs remove");
					} else sendNoPermission(p);
				} else if(sub.equalsIgnoreCase("update")) {
					if(hasPermUpdate) {
						if(args.length==1) {
							Main.getPlugin().runAutoUpdater(p);
						} else p.sendMessage(Variables.PREFIX+"§7Use §c/gs update");
					} else sendNoPermission(p);
				} else if(hasPermList||hasPermCreate||hasPermRemove||hasPermUpdate) {
					if(args.length==1) {
						if(sub.equalsIgnoreCase("create")) {
							if(hasPermCreate) sendCreateSuggestion(p);
							else sendNoPermission(p);
						} else if(sub.equalsIgnoreCase("help")) {
							sendHelp(p);
						} else if(sub.equalsIgnoreCase("info")) {
							sendInfo(p);
						} else if(sub.equalsIgnoreCase("cancel")) {
							if(hasPermCreate) PlayerManager.cancelPlacingPhase(p);
							else sendNoPermission(p);
						} else sendHelpSuggestion(p);
					} else {
						if(args.length>1&&sub.equalsIgnoreCase("create")) {
							if(hasPermCreate) {
								if(args.length>=2) {
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
									
									return false;
								} else sendCreateSuggestion(p);
							} else sendNoPermission(p);
						} else sendHelpSuggestion(p);
					}
				} else {
					sendInfo(p);
				}
			} else {
				if(hasPermList||hasPermCreate||hasPermRemove||hasPermUpdate) sendHelpSuggestion(p);
				else sendInfo(p);
			}
		} else sender.sendMessage(Variables.PREFIX_CONSOLE+"§7This command is for §cplayers§r only.");
		return false;
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
	private static void sendPageArrows(Player p, int page, int pages) {
		TextComponent comp=new TextComponent("    ");
		if(page>0) comp.addExtra(getPageArrow(page-1, true, "listfiles"));
		else comp.addExtra(getPageArrow(true));
		TextComponent line=new TextComponent(" §8| ");
		comp.addExtra(line);
		if(page<pages-1) comp.addExtra(getPageArrow(page+1, false, "listfiles"));
		else comp.addExtra(getPageArrow(false));
		comp.addExtra(line);
		comp.addExtra(
				createClickableComponent("§eRefresh", "§7Click here to refresh the §ecurrent§7 page.", "/gs listfiles "+(page+1), ClickEvent.Action.RUN_COMMAND)
		);
		p.spigot().sendMessage(comp);
	}
	private static TextComponent getPageArrow(int page, boolean dir, String cmd) {
	    return createClickableComponent(dir ? "§d§l←" : "§d§l→", dir ? "§7Click here to view the §eprevious§7 page." : "§7Click here to view the §enext§7 page.", "/gs "+cmd+" "+(page+1), ClickEvent.Action.RUN_COMMAND);
	}
	private static TextComponent getPageArrow(boolean dir) {
		TextComponent comp=new TextComponent(dir ? "§8§l←" : "§8§l→");
		return comp;
	}
	private static void sendHelpSuggestion(Player p) {
		p.sendMessage(Variables.PREFIX+"§7Use §e/gs help§7 to get a list of commands.");
	}
	private static void sendCreateSuggestion(Player p) {
		p.sendMessage(Variables.PREFIX+"§7Use §c/gs create (Code)");
	}
	private static void sendNoPermission(Player p) {
		p.sendMessage(Variables.PREFIX+"§7You don't have the right §cpermission§7 to do that.");
	}
	private static void sendHelp(Player p) {
		p.sendMessage("§2§m#§a§m⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯§2§m#");
		p.sendMessage("                     §a"+Variables.NAME);
		p.sendMessage("");
		p.sendMessage("§8- §a/gs create (Code) §8> §7Create sign§e*");
		p.sendMessage("§8- §a/gs remove §8> §7Remove sign§e*");
		p.sendMessage("§8- §a/gs cancel §8> §7Cancel placement§e*");
		p.sendMessage("§8- §a/gs listfiles [Page] §8> §7Lists your images§e*");
		p.sendMessage("§8- §a/gs update §8> §7Update plugin§e*");
		p.sendMessage("§8- §a/gs help §8> §7Show help");
		p.sendMessage("§8- §a/gs info §8> §7Show plugin information");
		p.sendMessage("");
		p.sendMessage("§7(): §aRequired§7, []: §aOptional");
		p.sendMessage("§e*§7: Needs extra permission to be executed");
		p.sendMessage("");
		p.sendMessage("§2§m#§a§m⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯§2§m#");
	}
	private static void sendInfo(Player p) {
		p.sendMessage("§2§m#§a§m⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯§2§m#");
		p.sendMessage("                   §aInformation");
		p.sendMessage("");
		p.sendMessage("§7§oThis server uses the free and open");
		p.sendMessage("§7§o source plugin §e"+Variables.NAME+"§7§o to put images and");
		p.sendMessage("§7§o gifs into item frames!");
		p.sendMessage("");
		p.sendMessage("§8- §7Developed by §8> §d"+Variables.AUTHOR);
		p.sendMessage("§8- §7Installed version §8> §d"+Variables.VERSION);
		p.sendMessage("");
		p.sendMessage("§2§m#§a§m⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯§2§m#");
	}
	
	private static TextComponent createClickableComponent(String baseText, String hoverText, String clickText, ClickEvent.Action clickAction) {
		if(WorldUtil.getMcVersion()==WorldUtil.MCVERSION_1_8) {
			TextComponent comp=new TextComponent(baseText);
			comp.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(hoverText).create()));
			comp.setClickEvent(new ClickEvent(clickAction, clickText));
			return comp;
		} else {
			TextComponent comp=new TextComponent(baseText);
			comp.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(new TextComponent(hoverText)).create()));
			comp.setClickEvent(new ClickEvent(clickAction, clickText));
			return comp;
		}
	}
	
}
