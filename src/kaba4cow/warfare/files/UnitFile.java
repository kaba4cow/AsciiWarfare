package kaba4cow.warfare.files;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;

import kaba4cow.ascii.toolbox.tools.Table;

public class UnitFile {

	private static final HashMap<String, UnitFile> map = new HashMap<>();
	private static final LinkedHashMap<String, ArrayList<UnitFile>> sorted = new LinkedHashMap<>();

	private static int minPrice = Integer.MAX_VALUE;
	private static int maxPrice = 0;

	private final String id;
	private final String name;
	private final String type;
	private final int side;
	private final int level;
	private final int price;
	private final int health;
	private final float armor;
	private final float moves;
	private final int visibility;
	private final int units;
	private final String[] weapons;

	private WeaponFile[] weaponFiles;

	private UnitFile(Table table, int row) {
		this.id = table.getCell("ID", row);

		this.name = table.getCell("Name", row);
		this.type = table.getCell("Type", row);

		this.side = Integer.parseInt(table.getCell("Side", row));
		this.level = Integer.parseInt(table.getCell("Level", row));

		this.health = Integer.parseInt(table.getCell("Health", row));
		this.armor = Float.parseFloat(table.getCell("Armor", row));
		this.moves = Float.parseFloat(table.getCell("Moves", row));
		this.visibility = Integer.parseInt(table.getCell("Visibility", row));
		this.units = Integer.parseInt(table.getCell("Units", row));
		this.weapons = table.getCell("Weapons", row).split(",");

		this.price = calculatePrice(this, weapons);

		map.put(id, this);

		ArrayList<UnitFile> list;
		if (!sorted.containsKey(type))
			sorted.put(type, new ArrayList<>());
		list = sorted.get(type);
		list.add(this);
		Collections.sort(list, Sorter.instance);

		if (price < minPrice)
			minPrice = price;
		if (price > maxPrice)
			maxPrice = price;
	}

	private static int calculatePrice(UnitFile unit, String[] weapons) {
		float price = 0f;
		price += 13.8f * unit.getHealth();
		price += 6.2f * unit.getArmor();
		price += 4.6f * unit.getMoves();
		price += 3.4f * unit.getVisibility();
		price += 1.3f * unit.getLevel() * unit.getLevel();
		for (int i = 0; i < weapons.length; i++)
			price += WeaponFile.get(weapons[i]).getPrice();
		price *= 0.47f;
		return (int) price + 43;
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

	public static LinkedHashMap<String, ArrayList<UnitFile>> getSorted() {
		return sorted;
	}

	public static int getMinPrice() {
		return minPrice;
	}

	public static int getMaxPrice() {
		return maxPrice;
	}

	public String getID() {
		return id;
	}

	public String getName() {
		return name;
	}

	public String getType() {
		return type;
	}

	public String getTypeName() {
		return UnitTypeFile.get(type).getName();
	}

	public char getTypeGlyph() {
		return UnitTypeFile.get(type).getGlyph();
	}

	public int getSide() {
		return side;
	}

	public int getLevel() {
		return level;
	}

	public int getPrice() {
		return price;
	}

	public int getHealth() {
		return health;
	}

	public float getArmor() {
		return armor;
	}

	public int getVisibility() {
		return visibility;
	}

	public float getMoves() {
		return moves;
	}

	public int getUnits() {
		return units;
	}

	public WeaponFile[] getWeapons() {
		if (weaponFiles == null) {
			weaponFiles = new WeaponFile[weapons.length];
			for (int i = 0; i < weapons.length; i++)
				weaponFiles[i] = WeaponFile.get(weapons[i]);
		}
		return weaponFiles;
	}

	private static class Sorter implements Comparator<UnitFile> {

		public static Sorter instance = new Sorter();

		@Override
		public int compare(UnitFile arg0, UnitFile arg1) {
			return Integer.compare(arg0.getPrice(), arg1.getPrice());
		}

	}

}
