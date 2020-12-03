package de.stylextv.gs.config;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

import de.stylextv.gs.gui.GuiManager;
import de.stylextv.gs.lang.LanguageManager;
import de.stylextv.gs.main.Variables;

public class ConfigManager {
	
	//max line length = 27
	public static final ConfigValueInteger VALUE_VIEW_DISTANCE = new ConfigValueInteger("viewing_distance", "trans.config.name.distance", 30, 4, 128, "trans.config.desc.distance1","trans.config.desc.distance2","trans.config.desc.distance3");
	public static final ConfigValueInteger VALUE_MAP_SENDS_PER_3TICKS = new ConfigValueInteger("map_transmission_rate", "trans.config.name.transmissionrate", 40, 1, 100, "trans.config.desc.transmissionrate1","trans.config.desc.transmissionrate2","trans.config.desc.transmissionrate3");
	public static final ConfigValueInteger VALUE_RESERVED_VANILLA_MAPS = new ConfigValueInteger("reserved_vanilla_maps", "trans.config.name.reservedmaps", 12, 6, 20, "trans.config.desc.reservedmaps1","trans.config.desc.reservedmaps2","trans.config.desc.reservedmaps3","trans.config.desc.reservedmaps4","trans.config.desc.reservedmaps5");
	public static final ConfigValueLanguage VALUE_LANGUAGE = new ConfigValueLanguage("language", "trans.config.name.language", new String[]{"english_us","german_de","french_fr","chinese_cn"}, new String[]{"eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvODM3ZmM4NDFhZDIwNjA4ZTc2MWQ0MTMxOTEyM2NkYjU1ODU2YTBlYmMzODA0NzAzODU2ZTVkODZhMTM1MzQ1In19fQ==","eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNWU3ODk5YjQ4MDY4NTg2OTdlMjgzZjA4NGQ5MTczZmU0ODc4ODY0NTM3NzQ2MjZiMjRiZDhjZmVjYzc3YjNmIn19fQ==","eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYmEyNTI4N2QxMTQwZmIxNzQxZDRiNmY3ZTY1NjcyZjllNjRmZmZlODBlYTczNzFjN2YzZWM1YTZmMDQwMzkifX19","eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvN2Y5YmMwMzVjZGM4MGYxYWI1ZTExOThmMjlmM2FkM2ZkZDJiNDJkOWE2OWFlYjY0ZGU5OTA2ODE4MDBiOThkYyJ9fX0="}, 0, "trans.config.desc.language1","trans.config.desc.language2","trans.config.desc.language3");
	
	private static final File CONFIG_FILE = new File("plugins/GSigns/config.yml");
	
	private static ArrayList<ConfigValue<?>> values;
	
	public static void registerValue(ConfigValue<?> value) {
		if(values == null) values = new ArrayList<ConfigValue<?>>();
		values.add(value);
	}
	
	public static void onEnable() {
		if(CONFIG_FILE.exists()) {
			readConfig();
		} else {
			writeConfig();
		}
	}
	
	public static void restoreDefaultConfig() {
		for(ConfigValue<?> value:values) {
			value.restoreDefault();
		}
		updateChanges(true);
	}
	public static void updateChanges(boolean updateGui) {
		writeConfig();
		if(updateGui) GuiManager.getConfigMenu().updateDynamicContent();
	}
	
	private static void writeConfig() {
		try {
			
			if(!CONFIG_FILE.exists()) CONFIG_FILE.getParentFile().mkdirs();
		    BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(CONFIG_FILE), StandardCharsets.UTF_8));
		    
		    writeBanner(writer);
		    for(ConfigValue<?> value:values) {
		    	writeValue(writer, value);
		    }
		    
		    writer.close();
			
		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}
	private static void readConfig() {
		try {
			
		    BufferedReader reader = new BufferedReader(new FileReader(CONFIG_FILE));
		    
		    String s;
		    while((s=reader.readLine())!=null) {
		    	if(!s.startsWith("#") && s.contains(": ")) {
		    		String[] split = s.split(": ");
		    		for(ConfigValue<?> value:values) {
		    			if(value.getId().equals(split[0])) {
		    				value.interpretValue(split[1]);
		    				break;
		    			}
		    		}
		    	}
		    }
		    
		    reader.close();
			
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	
	private static void writeBanner(BufferedWriter writer) throws IOException {
		writer.write("\r\n   ____ ____  _                 \r\n" +
				"  / ___/ ___|(_) __ _ _ __  ___ \r\n" +
				" | |  _\\___ \\| |/ _` | '_ \\/ __|\r\n" +
				" | |_| |___) | | (_| | | | \\__ \\\r\n" +
				"  \\____|____/|_|\\__, |_| |_|___/\r\n" +
				"                |___/           \r\n" +
				"\r\nDeveloped by "+Variables.AUTHOR+"\r\n\r\n\r\n");
	}
	private static void writeValue(BufferedWriter writer, ConfigValue<?> value) throws IOException {
		for(String line:value.getDescription()) {
			writer.write("# "+LanguageManager.parseMsg(line)+"\n");
		}
		writer.write("# \n");
		writer.write("# "+LanguageManager.parseMsg("trans.config.type", value.getTypeName())+"\n");
		value.writeAdditionalConfigInfo(writer);
		writer.write(value.getId()+": "+value.getValue()+"\n\n");
	}
	
}
