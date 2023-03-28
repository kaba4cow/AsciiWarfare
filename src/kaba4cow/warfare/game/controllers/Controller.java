package kaba4cow.warfare.game.controllers;

import kaba4cow.warfare.game.Player;
import kaba4cow.warfare.game.World;

public abstract class Controller {

	protected World world;
	protected Player player;

	private final int color;

	public Controller(int color) {
		this.color = color;
	}

	public abstract void update(float dt);

	public abstract void render(int offX, int offY);

	public int getColor() {
		return color;
	}

	public Controller setPlayer(World world, Player player) {
		this.world = world;
		this.player = player;
		return this;
	}

}
