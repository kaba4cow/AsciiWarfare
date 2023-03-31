package kaba4cow.warfare.game.controllers;

import kaba4cow.ascii.input.Keyboard;
import kaba4cow.ascii.input.Mouse;
import kaba4cow.warfare.Camera;
import kaba4cow.warfare.game.Unit;

public class PlayerController extends Controller {

	public PlayerController() {
		super(0x4F5);
	}

	@Override
	public void update(float dt) {
		if (player.getCurrentUnit().isShooting())
			return;

		Camera camera = world.getCamera();
		int mX = camera.getMouseX();
		int mY = camera.getMouseY();

		if (camera.isMouseInViewport()) {
			if (Mouse.isKeyDown(Mouse.LEFT)) {
				if (player.isAiming())
					player.getCurrentUnit().createAttackPath(mX, mY);
				else
					player.getCurrentUnit().createPath(mX, mY);
			} else if (Mouse.isKeyDown(Mouse.RIGHT)) {
				for (int i = 0; i < player.getUnits().size(); i++) {
					Unit unit = player.getUnits().get(i);
					if (unit.isDestroyed())
						continue;
					if (mX == unit.getX() && mY == unit.getY()) {
						player.setCurrentUnit(i);
						world.setCameraTarget(unit);
						break;
					}
				}
			} else if (Keyboard.isKeyDown(Keyboard.KEY_Q)) {
				player.prevUnit();
				player.setAiming(false);
				world.setCameraTarget(player.getCurrentUnit());
			} else if (Keyboard.isKeyDown(Keyboard.KEY_E)) {
				player.nextUnit();
				player.setAiming(false);
				world.setCameraTarget(player.getCurrentUnit());
			} else if (Keyboard.isKeyDown(Keyboard.KEY_F)) {
				player.getCurrentUnit().resetPath();
				player.getCurrentUnit().resetAttackPath();
				player.setAiming(!player.isAiming());
			} else if (Keyboard.isKeyDown(Keyboard.KEY_T))
				world.newTurn(player.getIndex(), true);
			else if (Keyboard.isKeyDown(Keyboard.KEY_SPACE))
				player.getCurrentUnit().switchMoving();
			else if (Keyboard.isKey(Keyboard.KEY_C))
				world.setCameraTarget(player.getCurrentUnit());
		}
	}

	@Override
	public void render(int offX, int offY) {
		player.renderAiming(offX, offY);
	}

}
