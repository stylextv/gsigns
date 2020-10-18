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
			
			if(hasPermList || hasPermCreate || hasPermRemove || hasPermUpdate) {
				if(args.length == 0) {
					sendHelpSuggestion(p);
				} else {
					
					String subCommand=args[0];
					if(subCommand.equalsIgnoreCase("listfiles")) {
						if(hasPermList) {
							handleListCommand(p, args);
						} else sendNoPermission(p);
					} else if(subCommand.equalsIgnoreCase("remove")) {
						if(hasPermRemove) {
							if(args.length==1) {
								PlayerManager.toggleRemovingPhase(p);
							} else p.sendMessage(Variables.PREFIX+"§7Use §c/gs remove");
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
				sendInfo(p);
			}
			
		} else sender.sendMessage(Variables.PREFIX_CONSOLE+"§7This command is for §cplayers§r only.");
		return false;
	}
	
	private static void handleListCommand(Player p, String[] args) {
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
				p.sendMessage("");
				p.sendMessage("§8- "+Variables.COLOR1+"Page 1 §8>");
				p.sendMessage("    §8- §7§oFolder is empty =(");
				for(int i=0; i<11; i++) p.sendMessage("");
				sendPageArrows(p, page, pages);
				p.sendMessage("§e*§7: Newly added");
				p.sendMessage("");
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
				p.sendMessage("");
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
					} else p.sendMessage("");
				}
				p.sendMessage("");
				sendPageArrows(p, page, pages);
				p.sendMessage("§e*§7: Newly added");
				p.sendMessage("");
				p.sendMessage(Variables.COLOR2+"§m#"+Variables.COLOR1+"§m"+UI_TEXT_LINE+Variables.COLOR2+"§m#");
			}
		} else p.sendMessage(Variables.PREFIX+"§7Use §c/gs listfiles [Page]");
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
		p.sendMessage(
				Variables.COLOR2+"§m#"+Variables.COLOR1+"§m"+UI_TEXT_LINE+Variables.COLOR2+"§m#"+"§r\n"+
				"                     "+Variables.COLOR1+Variables.NAME+"§r\n\n"+
				"§8- "+Variables.COLOR1+"/gs create (Code) §8> §7Create sign§e*"+"§r\n"+
				"§8- "+Variables.COLOR1+"/gs remove §8> §7Remove sign§e*"+"§r\n"+
				"§8- "+Variables.COLOR1+"/gs cancel §8> §7Cancel placement§e*"+"§r\n"+
				"§8- "+Variables.COLOR1+"/gs listfiles [Page] §8> §7Lists your images§e*"+"§r\n"+
				"§8- "+Variables.COLOR1+"/gs update §8> §7Update plugin§e*"+"§r\n"+
				"§8- "+Variables.COLOR1+"/gs help §8> §7Show help"+"§r\n"+
				"§8- "+Variables.COLOR1+"/gs info §8> §7Show plugin information"+"§r\n\n"+
				"§7(): "+Variables.COLOR1+"Required§7, []: "+Variables.COLOR1+"Optional"+"§r\n"+
				"§e*§7: Needs extra permission to be executed"+"§r\n\n"+
				Variables.COLOR2+"§m#"+Variables.COLOR1+"§m"+UI_TEXT_LINE+Variables.COLOR2+"§m#"
		);
	}
	private static void sendInfo(Player p) {
		p.sendMessage(
				Variables.COLOR2+"§m#"+Variables.COLOR1+"§m"+UI_TEXT_LINE+Variables.COLOR2+"§m#"+"§r\n"+
				"                   "+Variables.COLOR1+"Information"+"§r\n\n"+
				"§7§oThis server uses the free and open"+"§r\n"+
				"§7§o source plugin §e"+Variables.NAME+"§7§o to put images and"+"§r\n"+
				"§7§o gifs into item frames!"+"§r\n\n"+
				"§8- §7Developed by §8> §b"+Variables.AUTHOR+"§r\n"+
				"§8- §7Installed version §8> §b"+Variables.VERSION+"§r\n\n"+
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
