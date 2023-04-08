package kaba4cow.warfare.pathfinding;

public class Node {

	public final int x;
	public final int y;

	public final float penalty;
	public final int elevation;

	public float f;
	public float g;
	public float h;

	public Node previous;

	public Node(int x, int y, float penalty, int elevation) {
		this.x = x;
		this.y = y;
		this.penalty = penalty;
		this.elevation = elevation;
	}

	public void reset() {
		f = 0f;
		g = 0f;
		h = 0f;
		previous = null;
	}

}
