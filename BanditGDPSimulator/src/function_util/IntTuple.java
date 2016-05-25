package function_util;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


public class IntTuple{
	private final int[] tuple_value;
	private final int length;
	
	public IntTuple(IntTuple other){
		length = other.getLength();
		tuple_value = new int[length];
		for(int i =0; i < length;i++){
			tuple_value[i] = other.getEntry(i);
		}
	}
	
	public IntTuple(int length, int[] int_array) {
		this.tuple_value = new int[length];
		for(int i =0;i < length;i++){
			this.tuple_value[i] = int_array[i];
		}
		this.length = length;
	}
	
	public IntTuple(List<Integer> int_list){
		length = int_list.size();
		tuple_value = new int[length];
		Iterator<Integer> my_iter = int_list.iterator();
		for(int i =0; i< length;i++){
			tuple_value[i] = my_iter.next();
		}
	}
	
	public int getEntry(int i) {
		return tuple_value[i];
	}
	
	public IntTuple setEntry(int index, int value) {
		List<Integer> my_list = new ArrayList<Integer>(length);
		for(int i =0 ; i < length;i++){
			if(i == index){
				my_list.add(value);
			}else{
				my_list.add(tuple_value[i]);
			}
		}
		return new IntTuple(my_list);
	}
	public int getLength() {
		return length;
	}
	
	@Override
	public boolean equals(Object other){
		if(other == this){
			return true;
		}
		if(!(other instanceof IntTuple )){
			return false;
		}
		if(((IntTuple) other).getLength() != this.length){
			return false;
		}
		for(int i =0; i < length; i++){
			if(((IntTuple) other).getEntry(i) != this.getEntry(i)){
				return false;
			}
		}
		return true;
	}
	
	@Override
	public int hashCode(){
		int result = length;
		for(int i =0; i < length;i++){
			result = 37*result + tuple_value[i];
		}
		return result;
	}
	
	
}
