package de.stylextv.gs.player;

import java.awt.Color;
import java.net.URL;
import java.util.ArrayList;

import javax.imageio.ImageIO;

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
				
				if(ch=='\\') {
					ignoreNextChar=true;
				} else ignoreNextChar=false;
			}
			if(!currentLine.isEmpty()) {
				lines.add(currentLine);
			}
			
			Order order=new Order();
			for(String s:lines) {
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
					case "dith":
						order.setDither(Boolean.valueOf(value));
						break;
					case "fnt-siz":
						order.setFontSize(Integer.valueOf(value));
						break;
					case "fnt-sty":
						order.setFontStyle(Integer.valueOf(value));
						break;
					default:
						break;
					}
				} catch(Exception ex) {
					order.setError(var);
					return order;
				}
			}
			return order;
		}
		return null;
	}
	
}
