package kaba4cow.warfare.game;

import kaba4cow.ascii.core.Display;
import kaba4cow.ascii.toolbox.rng.RNG;
import kaba4cow.warfare.Game;
import kaba4cow.warfare.game.world.Generator;
import kaba4cow.warfare.game.world.TerrainTile;
import kaba4cow.warfare.game.world.VegetationTile;

public class MenuWorld {

	private final TerrainTile[][] terrainMap;
	private final VegetationTile[][] vegetationMap;
	private final float[][] temperatureMap;

	public MenuWorld() {
		this.terrainMap = new TerrainTile[Game.WORLD_SIZE][Game.WORLD_SIZE];
		this.vegetationMap = new VegetationTile[Game.WORLD_SIZE][Game.WORLD_SIZE];
		this.temperatureMap = new float[Game.WORLD_SIZE][Game.WORLD_SIZE];

		Generator generator = new Generator(RNG.randomInt(4), RNG.randomLong());
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