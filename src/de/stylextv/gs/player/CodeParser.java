package de.stylextv.gs.player;

import java.awt.Color;
import java.io.File;
import java.net.URL;
import java.util.ArrayList;

import javax.imageio.ImageIO;

import de.stylextv.gs.decode.GifDecoder;
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
						case "bg-url":
							order.setBackground(ImageIO.read(new URL(value)));
							break;
						case "bg-img":
							if(value.endsWith(".gif")) {
								GifDecoder decoder=new GifDecoder();
								int status=decoder.read(WorldUtil.getCustomImagesFolder().getPath()+"/"+value);
								if(status!=GifDecoder.STATUS_OK) {
									String msg="STATUS_OPEN_ERROR";
									if(status==GifDecoder.STATUS_FORMAT_ERROR) msg="STATUS_FORMAT_ERROR";
									throw new IllegalArgumentException(msg);
								}
								order.setBackgroundGif(decoder);
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
							if(value.equals("rdm")) f=MathUtil.getRandom().nextFloat();
							else f=Float.valueOf(value);
							order.setAbstractColor(Color.getHSBColor(f, 0.98f, 0.80f));
							break;
						case "sim-siz":
							order.setAbstractSize(Double.valueOf(value));
							break;
						default:
							break;
						}
					} catch(Exception ex) {
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
