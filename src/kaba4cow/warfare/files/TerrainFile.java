package kaba4cow.warfare.files;

import java.util.HashMap;

import kaba4cow.ascii.toolbox.Colors;
import kaba4cow.ascii.toolbox.tools.Table;

public class TerrainFile {

	private static final HashMap<String, TerrainFile> map = new HashMap<>();
	private static TerrainFile water, road, crater, ruins;

	private final String id;
	private final String name;
	private final boolean allowCrater;
	private final float penalty;
	private final int colorWarm, colorCold;
	private final char glyph;

	private TerrainFile(Table table, int row) {
		this.id = table.getCell("ID", row);

		this.name = table.getCell("Name", row);

		this.allowCrater = Integer.parseInt(table.getCell("Craters", row)) != 0;

		this.penalty = Float.parseFloat(table.getCell("Penalty", row));

		this.colorWarm = 0xFFF & Integer.parseInt(table.getCell("Warm Color", row), 16);
		this.colorCold = 0xFFF & Integer.parseInt(table.getCell("Cold Color", row), 16);

		this.glyph = (char) Integer.parseInt(table.getCell("Glyph", row));

		map.put(id, this);
		if (id.equals("CRATER"))
			crater = this;
		else if (id.equals("RUINS"))
			ruins = this;
		else if (id.equals("WATER"))
			water = this;
		else if (id.equals("ROAD"))
			road = this;
	}

	public static TerrainFile get(String name) {
		return map.get(name);
	}

	public static void loadFiles(Table table) {
		int rows = table.rows();
		for (int row = 0; row < rows; row++)
			new TerrainFile(table, row);
	}

	public String getID() {
		return id;
	}

	public String getName() {
		return name;
	}

	public char getGlyph() {
		return glyph;
	}

	public int getColor(float temperature) {
		return Colors.blendForeground(colorCold, colorWarm, temperature);
	}

	public float getPenalty() {
		return penalty;
	}

	public boolean allowsCrater() {
		return allowCrater;
	}

	public static TerrainFile getCrater() {
		return crater;
	}

	public static TerrainFile getRuins() {
		return ruins;
	}

	public static TerrainFile getWater() {
		return water;
	}

	public static TerrainFile getRoad() {
		return road;
	}

}
