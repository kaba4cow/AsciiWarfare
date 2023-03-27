package kaba4cow.warfare.game.players;

import kaba4cow.ascii.core.Display;
import kaba4cow.ascii.core.Engine;
import kaba4cow.ascii.drawing.drawers.Drawer;
import kaba4cow.ascii.drawing.glyphs.Glyphs;
import kaba4cow.ascii.input.Keyboard;
import kaba4cow.ascii.input.Mouse;
import kaba4cow.ascii.toolbox.files.DataFile;
import kaba4cow.warfare.Camera;
import kaba4cow.warfare.game.Unit;
import kaba4cow.warfare.game.World;

public class HumanPlayer extends Player {

	private static final int COLOR = 0x4F5;

	private boolean aiming = false;

	public HumanPlayer(World world) {
		super(world, COLOR);
	}

	public HumanPlayer(World world, DataFile data) {
		super(world, COLOR, data);
	}

	@Override
	public void update(float dt) {
		super.update(dt);

		if (getCurrentUnit().isShooting())
			return;

		Camera camera = world.getCamera();
		int mX = camera.getMouseX();
		int mY = camera.getMouseY();

		if (camera.isMouseInViewport()) {
			if (Mouse.isKeyDown(Mouse.LEFT)) {
				if (aiming)
					getCurrentUnit().createAttackPath(mX, mY);
				else
					getCurrentUnit().createPath(mX, mY);
			} else if (Mouse.isKeyDown(Mouse.RIGHT)) {
				for (int i = 0; i < units.size(); i++) {
					Unit unit = units.get(i);
					if (unit.isDestroyed())
						continue;
					if (mX == unit.getX() && mY == unit.getY()) {
						currentUnit = i;
						world.setCameraTarget(unit);
						break;
					}
				}
			} else if (Keyboard.isKeyDown(Keyboard.KEY_Q)) {
				prevUnit();
				aiming = false;
				world.setCameraTarget(getCurrentUnit());
			} else if (Keyboard.isKeyDown(Keyboard.KEY_E)) {
				nextUnit();
				aiming = false;
				world.setCameraTarget(getCurrentUnit());
			} else if (Keyboard.isKeyDown(Keyboard.KEY_F)) {
				getCurrentUnit().resetPath();
				getCurrentUnit().resetAttackPath();
				aiming = !aiming;
			} else if (Keyboard.isKeyDown(Keyboard.KEY_T))
				world.newTurn(this);
			else if (Keyboard.isKeyDown(Keyboard.KEY_SPACE))
				getCurrentUnit().switchMoving();
			else if (Keyboard.isKey(Keyboard.KEY_C))
				world.setCameraTarget(getCurrentUnit());
		}
	}

	@Override
	public void render(int offX, int offY) {
		super.render(offX, offY);

		Display.setDrawCursor(!aiming);
		if (aiming) {
			if (Engine.getElapsedTime() % 0.75f < 0.5f) {
				int mX = world.getCamera().getMouseX() - (int) world.getCamera().getX();
				int mY = world.getCamera().getMouseY() - (int) world.getCamera().getY();
				int color = Drawer.IGNORE_BACKGROUND | 0x000FFF;
				Drawer.draw(mX - 1, mY, Glyphs.RIGHTWARDS_ARROW, color);
				Drawer.draw(mX + 1, mY, Glyphs.LEFTWARDS_ARROW, color);
				Drawer.draw(mX, mY - 1, Glyphs.DOWNWARDS_ARROW, color);
				Drawer.draw(mX, mY + 1, Glyphs.UPWARDS_ARROW, color);
			}

			getCurrentUnit().renderAttackRange(offX, offY);
		}
	}

	public boolean isAiming() {
		return aiming;
	}

}
