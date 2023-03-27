package kaba4cow.warfare.pathfinding;

import java.util.ArrayList;

import kaba4cow.ascii.toolbox.maths.vectors.Vector2i;

public class AttackPath {

	private ArrayList<Vector2i> nodes;

	private int collisionIndex;

	private int endX;
	private int endY;

	public AttackPath() {
		this.nodes = new ArrayList<>();
		this.collisionIndex = -1;
	}

	public Vector2i move() {
		if (nodes.isEmpty())
			return null;
		return nodes.remove(0);
	}

	public void add(int x, int y) {
		nodes.add(new Vector2i(x, y));
		endX = x;
		endY = y;
	}

	public boolean contains(int x, int y) {
		return getIndex(x, y) != -1;
	}

	private int getIndex(int x, int y) {
		int size = nodes.size();
		Vector2i node;
		for (int i = 0; i < size; i++) {
			node = nodes.get(i);
			if (node.x == x && node.y == y)
				return i;
		}
		return -1;
	}

	public Vector2i getNode(int index) {
		if (index < 0 || index >= nodes.size())
			return null;
		return nodes.get(index);
	}

	public boolean hasCollision() {
		return collisionIndex != -1;
	}

	public void setCollisionIndex(int index) {
		collisionIndex = index;
	}

	public int getCollisionIndex() {
		return collisionIndex;
	}

	public int getLength() {
		return nodes.size();
	}

	public int getEndX() {
		return endX;
	}

	public int getEndY() {
		return endY;
	}

}
