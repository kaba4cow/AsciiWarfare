package kaba4cow.warfare.game.world;

import kaba4cow.ascii.drawing.drawers.Drawer;
import kaba4cow.warfare.files.VegetationFile;

public class VegetationTile {

	private final VegetationFile file;

	private final String name;
	private final char glyph;
	private final int color;

	public VegetationTile(VegetationFile file, float temperature) {
		this.file = file;
		this.name = file.getName();
		this.glyph = file.getGlyph(temperature);
		this.color = file.getColor(temperature);
	}

	public void render(int x, int y) {
		Drawer.draw(x, y, glyph, color);
	}

	public String getName() {
		return name;
	}

	public VegetationFile getFile() {
		return file;
	}

}
