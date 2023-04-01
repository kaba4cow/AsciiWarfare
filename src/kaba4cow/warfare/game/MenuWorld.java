package kaba4cow.warfare.game;

import kaba4cow.ascii.core.Display;
import kaba4cow.ascii.toolbox.rng.RNG;
import kaba4cow.warfare.game.world.Generator;
import kaba4cow.warfare.game.world.TerrainTile;
import kaba4cow.warfare.game.world.VegetationTile;

public class MenuWorld {

	private final int size;

	private final TerrainTile[][] terrainMap;
	private final VegetationTile[][] vegetationMap;
	private final float[][] temperatureMap;

	public MenuWorld() {
		this.size = World.calculateSize(0);
		this.terrainMap = new TerrainTile[size][size];
		this.vegetationMap = new VegetationTile[size][size];
		this.temperatureMap = new float[size][size];

		Generator generator = new Generator(0, RNG.randomInt(1, 4), size, RNG.randomLong());
		generator.generate();
		generator.populate(terrainMap, vegetationMap, temperatureMap);
	}

	public void render() {
		int x, y;
		for (y = 0; y < Display.getHeight(); y++) {
			for (x = 0; x < Display.getWidth(); x++) {
				if (vegetationMap[x][y] == null)
					terrainMap[x][y].render(x, y);
				else
					vegetationMap[x][y].render(x, y);
			}
		}
	}

}