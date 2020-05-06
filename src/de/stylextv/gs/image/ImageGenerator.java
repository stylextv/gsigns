package de.stylextv.gs.image;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.GraphicsEnvironment;
import java.awt.image.BufferedImage;
import java.awt.image.ConvolveOp;
import java.awt.image.Kernel;
import java.net.URL;
import java.util.HashMap;

import de.stylextv.gs.math.SimplexNoise;
import de.stylextv.gs.player.Order;
import de.stylextv.gs.player.PlayerManager;

public class ImageGenerator {
	
	private static HashMap<String, Font> cachedFonts=new HashMap<String, Font>();
	
	public static BufferedImage generate(Order order, int imgWidth, int imgHeight) {
		BufferedImage image=new BufferedImage(128*imgWidth, 128*imgHeight, BufferedImage.TYPE_INT_RGB);
		Graphics2D imageGraphics=(Graphics2D) image.getGraphics();
		RenderUtil.setRenderingHints(imageGraphics);
		
		if(order.getAbstractColor()!=null) {
			double size=order.getAbstractSize();
			int seed=order.getAbstractSeed();
			Color c=order.getAbstractColor();
			for(int x=0; x<image.getWidth(); x++) {
				for(int y=0; y<image.getHeight(); y++) {
					double d=(SimplexNoise.noise(x/size+seed, y/size+seed)+1)/2;
					
					d=d*0.7+0.3;
					
					int r=(int)Math.round(c.getRed()*d);
					int g=(int)Math.round(c.getGreen()*d);
					int b=(int)Math.round(c.getBlue()*d);
					image.setRGB(x, y, new Color(r,g,b).getRGB());
				}
			}
		}
		if(order.getBackground()!=null&&order.getBackgroundBrightness()>0) {
			BufferedImage backgroundImage;
			if(order.getBackgroundBlur()!=0) {
				BufferedImage blurred=getGaussianBlurFilter(order.getBackgroundBlur(), true).filter(order.getBackground(), null);
				blurred=getGaussianBlurFilter(order.getBackgroundBlur(), false).filter(blurred, null);
				backgroundImage=blurred;
			} else backgroundImage=order.getBackground();
			if(order.getBackgroundBrightness()!=1) {
				float f=order.getBackgroundBrightness();
				for(int x=0; x<backgroundImage.getWidth(); x++) {
					for(int y=0; y<backgroundImage.getHeight(); y++) {
						Color c=new Color(backgroundImage.getRGB(x, y));
						int r=(int) (c.getRed()*f);
						int g=(int) (c.getGreen()*f);
						int b=(int) (c.getBlue()*f);
						if(r>255)r=255;
						if(g>255)g=255;
						if(b>255)b=255;
						backgroundImage.setRGB(x, y, new Color(r,g,b).getRGB());
					}
				}
			}
			double bgRatio=(double)backgroundImage.getWidth()/backgroundImage.getHeight();
			double imgRatio=(double)image.getWidth()/image.getHeight();
			if(bgRatio>imgRatio) {
				int width=(int) (((double)backgroundImage.getWidth()/backgroundImage.getHeight())*image.getHeight());
				imageGraphics.drawImage(backgroundImage, image.getWidth()/2-width/2,0,width,image.getHeight(), null);
			} else if(bgRatio<imgRatio) {
				int height=(int) (((double)backgroundImage.getHeight()/backgroundImage.getWidth())*image.getWidth());
				imageGraphics.drawImage(backgroundImage, 0,image.getHeight()/2-height/2,image.getWidth(),height, null);
			} else {
				imageGraphics.drawImage(backgroundImage, 0,0,image.getWidth(),image.getHeight(), null);
			}
		}
		if(order.getText()!=null&&order.getTextColor()!=null) {
			BufferedImage textImage=new BufferedImage(image.getWidth()*2, image.getHeight()*2, BufferedImage.TYPE_INT_ARGB);
			Graphics2D graphics=(Graphics2D) textImage.getGraphics();
			RenderUtil.setRenderingHints(graphics);
			
			Font font=getFont(order.getFont());
			String name="";
			if(font!=null) name=font.getName();
			graphics.setFont(new Font(name, order.getFontStyle(), order.getFontSize()));
			int fontHeight=graphics.getFontMetrics().getAscent()-graphics.getFontMetrics().getDescent();
			graphics.setColor(new Color(0,0,0,128+16));
			graphics.drawString(order.getText(), textImage.getWidth()/2-graphics.getFontMetrics().stringWidth(order.getText())/2 -1, textImage.getHeight()/2+fontHeight/2 -1+9);
			graphics.setColor(order.getTextColor());
			graphics.drawString(order.getText(), textImage.getWidth()/2-graphics.getFontMetrics().stringWidth(order.getText())/2 -1, textImage.getHeight()/2+fontHeight/2 -1);
			
			imageGraphics.drawImage(textImage, 0,0,image.getWidth(),image.getHeight(), null);
		}
		
		if(order.shouldDither()) ditherImage(image, PlayerManager.matrix, PlayerManager.n);
		
		return image;
	}
	public static BufferedImage generate(Order order, int imgWidth, int imgHeight, int gifFrame) {
		BufferedImage image=new BufferedImage(128*imgWidth, 128*imgHeight, BufferedImage.TYPE_INT_RGB);
		Graphics2D imageGraphics=(Graphics2D) image.getGraphics();
		RenderUtil.setRenderingHints(imageGraphics);
		
		if(order.getAbstractColor()!=null) {
			double size=order.getAbstractSize();
			int seed=order.getAbstractSeed();
			Color c=order.getAbstractColor();
			for(int x=0; x<image.getWidth(); x++) {
				for(int y=0; y<image.getHeight(); y++) {
					double d=(SimplexNoise.noise(x/size+seed, y/size+seed)+1)/2;
					
					d=d*0.7+0.3;
					
					int r=(int)Math.round(c.getRed()*d);
					int g=(int)Math.round(c.getGreen()*d);
					int b=(int)Math.round(c.getBlue()*d);
					image.setRGB(x, y, new Color(r,g,b).getRGB());
				}
			}
		}
		if(order.getBackgroundGif()!=null&&order.getBackgroundBrightness()>0) {
			BufferedImage frame=order.getBackgroundGif().getFrame(gifFrame);
			if(order.getBackgroundBlur()!=0) {
				BufferedImage blurred=getGaussianBlurFilter(order.getBackgroundBlur(), true).filter(frame, null);
				blurred=getGaussianBlurFilter(order.getBackgroundBlur(), false).filter(blurred, null);
				frame=blurred;
			}
			if(order.getBackgroundBrightness()!=1) {
				float f=order.getBackgroundBrightness();
				for(int x=0; x<frame.getWidth(); x++) {
					for(int y=0; y<frame.getHeight(); y++) {
						Color c=new Color(frame.getRGB(x, y));
						int r=(int) (c.getRed()*f);
						int g=(int) (c.getGreen()*f);
						int b=(int) (c.getBlue()*f);
						if(r>255)r=255;
						if(g>255)g=255;
						if(b>255)b=255;
						frame.setRGB(x, y, new Color(r,g,b).getRGB());
					}
				}
			}
			double bgRatio=(double)frame.getWidth()/frame.getHeight();
			double imgRatio=(double)image.getWidth()/image.getHeight();
			if(bgRatio>imgRatio) {
				int width=(int) (((double)frame.getWidth()/frame.getHeight())*image.getHeight());
				imageGraphics.drawImage(frame, image.getWidth()/2-width/2,0,width,image.getHeight(), null);
			} else if(bgRatio<imgRatio) {
				int height=(int) (((double)frame.getHeight()/frame.getWidth())*image.getWidth());
				imageGraphics.drawImage(frame, 0,image.getHeight()/2-height/2,image.getWidth(),height, null);
			} else {
				imageGraphics.drawImage(frame, 0,0,image.getWidth(),image.getHeight(), null);
			}
		}
		if(order.getText()!=null&&order.getTextColor()!=null) {
			BufferedImage textImage=new BufferedImage(image.getWidth()*2, image.getHeight()*2, BufferedImage.TYPE_INT_ARGB);
			Graphics2D graphics=(Graphics2D) textImage.getGraphics();
			RenderUtil.setRenderingHints(graphics);
			
			Font font=getFont(order.getFont());
			String name="";
			if(font!=null) name=font.getName();
			graphics.setFont(new Font(name, order.getFontStyle(), order.getFontSize()));
			int fontHeight=graphics.getFontMetrics().getAscent()-graphics.getFontMetrics().getDescent();
			graphics.setColor(new Color(0,0,0,128+16));
			graphics.drawString(order.getText(), textImage.getWidth()/2-graphics.getFontMetrics().stringWidth(order.getText())/2 -1, textImage.getHeight()/2+fontHeight/2 -1+9);
			graphics.setColor(order.getTextColor());
			graphics.drawString(order.getText(), textImage.getWidth()/2-graphics.getFontMetrics().stringWidth(order.getText())/2 -1, textImage.getHeight()/2+fontHeight/2 -1);
			
			imageGraphics.drawImage(textImage, 0,0,image.getWidth(),image.getHeight(), null);
		}
		
		if(order.shouldDither()) ditherImage(image, PlayerManager.matrix, PlayerManager.n);
		
		return image;
	}
	private static Font getFont(String name) {
		if(name==null) return null;
		Font got=cachedFonts.get(name);
		if(got!=null) return got;
		else {
			String family;
			try {
				family=name.split("-")[0].toLowerCase();
			} catch (Exception ex) {return null;}
			try {
			    GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
			    String url="https://github.com/google/fonts/raw/master/ofl/"+family+"/"+name+".ttf";
			    URL u = new URL(url);
			    Font font=Font.createFont(Font.TRUETYPE_FONT, u.openStream());
			    ge.registerFont(font);
			    cachedFonts.put(name, font);
			    return font;
			} catch (Exception ex) {}
			try {
			    GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
			    String url="https://github.com/google/fonts/raw/master/apache/"+family+"/"+name+".ttf";
			    URL u = new URL(url);
			    Font font=Font.createFont(Font.TRUETYPE_FONT, u.openStream());
			    ge.registerFont(font);
			    cachedFonts.put(name, font);
			    return font;
			} catch (Exception ex) {}
			try {
			    GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
			    String url="https://github.com/google/fonts/raw/master/ufl/"+family+"/"+name+".ttf";
			    URL u = new URL(url);
			    Font font=Font.createFont(Font.TRUETYPE_FONT, u.openStream());
			    ge.registerFont(font);
			    cachedFonts.put(name, font);
			    return font;
			} catch (Exception ex) {}
			return null;
		}
	}
	
