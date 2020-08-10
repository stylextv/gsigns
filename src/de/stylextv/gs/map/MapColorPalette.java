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
            else if(WorldUtil.getMcVersion()>=WorldUtil.MCVERSION_1_16) path="/assets/color_tables/1_16.colors";
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
        
//        MCSDBubbleFormat data=generateBukkitTable();
//        try {
//			data.writeTo(new FileOutputStream("table_out.colors"));
//		} catch (IOException ex) {ex.printStackTrace();}
    }
//    @SuppressWarnings("deprecation")
//	private static MCSDBubbleFormat generateBukkitTable() {
//    	MCSDBubbleFormat data=new MCSDBubbleFormat();
//    	data.clear();
//        for (int i = 0; i < 256; i++) {
//            try {
//            	data.setColor((byte) i, MapPalette.getColor((byte) i));
//            } catch (Throwable t) {}
//        }
//        for (int r = 0; r < 256; r++) {
//            System.out.println("Generating Bukkit color map " + (r + 1) + "/256");
//            for (int g = 0; g < 256; g++) {
//                for (int b = 0; b < 256; b++) {
//                	data.set(r, g, b, MapPalette.matchColor(r, g, b));
//                }
//            }
//        }
//		return data;
//    }
	
	public static byte getColor(int r, int g, int b) {
        return COLOR_MAP_DATA.get(r, g, b);
    }
	public static Color getColor(byte b) {
    	return COLOR_MAP_DATA.getColor(b);
    }
	
}
