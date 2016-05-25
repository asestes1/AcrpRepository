package model;

import java.util.SortedMap;
import java.util.TreeMap;

import org.joda.time.DateTime;
import org.joda.time.Interval;

import bandit_objects.SimpleTmiAction;
import gdp_planning.GDPPlanningHelper;

public class GdpAction {
	private final SortedMap<DateTime, Integer> paars;
	private final Interval gdpInterval;
	private final Double radius;

	public GdpAction(SortedMap<DateTime, Integer> paars, Interval gdpInterval, double radius) {
		this.paars = new TreeMap<DateTime, Integer>(paars);
		this.gdpInterval = gdpInterval;
		this.radius = radius;
	}

	public GdpAction(DateTime currentTime, SimpleTmiAction tmiAction) {
		this.paars = new TreeMap<DateTime, Integer>();
		double startMinutes = tmiAction.getStartTimeMin();
		DateTime tmiStartTime = new DateTime(currentTime.getYear(), currentTime.getMonthOfYear(),
				currentTime.getDayOfMonth(), GDPPlanningHelper.dayHourStart, 0, currentTime.getChronology().getZone())
						.plusMinutes((int) startMinutes);
		DateTime tmiEndTime = tmiStartTime.plusMinutes(tmiAction.getDurationMin().intValue());
		this.gdpInterval = new Interval(tmiStartTime, tmiEndTime);
		this.radius = tmiAction.getRadius();
		paars.put(tmiStartTime, tmiAction.getRate().intValue());
	}

	public SortedMap<DateTime, Integer> getPaars() {
		return paars;
	}

	public Interval getGdpInterval() {
		return gdpInterval;
	}

	public double getRadius() {
		return radius;
	}

}
