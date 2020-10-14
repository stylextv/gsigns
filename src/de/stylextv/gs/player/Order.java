package de.stylextv.gs.player;

import java.awt.Color;
import java.awt.image.BufferedImage;

import org.bukkit.Location;

import de.stylextv.gs.decode.BetterGifDecoder.GifImage;
import de.stylextv.gs.math.MathUtil;

public class Order {
	
	private String text;
	private Color textColor = new Color(255,255,255);
	
	private Color backgroundColor;
	private BufferedImage background;
	private GifImage backgroundGif;
	private int backgroundBlur;
	private float backgroundBrightness = 1;
	
	private boolean dither = true;
	
	private String font = "Raleway-Bold";
	private int fontSize = 72;
	private int fontStyle = 0;
	
	private Color abstractColor;
	private double abstractSize = 150;
	private int abstractSeed = MathUtil.getRandom().nextInt(1000)+1;
	
	private Color outlineColor;
	private float outlineSize = 12f;
	private int outlineStyle;
	
	private String error;
	private Location firstCorner;
	private long lastSelect;
	
	public String getError() {
		return error;
	}
	public void setError(String error) {
		this.error = error;
	}
	public Location getFirstCorner() {
		return firstCorner;
	}
	public void setFirstCorner(Location firstCorner) {
		this.firstCorner = firstCorner;
	}
	public long getLastSelect() {
		return lastSelect;
	}
	public void setLastSelect(long lastSelect) {
		this.lastSelect = lastSelect;
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
	public Color getBackgroundColor() {
		return backgroundColor;
	}
	public void setBackgroundColor(Color backgroundColor) {
		this.backgroundColor = backgroundColor;
	}
	
	public BufferedImage getBackground() {
		return background;
	}
	public void setBackground(BufferedImage background) {
		this.background = background;
	}
	public GifImage getBackgroundGif() {
		return backgroundGif;
	}
	public void setBackgroundGif(GifImage backgroundGif) {
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
	
	public String getFont() {
		return font;
	}
	public void setFont(String font) {
		this.font = font;
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
	
	public Color getOutlineColor() {
		return outlineColor;
	}
	public void setOutlineColor(Color outlineColor) {
		this.outlineColor = outlineColor;
	}
	public float getOutlineSize() {
		return outlineSize;
	}
	public void setOutlineSize(float outlineSize) {
		this.outlineSize = outlineSize;
	}
	public int getOutlineStyle() {
		return outlineStyle;
	}
	public void setOutlineStyle(int outlineStyle) {
		this.outlineStyle = outlineStyle;
	}
	
}
