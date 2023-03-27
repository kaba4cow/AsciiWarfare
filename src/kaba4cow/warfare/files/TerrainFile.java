package kaba4cow.warfare.files;

import java.util.HashMap;

import kaba4cow.ascii.toolbox.Colors;
import kaba4cow.ascii.toolbox.maths.Maths;
import kaba4cow.ascii.toolbox.tools.Table;

public class TerrainFile {

	private static final HashMap<String, TerrainFile> map = new HashMap<>();
	private static TerrainFile water, road, track, crater, ruins;

	private final String id;
	private final String name;
	private final boolean allowTrack;
	private final boolean allowCrater;
	private final float penaltyWarm, penaltyCold;
	private final int colorWarm, colorCold;
	private final char glyph;

	private TerrainFile(Table table, int row) {
		this.id = table.getCell("ID", row);

		this.name = table.getCell("Name", row);

		this.allowTrack = Integer.parseInt(table.getCell("Tracks", row)) != 0;
		this.allowCrater = Integer.parseInt(table.getCell("Craters", row)) != 0;

		this.penaltyWarm = Float.parseFloat(table.getCell("Warm Penalty", row));
		this.penaltyCold = Float.parseFloat(table.getCell("Cold Penalty", row));

		this.colorWarm = 0xFFF & Integer.parseInt(table.getCell("Warm Color", row), 16);
		this.colorCold = 0xFFF & Integer.parseInt(table.getCell("Cold Color", row), 16);

		this.glyph = (char) Integer.parseInt(table.getCell("Glyph", row));

		map.put(id, this);
		if (id.equals("TRACK"))
			track = this;
		else if (id.equals("CRATER"))
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

	public float getPenalty(float temperature) {
		return Maths.blend(penaltyCold, penaltyWarm, temperature);
	}

	public boolean allowsTrack() {
		return allowTrack;
	}

	public boolean allowsCrater() {
		return allowCrater;
	}

	public static TerrainFile getTrack() {
		return track;
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
