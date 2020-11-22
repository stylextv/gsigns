package de.stylextv.gs.config;

import java.io.BufferedWriter;
import java.io.IOException;

import org.bukkit.inventory.ItemStack;

public abstract class ConfigValue<T> {
	
	private String id;
	private String name;
	private String[] description;
	
	public ConfigValue(String id, String name, String[] description) {
		this.id = id;
		this.name = name;
		this.description = description;
		
		ConfigManager.registerValue(this);
	}
	
	public String getId() {
		return id;
	}
	public String getName() {
		return name;
	}
	public String[] getDescription() {
		return description;
	}
	
	public abstract String getTypeName();
	public abstract void writeAdditionalConfigInfo(BufferedWriter writer) throws IOException;
	public abstract ItemStack getItemStack();
	public abstract ItemStack getLeftButton();
	public abstract ItemStack getRightButton();
	
	public abstract T getValue();
	public abstract void setValue(T value);
	public abstract void interpretValue(String s);
	public abstract void restoreDefault();
	public abstract boolean handleButtonPress(int dir, boolean shift);
	
}
