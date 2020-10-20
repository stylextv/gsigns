package de.stylextv.gs.gui;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.concurrent.CopyOnWriteArrayList;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import de.stylextv.gs.permission.PermissionUtil;
import de.stylextv.gs.util.ItemUtil;
import de.stylextv.gs.world.BetterSign;
import de.stylextv.gs.world.WorldUtil;

public class MainMenu extends Menu {
	
	@SuppressWarnings("unchecked")
	private static final Comparator<BetterSign>[] SORTING_COMPARATORS = new Comparator[3];
	
	static {
		SORTING_COMPARATORS[0] = new Comparator<BetterSign>() {
			@Override
			public int compare(BetterSign s1, BetterSign s2) {
				return ((Boolean)s2.isGif()).compareTo(s1.isGif());
			}
		};
		SORTING_COMPARATORS[1] = new Comparator<BetterSign>() {
			@Override
			public int compare(BetterSign s1, BetterSign s2) {
				World w1=s1.getWorld();
				World w2=s2.getWorld();
				int i=w1.getEnvironment().compareTo(w2.getEnvironment());
				if(i == 0) i=w1.getName().compareTo(w2.getName());
				return i;
			}
		};
		SORTING_COMPARATORS[2] = new Comparator<BetterSign>() {
			@Override
			public int compare(BetterSign s1, BetterSign s2) {
				return ((Integer)s2.getTotalSize()).compareTo(s1.getTotalSize());
			}
		};
	}
	
	private int page;
	private int sortingType;
	
	private BetterSign[] displayedSigns = new BetterSign[36];
	private ArrayList<Player> viewers = new ArrayList<Player>();
	
	private boolean needsUpdate;
	
	public MainMenu(int page, int sortingType) {
		this.page = page;
		this.sortingType = sortingType;
	}
	
	@Override
	public void createInventory() {
		inv = Bukkit.createInventory(null, 9*6, GuiManager.getDefaultTitle());
	}
	@Override
	public void fillConstants() {
		for(int x=0; x<9; x++) {
			setItem(x, 0, ItemUtil.BLANK);
			if(x!=1 && x!=4 && x!=7) setItem(x, getLastY(), ItemUtil.BLANK);
		}
		if(page > 0) setItem(1, getLastY(), ItemUtil.HEAD_LEFT);
		else setItem(1, getLastY(), ItemUtil.BLANK);
		
		setItem(4, getLastY(), ItemUtil.SORTED_BY[sortingType]);
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public void updateDynamicContent() {
		if(viewers.size() != 0) {
			CopyOnWriteArrayList<BetterSign> signs = (CopyOnWriteArrayList<BetterSign>) WorldUtil.getSigns().clone();
			
			signs.sort(SORTING_COMPARATORS[sortingType]);
			
			int firstElement = page*36;
			int lastElement = firstElement+35;
			if(signs.size()-1 > lastElement) setItem(7, getLastY(), ItemUtil.HEAD_RIGHT);
			else setItem(7, getLastY(), ItemUtil.BLANK);
			
			for(int y=0; y<4; y++) {
				for(int x=0; x<9; x++) {
					int j=y*9+x;
					int i=firstElement+j;
					ItemStack item;
					if(i<signs.size()) {
						BetterSign sign=signs.get(i);
						displayedSigns[j]=sign;
						item=ItemUtil.createItemStack(sign, sortingType);
					} else {
						displayedSigns[j]=null;
						item=ItemUtil.EMPTY;
					}
					setItem(x, 1+y, item);
				}
			}
			
		} else needsUpdate=true;
	}
	
	public void openForViewer(Player p) {
		viewers.add(p);
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
			if(slot==getLastY()*9+4) {
				if(PermissionUtil.hasGuiPermission(p)) {
					playClickSound(p, true);
					GuiManager.openMainGui(p, page, (sortingType+1)%3);
				} else {
					kickPlayerForNoPerm(p);
				}
			} else if(slot==getLastY()*9+1) {
				if(page>0) {
					if(PermissionUtil.hasGuiPermission(p)) {
						playClickSound(p, true);
						GuiManager.openMainGui(p, page-1, sortingType);
					} else {
						kickPlayerForNoPerm(p);
					}
				}
			} else if(slot==getLastY()*9+7) {
				int firstElement = page*36;
				int lastElement = firstElement+35;
				if(WorldUtil.getSigns().size()-1 > lastElement) {
					if(PermissionUtil.hasGuiPermission(p)) {
						playClickSound(p, true);
						GuiManager.openMainGui(p, page+1, sortingType);
					} else {
						kickPlayerForNoPerm(p);
					}
				}
			} else {
				int i=slot-9;
				if(i>=0&&i<displayedSigns.length) {
					BetterSign sign=displayedSigns[i];
					if(sign != null) {
						if(PermissionUtil.hasGuiPermission(p) && (PermissionUtil.hasTeleportPermission(p)||PermissionUtil.hasPausePermission(p)||PermissionUtil.hasRemovePermission(p))) {
							playClickSound(p, true);
							GuiManager.openSignGui(p, this, sign);
						} else {
							kickPlayerForNoPerm(p);
						}
					}
				}
			}
			
		}
	}
	@Override
	public void onClose(Player p) {
		viewers.remove(p);
	}
	
	public int getPage() {
		return page;
	}
	public int getSortingType() {
		return sortingType;
	}
	
}
