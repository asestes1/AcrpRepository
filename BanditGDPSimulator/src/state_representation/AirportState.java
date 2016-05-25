package state_representation;

import java.io.Serializable;

import org.joda.time.DateTime;

import bandit_objects.Immutable;

public class AirportState implements Immutable,Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = -8654901770667219328L;
	private final DateTime nextAvailable;
	private final Integer queueLength;
	
	public AirportState(DateTime nextAvailable){
		this.nextAvailable = nextAvailable;
		this.queueLength = 0;
	}
	
	public AirportState(DateTime nextAvailable,Integer queueLength){
		this.nextAvailable = nextAvailable;
		this.queueLength = queueLength;
	}
	
	public static long getSerialversionuid() {
		return serialVersionUID;
	}

	public Integer getQueueLength() {
		return queueLength;
	}

	@Override
	public String toString(){
		return "Next time: "+nextAvailable;
	}
	
	//---------- Getter ------------------
	public DateTime getNextAvailable(){
		return nextAvailable; 
	}
	
	//-----------Setter --------------
	public AirportState setNextAvailable(){
		return new AirportState(nextAvailable);
	}
}
