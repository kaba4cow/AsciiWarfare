package kaba4cow.warfare.files;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import kaba4cow.ascii.toolbox.rng.RNG;
import kaba4cow.ascii.toolbox.tools.Table;

public class BuildingFile {

	private static final ArrayList<BuildingFile> list = new ArrayList<>();

	private final float chance;
	private final int width;
	private final int height;

	private BuildingFile(Table table, int row) {
		this.chance = Float.parseFloat(table.getCell("Chance", row));

		this.width = Integer.parseInt(table.getCell("Width", row));
		this.height = Integer.parseInt(table.getCell("Height", row));

		list.add(this);
		Collections.sort(list, Sorter.instance);
	}

	public static BuildingFile get(RNG rng) {
		BuildingFile file = null;
		float chance = rng.nextFloat(0f, 1f);
		for (int i = 0; i < list.size(); i++) {
			file = list.get(i);
			if (chance <= file.chance)
				return file;
		}
		return file;
	}

	public static void loadFiles(Table table) {
		for (int row = 0; row < table.rows(); row++)
			new BuildingFile(table, row);
	}

	public float getChance() {
		return chance;
	}

	public int getWidth() {
		return width;
	}

	public int getHeight() {
		return height;
	}

	private static class Sorter implements Comparator<BuildingFile> {

		public static final Sorter instance = new Sorter();

		@Override
		public int compare(BuildingFile o1, BuildingFile o2) {
			return Float.compare(o1.chance, o2.chance);
		}

	}

}
