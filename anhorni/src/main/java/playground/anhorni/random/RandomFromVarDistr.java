package playground.anhorni.random;

import java.util.Random;


public class RandomFromVarDistr {
	
	private long seed = 109877L;
	private Random rnd;
	
	public RandomFromVarDistr() {
		this.rnd = new Random(this.seed);
	}
	
	public void setSeed(long seed) {
		this.seed = seed;
		this.rnd = new Random(this.seed);
	}
	
	public double getUniform(double h) {
		return this.rnd.nextDouble() * h;	
	}
	
	public double getNegLinear(double h) {
		double u = this.rnd.nextDouble();
		return h * (1 - Math.sqrt(1 - u));
	}
	
	public double getGaussian(double mean, double sigma) {
		return mean + sigma  * rnd.nextGaussian();
	}
	
	public double getGumbel(double mu, double beta) {
		double r = mu - beta * Math.log((Math.log(-1.0 * this.getUniform(1.0))));
		return r;
	}
}
