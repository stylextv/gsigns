package de.stylextv.gs.gui;

import java.util.HashMap;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;

import de.stylextv.gs.lang.LanguageManager;
import de.stylextv.gs.main.Main;
import de.stylextv.gs.main.Variables;
import de.stylextv.gs.permission.PermissionUtil;
import de.stylextv.gs.util.ItemUtil;
import de.stylextv.gs.world.WorldUtil;

public class SettingsMenu extends Menu {
	
	private HashMap<Player, MainMenu> mainMenues = new HashMap<Player, MainMenu>();
	
	@Override
	public void createInventory() {
		inv = Bukkit.createInventory(null, 9*5, GuiManager.getDefaultTitle());
	}
	@Override
	public void fillConstants() {
		for(int x=0; x<9; x++) {
			setItem(x, 0, ItemUtil.BLANK);
			if(x!=4) setItem(x, getLastY(), ItemUtil.BLANK);
		}
		
		setItem(4, getLastY(), ItemUtil.HEAD_BACK);
		
		setItem(2, 2, ItemUtil.SETTINGS_NUKE);
		setItem(4, 2, ItemUtil.SETTINGS_CONFIG);
		setItem(6, 2, ItemUtil.SETTINGS_UPDATE);
	}
	
	@Override
	public void updateDynamicContent() {
	}
	
	public void openFor(Player p, MainMenu mainMenu) {
		mainMenues.put(p, mainMenu);
		openFor(p);
	}
	
	@Override
	public void onClick(Player p, InventoryClickEvent e) {
		if(e.getClickedInventory()!=null && e.getClickedInventory().equals(inv)) {
			e.setCancelled(true);
			
			int slot=e.getSlot();
			if(slot==20) {
				if(PermissionUtil.hasRemovePermission(p)) {
					playClickSound(p, true);
					p.closeInventory();
					int n=WorldUtil.removeAllSigns();
					if(n == 0) {
						p.sendMessage(Variables.PREFIX+LanguageManager.parseMsg("trans.menu.settings.removeall.error.nosigns"));
					} else p.sendMessage(Variables.PREFIX+LanguageManager.parseMsg(n==1?"trans.menu.settings.removeall.success.one":"trans.menu.settings.removeall.success.multiple", n+""));
				} else {
					kickPlayerForNoPerm(p);
				}
			} else if(slot==22) {
				if(PermissionUtil.hasGuiPermission(p) && PermissionUtil.hasConfigPermission(p)) {
					playClickSound(p, true);
					GuiManager.openConfigMenu(p, mainMenues.get(p));
				} else {
					kickPlayerForNoPerm(p);
				}
			} else if(slot==24) {
				if(PermissionUtil.hasUpdatePermission(p)) {
					playClickSound(p, true);
					p.closeInventory();
					Main.getPlugin().runAutoUpdater(p);
				} else {
					kickPlayerForNoPerm(p);
				}
			} else if(slot == getLastY()*9+4) {
				if(PermissionUtil.hasGuiPermission(p)) {
					playClickSound(p, true);
					kickPlayerToMainMenu(p);
				} else {
					kickPlayerForNoPerm(p);
				}
			}
			
		}
	}
	@Override
	public void onClose(Player p) {
		mainMenues.remove(p);
	}
	
	public void kickPlayerToMainMenu(Player p) {
		MainMenu menu = mainMenues.get(p);
		if(menu != null) GuiManager.openMainGui(p, menu.getPage(), menu.getSortingType());
	}
	
}
