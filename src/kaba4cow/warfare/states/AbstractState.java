package kaba4cow.warfare.states;

import java.util.function.Consumer;

import kaba4cow.ascii.core.Engine;
import kaba4cow.ascii.core.Window;
import kaba4cow.ascii.drawing.BoxDrawer;
import kaba4cow.ascii.drawing.Drawer;
import kaba4cow.ascii.drawing.Glyphs;
import kaba4cow.ascii.gui.GUIFrame;
import kaba4cow.ascii.gui.GUIProgressBar;
import kaba4cow.warfare.game.MenuWorld;
import kaba4cow.warfare.gui.GUI;

public abstract class AbstractState {

	public static float PROGRESS;

	private static MenuWorld menuWorld;

	private static boolean waiting;
	protected static GUIFrame progressBar;

	private static int titleWidth;

	public AbstractState() {

	}

	public abstract void update(float dt);

	public abstract void render();

	public void onLostFocus() {
	}

	public static void init() {
		menuWorld = new MenuWorld();

		waiting = false;
		progressBar = new GUIFrame(GUI.COLOR, false, false);
		new GUIProgressBar(progressBar, -1, (Void) -> {
			return PROGRESS;
		});

		titleWidth = Drawer.totalWidth(Window.getTitle());
	}

	protected static void renderTitle() {
		menuWorld.render();
		int pos = (int) (12f * Engine.getElapsedTime()) % (Window.getWidth() + titleWidth);
		Drawer.drawBigString(Window.getWidth() - pos, 0, false, Window.getTitle(), Glyphs.LIGHT_SHADE, GUI.COLOR);
	}

	protected static void renderFrame(GUIFrame frame) {
		frame.render(0, 16, 22, Window.getHeight() - 16, false);
	}

	public static void renderProgressBar() {
		BoxDrawer.disableCollision();
		progressBar.render(Window.getWidth() / 2, Window.getHeight() / 2, Window.getWidth() / 2, 3, true);
		BoxDrawer.enableCollision();
	}

	public static boolean isWaiting() {
		return waiting;
	}

	public static void thread(String process, Consumer<Object> function) {
		new Thread(process) {
			@Override
			public void run() {
				PROGRESS = 0f;
				progressBar.setTitle(process);
				waiting = true;
				function.accept(null);
				waiting = false;
			}
		}.start();
	}

}
