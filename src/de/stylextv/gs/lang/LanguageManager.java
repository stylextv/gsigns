package de.stylextv.gs.lang;

import de.stylextv.gs.command.MainTabCompleter;
import de.stylextv.gs.config.ConfigManager;
import de.stylextv.gs.gui.GuiManager;
import de.stylextv.gs.util.ItemUtil;

public class LanguageManager {
	
	private static final Language[] LANGUAGES = new Language[3];
	
	private static boolean inPluginRefresh = false;
	
	public static String getTranslation(String id) {
		int index = ConfigManager.VALUE_LANGUAGE.getCurrentIndex();
		Language l = LANGUAGES[index];
		if(l == null) {
			l = new Language(ConfigManager.VALUE_LANGUAGE.getOptions()[index]);
			LANGUAGES[index] = l;
		}
		
		return l.getTranslation(id);
	}
	
	public static void refreshPlugin() {
		if(!inPluginRefresh) {
			inPluginRefresh = true;
			
			ItemUtil.create();
			GuiManager.updateTranslations();
			MainTabCompleter.recreateFromLanguage();
			
			inPluginRefresh = false;
		}
	}
	
	public static String parseMsg(String s, String... arguments) {
		String trans = getTranslation(s);
		if(trans != null) {
			if(arguments.length != 0) for(int i=0; i<arguments.length; i++) {
				trans = trans.replace("%"+i+"%", arguments[i]);
			}
			return trans;
		}
		return s;
	}
	
}
