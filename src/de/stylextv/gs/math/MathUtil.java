package de.stylextv.gs.math;

import java.util.Random;

public class MathUtil {
	
	private static Random random=new Random();
	public static Random getRandom() {
		return random;
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
