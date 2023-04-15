package kaba4cow.warfare.game;

import java.util.ArrayList;

import kaba4cow.ascii.core.Engine;
import kaba4cow.ascii.drawing.Drawer;
import kaba4cow.ascii.toolbox.maths.Maths;
import kaba4cow.ascii.toolbox.maths.vectors.Vector2i;

public class Village {

	public final int x;
	public final int y;
	public final int radius;
	private ArrayList<Vector2i> houses;

	public Village(int x, int y, int radius) {
		this.x = x;
		this.y = y;
		this.radius = radius;
		this.houses = null;
	}

	public void update(World world) {
		for (int i = houses.size() - 1; i >= 0; i--) {
			int x = houses.get(i).x;
			int y = houses.get(i).y;
			if (world.getVegetation(x, y) == null)
				houses.remove(i);
		}
	}

	public void render(World world, int offX, int offY) {
		if (Engine.getElapsedTime() % 1f < 0.5f)
			return;
		Player occupier = getOccupier(world);
		if (occupier == null)
			return;
		int color = occupier.getColor() | Drawer.IGNORE_BACKGROUND | Drawer.IGNORE_GLYPH;
		for (int i = 0; i < houses.size(); i++) {
			int x = houses.get(i).x;
			int y = houses.get(i).y;
			Drawer.draw(x - offX, y - offY, ' ', color);
		}
	}

	public Player getOccupier(World world) {
		Player player1 = world.getPlayer(0);
		Player player2 = world.getPlayer(1);
		boolean inVillage1 = inVillage(player1);
		boolean inVillage2 = inVillage(player2);
		if (inVillage1 && !inVillage2)
			return player1;
		else if (inVillage2 && !inVillage1)
			return player2;
		else
			return null;
	}

	public int getTotalUnits(World world, Player player) {
		if (!player.hasUnits())
			return 0;
		ArrayList<Unit> units = player.getUnits();
		float maxDistSq = radius * radius;
		float distSq;
		int total = 0;
		for (int i = 0; i < units.size(); i++) {
			Unit unit = units.get(i);
			distSq = Maths.distSq(x, y, unit.getX(), unit.getY());
			if (distSq < maxDistSq)
				total++;
		}
		return total;
	}

	private boolean inVillage(Player player) {
		if (!player.hasUnits())
			return false;
		ArrayList<Unit> units = player.getUnits();
		float maxDistSq = radius * radius;
		float distSq;
		for (int i = 0; i < units.size(); i++) {
			Unit unit = units.get(i);
			distSq = Maths.distSq(x, y, unit.getX(), unit.getY());
			if (distSq < maxDistSq)
				return true;
		}
		return false;
	}

	public void setHouses(ArrayList<Vector2i> houses) {
		this.houses = houses;
	}

	public int getHouses() {
		return houses.size();
	}

	public int getIncome() {
		if (houses.isEmpty())
			return 0;
		return 16 + 2 * getHouses() + (x ^ y) % 4;
	}

}
