package kaba4cow.warfare.pathfinding;

public class Node {

	public final int x;
	public final int y;

	public final float penalty;

	public float f;
	public float g;
	public float h;

	public Node previous;

	public Node(int x, int y, float penalty) {
		this.x = x;
		this.y = y;
		this.penalty = penalty;
	}

	public void reset() {
		f = 0f;
		g = 0f;
		h = 0f;
		previous = null;
	}

}
