package kaba4cow.warfare;

import java.util.LinkedList;

import kaba4cow.ascii.core.Display;
import kaba4cow.ascii.core.Engine;
import kaba4cow.ascii.drawing.drawers.Drawer;
import kaba4cow.ascii.drawing.glyphs.Glyphs;
import kaba4cow.ascii.input.Keyboard;
import kaba4cow.ascii.toolbox.DisplayListener;
import kaba4cow.ascii.toolbox.MemoryAnalyzer;

public class InfoPanel implements DisplayListener {

	private static final int COLOR = 0x111FFF;

	private static final float MAX_TIME = 0.25f;

	private static int mode = 0;

	private static LinkedList<Float> memory = new LinkedList<>();

	private static float time;

	private InfoPanel() {

	}

	public static void init() {
		Display.addListener(new InfoPanel());
	}

	public static void update(float dt) {
		if (Keyboard.isKeyDown(Keyboard.KEY_F3)) {
			mode++;
			if (mode >= 3)
				mode = 0;
		}

		time += dt;
		if (time >= MAX_TIME) {
			time = 0f;

			float value = MemoryAnalyzer.getCurrentUsage() / (float) MemoryAnalyzer.getMaxMemory();
			memory.removeFirst();
			memory.add(value);
		}
	}

	public static void render() {
		if (mode == 0)
			return;
		else if (mode == 1) {
			Drawer.drawString(0, 0, false, "FPS " + Engine.getCurrentFramerate(), COLOR);
		} else if (mode == 2) {
			int x, y;
			int height = Display.getHeight() / 2;
			for (x = 0; x < memory.size(); x++) {
				int value = height - (int) (height * memory.get(x));
				for (y = 0; y < height; y++)
					if (y < value)
						Drawer.draw(x, y, Glyphs.SPACE, COLOR);
					else if (y == value)
						Drawer.draw(x, y, Glyphs.BLACK_UP_POINTING_TRIANGLE, COLOR);
					else
						Drawer.draw(x, y, Glyphs.VERTICAL_LINE, COLOR);
			}
			Drawer.drawString(0, 0, false, String.format("%8s - %d KB", "Total", MemoryAnalyzer.getMaxMemory() / 1024l),
					COLOR);
			Drawer.drawString(0, 1, false, String.format("%8s - %d KB", "Max", MemoryAnalyzer.getMaxUsage() / 1024l),
					COLOR);
			Drawer.drawString(0, 2, false,
					String.format("%8s - %d KB", "Current", MemoryAnalyzer.getCurrentUsage() / 1024l), COLOR);
		}
	}

	@Override
	public void onWindowCreated(int width, int height) {
		memory.clear();
		for (int i = 0; i < width / 3; i++)
			memory.add(0f);
	}

}
