package model;

import java.io.Serializable;

/**
 * An implementation of a pair of items, should only be used with immutable items.
 * @author Alex2
 *
 * @param <S> - the type of the first item
 * @param <T> - the type of the second item
 */
public class Pair<S,T> implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = -7892399107713297151L;
	private final S itemA;
	private final T itemB;
	
	/**
	 * Standard constructor.
	 * @param itemA - first item
	 * @param itemB - second item
	 */
	public Pair(S itemA, T itemB) {
		this.itemA = itemA;
		this.itemB = itemB;
	}

	/**
	 * Get first item.
	 * @return the first item.
	 */
	public S getItemA() {
		return itemA;
	}

	/**
	 * Get second item.
	 * @return the second item.
	 */
	public T getItemB() {
		return itemB;
	}
}
