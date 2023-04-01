package kaba4cow.warfare.game;

import java.util.ArrayList;
import java.util.HashMap;

import kaba4cow.ascii.core.Display;
import kaba4cow.ascii.core.Engine;
import kaba4cow.ascii.drawing.drawers.Drawer;
import kaba4cow.ascii.drawing.glyphs.Glyphs;
import kaba4cow.ascii.toolbox.files.DataFile;
import kaba4cow.ascii.toolbox.maths.Maths;
import kaba4cow.ascii.toolbox.maths.vectors.Vector2i;
import kaba4cow.ascii.toolbox.rng.RNG;
import kaba4cow.warfare.files.UnitFile;
import kaba4cow.warfare.game.controllers.Controller;

public class Player {

	private final World world;

	private Controller controller;

	private ArrayList<Unit> units;
	private int currentUnit;

	private boolean aiming = false;

	private int cash;
	private int income;

	private HashMap<Vector2i, Integer> visibility;
	private boolean[][] visibilityMap;

	private final int index;
	private final int village;

	private int color;

	private int unitsHired;
	private int unitsLost;
	private int unitsKilled;
	private int cashEarned;
	private int cashSpent;

	private boolean ignoreVisibility;

	public Player(World world, int village, int index) {
		this.world = world;
		this.index = index;
		this.village = village;
		this.units = new ArrayList<Unit>();
		this.visibility = new HashMap<>();
		this.visibilityMap = new boolean[world.getSize()][world.getSize()];
		this.currentUnit = 0;
		this.aiming = false;
		this.cash = 100;
		this.color = 0xAAA;
		this.ignoreVisibility = false;
	}

	public Player(World world, int index, DataFile data) {
		this(world, data.node("Village").getInt(), index);
		DataFile node;

		cash = data.node("Cash").getInt();

		unitsHired = data.node("Stats").getInt(0);
		unitsLost = data.node("Stats").getInt(1);
		unitsKilled = data.node("Stats").getInt(2);
		cashEarned = data.node("Stats").getInt(3);
		cashSpent = data.node("Stats").getInt(4);

		node = data.node("Units");
		for (int i = 0; i < node.objectSize(); i++) {
			DataFile unit = node.node(i);
			units.add(new Unit(world, this, unit));
		}

		node = data.node("Visibility");
		for (int i = 0; i < node.objectSize(); i++) {
			DataFile point = node.node(i);
			int x = point.getInt(0);
			int y = point.getInt(1);
			int radius = point.getInt(2);
			updateVisibility(x, y, radius);
		}
	}

	public void save(DataFile data) {
		DataFile node;
		int index;

		data.node("Village").clear().setInt(village);
		data.node("Cash").clear().setInt(cash);

		data.node("Stats").setInt(unitsHired).setInt(unitsLost).setInt(unitsKilled).setInt(cashEarned)
				.setInt(cashSpent);

		node = data.node("Units").clear();
		for (index = 0; index < units.size(); index++)
			units.get(index).save(node.node(Integer.toString(index)));

		node = data.node("Visibility").clear();
		index = 0;
		for (Vector2i pos : visibility.keySet())
			node.node(Integer.toString(index++)).setInt(pos.x).setInt(pos.y).setInt(visibility.get(pos));
	}

	public void setController(Controller controller) {
		if (controller == null) {
			this.controller = null;
			this.color = 0xAAA;
		} else {
			this.controller = controller.setPlayer(world, this);
			this.color = controller.getColor();
		}
	}

	public void createUnits() {
		HashMap<String, UnitFile> files = UnitFile.getFiles();
		for (String file : files.keySet())
			if (RNG.chance(0.3f))
				units.add(new Unit(world, world.getVillage(village), this, files.get(file)));
	}

	public void update(float dt) {
		for (int i = 0; i < units.size(); i++)
			units.get(i).update(dt);
	}

	public void render(int offX, int offY) {
		for (int i = 0; i < units.size(); i++) {
			Unit unit = units.get(i);
			unit.render(offX, offY, Drawer.IGNORE_BACKGROUND | color);

			if (i == currentUnit && this == world.getPlayer())
				unit.renderPaths(offX, offY);
		}
	}

	public void updateVisibility(int x, int y, int radius) {
		Vector2i pos = new Vector2i(x, y);
		int prevRadius = 0;
		if (visibility.containsKey(pos))
			prevRadius = visibility.get(pos);

		if (radius > prevRadius) {
			visibility.put(pos, radius);

			int oX, oY;
			for (oY = y - radius; oY <= y + radius; oY++)
				for (oX = x - radius; oX <= x + radius; oX++) {
					if (oX < 0 || oX >= visibilityMap.length || oY < 0 || oY >= visibilityMap.length
							|| visibilityMap[oX][oY])
						continue;
					float dist = Maths.dist(x, y, oX, oY);
					if (dist < radius)
						visibilityMap[oX][oY] = true;
				}
		}
	}

	public void updateController(float dt) {
		if (controller != null && hasUnits() && !getCurrentUnit().isShooting())
			controller.update(dt);
	}

	public void renderController(int offX, int offY) {
		if (controller != null && hasUnits() && !getCurrentUnit().isShooting())
			controller.render(offX, offY);
	}

