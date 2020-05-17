package de.stylextv.gs.decode;

import java.awt.image.BufferedImage;

public class GifObject {
	
	private ImageFrame[] frames;
	
	public GifObject(ImageFrame[] frames) {
		this.frames=frames;
	}
	
	public BufferedImage getFrame(int index) {
		return frames[index].getImage();
	}
	public int getDelay(int index) {
		return frames[index].getDelay();
	}
	
	public int getFrameCount() {
		return frames.length;
	}
	
}
