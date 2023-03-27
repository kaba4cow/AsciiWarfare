package kaba4cow.warfare.files;

import java.util.HashMap;

import kaba4cow.ascii.toolbox.Colors;
import kaba4cow.ascii.toolbox.tools.Table;

public class VegetationFile {

	private static final HashMap<String, VegetationFile> map = new HashMap<>();
	private static VegetationFile building;

	private final String id;
	private final String name;
	private final float threshold;
	private final char glyphWarm, glyphCold;
	private final int colorWarm, colorCold;

	private VegetationFile(Table table, int row) {
		this.id = table.getCell("ID", row);

		this.name = table.getCell("Name", row);

		this.threshold = Float.parseFloat(table.getCell("Threshold", row));

		this.glyphWarm = (char) Integer.parseInt(table.getCell("Warm Glyph", row));
		this.glyphCold = (char) Integer.parseInt(table.getCell("Cold Glyph", row));

		this.colorWarm = 0xFFF & Integer.parseInt(table.getCell("Warm Color", row), 16);
		this.colorCold = 0xFFF & Integer.parseInt(table.getCell("Cold Color", row), 16);

		map.put(id, this);
		if (id.equalsIgnoreCase("BUILDING"))
			building = this;
	}

	public static VegetationFile get(String name) {
		return map.get(name);
	}

	public static VegetationFile getBuilding() {
		return building;
	}

	public static void loadFiles(Table table) {
		int rows = table.rows();
		for (int row = 0; row < rows; row++)
			new VegetationFile(table, row);
	}

	public String getID() {
		return id;
	}

	public String getName() {
		return name;
	}

	public char getGlyph(float temperature) {
		return temperature > 0.5f ? glyphWarm : glyphCold;
	}

	public int getColor(float temperature) {
		return Colors.blendForeground(colorCold, colorWarm, temperature);
	}

	public boolean isDestroyed(float temperature) {
		return temperature < threshold;
	}

}
