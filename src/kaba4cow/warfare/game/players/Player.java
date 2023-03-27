package kaba4cow.warfare.game.players;

import java.util.ArrayList;
import java.util.HashMap;

import kaba4cow.ascii.drawing.drawers.Drawer;
import kaba4cow.ascii.toolbox.files.DataFile;
import kaba4cow.ascii.toolbox.maths.Maths;
import kaba4cow.warfare.files.UnitFile;
import kaba4cow.warfare.game.Unit;
import kaba4cow.warfare.game.Village;
import kaba4cow.warfare.game.World;

public abstract class Player {

	protected final World world;

	protected ArrayList<Unit> units;
	protected int currentUnit;

	private boolean[][] visibility;

	private final int color;

	public Player(World world, int color) {
		this.world = world;
		this.units = new ArrayList<Unit>();
		this.visibility = new boolean[world.getSize()][world.getSize()];
		this.currentUnit = 0;
		this.color = color;
	}

	public Player(World world, int color, DataFile data) {
		this(world, color);
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

	public void createUnits(Village village) {
		HashMap<String, UnitFile> files = UnitFile.getFiles();
		for (String file : files.keySet())
			units.add(new Unit(world, village, this, files.get(file)));
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

	public void onNewTurn() {
		for (Unit unit : units)
			unit.onNewTurn();
	}

	protected void prevUnit() {
		int killed = 0;
		do {
			currentUnit--;
			if (currentUnit < 0)
				currentUnit = units.size() - 1;
		} while (getCurrentUnit().isDestroyed() && ++killed < units.size());
	}

	protected void nextUnit() {
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

	public Unit getCurrentUnit() {
		return units.get(currentUnit);
	}

	public int getColor() {
		return color;
	}

}
