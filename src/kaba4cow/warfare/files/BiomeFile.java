package kaba4cow.warfare.files;

import java.util.ArrayList;

import kaba4cow.ascii.toolbox.rng.RNG;
import kaba4cow.ascii.toolbox.tools.Table;

public class BiomeFile {

	private static final ArrayList<BiomeFile> list = new ArrayList<>();
	private static BiomeFile river;

	private final String id;
	private final String name;
	private final float terrainBias;
	private final float vegetationDensity;
	private final String[] terrain;
	private final String[] vegetation;

	private BiomeFile(Table table, int row) {
		this.id = table.getCell("ID", row);

		this.name = table.getCell("Name", row);

		this.terrainBias = Float.parseFloat(table.getCell("Terrain Bias", row));
		this.vegetationDensity = Float.parseFloat(table.getCell("Vegetation Density", row));

		this.terrain = table.getCell("Terrain", row).split(",");
		this.vegetation = table.getCell("Vegetation", row).split(",");

		if (id.equals("RIVER"))
			river = this;
		else
			list.add(this);
	}

	public static ArrayList<BiomeFile> getBiomes(RNG rng) {
		ArrayList<BiomeFile> shuffled = new ArrayList<>(list);

		for (int i = list.size() - 1; i > 0; i--) {
			int j = rng.nextInt(0, i);
			BiomeFile temp = shuffled.get(i);
			shuffled.set(i, shuffled.get(j));
			shuffled.set(j, temp);
		}

		return shuffled;
	}

	public static BiomeFile get(String id) {
		for (int i = 0; i < list.size(); i++)
			if (list.get(i).id.equalsIgnoreCase(id))
				return list.get(i);
		return river;
	}

	public static void loadFiles(Table table) {
		int rows = table.rows();
		for (int row = 0; row < rows; row++)
			new BiomeFile(table, row);
	}

	public static BiomeFile getRiver() {
		return river;
	}

	public String getID() {
		return id;
	}

	public String getName() {
		return name;
	}

	public float getTerrainBias() {
		return terrainBias;
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
