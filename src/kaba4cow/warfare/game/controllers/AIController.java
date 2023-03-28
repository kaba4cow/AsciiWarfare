package kaba4cow.warfare.game.controllers;

public class AIController extends Controller {

	public AIController() {
		super(0xF54);
	}

	@Override
	public void update(float dt) {
		if (player.getCurrentUnit().isShooting())
			return;

		world.newTurn(player, false);
	}

	@Override
	public void render(int offX, int offY) {

	}

}
