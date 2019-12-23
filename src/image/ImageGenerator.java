package de.stylextv.gs.image;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

import de.stylextv.gs.player.Order;
import de.stylextv.gs.player.PlayerManager;

public class ImageGenerator {
	
	public static BufferedImage generate(Order order) {
		BufferedImage image=new BufferedImage(256, 128, BufferedImage.TYPE_INT_RGB);
		Graphics2D imageGraphics=(Graphics2D) image.getGraphics();
		RenderUtil.setRenderingHints(imageGraphics);
		
		if(order.getBackground()!=null) imageGraphics.drawImage(order.getBackground(), 0,0,256,128, null);
		if(order.getText()!=null&&order.getTextColor()!=null) {
			BufferedImage textImage=new BufferedImage(512, 256, BufferedImage.TYPE_INT_ARGB);
			Graphics2D graphics=(Graphics2D) textImage.getGraphics();
			RenderUtil.setRenderingHints(graphics);
			
			graphics.setFont(new Font("Raleway Bold", order.getFontStyle(), order.getFontSize()));
			int fontHeight=graphics.getFontMetrics().getAscent()-graphics.getFontMetrics().getDescent();
			graphics.setColor(new Color(0,0,0,128+16));
			graphics.drawString(order.getText(), 512/2-graphics.getFontMetrics().stringWidth(order.getText())/2 -1, 256/2+fontHeight/2 -1+9);
			graphics.setColor(order.getTextColor());
			graphics.drawString(order.getText(), 512/2-graphics.getFontMetrics().stringWidth(order.getText())/2 -1, 256/2+fontHeight/2 -1);
			
			imageGraphics.drawImage(textImage, 0,0,256,128, null);
		}
		
		if(order.shouldDither()) ditherImage(image, PlayerManager.matrix, PlayerManager.n);
		
		return image;
	}
	
	public static void ditherImage(BufferedImage image, double[] matrix, int n) {
		for(int y=0; y<128; y++) {
			for(int x=0; x<256; x++) {
				Color c=new Color(image.getRGB(x, y));
				double mValue=matrix[(y%n)+(x%n)*n];
				double d=mValue*(255.0/n);
				int r=(int)Math.round(c.getRed()+d);
				int g=(int)Math.round(c.getGreen()+d);
				int b=(int)Math.round(c.getBlue()+d);
				if(r>255)r=255;
				else if(r<0)r=0;
				if(g>255)g=255;
				else if(g<0)g=0;
				if(b>255)b=255;
				else if(b<0)b=0;
				image.setRGB(x, y, new Color(r,g,b).getRGB());
			}
		}
	}
	
}
