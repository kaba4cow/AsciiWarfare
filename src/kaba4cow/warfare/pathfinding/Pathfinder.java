package kaba4cow.warfare.pathfinding;

import java.util.ArrayList;

import kaba4cow.ascii.toolbox.maths.Maths;
import kaba4cow.warfare.files.WeaponFile;
import kaba4cow.warfare.game.Player;
import kaba4cow.warfare.game.World;

public final class Pathfinder {

	private static Node start, end, current, neighbor;
	private static Node[][] nodes;
	private static final ArrayList<Node> openSet = new ArrayList<>();
	private static final ArrayList<Node> closedSet = new ArrayList<>();

	private static int x0, y0, x1, y1, dx, dy, sx, sy, e1, e2, i, j, winner, size, range, startElevation,
			currentElevation;
	private static float tempG;
	private static boolean newPath, artillery;

	public static AttackPath getAttackPath(World world, Player player, WeaponFile weapon, int startX, int startY,
			int endX, int endY) {
		if (world == null || startX == endX && startY == endY || !world.isVisible(player, endX, endY))
			return null;
		nodes = world.getNodeMap();

		artillery = weapon.isArtillery();
		range = (int) weapon.getRange();

		startElevation = world.getElevation(startX, startY);
		currentElevation = startElevation;

		AttackPath path = new AttackPath();

		x0 = startX;
		y0 = startY;
		x1 = endX;
		y1 = endY;

		i = 0;
		dx = Math.abs(x1 - x0);
		dy = Math.abs(y1 - y0);
		sx = x0 < x1 ? 1 : -1;
		sy = y0 < y1 ? 1 : -1;
		e1 = dx - dy;
		e2 = 0;
		while (true) {
			if (i > 0 && !artillery && !path.hasCollision()
					&& (nodes[x0][y0] == null || !world.isVisible(player, x0, y0) || world.isObstacle(x0, y0))
					|| (int) Maths.dist(startX, startY, x0, y0) >= range
					|| !artillery && world.getElevation(x0, y0) > startElevation + 1) {
				path.setCollisionIndex(i - 1);
				path.add(x0, y0);
				break;
			}
			if (world.getElevation(x0, y0) < currentElevation) {
				currentElevation--;
				range += 2;
			}
			if (x0 == x1 && y0 == y1)
				break;
			e2 = 2 * e1;
			if (e2 > -1 * dy) {
				e1 -= dy;
				x0 += sx;
			}
			if (e2 < dx) {
				e1 += dx;
				y0 += sy;
			}
			path.add(x0, y0);
			i++;
		}
		if (!path.hasCollision())
			path.setCollisionIndex(i - 1);

		return path;
	}

	public static MovePath getMovePath(World world, Player player, int startX, int startY, int endX, int endY) {
		if (world == null || startX == endX && startY == endY || !world.isVisible(player, endX, endY)
				|| world.isObstacle(endX, endY))
			return null;
		nodes = world.getNodeMap();

		if (nodes[startX][startY] == null || nodes[endX][endY] == null)
			return null;

		size = nodes.length;
		for (j = 0; j < size; j++)
			for (i = 0; i < size; i++)
				if (nodes[i][j] != null)
					nodes[i][j].reset();

		start = nodes[startX][startY];
		end = nodes[endX][endY];

		openSet.clear();
		closedSet.clear();
		openSet.add(start);

		while (!openSet.isEmpty()) {
			winner = 0;
			size = openSet.size();
			for (i = 0; i < size; i++)
				if (openSet.get(i).f < openSet.get(winner).f)
					winner = i;
			current = openSet.get(winner);

			if (current == end)
				return createPath(current);

			openSet.remove(current);
			closedSet.add(current);

			size = nodes.length;
			for (j = current.y - 1; j <= current.y + 1; j++) {
				if (j < 0 || j >= size)
					continue;
				for (i = current.x - 1; i <= current.x + 1; i++) {
					if (i == current.x && j == current.y || i < 0 || i >= size || nodes[i][j] == null
							|| !world.isVisible(player, i, j) || world.isObstacle(i, j))
						continue;
					neighbor = nodes[i][j];
					if (Maths.dist(current.elevation, neighbor.elevation) > 1)
						continue;

					if (!closedSet.contains(neighbor)) {
						tempG = current.g + heuristic(neighbor, current);

						newPath = false;
						if (openSet.contains(neighbor)) {
							if (tempG < neighbor.g) {
								neighbor.g = tempG;
								newPath = true;
							}
						} else {
							neighbor.g = tempG;
							newPath = true;
							openSet.add(neighbor);
						}

						if (newPath) {
							neighbor.h = dist(neighbor, end);
							neighbor.f = neighbor.g + neighbor.h;
							neighbor.previous = current;
						}
					}
				}
			}
		}

		return null;
	}

	private static float dist(Node a, Node b) {
		return Maths.dist(a.x, a.y, b.x, b.y);
	}

	private static float heuristic(Node a, Node b) {
		int elevation = Maths.dist(a.elevation, b.elevation);
		if (elevation > 1)
			return Float.POSITIVE_INFINITY;
		float penalty = 1f + (1f + elevation) * b.penalty;
		return dist(a, b) + penalty;
	}

	private static MovePath createPath(Node node) {
		MovePath path = new MovePath();
		path.add(current);
		while (node.previous != null) {
			path.add(node.previous);
			node = node.previous;
		}
		return path.finish();
	}

}
