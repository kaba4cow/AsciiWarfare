package kaba4cow.warfare.gui;

import kaba4cow.ascii.core.Display;
import kaba4cow.ascii.drawing.Frame;
import kaba4cow.ascii.drawing.drawers.Drawer;

public class Viewport extends Frame {

	public final int x;
	public final int y;

	public Viewport() {
		super(Display.getWidth() - Display.getWidth() / 4, Display.getHeight() - Display.getHeight() / 5);
		this.x = Display.getWidth() / 4;
		this.y = Display.getHeight() / 5;
	}

	public void render() {
		Drawer.drawFrame(x, y, false, this);
	}

}
