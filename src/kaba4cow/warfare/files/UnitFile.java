package kaba4cow.warfare.files;

import java.util.HashMap;

import kaba4cow.ascii.toolbox.tools.Table;

public class UnitFile {

	private static final HashMap<String, UnitFile> map = new HashMap<>();

	private final String id;
	private final String name;
	private final String type;
	private final float health;
	private final float armor;
	private final float moves;
	private final float visibility;
	private final int maxUnits;
	private final String[] weapons;

	private WeaponFile[] weaponFiles;

	private UnitFile(Table table, int row) {
		this.id = table.getCell("ID", row);

		this.name = table.getCell("Name", row);
		this.type = table.getCell("Type", row);

		this.health = Float.parseFloat(table.getCell("Health", row));

		this.armor = Float.parseFloat(table.getCell("Armor", row));

		this.moves = Float.parseFloat(table.getCell("Moves", row));

		this.visibility = Float.parseFloat(table.getCell("Visibility", row));

		this.maxUnits = Integer.parseInt(table.getCell("Units", row));

		this.weapons = table.getCell("Weapons", row).split(",");

		map.put(id, this);
	}

	public static UnitFile get(String id) {
		return map.get(id);
	}

	public static void loadFiles(Table table) {
		int rows = table.rows();
		for (int row = 0; row < rows; row++)
			new UnitFile(table, row);
	}

	public static HashMap<String, UnitFile> getFiles() {
		return map;
	}

	public String getID() {
		return id;
	}

	public String getName() {
		return name;
	}

	public String getType() {
		return UnitTypeFile.get(type).getName();
	}

	public char getGlyph() {
		return UnitTypeFile.get(type).getGlyph();
	}

	public WeaponFile[] getWeapons() {
		if (weaponFiles == null) {
			weaponFiles = new WeaponFile[weapons.length];
			for (int i = 0; i < weapons.length; i++)
				weaponFiles[i] = WeaponFile.get(weapons[i]);
		}
		return weaponFiles;
	}

	public float getHealth() {
		return health;
	}

	public float getArmor() {
		return armor;
	}

	public float getVisibility() {
		return visibility;
	}

	public float getMoves() {
		return moves;
	}

	public int getMaxUnits() {
		return maxUnits;
	}

}
