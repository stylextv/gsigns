package de.stylextv.gs.gui;

import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.scheduler.BukkitRunnable;

import de.stylextv.gs.main.Main;
import de.stylextv.gs.main.Variables;
import de.stylextv.gs.world.BetterSign;
import de.stylextv.gs.world.WorldUtil;

public class GuiManager {
	
	private static HashMap<Integer, MainMenu> mainMenuPages0 = new HashMap<Integer, MainMenu>();
	private static HashMap<Integer, MainMenu> mainMenuPages1 = new HashMap<Integer, MainMenu>();
	private static HashMap<Integer, MainMenu> mainMenuPages2 = new HashMap<Integer, MainMenu>();
	private static HashMap<BetterSign, SignMenu> signMenues = new HashMap<BetterSign, SignMenu>();
	
	private static ConcurrentHashMap<Player, Menu> openedMenues = new ConcurrentHashMap<Player, Menu>();
	
	private static boolean inDynamicContentUpdate;
	
	public static void openMainGui(Player p, int page, int sortingType) {
		int n=WorldUtil.getSigns().size();
		int lastPage=n/36;
		if(n%36==0 && lastPage>0) lastPage--;
		
		if(page>lastPage) page=lastPage;
		
		HashMap<Integer, MainMenu> map=null;
		if(sortingType==0) map=mainMenuPages0;
		else if(sortingType==1) map=mainMenuPages1;
		else map=mainMenuPages2;
		
		MainMenu menu = map.get(page);
		if(menu == null) {
			menu = new MainMenu(page, sortingType);
			menu.create();
			map.put(page, menu);
		}
		openMainGui(p, menu);
	}
	public static void openSignGui(Player p, MainMenu mainMenu, BetterSign sign) {
		if(WorldUtil.getSigns().contains(sign)) {
			SignMenu menu=signMenues.get(sign);
			if(menu == null) {
				menu = new SignMenu(sign);
				menu.create();
				signMenues.put(sign, menu);
			}
			
			menu.openFor(p, mainMenu);
			openedMenues.put(p, menu);
		}
	}
	public static void openMainGui(Player p, MainMenu menu) {
		menu.openForViewer(p);
		openedMenues.put(p, menu);
	}
	
	public static String getDefaultTitle() {
		return "GSigns v"+Variables.VERSION;
	}
	
	public static void onClick(InventoryClickEvent e) {
		if(e.getWhoClicked() instanceof Player) {
			Player p=(Player) e.getWhoClicked();
			Menu menu=openedMenues.get(p);
			if(menu!=null) menu.onClick(p, e);
		}
	}
	public static void onClose(InventoryCloseEvent e) {
		if(e.getPlayer() instanceof Player) {
			Player p=(Player) e.getPlayer();
			Menu menu=openedMenues.get(p);
			if(menu!=null) {
				menu.onClose(p);
				removePlayer(p);
			}
		}
	}
	public static void onOpen(InventoryOpenEvent e) {
		if(e.getPlayer() instanceof Player) {
			Player p=(Player) e.getPlayer();
			Menu menu=openedMenues.get(p);
			if(menu!=null) {
				menu.onClose(p);
				removePlayer(p);
			}
		}
	}
	
	public static void onPlayPause(BetterSign sign) {
		SignMenu menu=signMenues.get(sign);
		if(menu != null) {
			menu.updateDynamicContent();
		}
	}
	public static void onSignsListChange() {
		if(!inDynamicContentUpdate) {
			inDynamicContentUpdate=true;
			new BukkitRunnable() {
				@Override
				public void run() {
					int n=WorldUtil.getSigns().size();
					int lastPage=n/36;
					if(n%36==0 && lastPage>0) lastPage--;
					int maxSize=lastPage+1;
					
					while(mainMenuPages0.size()>maxSize) {
						MainMenu m=mainMenuPages0.remove(mainMenuPages0.size()-1);
						for(Player p:openedMenues.keySet()) {
							Menu value=openedMenues.get(p);
							if(value.equals(m)) {
								openMainGui(p, lastPage, m.getSortingType());
							}
						}
					}
					while(mainMenuPages1.size()>maxSize) {
						MainMenu m=mainMenuPages1.remove(mainMenuPages1.size()-1);
						for(Player p:openedMenues.keySet()) {
							Menu value=openedMenues.get(p);
							if(value.equals(m)) {
								openMainGui(p, lastPage, m.getSortingType());
							}
						}
					}
					while(mainMenuPages2.size()>maxSize) {
						MainMenu m=mainMenuPages2.remove(mainMenuPages2.size()-1);
						for(Player p:openedMenues.keySet()) {
							Menu value=openedMenues.get(p);
							if(value.equals(m)) {
								openMainGui(p, lastPage, m.getSortingType());
							}
						}
					}
					
					for(MainMenu menu:mainMenuPages0.values()) {
						menu.updateDynamicContent();
					}
					for(MainMenu menu:mainMenuPages1.values()) {
						menu.updateDynamicContent();
					}
					for(MainMenu menu:mainMenuPages2.values()) {
						menu.updateDynamicContent();
					}
					inDynamicContentUpdate=false;
				}
			}.runTask(Main.getPlugin());
		}
	}
	
	public static void onDisable() {
		for(Player p:openedMenues.keySet()) {
			p.closeInventory();
		}
	}
	
	public static void removeSignMenu(BetterSign sign) {
		SignMenu menu = signMenues.remove(sign);
		if(menu != null) {
			for(Player p:openedMenues.keySet()) {
				Menu value=openedMenues.get(p);
				if(value instanceof SignMenu) {
					SignMenu valueSignMenu=(SignMenu) value;
					if(valueSignMenu.getSign().equals(sign)) {
						valueSignMenu.kickPlayerToMainMenu(p);
					}
				}
			}
		}
	}
	public static void removePlayer(Player p) {
		openedMenues.remove(p);
	}
	
}
