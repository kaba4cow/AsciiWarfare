package kaba4cow.warfare.game.players;

import kaba4cow.ascii.toolbox.files.DataFile;
import kaba4cow.warfare.game.World;

public class EnemyPlayer extends Player {

	private static final int COLOR = 0xF54;

	public EnemyPlayer(World world) {
		super(world, COLOR);
	}

	public EnemyPlayer(World world, DataFile data) {
		super(world, COLOR, data);
	}

	@Override
	public void update(float dt) {
		super.update(dt);
	}

}
