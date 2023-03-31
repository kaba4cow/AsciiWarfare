package kaba4cow.warfare.game;

import kaba4cow.ascii.drawing.drawers.Drawer;
import kaba4cow.ascii.drawing.glyphs.Glyphs;
import kaba4cow.ascii.toolbox.files.DataFile;
import kaba4cow.ascii.toolbox.maths.Maths;
import kaba4cow.ascii.toolbox.maths.vectors.Vector2i;
import kaba4cow.ascii.toolbox.rng.RNG;
import kaba4cow.warfare.files.UnitFile;
import kaba4cow.warfare.files.WeaponFile;
import kaba4cow.warfare.gui.game.WeaponFrame;
import kaba4cow.warfare.pathfinding.AttackPath;
import kaba4cow.warfare.pathfinding.Node;
import kaba4cow.warfare.pathfinding.Pathfinder;
import kaba4cow.warfare.pathfinding.UnitPath;

public class Unit {

	private final World world;
	private final Player player;

	private int x;
	private int y;

	private final UnitFile file;

	private int attackPos = -1;

	private UnitPath path = null;
	private AttackPath attackPath = null;
	private long attackSeed;
	private boolean moving = false;

	private float moveDelayTime;
	private float attackDelayTime;

	private float health;
	private int units;
	private float moves;

	private int currentWeapon;
	private int[] attacks;

	private WeaponFrame weaponFrame;

	public Unit(World world, Village village, Player player, UnitFile file) {
		this.world = world;
		this.player = player;
		this.file = file;
		do {
			x = RNG.randomInt(village.x - 2 * village.radius, village.x + 2 * village.radius);
			y = RNG.randomInt(village.y - 2 * village.radius, village.y + 2 * village.radius);
		} while (world.getPenalty(x, y) > 1f || world.isObstacle(x, y));

		this.attacks = new int[file.getWeapons().length];

		this.currentWeapon = 0;
		this.moveDelayTime = 0f;
		this.attackDelayTime = 0f;

		this.health = getMaxHealth();
		this.units = getMaxUnits();
		onNewTurn();

		this.weaponFrame = new WeaponFrame(this);
		player.updateVisibility(x, y, getVisibilityRadius());
	}

	public Unit(World world, Player player, DataFile data) {
		this.world = world;
		this.player = player;

		this.file = UnitFile.get(data.getString(0));
		this.x = data.getInt(1);
		this.y = data.getInt(2);
		this.health = data.getFloat(3);
		this.units = data.getInt(4);
		this.moves = data.getFloat(5);
		this.attacks = new int[file.getWeapons().length];
		for (int i = 0; i < attacks.length; i++)
			attacks[i] = data.getInt(6 + i);

		this.currentWeapon = 0;
		this.moveDelayTime = 0f;
		this.attackDelayTime = 0f;

		this.weaponFrame = new WeaponFrame(this);
	}

	public void save(DataFile data) {
		data.clear();
		data.setString(file.getID());
		data.setInt(x).setInt(y);
		data.setFloat(health);
		data.setInt(units);
		data.setFloat(moves);
		for (int i = 0; i < attacks.length; i++)
			data.setInt(attacks[i]);
	}

	public void onNewTurn() {
		if (isDestroyed()) {
			moves = 0;
			for (int i = 0; i < attacks.length; i++)
				attacks[i] = 0;
			path = null;
			attackPath = null;
		} else {
			moves = getMaxMoves();
			for (int i = 0; i < attacks.length; i++)
				attacks[i] = file.getWeapons()[i].getAttacks();
		}
		moving = false;
	}

