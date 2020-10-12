package de.stylextv.gs.player;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;

import javax.imageio.ImageIO;

import de.stylextv.gs.decode.BetterGifDecoder;
import de.stylextv.gs.decode.BetterGifDecoder.GifImage;
import de.stylextv.gs.math.MathUtil;
import de.stylextv.gs.world.WorldUtil;

public class CodeParser {
	
	public static Order parseCode(String code) {
		if(code.startsWith("{")&&code.endsWith("}")) {
			String codeContent=code.substring(1, code.length()-1);
			ArrayList<String> lines=new ArrayList<String>();
			boolean inQuote=false;
			boolean ignoreNextChar=false;
			String currentLine="";
			for(char ch:codeContent.toCharArray()) {
				if(ch==','&&!inQuote) {
					lines.add(currentLine);
					currentLine="";
				} else {
					if(ch=='\"'&&!ignoreNextChar) {
						inQuote=!inQuote;
					} else currentLine=currentLine+ch;
				}
				
				if(ch=='\\'&&!ignoreNextChar) {
					ignoreNextChar=true;
					currentLine=currentLine.substring(0,currentLine.length()-1);
				} else ignoreNextChar=false;
			}
			if(!currentLine.isEmpty()) {
				lines.add(currentLine);
			}
			
			Order order=new Order();
			for(String s:lines) {
				if(s.contains(":")) {
					String[] split=s.split(":", 2);
					String var=split[0];
					String value=split[1];
					
					try {
						switch(var) {
						case "txt":
							order.setText(value);
							break;
						case "txt-col":
							order.setTextColor(Color.decode(value));
							break;
						case "bg-col":
							order.setBackgroundColor(Color.decode(value));
							break;
						case "bg-url":
							boolean isGif=false;
							for(String section:value.split("\\?")) {
								if(section.endsWith(".gif")) {
									isGif=true;
									break;
								}
							}
							if(isGif) {
								
								ByteArrayOutputStream baos = new ByteArrayOutputStream();
								InputStream is = null;
								is = new URL(value).openStream ();
								byte[] byteChunk = new byte[4096]; // Or whatever size you want to read in at a time.
								int n;
								while ( (n = is.read(byteChunk)) > 0 ) {
								    baos.write(byteChunk, 0, n);
								}
								
								GifImage gif=BetterGifDecoder.read(baos.toByteArray());
								order.setBackgroundGif(gif);
							} else order.setBackground(ImageIO.read(new URL(value)));
							break;
						case "bg-img":
							if(value.endsWith(".gif")) {
								GifImage gif=BetterGifDecoder.read(new FileInputStream(new File(WorldUtil.getCustomImagesFolder().getPath()+"/"+value)));
								order.setBackgroundGif(gif);
							} else order.setBackground(ImageIO.read(new File(WorldUtil.getCustomImagesFolder().getPath()+"/"+value)));
							break;
						case "bg-blur":
							order.setBackgroundBlur(Integer.valueOf(value));
							break;
						case "bg-bright":
							order.setBackgroundBrightness(Float.valueOf(value));
							break;
						case "dith":
							order.setDither(Boolean.valueOf(value));
							break;
						case "fnt-siz":
							order.setFontSize(Integer.valueOf(value));
							break;
						case "fnt-sty":
							order.setFontStyle(Integer.valueOf(value));
							break;
						case "fnt":
							order.setFont(value);
							break;
						case "sim-seed":
							order.setAbstractSeed(Integer.valueOf(value));
							break;
						case "sim-hue":
							float f;
							if("rdm".equals(value)) f=MathUtil.getRandom().nextFloat();
							else f=Float.valueOf(value);
							order.setAbstractColor(Color.getHSBColor(f, 0.98f, 0.80f));
							break;
						case "sim-siz":
							order.setAbstractSize(Double.valueOf(value));
							break;
						case "outl-col":
							order.setOutlineColor(Color.decode(value));
							break;
						case "outl-siz":
							order.setOutlineSize(Float.valueOf(value));
							break;
						case "outl-sty":
							order.setOutlineStyle(Integer.valueOf(value));
							break;
						default:
							break;
						}
					} catch(Exception ex) {
						ex.printStackTrace();
						order.setError(var+" | "+ex.getMessage());
						return order;
					}
				}
			}
			return order;
		}
		return null;
	}
	
}
