package de.stylextv.gs.render;

import java.awt.Color;
import java.awt.image.BufferedImage;

import org.bukkit.entity.Player;
import org.bukkit.map.MapCanvas;
import org.bukkit.map.MapPalette;
import org.bukkit.map.MapRenderer;
import org.bukkit.map.MapView;

public class GifMapRenderer extends MapRenderer {
	
	private byte[][] framesData;
	private int delay;
	
	private int currentFrame;
	private long startTime;
	
	@SuppressWarnings("deprecation")
	public GifMapRenderer(BufferedImage[] frames, int delay, long startTime) {
		framesData=new byte[frames.length][];
		int l=128*128;
		for(int i=0; i<frames.length; i++) {
			BufferedImage frame=frames[i];
			byte[] bytes=new byte[l];
			for(int j=0; j<l; j++) {
				bytes[j]=MapPalette.matchColor(new Color(frame.getRGB(j%128, j/128)));
			}
			framesData[i]=bytes;
		}
		this.delay=delay;
		this.startTime=startTime;
	}
	public GifMapRenderer(byte[][] framesData, int delay, long startTime) {
		this.framesData=framesData;
		this.delay=delay;
		this.startTime=startTime;
	}
	
	public boolean update(long currentTime) {
		if(framesData!=null) {
			int a=framesData.length;
			int totalTime=delay*a;
			long msIntoGif=currentTime-startTime;
			double d=(msIntoGif%totalTime)/(double)totalTime;
			int prevFrame=currentFrame;
			currentFrame=(int) (a*d);
			return prevFrame!=currentFrame;
		}
		return false;
	}
	
	@Override
	public void render(MapView view, MapCanvas canvas, Player p) {
		if(!Thread.currentThread().getName().equals("Server thread")) {
			if(framesData!=null&&framesData.length>0) {
				byte[] bytes=framesData[currentFrame];
				if(bytes!=null) {
					for(int i=0; i<bytes.length; i++) {
						canvas.setPixel(i%128, i/128, bytes[i]);
					}
				}
			}
		}
	}
	
	public int getDelay() {
		return delay;
	}
	public byte[][] getFramesData() {
		return framesData;
	}
	
}
