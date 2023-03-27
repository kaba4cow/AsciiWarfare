package kaba4cow.warfare.files;

import java.util.HashMap;

import kaba4cow.ascii.toolbox.tools.Table;

public class UnitTypeFile {

	private static final HashMap<String, UnitTypeFile> map = new HashMap<>();

	private final String name;
	private final char glyph;

	private UnitTypeFile(Table table, int row) {
		String id = table.getCell("ID", row);

		this.name = table.getCell("Name", row);
		this.glyph = table.getCell("Glyph", row).charAt(0);

		map.put(id, this);
	}

	public static void loadFiles(Table table) {
		int rows = table.rows();
		for (int row = 0; row < rows; row++)
			new UnitTypeFile(table, row);
	}

	public static UnitTypeFile get(String id) {
		return map.get(id);
	}

	public String getName() {
		return name;
	}

	public char getGlyph() {
		return glyph;
	}

}
