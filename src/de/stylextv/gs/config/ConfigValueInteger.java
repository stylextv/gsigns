package de.stylextv.gs.config;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.ArrayList;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import de.stylextv.gs.lang.LanguageManager;
import de.stylextv.gs.util.ItemUtil;

public class ConfigValueInteger extends ConfigValue<Integer> {
	
	private int defaultValue;
	private int min;
	private int max;
	private int value;
	
	public ConfigValueInteger(String id, String name, int defaultValue, int min, int max, String... description) {
		super(id, name, description);
		this.defaultValue = defaultValue;
		this.min = min;
		this.max = max;
		this.value = defaultValue;
	}
	
	public int getDefaultValue() {
		return defaultValue;
	}
	public int getMin() {
		return min;
	}
	public int getMax() {
		return max;
	}
	@Override
	public Integer getValue() {
		return value;
	}
	@Override
	public void setValue(Integer value) {
		if(value > max) this.value = max;
		else if(value < min) this.value = min;
		else this.value = value;
	}
	@Override
	public void interpretValue(String s) {
		setValue(Integer.valueOf(s));
	}
	
	@Override
	public String getTypeName() {
		return "Integer";
	}
	@Override
	public void writeAdditionalConfigInfo(BufferedWriter writer) throws IOException {
		writer.write("# "+LanguageManager.parseMsg("trans.config.range", min+"", max+"")+"\n");
	}
	@Override
	public ItemStack getItemStack() {
		ArrayList<String> list = new ArrayList<String>();
		for(String s : getDescription()) list.add("§7"+LanguageManager.parseMsg(s));
		list.add("");
		list.add("§7"+LanguageManager.parseMsg("trans.config.currentvalue", getValue()+""));
		return ItemUtil.createItemStack(Material.PAPER, "§e§l"+LanguageManager.parseMsg(getName()), list);
	}
	@Override
	public ItemStack getLeftButton() {
		return ItemUtil.createConfigMinusItemStack();
	}
	@Override
	public ItemStack getRightButton() {
		return ItemUtil.createConfigPlusItemStack();
	}
	
	@Override
	public void restoreDefault() {
		this.value = defaultValue;
	}
	@Override
	public boolean handleButtonPress(int dir, boolean shift) {
		int m = shift ? 5 : 1;
		int a = m * dir;
		boolean sound = true;
		
		int setTo = value+a;
		if(setTo > max) {
			setTo = max;
			sound = false;
		} else if(setTo < min) {
			setTo = min;
			sound = false;
		}
		if(value != setTo) {
			value = setTo;
			ConfigManager.updateChanges(true);
		}
		return sound;
	}
	
}
