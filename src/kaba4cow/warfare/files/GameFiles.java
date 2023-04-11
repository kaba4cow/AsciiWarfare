package kaba4cow.warfare.files;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

import kaba4cow.ascii.toolbox.files.TableFile;

public class GameFiles {

	private GameFiles() {

	}

	public static void init() {
		InputStream is = GameFiles.class.getClassLoader().getResourceAsStream("kaba4cow/warfare/files/DATA");
		BufferedReader reader = new BufferedReader(new InputStreamReader(is));

		TableFile data = TableFile.read(reader);
		TerrainFile.loadFiles(data.getTable("TERRAIN"));
		VegetationFile.loadFiles(data.getTable("VEGETATION"));
		BiomeFile.loadFiles(data.getTable("BIOMES"));
		WeaponTypeFile.loadFiles(data.getTable("WEAPON TYPES"));
		WeaponFile.loadFiles(data.getTable("WEAPONS"));
		UnitTypeFile.loadFiles(data.getTable("UNIT TYPES"));
		UnitFile.loadFiles(data.getTable("UNITS"));
	}

}
