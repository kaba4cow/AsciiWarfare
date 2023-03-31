package kaba4cow.warfare.gui;

import kaba4cow.ascii.drawing.Frame;
import kaba4cow.ascii.drawing.drawers.Drawer;

public class Viewport extends Frame {

	public final int x;
	public final int y;

	public Viewport(int x, int y, int width, int height) {
		super(width, height);
		this.x = x;
		this.y = y;
	}

	public void render() {
		Drawer.drawFrame(x, y, false, this);
	}

}
