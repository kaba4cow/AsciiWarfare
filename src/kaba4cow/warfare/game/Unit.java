package kaba4cow.warfare.game;

import kaba4cow.ascii.core.Engine;
import kaba4cow.ascii.drawing.Drawer;
import kaba4cow.ascii.drawing.Glyphs;
import kaba4cow.ascii.toolbox.Colors;
import kaba4cow.ascii.toolbox.files.DataFile;
import kaba4cow.ascii.toolbox.maths.Maths;
import kaba4cow.ascii.toolbox.maths.vectors.Vector2i;
import kaba4cow.ascii.toolbox.rng.RNG;
import kaba4cow.ascii.toolbox.utils.StringUtils;
import kaba4cow.warfare.files.UnitFile;
import kaba4cow.warfare.files.WeaponFile;
import kaba4cow.warfare.gui.game.WeaponFrame;
import kaba4cow.warfare.pathfinding.AttackPath;
import kaba4cow.warfare.pathfinding.MovePath;
import kaba4cow.warfare.pathfinding.Node;
import kaba4cow.warfare.pathfinding.Pathfinder;

public class Unit {

	private final World world;
	private final Player player;

	private int x;
	private int y;

	private final UnitFile file;

	private int attackPos = -1;

	private MovePath movePath = null;
	private AttackPath attackPath = null;
	private long attackSeed;
	private boolean moving = false;

	private float moveDelayTime;
	private float attackDelayTime;

	private float health;
	private int units;
	private float moves;

	private int currentWeapon;
	private boolean[] attacks;

	private WeaponFrame weaponFrame;

	public Unit(World world, Village village, Player player, UnitFile file) {
		this.world = world;
		this.player = player;
		this.file = file;
		do {
			x = RNG.randomInt(village.x - village.radius, village.x + village.radius);
			y = RNG.randomInt(village.y - village.radius, village.y + village.radius);
		} while (world.getPenalty(x, y) > 1f || world.isObstacle(x, y));

		this.attacks = new boolean[file.getWeapons().length];

		this.currentWeapon = 0;
		this.moveDelayTime = 0f;
		this.attackDelayTime = 0f;

		this.health = getMaxHealth();
		this.units = 1;
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
		this.attacks = new boolean[file.getWeapons().length];
		for (int i = 0; i < attacks.length; i++)
			attacks[i] = data.getInt(6 + i) != 0;

		this.currentWeapon = 0;
		this.moveDelayTime = 0f;
		this.attackDelayTime = 0f;

		this.weaponFrame = new WeaponFrame(this);
		player.updateVisibility(x, y, getVisibilityRadius());
	}

	public void save(DataFile data) {
		data.clear();
		data.setString(file.getID());
		data.setInt(x).setInt(y);
		data.setFloat(health);
		data.setInt(units);
		data.setFloat(moves);
		for (int i = 0; i < attacks.length; i++)
			data.setInt(attacks[i] ? 1 : 0);
	}

	public void onNewTurn() {
		if (isDestroyed()) {
			moves = 0;
			for (int i = 0; i < attacks.length; i++)
				attacks[i] = false;
			movePath = null;
			attackPath = null;
		} else {
			moves = getMaxMoves();
			for (int i = 0; i < attacks.length; i++)
				attacks[i] = true;
		}
		moving = false;
	}

	public void update(float dt) {
		if (movePath == null || moves <= 0)
			moving = false;

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
		} else {
			moveDelayTime += dt;
			if (moving && moveDelayTime >= (world.getPenalty(x, y) + 1f) / file.getMoves()) {
				moveDelayTime = 0f;
				Node nextNode = movePath.move();
				if (nextNode == null)
					movePath = null;
				else {
					move(nextNode.x, nextNode.y);
					world.moveUnit(player.getIndex(), getIndex(), x, y, true);
				}
			}
		}

