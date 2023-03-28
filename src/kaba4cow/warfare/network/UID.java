package kaba4cow.warfare.network;

import java.util.Collections;
import java.util.LinkedList;

public final class UID {

	public static final int MAX_IDS = 100;

	private static final LinkedList<Integer> ids = new LinkedList<>();

	private UID() {

	}

	static {
		for (int i = 0; i < MAX_IDS; i++)
			ids.add(i);
		Collections.shuffle(ids);
	}

	public static int getID() {
		return ids.removeFirst();
	}

}
