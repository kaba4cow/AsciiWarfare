package kaba4cow.warfare.pathfinding;

import java.util.ArrayList;
import java.util.Collections;

public class MovePath {

	private ArrayList<Node> nodes;

	private int endX;
	private int endY;

	private boolean finished;

	public MovePath() {
		this.nodes = new ArrayList<>();
		this.finished = false;
	}

	public Node move() {
		if (nodes.isEmpty())
			return null;
		return nodes.remove(0);
	}

	public void add(Node node) {
		nodes.add(node);
	}

	public boolean contains(int x, int y) {
		return getIndex(x, y) != -1;
	}

	private int getIndex(int x, int y) {
		int size = nodes.size();
		Node node;
		for (int i = 0; i < size; i++) {
			node = nodes.get(i);
			if (node.x == x && node.y == y)
				return i;
		}
		return -1;
	}

	public void shrink(int x, int y) {
		int index = getIndex(x, y);
		if (index == -1)
			return;
		for (int i = nodes.size() - 1; i > index; i--)
			nodes.remove(i);
		endX = x;
		endY = y;
	}

	public MovePath finish() {
		if (finished)
			return this;
		finished = true;
		Collections.reverse(nodes);
		nodes.remove(0);
		endX = nodes.get(nodes.size() - 1).x;
		endY = nodes.get(nodes.size() - 1).y;
		return this;
	}

	public Node getNode(int index) {
		if (index < 0 || index >= nodes.size())
			return null;
		return nodes.get(index);
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
