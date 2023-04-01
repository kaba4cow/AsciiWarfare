package kaba4cow.warfare.game;

import java.util.ArrayList;

import kaba4cow.ascii.toolbox.files.DataFile;
import kaba4cow.ascii.toolbox.maths.Maths;

public class Village {

	public final int x;
	public final int y;
	public final int radius;
	private int income;

	public Village(int x, int y, int radius) {
		this.x = x;
		this.y = y;
		this.radius = radius;
		this.income = 0;
	}

	public Village(DataFile data) {
		this.x = data.getInt(0);
		this.y = data.getInt(1);
		this.radius = data.getInt(2);
		this.income = data.getInt(3);
	}

	public void save(DataFile data) {
		data.clear().setInt(x).setInt(y).setInt(radius).setInt(income);
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

	public void calculateIncome(int totalHouses) {
		this.income = totalHouses / 2;
	}

	public int getIncome() {
		return income;
	}

}
