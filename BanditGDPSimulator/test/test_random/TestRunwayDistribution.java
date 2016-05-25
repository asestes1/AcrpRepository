package test_random;
import org.junit.Test;

import util_random.ConstantRunwayDistribution;


public class TestRunwayDistribution {
	
	@Test
	public void testConstantRunwayDistribution(){
		ConstantRunwayDistribution myConstantRunwayDistribution = new ConstantRunwayDistribution();
		for(int i =1; i < 100;i++){
			System.out.println(i+": "+myConstantRunwayDistribution.sample(i).toString());
		}
	}
}
