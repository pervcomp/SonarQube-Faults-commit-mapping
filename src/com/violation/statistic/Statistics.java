package com.violation.statistic;
import java.util.Arrays;
import java.util.List;

public class Statistics {
	 List<Double> data;
    int size;   

    public Statistics( List<Double> data) {
        this.data = data;
        size = data.size();
    }   

    double getMean() {
        double sum = 0.0;
        for(double a : data )
            sum += a;
        return sum/size;
    }

    double getVariance() {
        double mean = getMean();
        double temp = 0;
        for(double a : data)
            temp += (a-mean)*(a-mean);
        return temp/(size-1);
    }

    double getStdDev() {
        return Math.sqrt(getVariance());
    }
    

    /**
     * Calculate the minimum and maximum values out of a list of doubles.
     * 
     * @param values the input values
     * @return an array with the minimum and maximum values
     */
    public  double[] minmax() {
      if (data.size() == 0) {
        return new double[2];
      }
      double min = data.get(0);
      double max = min;
      int length = data.size();
      for (int i = 1; i < length; i++) {
        double value = data.get(i);
        min = Math.min(min, value);
        max = Math.max(max, value);
      }
      return new double[] { min, max };
    }

    
    
    public int getZero(){
    	int count = 0;
    	for(double d :data)
    		if (d == 0)
    			count++;
    	return  count;
    	
    }
    
    public int getBiggerThanZero(){
    	int count = 0;
    	for(double d :data)
    		if (d > 0)
    			count++;
    	return  count;
    	
    }
    
    public int getSmallerThanZero(){
    	int count = 0;
    	for(double d :data)
    		if (d > 0)
    			count++;
    	return  count;
    	
    }
}
       
       
       
       