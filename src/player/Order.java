package de.stylextv.gs.player;

import java.awt.Color;
import java.awt.image.BufferedImage;

public class Order {
	
	private String text;
	private Color textColor=new Color(255,255,255);
	private BufferedImage background;
	private boolean dither=true;
	private int fontSize=72,fontStyle=0;
	private String font="Raleway-Bold";
	
	private String error;
	public String getError() {
		return error;
	}
	public void setError(String error) {
		this.error = error;
	}
	
	public String getText() {
		return text;
	}
	public void setText(String text) {
		this.text = text;
	}
	
	public Color getTextColor() {
		return textColor;
	}
	public void setTextColor(Color textColor) {
		this.textColor = textColor;
	}
	
	public BufferedImage getBackground() {
		return background;
	}
	public void setBackground(BufferedImage background) {
		this.background = background;
	}
	
	public boolean shouldDither() {
		return dither;
	}
	public void setDither(boolean dither) {
		this.dither = dither;
	}
	
	public int getFontSize() {
		return fontSize;
	}
	public void setFontSize(int fontSize) {
		this.fontSize = fontSize;
	}
	public int getFontStyle() {
		return fontStyle;
	}
	public void setFontStyle(int fontStyle) {
		this.fontStyle = fontStyle;
	}
	public String getFont() {
		return font;
	}
	public void setFont(String font) {
		this.font = font;
	}
	
}
