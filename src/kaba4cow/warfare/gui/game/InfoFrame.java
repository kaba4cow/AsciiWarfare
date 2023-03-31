package kaba4cow.warfare.gui.game;

import kaba4cow.ascii.core.Display;
import kaba4cow.ascii.drawing.gui.GUIFrame;
import kaba4cow.ascii.drawing.gui.GUISeparator;
import kaba4cow.ascii.drawing.gui.GUIText;
import kaba4cow.warfare.Game;
import kaba4cow.warfare.game.World;
import kaba4cow.warfare.game.world.TerrainTile;
import kaba4cow.warfare.game.world.VegetationTile;

public class InfoFrame extends GUIFrame {

	private final GUIText weeks;
	private final GUIText days;
	private final GUIText hours;

	private final GUIText cash;
	private final GUIText income;

	private final GUIText position;
	private final GUIText biome;
	private final GUIText tile;
	private final GUIText temperature;

	public InfoFrame() {
		super(Game.GUI_COLOR, false, false);
		setTitle("Info");

		weeks = new GUIText(this, -1, "");
		days = new GUIText(this, -1, "");
		hours = new GUIText(this, -1, "");
		new GUISeparator(this, -1, false);

		cash = new GUIText(this, -1, "");
		income = new GUIText(this, -1, "");
		new GUISeparator(this, -1, false);

		position = new GUIText(this, -1, "");
		new GUISeparator(this, -1, false);

		biome = new GUIText(this, -1, "");
		tile = new GUIText(this, -1, "");
		temperature = new GUIText(this, -1, "");
	}

	public void render(World world) {
		weeks.setText("Week: " + (world.getWorldWeek() + 1));
		days.setText("Day: " + (world.getWorldDay() + 1));
		hours.setText("Hour: " + (world.getWorldHour() + 1));

		cash.setText("Cash: " + world.getPlayer().getCash());
		income.setText("Income: " + world.getPlayer().getIncome());

		int x = world.getCamera().getMouseX();
		int y = world.getCamera().getMouseY();
		TerrainTile terrain = world.getTerrain(x, y);
		VegetationTile vegetation = world.getVegetation(x, y);
		if (world.getCamera().isMouseInViewport() && world.isVisible(world.getPlayer(), x, y)) {
			position.setText("Position: " + x + ", " + y);
			biome.setText("Biome: " + terrain.getBiomeName());

			if (vegetation != null)
				tile.setText("Terrain: " + vegetation.getName());
			else
				tile.setText("Terrain: " + terrain.getName());

			float temp = world.getTemperature(x, y);
			if (temp < 0.25f)
				temperature.setText("Temperature: Freezing");
			else if (temp < 0.5f)
				temperature.setText("Temperature: Cold");
			else if (temp < 0.75f)
				temperature.setText("Temperature: Warm");
			else
				temperature.setText("Temperature: Hot");
		} else {
			position.setText("Position: ?, ?");
			biome.setText("Biome: ?");
			tile.setText("Terrain: ?");
			temperature.setText("Temperature: ?");
		}

		super.render(0, Display.getHeight() / 2, Display.getWidth() / 4, Display.getHeight() / 2, false);
	}

}
