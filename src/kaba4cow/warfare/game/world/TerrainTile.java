package kaba4cow.warfare.game.world;

import kaba4cow.ascii.drawing.drawers.Drawer;
import kaba4cow.warfare.files.BiomeFile;
import kaba4cow.warfare.files.TerrainFile;

public class TerrainTile {

	private final TerrainFile file;

	private final String name;
	private final BiomeFile biome;
	private final float penalty;
	private final char glyph;
	private final int color;
	private final boolean allowCrater;

	public TerrainTile(TerrainFile file, BiomeFile biome, float temperature) {
		this.file = file;
		this.name = file.getName();
		this.biome = biome;
		this.penalty = file.getPenalty(temperature);
		this.glyph = file.getGlyph();
		this.color = file.getColor(temperature);
		this.allowCrater = file.allowsCrater();
	}

	public void render(int x, int y) {
		Drawer.draw(x, y, glyph, color);
	}

	public String getName() {
		return name;
	}

	public String getBiomeName() {
		return biome.getName();
	}

	public BiomeFile getBiome() {
		return biome;
	}

	public float getPenalty() {
		return penalty;
	}

	public boolean allowsCrater() {
		return allowCrater;
	}

	public int getColor() {
		return color;
	}

	public TerrainFile getFile() {
		return file;
	}

}
