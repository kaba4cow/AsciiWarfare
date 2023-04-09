package kaba4cow.warfare.files;

import java.util.ArrayList;

import kaba4cow.ascii.toolbox.tools.Table;

public class BiomeFile {

	private static final ArrayList<BiomeFile> list = new ArrayList<>();

	private final String id;
	private final String name;
	private final float vegetationDensity;
	private final String[] terrain;
	private final String[] vegetation;

	private BiomeFile(Table table, int row) {
		this.id = table.getCell("ID", row);

		this.name = table.getCell("Name", row);

		this.vegetationDensity = Float.parseFloat(table.getCell("Vegetation Density", row));

		this.terrain = table.getCell("Terrain", row).split(",");
		this.vegetation = table.getCell("Vegetation", row).split(",");

		list.add(this);
	}

	public static ArrayList<BiomeFile> getBiomes() {
		return list;
	}

	public static void loadFiles(Table table) {
		int rows = table.rows();
		for (int row = 0; row < rows; row++)
			new BiomeFile(table, row);
	}

	public String getID() {
		return id;
	}

	public String getName() {
		return name;
	}

	public String[] getTerrain() {
		return terrain;
	}

	public float getVegetationDensity() {
		return vegetationDensity;
	}

	public String[] getVegetation() {
		return vegetation;
	}

}
