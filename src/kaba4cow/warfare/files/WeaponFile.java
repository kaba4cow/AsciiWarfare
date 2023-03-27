package kaba4cow.warfare.files;

import java.util.HashMap;

import kaba4cow.ascii.toolbox.tools.Table;

public class WeaponFile {

	private static final HashMap<String, WeaponFile> map = new HashMap<>();

	private final String name;
	private final String type;
	private final float range;
	private final float damage;
	private final float piercing;
	private final float accuracy;
	private final float penalty;
	private final float radius;
	private final int attacks;
	private final boolean artillery;
	private final boolean createCrater;

	private WeaponFile(Table table, int row) {
		String id = table.getCell("ID", row);

		this.name = table.getCell("Name", row);
		this.type = table.getCell("Type", row);

		this.range = Float.parseFloat(table.getCell("Range", row));
		this.damage = Float.parseFloat(table.getCell("Damage", row));
		this.piercing = Float.parseFloat(table.getCell("Piercing", row));
		this.accuracy = Float.parseFloat(table.getCell("Accuracy", row));
		this.penalty = Float.parseFloat(table.getCell("Penalty", row));
		this.radius = Float.parseFloat(table.getCell("Radius", row));

		this.attacks = Integer.parseInt(table.getCell("Attacks", row));
		this.artillery = Integer.parseInt(table.getCell("Artillery", row)) != 0;
		this.createCrater = Integer.parseInt(table.getCell("Craters", row)) != 0;

		map.put(id, this);
	}

	public static WeaponFile get(String id) {
		return map.get(id);
	}

	public static void loadFiles(Table table) {
		int rows = table.rows();
		for (int row = 0; row < rows; row++)
			new WeaponFile(table, row);
	}

	public static HashMap<String, WeaponFile> getFiles() {
		return map;
	}

	public String getName() {
		return name;
	}

	public String getType() {
		return WeaponTypeFile.get(type).getName();
	}

	public char getGlyph() {
		return WeaponTypeFile.get(type).getGlyph();
	}

	public float getRange() {
		return range;
	}

	public float getDamage() {
		return damage;
	}

	public float getPiercing() {
		return piercing;
	}

	public float getAccuracy() {
		return accuracy;
	}

	public float getPenalty() {
		return penalty;
	}

	public float getRadius() {
		return radius;
	}

	public int getAttacks() {
		return attacks;
	}

	public boolean isArtillery() {
		return artillery;
	}

	public boolean createsCrater() {
		return createCrater;
	}

}
