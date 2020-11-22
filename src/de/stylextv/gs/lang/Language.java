package de.stylextv.gs.lang;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;

import de.stylextv.gs.map.MapColorPalette;

public class Language {
	
	private HashMap<String, String> translations = new HashMap<String, String>();
	
	public Language(String id) {
		try {
			
			InputStream input = MapColorPalette.class.getResourceAsStream("/assets/languages/"+id+".json");
			BufferedReader reader = new BufferedReader(new InputStreamReader(input, StandardCharsets.UTF_8));
			
			String line;
			while((line=reader.readLine())!=null) {
				if(line.length() != 0) {
					String[] split = line.split(": ", 2);
					translations.put(split[0], split[1]);
				}
			}
			
			reader.close();
			
		} catch (Exception ex) {
			System.err.println("Failed to load the following language: " + id);
			ex.printStackTrace();
		}
	}
	
	public String getTranslation(String id) {
		return translations.get(id);
	}
	
}
