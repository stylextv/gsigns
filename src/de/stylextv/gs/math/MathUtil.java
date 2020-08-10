package de.stylextv.gs.math;

import java.util.Random;

public class MathUtil {
	
	private static Random random=new Random();
	public static Random getRandom() {
		return random;
	}
	
	public static double lerpValue(double a, double b, double speed) {
		if(a>b) {
			return b;
		} else {
			a=a + (b-a)*speed;
			a+=speed/32.0;
			return a;
		}
	}
	public static double lerpValueBothDir(double a, double b, double speed, boolean b2) {
		a=a + (b-a)*speed;
		if(b2) {
			if(a<b) {
				a+=speed/32.0;
				if(a>b) return b;
			} else if(a>b) {
				a-=speed/32.0;
				if(a<b) return b;
			}
		}
		return a;
	}
	
	public static double sigmoid(double x) {
		return (1/( 1 + Math.pow(Math.E,(-1*(x*16-8)))));
	}
	
	public static int floor(double value) {
        int i = (int) value;
        return value < (double) i ? i - 1 : i;
    }
	/**
     * Rounds the specified value to the amount of decimals specified
    *
    * @param value to round
    * @param decimals count
    * @return value round to the decimal count specified
    */
    public static double round(double value, int decimals) {
    	double p = Math.pow(10, decimals);
    	return Math.round(value * p) / p;
    }
   
}
