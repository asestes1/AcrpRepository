package random_processes;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;

import bandit_objects.SimpleTmiAction;

/**
 * This uses Gaussian kernels to find the similarity between actions.
 * @author Alex
 *
 */
public class GaussianTmiComparer implements BiFunction<SimpleTmiAction,SimpleTmiAction,Double>{
	private final Map<Integer,GaussianKernel>  kernelMap;

	public GaussianTmiComparer(Map<Integer,GaussianKernel> kernel){
		this.kernelMap = new HashMap<Integer,GaussianKernel>(kernel);
	}
	
	@Override
	public Double apply(SimpleTmiAction action_1, SimpleTmiAction action_2) {
		if(action_1.getType() != action_2 .getType()){
			return 0.0;
		}else if(action_1.getType() == SimpleTmiAction.NONE_TYPE){
			return 1.0;
		}else{
			return kernelMap.get(action_1.getType()).apply(action_1.asVector(),action_2.asVector());
		}
	}
	
	
}
