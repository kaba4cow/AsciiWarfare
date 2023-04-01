package kaba4cow.warfare.game.controllers;

import kaba4cow.ascii.toolbox.rng.RNG;

public class AIController extends Controller {

	public AIController() {
		super(0xF54);
	}

	@Override
	public void update(float dt) {
		if (RNG.chance(0.1f))
			world.newTurn(player.getIndex(), false);
	}

	@Override
	public void render(int offX, int offY) {

	}

}
