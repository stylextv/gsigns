package de.stylextv.gs.player;

import java.awt.Color;
import java.awt.image.BufferedImage;

import de.stylextv.gs.decode.GifObject;
import de.stylextv.gs.math.MathUtil;

public class Order {
	
	private String text;
	private Color textColor=new Color(255,255,255);
	private BufferedImage background;
	private GifObject backgroundGif;
	private int backgroundBlur;
	private float backgroundBrightness=1;
	private boolean dither=true;
	private int fontSize=72,fontStyle=0;
	private String font="Raleway-Bold";
	private Color abstractColor;
	private double abstractSize=150;
	private int abstractSeed=MathUtil.getRandom().nextInt(1000)+1;
	
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
	public GifObject getBackgroundGif() {
		return backgroundGif;
	}
	public void setBackgroundGif(GifObject backgroundGif) {
		this.backgroundGif = backgroundGif;
	}
	public int getBackgroundBlur() {
		return backgroundBlur;
	}
	public void setBackgroundBlur(int backgroundBlur) {
		this.backgroundBlur = backgroundBlur;
	}
	public float getBackgroundBrightness() {
		return backgroundBrightness;
	}
	public void setBackgroundBrightness(float backgroundBrightness) {
		this.backgroundBrightness = backgroundBrightness;
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
	
	public int getAbstractSeed() {
		return abstractSeed;
	}
	public void setAbstractSeed(int abstractSeed) {
		this.abstractSeed = abstractSeed;
	}
	public double getAbstractSize() {
		return abstractSize;
	}
	public void setAbstractSize(double abstractSize) {
		this.abstractSize = abstractSize;
	}
	public Color getAbstractColor() {
		return abstractColor;
	}
	public void setAbstractColor(Color abstractColor) {
		this.abstractColor = abstractColor;
	}
	
}
