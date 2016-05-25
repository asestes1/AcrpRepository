package gdp_planning;

import java.util.SortedMap;
import java.util.function.Function;

import org.joda.time.DateTime;
import org.joda.time.Interval;

import function_util.BiFunctionEx;
import function_util.FunctionEx;
import model.GdpAction;
import state_representation.DefaultState;

public class IndParamPlanner implements FunctionEx<DefaultState, GdpAction, Exception> {
	private final BiFunctionEx<DefaultState, Interval, SortedMap<DateTime, Integer>, Exception> myPaarChooser;
	private final Function<DefaultState, Interval> myIntervalChooser;
	private final Function<DefaultState, Double> myRadiusChooser;

	public IndParamPlanner(BiFunctionEx<DefaultState, Interval, SortedMap<DateTime, Integer>, Exception> myPaarChooser,
			Function<DefaultState, Interval> myIntervalChooser, Function<DefaultState, Double> myRadiusChooser) {
		super();
		this.myPaarChooser = myPaarChooser;
		this.myIntervalChooser = myIntervalChooser;
		this.myRadiusChooser = myRadiusChooser;
	}

	@Override
	public GdpAction apply(DefaultState state) throws Exception {
		Interval myInterval = myIntervalChooser.apply(state);
		SortedMap<DateTime, Integer> myPaars = myPaarChooser.apply(state, myInterval);
		Double radius = myRadiusChooser.apply(state);
		return new GdpAction(myPaars, myInterval, radius);
	}

}
