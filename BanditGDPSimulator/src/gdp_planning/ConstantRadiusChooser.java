package gdp_planning;

import java.util.function.Function;

import state_representation.DefaultState;

public class ConstantRadiusChooser implements Function<DefaultState,Double> {
	private final Double radius;
	
	public ConstantRadiusChooser(Double radius) {
		super();
		this.radius = radius;
	}

	@Override
	public Double apply(DefaultState state) {
		return radius;
	}

}