	public void update(float dt) {
		if (path == null || moves <= 0)
			moving = false;

		moveDelayTime += dt;
		if (moving && moveDelayTime >= (world.getPenalty(x, y) + 1f) / file.getMoves()) {
			moveDelayTime = 0f;
			Node nextNode = path.move();
			if (nextNode == null)
				path = null;
			else {
				x = nextNode.x;
				y = nextNode.y;
				player.updateVisibility(x, y, getVisibilityRadius());
				moves = Maths.max(moves - nextNode.penalty - 1f, 0f);
				world.moveUnit(player.getIndex(), getIndex(), x, y, true);
			}
		}

		if (isShooting()) {
			attackDelayTime += dt;
			if (attackDelayTime >= 1f / getCurrentWeapon().getRange()) {
				attackDelayTime = 0f;
				attackPos++;
			}
			if (attackPos >= attackPath.getLength()) {
				Vector2i pos = attackPath.getNode(attackPos - 1);
				world.damageUnits(pos.x, pos.y, this, attackSeed, getCurrentWeapon());
				attackPos = -1;
				attackPath = null;
			}
		}

		weaponFrame.update();
	}

	public void render(int offX, int offY, int color) {
		if (!world.isVisible(world.getPlayer(), x, y))
			return;

		if (isDestroyed())
			Drawer.draw(x - offX, y - offY, Glyphs.CROSS, color);
		else
			Drawer.draw(x - offX, y - offY, file.getGlyph(), color);

		if (isShooting()) {
			Vector2i pos = attackPath.getNode(attackPos);
			Drawer.draw(pos.x - offX, pos.y - offY, getCurrentWeapon().getGlyph(), 0xF52);
		}
	}

	public void renderPaths(int offX, int offY) {
		if (path != null)
			for (int i = 0; i < path.getLength(); i++) {
				Node pos = path.getNode(i);
				Drawer.draw(pos.x - offX, pos.y - offY, Glyphs.SPACE,
						Drawer.IGNORE_FOREGROUND | Drawer.IGNORE_GLYPH | 0x334000);
			}

		if (attackPath != null && !isShooting()) {
			for (int i = 0; i < attackPath.getLength(); i++) {
				Vector2i pos = attackPath.getNode(i);
				int color = (!attackPath.hasCollision() || i < attackPath.getCollisionIndex()) ? 0x520000 : 0x600000;
				Drawer.draw(pos.x - offX, pos.y - offY, Glyphs.SPACE,
						Drawer.IGNORE_FOREGROUND | Drawer.IGNORE_GLYPH | color);
			}

			float radius = getCurrentWeapon().getRadius();
			if (radius > 0f) {
				Vector2i pos = attackPath.getNode(attackPath.getCollisionIndex());
				int range = 1 + (int) radius;
				int ix, iy;
				float dist;
				for (iy = pos.y - range; iy <= pos.y + range; iy++)
					for (ix = pos.x - range; ix <= pos.x + range; ix++) {
						if (ix == pos.x && iy == pos.y)
							continue;
						dist = Maths.dist(pos.x, pos.y, ix, iy);
						if (dist <= radius)
							Drawer.draw(ix - offX, iy - offY, Glyphs.SPACE,
									Drawer.IGNORE_FOREGROUND | Drawer.IGNORE_GLYPH | 0x520000);
					}
			}
		}
	}

	public void renderAttackRange(int offX, int offY) {
		int range = (int) getCurrentWeapon().getRange();
		Drawer.drawCircle(x - offX, y - offY, range, Glyphs.SPACE, 0x01630000);
	}

	public void renderWeaponFrame() {
		weaponFrame.render();
	}