		weaponFrame.update();
	}

	public void render(int offX, int offY, int color) {
		if (isDestroyed() || !world.isVisible(world.getPlayer(), x, y))
			return;

		if (player == world.getPlayer() && this == player.getCurrentUnit()) {
			float mod = (Maths.SQRT2 * Engine.getElapsedTime()) % 4f;
			color = Colors.blendForeground(0, color, mod % 2f);
			if (mod < 1f) {
				float radius = mod * getVisibilityRadius();
				int brightness = Colors.blend(0x040000, 0x010000, mod);
				if (radius > 1f)
					Drawer.drawCircle(x - offX, y - offY, (int) radius, ' ',
							Drawer.IGNORE_FOREGROUND | Drawer.IGNORE_GLYPH | brightness);
			}
		}

		Drawer.draw(x - offX, y - offY, file.getTypeGlyph(), color);

		if (isShooting()) {
			Vector2i pos = attackPath.getNode(attackPos);
			Drawer.draw(pos.x - offX, pos.y - offY, getCurrentWeapon().getGlyph(), 0xD52);
		}
	}

	public void renderPaths(int offX, int offY) {
		if (movePath != null) {
			float movesLeft = moves;
			int nextX, nextY;
			int prevX = x;
			int prevY = y;
			int color;
			for (int i = 0; i < movePath.getLength(); i++) {
				nextX = movePath.getNode(i).x;
				nextY = movePath.getNode(i).y;
				color = movesLeft > 0f ? 0x223000 : 0x211000;
				Drawer.draw(nextX - offX, nextY - offY, Glyphs.SPACE,
						Drawer.IGNORE_FOREGROUND | Drawer.IGNORE_GLYPH | color);
				movesLeft -= getPenalty(prevX, prevY, nextX, nextY);
				prevX = nextX;
				prevY = nextY;
			}
		}

		if (attackPath != null && !isShooting()) {
			for (int i = 0; i < attackPath.getLength(); i++) {
				Vector2i pos = attackPath.getNode(i);
				int color = (!attackPath.hasCollision() || i < attackPath.getCollisionIndex()) ? 0x520000 : 0x600000;
				Drawer.draw(pos.x - offX, pos.y - offY, Glyphs.SPACE,
						Drawer.IGNORE_FOREGROUND | Drawer.IGNORE_GLYPH | color);
			}

			int radius = getCurrentWeapon().getRadius();
			if (radius > 0) {
				Vector2i pos = attackPath.getNode(attackPath.getCollisionIndex());
				int ix, iy;
				float dist;
				for (iy = pos.y - radius; iy <= pos.y + radius; iy++)
					for (ix = pos.x - radius; ix <= pos.x + radius; ix++) {
						if (ix == pos.x && iy == pos.y)
							continue;
						dist = Maths.dist(pos.x, pos.y, ix, iy);
						if (dist <= radius)
							Drawer.draw(ix - offX, iy - offY, Glyphs.SPACE,
									Drawer.IGNORE_FOREGROUND | Drawer.IGNORE_GLYPH | 0x630000);
					}
			}
		}
	}

	public void renderAttackRange(int offX, int offY) {
		int range = (int) getCurrentWeapon().getRange();
		Drawer.drawCircle(x - offX, y - offY, range, Glyphs.SPACE,
				Drawer.IGNORE_FOREGROUND | Drawer.IGNORE_GLYPH | 0x630000);
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
				.addText(" (" + StringUtils.format1(-damageTaken) + ")", -1);

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

		if (killed > 0) {
			world.addAction()//
					.addText("<" + source.getUnitFile().getName() + ">", source.getPlayer().getColor())//
					.addText(" destroys " + killed + " ", -1)//
					.addText("<" + file.getName() + ">", player.getColor());
			source.getPlayer().onUnitKilled(this, killed);
		}

		units = maxUnits;
		health = remainingHealth;

		if (isDestroyed())
			player.removeDestroyedUnits();
	}

	public void createPath(int x, int y) {
		if (isDestroyed())
			return;

		moving = false;
		if (movePath == null)
			movePath = Pathfinder.getMovePath(world, player, this.x, this.y, x, y);
		else {
			if (x == movePath.getEndX() && y == movePath.getEndY()) {
				moving = true;
			} else if (movePath.contains(x, y))
				movePath.shrink(x, y);
			else
				movePath = null;
		}
	}

	public void createAttackPath(int x, int y) {
		createAttackPath(x, y, RNG.randomLong());
	}

	public void createAttackPath(int x, int y, long seed) {
		if (!attacks[currentWeapon])
			return;
		if (attackPath != null) {
			if (x == attackPath.getEndX() && y == attackPath.getEndY()) {
				moves = Maths.max(moves - getCurrentWeapon().getPenalty(), 0f);
				attacks[currentWeapon] = false;
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

	public void move(int x, int y) {
		float penalty = getPenalty(this.x, this.y, x, y);
		if (Float.isInfinite(penalty))
			return;

		this.x = x;
		this.y = y;
		player.updateVisibility(x, y, getVisibilityRadius());
		moves = Maths.max(moves - penalty, 0f);
	}

	public float getPenalty(int x0, int y0, int x1, int y1) {
		int elevation = Maths.dist(world.getElevation(x0, y0), world.getElevation(x1, y1));
		if (elevation > 1)
			return Float.POSITIVE_INFINITY;
		return (1f + elevation) * (1f + world.getPenalty(x1, y1));
	}

	public void switchMoving() {
		moving = !moving;
		resetAttackPath();
	}

	public void addUnits(int units) {
		this.units += units;
	}

	public boolean canJoin(Unit unit) {
		return this != unit && file == unit.file && Maths.dist(x, y, unit.x, unit.y) < 1.5f;
	}

	public boolean canShoot() {
		return attacks[currentWeapon];
	}

	public boolean canShoot(int weapon) {
		return attacks[weapon];
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

	public WeaponFile getCurrentWeapon() {
		return file.getWeapons()[currentWeapon];
	}

	public int getCurrentWeaponIndex() {
		return currentWeapon;
	}

	public void setCurrentWeapon(int weapon) {
		this.currentWeapon = weapon;
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
