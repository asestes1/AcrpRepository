package model;

import org.joda.time.DateTime;

import bandit_objects.Immutable;

public interface TimeState extends Immutable {
	public DateTime getCurrentTime();
}
