package de.stylextv.gs.image;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.GraphicsEnvironment;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.awt.image.ConvolveOp;
import java.awt.image.Kernel;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;

import de.stylextv.gs.map.MapColorPalette;
import de.stylextv.gs.math.SimplexNoise;
import de.stylextv.gs.player.Order;

public class ImageGenerator {
	
	private static HashMap<String, Font> cachedFonts=new HashMap<String, Font>();
	
	private static Color TEXT_SHADOWCOLOR=new Color(0,0,0,128+16);
	
	public static byte[] generate(Order order, int imgWidth, int imgHeight) {
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
		} else if(order.getBackgroundColor()!=null) {
			imageGraphics.setColor(order.getBackgroundColor());
			imageGraphics.fillRect(0, 0, image.getWidth(), image.getHeight());
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
			drawText(image, imageGraphics, order);
		}
		
		if(order.shouldDither()) return ditherImage(image);
		
		byte[] data=new byte[image.getWidth()*image.getHeight()];
		for(int y=0; y<image.getHeight(); y++) {
			for(int x=0; x<image.getWidth(); x++) {
				Color c=new Color(image.getRGB(x, y));
				byte b=MapColorPalette.getColor(c.getRed(),c.getGreen(),c.getBlue());
				data[y*image.getWidth()+x]=b;
			}
		}
		return data;
	}
	public static byte[] generate(Order order, int imgWidth, int imgHeight, int gifFrame) {
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
		} else if(order.getBackgroundColor()!=null) {
			imageGraphics.setColor(order.getBackgroundColor());
			imageGraphics.fillRect(0, 0, image.getWidth(), image.getHeight());
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
			drawText(image, imageGraphics, order);
		}
		
		if(order.shouldDither()) return ditherImage(image);
		
		byte[] data=new byte[image.getWidth()*image.getHeight()];
		for(int y=0; y<image.getHeight(); y++) {
			for(int x=0; x<image.getWidth(); x++) {
				Color c=new Color(image.getRGB(x, y));
				byte b=MapColorPalette.getColor(c.getRed(),c.getGreen(),c.getBlue());
				data[y*image.getWidth()+x]=b;
			}
		}
		return data;
	}
	private static void drawText(BufferedImage image, Graphics2D imageGraphics, Order order) {
		BufferedImage textImage=new BufferedImage(image.getWidth()*2, image.getHeight()*2, BufferedImage.TYPE_INT_ARGB);
		Graphics2D graphics=(Graphics2D) textImage.getGraphics();
		RenderUtil.setRenderingHints(graphics);
		
		Font font=getFont(order.getFont());
		String name="";
		if(font!=null) name=font.getName();
		graphics.setFont(new Font(name, order.getFontStyle(), order.getFontSize()));
		
		ArrayList<String> lines=new ArrayList<String>();
		String currentLine=null;
		for(String s:order.getText().split(" ")) {
			if(s.contains("\\n")) {
				String currentElement="";
				char[] chars=s.toCharArray();
				for(int i=0; i<chars.length; i++) {
					char ch=chars[i];
					if(ch=='\\'&&i+1<chars.length&&chars[i+1]=='n') {
						if(!currentElement.isEmpty()) {
							if(currentLine==null) {
								currentLine=currentElement;
							} else if(graphics.getFontMetrics().stringWidth(currentLine+" "+currentElement)<textImage.getWidth()) {
								currentLine=currentLine+" "+currentElement;
							} else {
								lines.add(currentLine);
								currentLine=currentElement;
							}
						}
						currentElement="";
						if(currentLine==null) {
							lines.add("");
						} else {
							lines.add(currentLine);
							currentLine=null;
						}
						i++;
					} else {
						currentElement=currentElement+ch;
					}
				}
				if(!currentElement.isEmpty()) {
					if(currentLine==null) {
						currentLine=currentElement;
					} else if(graphics.getFontMetrics().stringWidth(currentLine+" "+currentElement)<textImage.getWidth()) {
						currentLine=currentLine+" "+currentElement;
					} else {
						lines.add(currentLine);
						currentLine=currentElement;
					}
				}
			} else {
				if(currentLine==null) {
					currentLine=s;
				} else if(graphics.getFontMetrics().stringWidth(currentLine+" "+s)<textImage.getWidth()) {
					currentLine=currentLine+" "+s;
				} else {
					lines.add(currentLine);
					currentLine=s;
				}
			}
		}
		if(currentLine!=null) lines.add(currentLine);
		
		int fontHeight=graphics.getFontMetrics().getAscent()-graphics.getFontMetrics().getDescent();
		int spacing=fontHeight/2;
		int yOffset=-((fontHeight+spacing)*(lines.size()-1))/2;
		int i=0;
		for(String line:lines) {
			int stringWidth=graphics.getFontMetrics().stringWidth(line);
			int x=textImage.getWidth()/2-stringWidth/2 -1;
			int y=textImage.getHeight()/2+fontHeight/2+i*(fontHeight+spacing)+yOffset -1;
			graphics.setColor(TEXT_SHADOWCOLOR);
			graphics.drawString(line, x, y+9);
			graphics.setColor(order.getTextColor());
			graphics.drawString(line, x, y);
			i++;
		}
		
		imageGraphics.drawImage(textImage, 0,0,image.getWidth(),image.getHeight(), null);
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
	
	public static byte[] ditherImage(BufferedImage image) {
		float[] pixels=new float[image.getWidth()*image.getHeight()*3];
		for(int y=0; y<image.getHeight(); y++) {
			for(int x=0; x<image.getWidth(); x++) {
				Color c=new Color(image.getRGB(x, y));
				int i=(y*image.getWidth()+x)*3;
				pixels[i]=c.getRed()/255f;
				pixels[i+1]=c.getGreen()/255f;
				pixels[i+2]=c.getBlue()/255f;
			}
		}
		byte[] data=new byte[image.getWidth()*image.getHeight()];
		for(int y=0; y<image.getHeight(); y++) {
			if(y%2!=0) {
				for(int x=image.getWidth()-1; x>=0; x--) {
					int i=(y*image.getWidth()+x)*3;
					float oldpixelR=pixels[i];
					float oldpixelG=pixels[i+1];
					float oldpixelB=pixels[i+2];
					int searchR=Math.round(oldpixelR*255);
					int searchG=Math.round(oldpixelG*255);
					int searchB=Math.round(oldpixelB*255);
					if(searchR<0) searchR=0;
					else if(searchR>255) searchR=255;
					if(searchG<0) searchG=0;
					else if(searchG>255) searchG=255;
					if(searchB<0) searchB=0;
					else if(searchB>255) searchB=255;
					byte newColorByte=MapColorPalette.getColor(searchR,searchG,searchB);
					Color newColor=MapColorPalette.getColor(newColorByte);
					data[i/3]=newColorByte;
					
					float quant_errorR=oldpixelR - newColor.getRed()/255f;
					float quant_errorG=oldpixelG - newColor.getGreen()/255f;
					float quant_errorB=oldpixelB - newColor.getBlue()/255f;
					spreadError(x+1, y, pixels, image.getWidth(),image.getHeight(), quant_errorR, quant_errorG, quant_errorB, 7f / 16);
					spreadError(x-1, y+1, pixels, image.getWidth(),image.getHeight(), quant_errorR, quant_errorG, quant_errorB, 3f / 16);
					spreadError(x, y+1, pixels, image.getWidth(),image.getHeight(), quant_errorR, quant_errorG, quant_errorB, 5f / 16);
					spreadError(x+1, y+1, pixels, image.getWidth(),image.getHeight(), quant_errorR, quant_errorG, quant_errorB, 1f / 16);
				}
			} else {
				for(int x=0; x<image.getWidth(); x++) {
					int i=(y*image.getWidth()+x)*3;
					float oldpixelR=pixels[i];
					float oldpixelG=pixels[i+1];
					float oldpixelB=pixels[i+2];
					int searchR=Math.round(oldpixelR*255);
					int searchG=Math.round(oldpixelG*255);
					int searchB=Math.round(oldpixelB*255);
					if(searchR<0) searchR=0;
					else if(searchR>255) searchR=255;
					if(searchG<0) searchG=0;
					else if(searchG>255) searchG=255;
					if(searchB<0) searchB=0;
					else if(searchB>255) searchB=255;
					byte newColorByte=MapColorPalette.getColor(searchR,searchG,searchB);
					Color newColor=MapColorPalette.getColor(newColorByte);
					data[i/3]=newColorByte;
					
					float quant_errorR=oldpixelR - newColor.getRed()/255f;
					float quant_errorG=oldpixelG - newColor.getGreen()/255f;
					float quant_errorB=oldpixelB - newColor.getBlue()/255f;
					spreadError(x+1, y, pixels, image.getWidth(),image.getHeight(), quant_errorR, quant_errorG, quant_errorB, 7f / 16);
					spreadError(x-1, y+1, pixels, image.getWidth(),image.getHeight(), quant_errorR, quant_errorG, quant_errorB, 3f / 16);
					spreadError(x, y+1, pixels, image.getWidth(),image.getHeight(), quant_errorR, quant_errorG, quant_errorB, 5f / 16);
					spreadError(x+1, y+1, pixels, image.getWidth(),image.getHeight(), quant_errorR, quant_errorG, quant_errorB, 1f / 16);
				}
			}
		}
		return data;
	}
	private static void spreadError(int x, int y, float[] pixels, int w, int h, float errorR, float errorG, float errorB, float m) {
		if(x<0||y<0 || x>=w||y>=h) return;
		
		int i=(y*w+x)*3;
		pixels[i  ]=pixels[i  ] +errorR*m;
		pixels[i+1]=pixels[i+1] +errorG*m;
		pixels[i+2]=pixels[i+2] +errorB*m;
		
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
	
	public static byte[] rotateImage(byte[] image, int imgWidth,int imgHeight, int angle) {
		byte[] rotated=new byte[image.length];
		
		double rad=Math.toRadians(-angle);
		
		double centerX=(imgWidth-1)/2.0;
		double centerY=(imgHeight-1)/2.0;
		for(int y=0; y<imgHeight; y++) {
			for(int x=0; x<imgWidth; x++) {
				double[] pt = {x, y};
				AffineTransform.getRotateInstance(rad, centerX, centerY)
				  .transform(pt, 0, pt, 0, 1); // specifying to use this double[] to hold coords
				
				int newX=(int)pt[0];
				int newY=(int)pt[1];
				rotated[y*imgWidth+x]=image[newY*imgWidth+newX];
			}
		}
		return rotated;
	}
	public static byte[] getSubimage(byte[] data, int imgWidth, int x, int y, int width, int height) {
		int realImgWidth=imgWidth*128;
		
		byte[] subData=new byte[width*height];
		for(int cy=0; cy<height; cy++) {
			for(int cx=0; cx<width; cx++) {
				int dataX=x+cx;
				int dataY=y+cy;
				subData[cy*width+cx]=data[dataY*realImgWidth+dataX];
			}
		}
		return subData;
	}
	
}
