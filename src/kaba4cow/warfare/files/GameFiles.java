package kaba4cow.warfare.files;

import java.io.File;

import kaba4cow.ascii.toolbox.files.TableFile;

public class GameFiles {

	private GameFiles() {

	}

	public static void init() {
		TableFile data = TableFile.read(new File("DATA"));
		TerrainFile.loadFiles(data.getTable("TERRAIN"));
		VegetationFile.loadFiles(data.getTable("VEGETATION"));
		BiomeFile.loadFiles(data.getTable("BIOMES"));
		WeaponTypeFile.loadFiles(data.getTable("WEAPON TYPES"));
		WeaponFile.loadFiles(data.getTable("WEAPONS"));
		UnitTypeFile.loadFiles(data.getTable("UNIT TYPES"));
		UnitFile.loadFiles(data.getTable("UNITS"));
	}

}
