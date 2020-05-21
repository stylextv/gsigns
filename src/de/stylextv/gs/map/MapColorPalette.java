package de.stylextv.gs.map;

import java.awt.Color;
import java.io.InputStream;

import de.stylextv.gs.world.WorldUtil;

public class MapColorPalette {
	
    private static final MapColorSpaceData COLOR_MAP_DATA = new MapColorSpaceData();
    
    static {
        try {
            String path = "/assets/color_tables/1_12.colors";
            if(WorldUtil.getMcVersion()<WorldUtil.MCVERSION_1_12) path="/assets/color_tables/1_8_8.colors";
            MCSDBubbleFormat bubbleData = new MCSDBubbleFormat();
            
            InputStream input = MapColorPalette.class.getResourceAsStream(path);
            if (input == null) {
				System.err.println("Missing color table: " + path);
            } else {
                bubbleData.readFrom(input);
                COLOR_MAP_DATA.readFrom(bubbleData);
            }
        } catch(Exception ex) {
            ex.printStackTrace();
        }
    }
	
    public static byte getColor(int r, int g, int b) {
        return COLOR_MAP_DATA.get(r, g, b);
    }
    public static Color getColor(byte b) {
        return COLOR_MAP_DATA.getColor(b);
    }
	
}