	public void renderAiming(int offX, int offY) {
		if (world.getTurnPlayer() != getIndex())
			return;

		Display.setDrawCursor(!aiming);
		if (aiming) {
			if (Engine.getElapsedTime() % 0.75f < 0.5f) {
				int mX = world.getCamera().getMouseX() - (int) world.getCamera().getX();
				int mY = world.getCamera().getMouseY() - (int) world.getCamera().getY();
				int color = Drawer.IGNORE_BACKGROUND | 0x000FFF;
				Drawer.draw(mX - 1, mY, Glyphs.RIGHTWARDS_ARROW, color);
				Drawer.draw(mX + 1, mY, Glyphs.LEFTWARDS_ARROW, color);
				Drawer.draw(mX, mY - 1, Glyphs.DOWNWARDS_ARROW, color);
				Drawer.draw(mX, mY + 1, Glyphs.UPWARDS_ARROW, color);
			}

			getCurrentUnit().renderAttackRange(offX, offY);
		}
	}

	public void onNewTurn() {
		world.setStats(getIndex(), cashEarned, cashSpent, unitsHired, unitsLost, unitsKilled, true);
		for (Unit unit : units)
			unit.onNewTurn();
	}

	public void prevUnit() {
		if (!hasUnits())
			return;
		currentUnit--;
		if (currentUnit < 0)
			currentUnit = units.size() - 1;
	}

	public void nextUnit() {
		if (!hasUnits())
			return;
		currentUnit++;
		if (currentUnit >= units.size())
			currentUnit = 0;
	}

	public void removeDestroyedUnits() {
		Unit current = getCurrentUnit();
		Unit closest = null;
		float minDist = Float.POSITIVE_INFINITY;
		for (int i = units.size() - 1; i >= 0; i--) {
			Unit unit = units.get(i);
			if (unit.isDestroyed()) {
				unitsLost++;
				units.remove(i);
				if (i == currentUnit)
					nextUnit();
			} else {
				float dist = Maths.dist(current.getX(), current.getY(), unit.getX(), unit.getY());
				if (dist < minDist) {
					minDist = dist;
					closest = unit;
				}
			}
		}
		if ((currentUnit >= units.size() || current != getCurrentUnit()) && closest != null) {
			aiming = false;
			int closestIndex = 0;
			for (int i = 0; i < units.size(); i++)
				if (units.get(i) == closest) {
					closestIndex = i;
					break;
				}
			currentUnit = closestIndex;
		}
	}

	public void addUnit(String id, int x, int y) {
		Unit unit = new Unit(world, world.getVillage(village), this, UnitFile.get(id));
		units.add(unit);
		unitsHired++;
		if (x != -1 && y != -1)
			unit.setPos(x, y);
		else
			world.addUnit(getIndex(), id, unit.getX(), unit.getY(), true);
	}

	public void setStats(int cashEarned, int cashSpent, int unitsHired, int unitsLost, int unitsKilled) {
		this.cashEarned = cashEarned;
		this.cashSpent = cashSpent;
		this.unitsHired = unitsHired;
		this.unitsLost = unitsLost;
		this.unitsKilled = unitsKilled;
	}

	public Player resetIncome() {
		this.income = 0;
		return this;
	}

	public int getIncome() {
		return income;
	}

	public void addIncome(int amount) {
		this.income += amount;
	}

	public void addIncomeCash() {
		this.cash += income;
		cashEarned += income;
		world.setCash(getIndex(), cash, true);
	}

	public void removeCash(int amount) {
		this.cash -= amount;
		cashSpent += amount;
		world.setCash(getIndex(), cash, true);
	}

	public void setCash(int amount) {
		this.cash = amount;
	}

	public int getCash() {
		return cash;
	}

	public boolean hasUnits() {
		return !units.isEmpty();
	}

	public boolean canAccessShop() {
		return this == world.getVillage(village).getOccupier(world);
	}

	public boolean isVisible(int x, int y) {
		if (ignoreVisibility)
			return true;
//		if (controller instanceof PlayerController)
//			return true;
		return visibilityMap[x][y]; // TODO
	}

	public int getVillage() {
		return village;
	}

	public Unit getUnit(int x, int y) {
		Unit unit;
		for (int i = 0; i < units.size(); i++) {
			unit = units.get(i);
			if (x == unit.getX() && y == unit.getY())
				return unit;
		}
		return null;
	}

	public Unit getUnit(int index) {
		return units.get(index);
	}

	public int getUnitIndex(Unit unit) {
		for (int i = 0; i < units.size(); i++)
			if (unit == units.get(i))
				return i;
		return -1;
	}

	public ArrayList<Unit> getUnits() {
		return units;
	}

	public Unit getCurrentUnit() {
		if (!hasUnits())
			return null;
		return units.get(currentUnit);
	}

	public void setCurrentUnit(int currentUnit) {
		this.currentUnit = currentUnit;
	}

	public void onUnitKilled() {
		unitsKilled++;
	}

	public boolean isAiming() {
		return aiming;
	}

	public void setAiming(boolean aiming) {
		this.aiming = aiming;
	}

	public int getColor() {
		return color;
	}

	public int getIndex() {
		return index;
	}

	public void setIgnoreVisibility(boolean ignoreVisibility) {
		this.ignoreVisibility = ignoreVisibility;
	}

	public int getUnitsHired() {
		return unitsHired;
	}

	public int getUnitsLost() {
		return unitsLost;
	}

	public int getUnitsKilled() {
		return unitsKilled;
	}

	public int getCashEarned() {
		return cashEarned;
	}

	public int getCashSpent() {
		return cashSpent;
	}

	public float getMapUncovered() {
		int uncovered = 0;
		for (int y = 0; y < visibilityMap.length; y++)
			for (int x = 0; x < visibilityMap.length; x++)
				if (visibilityMap[x][y])
					uncovered++;
		int total = visibilityMap.length * visibilityMap.length;
		return (float) uncovered / (float) total;
	}

}
