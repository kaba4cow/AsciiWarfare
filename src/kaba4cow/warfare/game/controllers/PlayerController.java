package kaba4cow.warfare.game.controllers;

import kaba4cow.warfare.Camera;
import kaba4cow.warfare.Controls;
import kaba4cow.warfare.game.Unit;

public class PlayerController extends Controller {

	public PlayerController() {
		super(0x4F5);
	}

	@Override
	public void update(float dt) {
		Camera camera = world.getCamera();
		int mX = camera.getMouseX();
		int mY = camera.getMouseY();

		if (camera.isMouseInViewport()) {
			if (Controls.PATH.isKeyDown()) {
				Unit target = player.getUnit(mX, mY);
				if (target != null && target.getPlayer() == player)
					player.joinUnits(target, player.getCurrentUnit(), true);
				else if (player.isAiming())
					player.getCurrentUnit().createAttackPath(mX, mY);
				else
					player.getCurrentUnit().createPath(mX, mY);
			} else if (Controls.SELECT_UNIT.isKeyDown()) {
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
			} else if (Controls.PREV_UNIT.isKeyDown()) {
				player.prevUnit();
				player.setAiming(false);
				world.setCameraTarget(player.getCurrentUnit());
			} else if (Controls.NEXT_UNIT.isKeyDown()) {
				player.nextUnit();
				player.setAiming(false);
				world.setCameraTarget(player.getCurrentUnit());
			} else if (Controls.FIRE_MODE.isKeyDown()) {
				player.getCurrentUnit().resetAttackPath();
				player.setAiming(!player.isAiming());
			} else if (Controls.NEW_TURN.isKeyDown())
				world.newTurn(player.getIndex(), true);
			else if (Controls.MOVE_UNIT.isKeyDown())
				player.getCurrentUnit().switchMoving();
			else if (Controls.CAMERA_FOLLOW.isKeyDown())
				world.setCameraTarget(player.getCurrentUnit());
		}
	}

	@Override
	public void render(int offX, int offY) {
		player.renderAiming(offX, offY);
	}

}