	public static void ditherImage(BufferedImage image, double[] matrix, int n) {
		for(int y=0; y<image.getHeight(); y++) {
			for(int x=0; x<image.getWidth(); x++) {
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
	public static ConvolveOp getGaussianBlurFilter(int radius, boolean horizontal) {
        if (radius < 1) {
            throw new IllegalArgumentException("Radius must be >= 1");
        }
        
        int size = radius * 2 + 1;
        float[] data = new float[size];
        
        float sigma = radius / 3.0f;
        float twoSigmaSquare = 2.0f * sigma * sigma;
        float sigmaRoot = (float) Math.sqrt(twoSigmaSquare * Math.PI);
        float total = 0.0f;
        
        for (int i = -radius; i <= radius; i++) {
            float distance = i * i;
            int index = i + radius;
            data[index] = (float) Math.exp(-distance / twoSigmaSquare) / sigmaRoot;
            total += data[index];
        }
        
        for (int i = 0; i < data.length; i++) {
            data[i] /= total;
        }        
        
        Kernel kernel = null;
        if (horizontal) {
            kernel = new Kernel(size, 1, data);
        } else {
            kernel = new Kernel(1, size, data);
        }
        return new ConvolveOp(kernel, ConvolveOp.EDGE_NO_OP, null);
    }
	public static BufferedImage rotateImage(BufferedImage image, int angle) {
		if(angle==0) return image;
		BufferedImage rotated=new BufferedImage(image.getWidth(), image.getHeight(), image.getType());
		Graphics2D graphics=(Graphics2D) rotated.getGraphics();
		
		graphics.rotate(Math.toRadians(angle), image.getWidth()/2,image.getHeight()/2);
		graphics.drawImage(image, 0,0, null);
		return rotated;
	}
	
}
