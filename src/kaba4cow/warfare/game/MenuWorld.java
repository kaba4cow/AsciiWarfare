package kaba4cow.warfare.game;

import kaba4cow.ascii.core.Display;
import kaba4cow.ascii.drawing.drawers.Drawer;
import kaba4cow.ascii.drawing.glyphs.Glyphs;
import kaba4cow.ascii.toolbox.Colors;
import kaba4cow.ascii.toolbox.rng.RNG;
import kaba4cow.warfare.game.world.Generator;
import kaba4cow.warfare.game.world.TerrainTile;
import kaba4cow.warfare.game.world.VegetationTile;

public class MenuWorld {

	private final TerrainTile[][] terrainMap;
	private final VegetationTile[][] vegetationMap;
	private final int[][] elevationMap;
	private final boolean[][] topologyMap;

	public MenuWorld() {
		this.terrainMap = new TerrainTile[World.SIZE][World.SIZE];
		this.vegetationMap = new VegetationTile[World.SIZE][World.SIZE];
		this.elevationMap = new int[World.SIZE][World.SIZE];
		this.topologyMap = new boolean[World.SIZE][World.SIZE];
		float[][] temperatureMap = new float[World.SIZE][World.SIZE];

		Generator generator = new Generator(RNG.randomInt(4), RNG.randomLong());
		generator.generate();
		generator.populate(terrainMap, vegetationMap, elevationMap, topologyMap, temperatureMap);
	}

	public void render() {
		int x, y;
		for (y = 0; y < Display.getHeight(); y++) {
			for (x = 0; x < Display.getWidth(); x++) {
				if (x >= World.SIZE || y >= World.SIZE)
					continue;

				if (vegetationMap[x][y] == null)
					terrainMap[x][y].render(x, y);
				else
					vegetationMap[x][y].render(x, y);

				if (topologyMap[x][y])
					Drawer.draw(x, y, Glyphs.SPACE, Drawer.IGNORE_FOREGROUND | Drawer.IGNORE_GLYPH
							| Colors.createBackground(elevationMap[x][y]));
			}
		}
	}

}