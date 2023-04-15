package kaba4cow.warfare.gui;

import kaba4cow.ascii.core.Window;
import kaba4cow.ascii.drawing.Drawer;
import kaba4cow.ascii.drawing.Frame;

public class Viewport extends Frame {

	public final int x;
	public final int y;

	public Viewport() {
		super(Window.getWidth() - Window.getWidth() / 4, Window.getHeight() - Window.getHeight() / 5);
		this.x = Window.getWidth() / 4;
		this.y = Window.getHeight() / 5;
	}

	public void render() {
		Drawer.drawFrame(x, y, false, this);
	}

}