	public void damage(Unit source, float chance) {
		if (isDestroyed())
			return;

		if (chance > source.getCurrentWeapon().getAccuracy()) {
			world.addAction()//
					.addText("<" + source.getUnitFile().getName() + ">", source.getPlayer().getColor())//
					.addText(" misses ", -1)//
					.addText("<" + file.getName() + ">", player.getColor());
			return;
		}

		float armorLeft = units * getArmor() - source.getUnits() * source.getCurrentWeapon().getPiercing();
		if (armorLeft < 0f)
			armorLeft = 0f;
		float damageTaken = source.getUnits() * source.getCurrentWeapon().getDamage() - armorLeft;
		if (damageTaken < 0f)
			damageTaken = 0f;
		world.addAction()//
				.addText("<" + source.getUnitFile().getName() + ">", source.getPlayer().getColor())//
				.addText(" hits ", -1)//
				.addText("<" + file.getName() + ">", player.getColor())//
				.addText(" (" + (int) -damageTaken + ")", -1);

		int killed = 0;
		int maxUnits = units;
		float remainingHealth = health;

		while (damageTaken >= remainingHealth && maxUnits > 0) {
			maxUnits--;
			killed++;
			damageTaken -= remainingHealth;
			remainingHealth = getMaxHealth();
		}

		if (maxUnits > 0 && damageTaken > 0) {
			remainingHealth -= damageTaken;
		} else if (maxUnits <= 0) {
			maxUnits = 0;
			remainingHealth = 0;
		}

		if (killed > 0)
			world.addAction()//
					.addText("<" + source.getUnitFile().getName() + ">", source.getPlayer().getColor())//
					.addText(" destroys " + killed + " ", -1)//
					.addText("<" + file.getName() + ">", player.getColor());

		units = maxUnits;
		health = remainingHealth;

		if (isDestroyed())
			onNewTurn();
	}

	public void createPath(int x, int y) {
		if (isDestroyed())
			return;

		moving = false;
		if (path == null)
			path = Pathfinder.getUnitPath(world, player, this.x, this.y, x, y);
		else {
			if (x == path.getEndX() && y == path.getEndY()) {
				moving = true;
			} else if (path.contains(x, y))
				path.shrink(x, y);
			else
				path = null;
		}
	}

	public void resetPath() {
		path = null;
	}

	public void createAttackPath(int x, int y) {
		createAttackPath(x, y, RNG.randomLong());
	}

	public void createAttackPath(int x, int y, long seed) {
		if (attacks[currentWeapon] <= 0)
			return;
		if (attackPath != null) {
			if (x == attackPath.getEndX() && y == attackPath.getEndY()) {
				moves = Maths.max(moves - getCurrentWeapon().getPenalty(), 0f);
				attacks[currentWeapon]--;
				attackSeed = seed;
				attackPos = 0;
				if (player == world.getPlayer())
					world.createProjectile(player.getIndex(), getIndex(), currentWeapon, x, y, attackSeed, true);
			} else
				attackPath = null;
			return;
		}
		moving = false;
		attackPath = Pathfinder.getAttackPath(world, player, getCurrentWeapon(), this.x, this.y, x, y);
		attackPos = -1;
	}

	public void resetAttackPath() {
		attackPath = null;
		attackPos = -1;
	}

	public void switchMoving() {
		moving = !moving;
		resetAttackPath();
	}

	public boolean canShoot() {
		return attacks[currentWeapon] > 0;
	}

	public boolean isDestroyed() {
		return units <= 0;
	}

	public float getHealth() {
		return health;
	}

	public float getMoves() {
		return moves;
	}

	public int getUnits() {
		return units;
	}

	public boolean isMoving() {
		return moving;
	}

	public boolean isShooting() {
		return attackPos != -1;
	}

	public float getMaxHealth() {
		return file.getHealth();
	}

	public float getMaxMoves() {
		return file.getMoves();
	}

	public float getArmor() {
		return file.getArmor();
	}

	public int getVisibilityRadius() {
		return file.getVisibility();
	}

	public int getMaxUnits() {
		return file.getMaxUnits();
	}

	public WeaponFile getCurrentWeapon() {
		return file.getWeapons()[currentWeapon];
	}

	public int getCurrentWeaponIndex() {
		return currentWeapon;
	}

	public void setCurrentWeapon(int weapon) {
		this.currentWeapon = weapon;
	}

	public int getAttacks(int weapon) {
		return attacks[weapon];
	}

	public UnitFile getUnitFile() {
		return file;
	}

	public Player getPlayer() {
		return player;
	}

	public int getIndex() {
		return player.getUnitIndex(this);
	}

	public void setPos(int x, int y) {
		this.x = x;
		this.y = y;
	}

	public int getX() {
		return x;
	}

	public int getY() {
		return y;
	}

}
