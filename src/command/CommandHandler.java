package de.stylextv.gs.command;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import de.stylextv.gs.main.Main;
import de.stylextv.gs.main.Vars;
import de.stylextv.gs.permission.PermissionUtil;
import de.stylextv.gs.player.CodeParser;
import de.stylextv.gs.player.Order;
import de.stylextv.gs.player.PlayerManager;

public class CommandHandler {
	
	public static boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if(sender instanceof Player) {
			Player p=(Player) sender;
			if(PermissionUtil.hasCreatePermission(p)) {
				if(args.length==1) {
					String sub=args[0];
					if(sub.equalsIgnoreCase("create")) {
						sendCreateSuggestion(p);
					} else if(sub.equalsIgnoreCase("help")) {
						sendHelp(p);
					} else if(sub.equalsIgnoreCase("info")) {
						sendInfo(p);
					} else sendHelpSuggestion(p);
				} else {
					if(args.length>1&&args[0].equalsIgnoreCase("create")) {
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
											p.sendMessage(Vars.PREFIX+"§7The following value could not be parsed: §c"+order.getError());
										} else {
											PlayerManager.startPlacingPhase(p, order);
										}
									} else p.sendMessage(Vars.PREFIX+"§7The §ccode§7 you provided could not be parsed.");
									
								}
							}.runTaskAsynchronously(Main.getPlugin());
							
							return false;
						} else sendCreateSuggestion(p);
					} else sendHelpSuggestion(p);
				}
			} else sendInfo(p);
		} else sender.sendMessage(Vars.PREFIX_CONSOLE+"§7This command is for §cplayers§7 only.");
		return false;
	}
	private static void sendHelpSuggestion(Player p) {
		p.sendMessage(Vars.PREFIX+"§7Please enter §8\"§7/gs §ehelp§8\"§7 to get a list of commands.");
	}
	private static void sendCreateSuggestion(Player p) {
		p.sendMessage(Vars.PREFIX+"§7Please use §8\"§7/gs create §c<code>§8\"§7.");
	}
	private static void sendHelp(Player p) {
		p.sendMessage("§a>§m---------------------§6  Help  §a§m---------------------§a<");
		p.sendMessage("");
		p.sendMessage("    §7/gs §ecreate §7<code>§7: Lets you create and place a");
		p.sendMessage("    §7 new sign");
		p.sendMessage("    §8 (Requires the permission: gsigns.create)");
		p.sendMessage("");
		p.sendMessage("    §7/gs §ehelp§7: Shows this command list");
		p.sendMessage("    §8 (Requires the permission: gsigns.create)");
		p.sendMessage("");
		p.sendMessage("    §7/gs §einfo§7: Shows general information about this plugin");
		p.sendMessage("");
		p.sendMessage("§a>§m------------------------------------------------§a<");
	}
	private static void sendInfo(Player p) {
		p.sendMessage("§5>§m------------------§6  Information  §5§m------------------§5<");
		p.sendMessage("");
		p.sendMessage("    §7This server uses the free plugin §bGSigns§7.");
		p.sendMessage("");
		p.sendMessage("    §7Author: §e"+Vars.AUTHOR);
		p.sendMessage("");
		p.sendMessage("    §7Version: §6"+Vars.VERSION);
		p.sendMessage("    §8 (Please visit our spigot site for updates)");
		p.sendMessage("");
		p.sendMessage("§5>§m------------------------------------------------§5<");
	}
	
}
