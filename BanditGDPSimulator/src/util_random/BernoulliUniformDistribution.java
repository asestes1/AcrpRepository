package util_random;

import org.joda.time.Duration;


public class BernoulliUniformDistribution implements Distribution<Duration> {
	private final double p;
	private final UniformDurationDistribution uniform;
	private final Duration unit;
	
	public BernoulliUniformDistribution(double p, int low, int high, Duration unit) {
		this.p = p;
		this.unit = unit;
		this.uniform = new UniformDurationDistribution(low, high,unit);
	}

	@Override
	public Duration sample() {
		double coin = Math.random();
		if(coin < p){
			return uniform.sample();
		}
		return unit.multipliedBy(0);
	}

	@Override
	public String toString(){
		return "Distribution with "+(1-p)+" chance of 0 and "+p
				+" chance of value with following distribution: \n"+uniform.toString();
	}

}
