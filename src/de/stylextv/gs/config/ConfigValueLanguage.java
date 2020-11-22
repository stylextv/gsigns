package de.stylextv.gs.config;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.ArrayList;

import org.bukkit.inventory.ItemStack;

import de.stylextv.gs.lang.LanguageManager;
import de.stylextv.gs.util.ItemUtil;

public class ConfigValueLanguage extends ConfigValue<String> {
	
	private String[] options;
	private String[] textures;
	private int currentIndex;
	private int defaultIndex;
	
	public ConfigValueLanguage(String id, String name, String[] options, String[] textures, int defaultValue, String... description) {
		super(id, name, description);
		this.options = options;
		this.textures = textures;
		this.defaultIndex = defaultValue;
		this.currentIndex = defaultValue;
	}
	
	public String getDefaultValue() {
		return options[defaultIndex];
	}
	@Override
	public String getValue() {
		return options[currentIndex];
	}
	@Override
	public void setValue(String value) {
		int i = defaultIndex;
		for(int j=0; j<options.length; j++) {
			if(options[j].equals(value)) {
				i = j;
				break;
			}
		}
		currentIndex = i;
	}
	@Override
	public void interpretValue(String s) {
		setValue(s);
	}
	
	@Override
	public String getTypeName() {
		return "String";
	}
	public String[] getOptions() {
		return options;
	}
	public int getCurrentIndex() {
		return currentIndex;
	}
	@Override
	public void writeAdditionalConfigInfo(BufferedWriter writer) throws IOException {
		String s = "";
		for(int i=0; i<options.length; i++) {
			s = s+options[i];
			if(i+1 < options.length) s = s+", ";
		}
		writer.write("# "+LanguageManager.parseMsg("trans.config.options", s)+"\n");
	}
	@Override
	public ItemStack getItemStack() {
		ArrayList<String> list = new ArrayList<String>();
		for(String s : getDescription()) list.add("§7"+LanguageManager.parseMsg(s));
		list.add("");
		list.add("§7"+LanguageManager.parseMsg("trans.config.currentvalue", getValue()));
		return ItemUtil.createItemStack(textures[currentIndex], "§e§l"+LanguageManager.parseMsg(getName()), list);
	}
	@Override
	public ItemStack getLeftButton() {
		return ItemUtil.createConfigOptionLeftItemStack();
	}
	@Override
	public ItemStack getRightButton() {
		return ItemUtil.createConfigOptionRightItemStack();
	}
	
	@Override
	public void restoreDefault() {
		this.currentIndex = defaultIndex;
		LanguageManager.refreshPlugin();
	}
	@Override
	public boolean handleButtonPress(int dir, boolean shift) {
		currentIndex = (currentIndex+dir)%options.length;
		if(currentIndex < 0) currentIndex+=options.length;
		
		ConfigManager.updateChanges(false);
		LanguageManager.refreshPlugin();
		return true;
	}
	
}
