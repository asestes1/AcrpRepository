package function_util;

public class HashDoubleUtil {
	private HashDoubleUtil(){
		
	}
	
	public static final int HashDouble(Double myDouble){
		if(myDouble == null){
			return 0;
		}
		long firstStep = Double.doubleToLongBits(myDouble);
		return (int) (firstStep ^ (firstStep>>>32));
	}
}
