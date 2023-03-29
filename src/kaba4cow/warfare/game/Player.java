package kaba4cow.warfare.game;

import java.util.ArrayList;
import java.util.HashMap;

import kaba4cow.ascii.core.Display;
import kaba4cow.ascii.core.Engine;
import kaba4cow.ascii.drawing.drawers.Drawer;
import kaba4cow.ascii.drawing.glyphs.Glyphs;
import kaba4cow.ascii.toolbox.files.DataFile;
import kaba4cow.ascii.toolbox.maths.Maths;
import kaba4cow.ascii.toolbox.rng.RNG;
import kaba4cow.warfare.files.UnitFile;
import kaba4cow.warfare.game.controllers.Controller;

public class Player {

	protected final World world;

	protected Controller controller;

	protected ArrayList<Unit> units;
	protected int currentUnit;

	protected boolean aiming = false;

	private boolean[][] visibility;

	private int color;

	public Player(World world) {
		this.world = world;
		this.units = new ArrayList<Unit>();
		this.visibility = new boolean[world.getSize()][world.getSize()];
		this.currentUnit = 0;
		this.aiming = false;
		this.color = 0xAAA;
	}

	public Player(World world, DataFile data) {
		this(world);
		DataFile node;

		node = data.node("Visibility");
		for (int i = 0; i < node.objectSize(); i++) {
			DataFile tile = node.node(i);
			int x = tile.getInt(0);
			int y = tile.getInt(1);
			visibility[x][y] = true;
		}

		node = data.node("Units");
		for (int i = 0; i < node.objectSize(); i++) {
			DataFile unit = node.node(i);
			units.add(new Unit(world, this, unit));
		}
	}

	public void save(DataFile data) {
		DataFile node;
		int x, y, index;

		node = data.node("Units").clear();
		for (index = 0; index < units.size(); index++)
			units.get(index).save(node.node(Integer.toString(index)));

		node = data.node("Visibility").clear();
		index = 0;
		int size = world.getSize();
		for (y = 0; y < size; y++)
			for (x = 0; x < size; x++)
				if (visibility[x][y])
					node.node(Integer.toString(index++)).setInt(x).setInt(y);
	}

	public void setController(Controller controller) {
		this.controller = controller.setPlayer(world, this);
		this.color = controller.getColor();
	}

	public void createUnits(Village village, RNG rng) {
		HashMap<String, UnitFile> files = UnitFile.getFiles();
		for (String file : files.keySet())
			units.add(new Unit(world, village, this, files.get(file), rng));
	}

	public void update(float dt) {
		for (int i = 0; i < units.size(); i++) {
			Unit unit = units.get(i);
			unit.update(dt);
		}
	}

	public void render(int offX, int offY) {
		int i, x, y;

		for (i = 0; i < units.size(); i++) {
			Unit unit = units.get(i);
			unit.render(offX, offY, Drawer.IGNORE_BACKGROUND | color);

			int uX = unit.getX();
			int uY = unit.getY();
			int unitVisibility = (int) unit.getVisibilityRadius();
			for (y = uY - unitVisibility; y <= uY + unitVisibility; y++)
				for (x = uX - unitVisibility; x <= uX + unitVisibility; x++) {
					if (x < 0 || x >= visibility.length || y < 0 || y >= visibility.length || visibility[x][y])
						continue;
					float dist = Maths.dist(uX, uY, x, y);
					if (dist < unitVisibility)
						visibility[x][y] = true;
				}

			if (i == currentUnit)
				unit.renderPaths(offX, offY);
		}
	}

	public void updateController(float dt) {
		if (controller != null)
			controller.update(dt);
	}

	public void renderController(int offX, int offY) {
		if (controller != null)
			controller.render(offX, offY);
	}

	public void renderAiming(int offX, int offY) {
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
		for (Unit unit : units)
			unit.onNewTurn();
	}

	public void prevUnit() {
		int killed = 0;
		do {
			currentUnit--;
			if (currentUnit < 0)
				currentUnit = units.size() - 1;
		} while (getCurrentUnit().isDestroyed() && ++killed < units.size());
	}

	public void nextUnit() {
		int killed = 0;
		do {
			currentUnit++;
			if (currentUnit >= units.size())
				currentUnit = 0;
		} while (getCurrentUnit().isDestroyed() && ++killed < units.size());
	}

	public boolean isVisible(int x, int y) {
		return !true | visibility[x][y]; // TODO
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
		return units.get(currentUnit);
	}

	public void setCurrentUnit(int currentUnit) {
		this.currentUnit = currentUnit;
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

}
