package kaba4cow.warfare.files;

import java.util.HashMap;

import kaba4cow.ascii.toolbox.tools.Table;

public class WeaponTypeFile {

	private static final HashMap<String, WeaponTypeFile> map = new HashMap<>();

	private final String name;
	private final char glyph;

	private WeaponTypeFile(Table table, int row) {
		String id = table.getCell("ID", row);

		this.name = table.getCell("Name", row);
		this.glyph = (char) Integer.parseInt(table.getCell("Glyph", row));

		map.put(id, this);
	}

	public static void loadFiles(Table table) {
		int rows = table.rows();
		for (int row = 0; row < rows; row++)
			new WeaponTypeFile(table, row);
	}

	public static WeaponTypeFile get(String id) {
		return map.get(id);
	}

	public String getName() {
		return name;
	}

	public char getGlyph() {
		return glyph;
	}

}
