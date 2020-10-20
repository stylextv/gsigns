package de.stylextv.gs.gui;

import java.util.HashMap;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;

import de.stylextv.gs.main.Variables;
import de.stylextv.gs.permission.PermissionUtil;
import de.stylextv.gs.util.ItemUtil;
import de.stylextv.gs.world.BetterSign;
import de.stylextv.gs.world.WorldUtil;

public class SignMenu extends Menu {
	
	private BetterSign sign;
	private HashMap<Player, MainMenu> mainMenues = new HashMap<Player, MainMenu>();
	
	private boolean needsUpdate;
	
	public SignMenu(BetterSign sign) {
		this.sign = sign;
	}
	
	@Override
	public void createInventory() {
		inv = Bukkit.createInventory(null, 9*5, GuiManager.getDefaultTitle());
	}
	@Override
	public void fillConstants() {
		for(int x=0; x<9; x++) {
			if(x!=4) setItem(x, 0, ItemUtil.BLANK);
			setItem(x, getLastY(), ItemUtil.BLANK);
		}
		
		Material m;
		if(WorldUtil.getMcVersion() < WorldUtil.MCVERSION_1_14) m=Material.valueOf("SIGN");
		else m=Material.OAK_SIGN;
		setItem(4, 0, ItemUtil.createItemStack(m, "§7UUID: §b"+sign.getUid().toString().substring(0,6)+"...", "§7World: §e"+sign.getWorld().getName(),"§7Size: §e"+sign.getSize()));
		setItem(4, getLastY(), ItemUtil.HEAD_BACK);
		if(sign.isGif()) {
			setItem(2, 2, ItemUtil.SIGN_TP);
			setItem(6, 2, ItemUtil.SIGN_REMOVE);
		} else {
			setItem(3, 2, ItemUtil.SIGN_TP);
			setItem(5, 2, ItemUtil.SIGN_REMOVE);
		}
	}
	
	@Override
	public void updateDynamicContent() {
		if(sign.isGif()) {
			if(mainMenues.size() != 0) {
				if(sign.isPaused()) setItem(4, 2, ItemUtil.SIGN_PLAY);
				else setItem(4, 2, ItemUtil.SIGN_PAUSE);
			} else needsUpdate=true;
		}
	}
	
	public void openFor(Player p, MainMenu mainMenu) {
		mainMenues.put(p, mainMenu);
		if(needsUpdate) {
			needsUpdate=false;
			updateDynamicContent();
		}
		openFor(p);
	}
	
	@Override
	public void onClick(Player p, InventoryClickEvent e) {
		if(e.getClickedInventory()!=null && e.getClickedInventory().equals(inv)) {
			e.setCancelled(true);
			
			int slot=e.getSlot();
			if(sign.isGif()) {
				if(slot==22) {
					if(PermissionUtil.hasPausePermission(p)) {
						playClickSound(p, true);
						if(sign.isPaused()) sign.play();
						else sign.pause();
					} else {
						kickPlayerForNoPerm(p);
					}
				}
			}
			
			int tpSlot=sign.isGif() ? 20:21;
			int removeSlot=sign.isGif() ? 24:23;
			if(slot == tpSlot) {
				if(PermissionUtil.hasTeleportPermission(p)) {
					p.closeInventory();
					sign.teleport(p);
					playClickSound(p, true);
					p.sendMessage(Variables.PREFIX+"§7You have been §ateleported §7successfully.");
				} else {
					kickPlayerForNoPerm(p);
				}
			} else if(slot == removeSlot) {
				if(PermissionUtil.hasRemovePermission(p)) {
					playClickSound(p, true);
					if(PermissionUtil.hasGuiPermission(p)) {
						kickPlayerToMainMenu(p);
					} else {
						p.sendMessage(Variables.PREFIX+"§7The sign was §aremoved §7successfully.");
						p.closeInventory();
					}
					WorldUtil.removeSign(sign);
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
	
	public BetterSign getSign() {
		return sign;
	}
	
}
