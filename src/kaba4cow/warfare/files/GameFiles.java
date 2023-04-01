package kaba4cow.warfare.files;

import java.io.File;

import kaba4cow.ascii.toolbox.files.TableFile;

public class GameFiles {

	private GameFiles() {

	}

	public static void init() {
		TableFile data = TableFile.read(new File("DATA"));
		TerrainFile.loadFiles(data.getTable("Terrain"));
		VegetationFile.loadFiles(data.getTable("Vegetation"));
		BiomeFile.loadFiles(data.getTable("Biomes"));
		WeaponTypeFile.loadFiles(data.getTable("Weapon Types"));
		WeaponFile.loadFiles(data.getTable("Weapons"));
		UnitTypeFile.loadFiles(data.getTable("Unit Types"));
		UnitFile.loadFiles(data.getTable("Units"));
		BuildingFile.loadFiles(data.getTable("Buildings"));
	}

}
