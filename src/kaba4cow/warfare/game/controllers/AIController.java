package kaba4cow.warfare.game.controllers;

import java.util.ArrayList;
import java.util.HashMap;

import kaba4cow.ascii.toolbox.maths.Maths;
import kaba4cow.ascii.toolbox.maths.vectors.Vector2i;
import kaba4cow.ascii.toolbox.rng.RNG;
import kaba4cow.warfare.files.UnitFile;
import kaba4cow.warfare.files.WeaponFile;
import kaba4cow.warfare.game.Unit;
import kaba4cow.warfare.game.Village;
import kaba4cow.warfare.pathfinding.Pathfinder;
import kaba4cow.warfare.states.State;

public class AIController extends Controller {

	private ArrayList<UnitAI> ais;

	private int currentUnit;

	private int targetPrice;

	public AIController() {
		super(0xF54);
		this.ais = null;
		this.currentUnit = -1;
		resetTargetPrice();
	}

	@Override
	public void update(float dt) {
		if (currentUnit == -1)
			onNewTurn();
		else if (player.getCurrentUnit().isMoving() || player.getCurrentUnit().isShooting())
			return;

		player.setCurrentUnit(currentUnit);
		State.PROGRESS = (float) currentUnit / (float) ais.size();

		UnitAI ai = ais.get(currentUnit);
		if (ai.unit.isDestroyed())
			ais.remove(ai);
		else if (ai.update(world.getVillages(), world.getPlayer().getUnits())) {
			currentUnit++;
			if (currentUnit >= player.getUnits().size()) {
				currentUnit = -1;
				world.newTurn(player.getIndex(), false);
			}
		}
	}

	private void onNewTurn() {
		if (ais == null) {
			ais = new ArrayList<>();
			ArrayList<Unit> list = player.getUnits();
			for (int i = 0; i < list.size(); i++)
				ais.add(new UnitAI(list.get(i), i));
		}
		player.setCurrentUnit(0);
		currentUnit = 0;

		processHire();
	}

	private void resetTargetPrice() {
		targetPrice = RNG.randomInt(UnitFile.getMinPrice(), UnitFile.getMaxPrice() + 1);
	}

	private void processHire() {
		if (player.getCash() < targetPrice || player.getClosestVillage() == null)
			return;

		float minPriceDist = Float.POSITIVE_INFINITY;
		UnitFile unit = null;
		HashMap<String, UnitFile> units = UnitFile.getFiles();
		for (String id : units.keySet()) {
			if (!player.isUnitAvailable(units.get(id)))
				continue;
			int price = units.get(id).getPrice();
			float priceDist = Maths.dist(targetPrice, price);
			if (priceDist < minPriceDist) {
				minPriceDist = priceDist;
				unit = units.get(id);
			}
		}

		if (unit == null)
			return;

		Unit newUnit = player.addUnit(unit.getID(), -1, -1);
		ais.add(new UnitAI(newUnit, player.getUnits().size()));
		player.removeCash(unit.getPrice());
		resetTargetPrice();
	}

	private void processUnitAttack(Unit unit, Unit target, int weapon) {
		if (target == null)
			return;
		unit.setCurrentWeapon(weapon);
		unit.createAttackPath(target.getX(), target.getY());
		if (!unit.isShooting())
			unit.createAttackPath(target.getX(), target.getY());
		if (!unit.isShooting())
			unit.createAttackPath(target.getX(), target.getY());
	}

	private void processUnitMove(Unit unit, Vector2i pos) {
		if (pos == null)
			return;
		player.setIgnoreVisibility(true);
		unit.createPath(pos.x, pos.y);
		if (!unit.isMoving())
			unit.createPath(pos.x, pos.y);
		if (!unit.isMoving())
			unit.createPath(pos.x, pos.y);
		player.setIgnoreVisibility(false);
	}

	private Unit getClosestUnit(Unit source) {
		ArrayList<Unit> list = world.getPlayer().getUnits();
		Unit target = null;
		float minDist = Float.POSITIVE_INFINITY;
		for (int i = 0; i < list.size(); i++) {
			Unit current = list.get(i);
			float dist = Maths.dist(source.getX(), source.getY(), current.getX(), current.getY());
			if (dist < minDist) {
				minDist = dist;
				target = current;
			}
		}
		return target;
	}

	private Unit getUnitTarget(Unit source, WeaponFile weapon) {
		Unit target = null;

		float maxDist = weapon.getRange();

		float minValue = Float.POSITIVE_INFINITY;
		float value, dist, health;
		ArrayList<Unit> units = world.getPlayer().getUnits();
		for (int i = 0; i < units.size(); i++) {
			Unit unit = units.get(i);
			dist = Maths.dist(source.getX(), source.getY(), unit.getX(), unit.getY());
			if (dist > maxDist)
				continue;
			health = unit.getUnits() * unit.getMaxHealth() + unit.getHealth();
			value = health + dist;

			if (value < minValue) {
				minValue = value;
				target = unit;
			}
		}

		return target;
	}

	private Vector2i getVillagePoint(Unit unit, Village village) {
		if (village == null)
			return null;

		int x, y;
		int range = village.radius;
		float maxDistSq = range * range;
		float distSq;

		distSq = Maths.distSq(unit.getX(), unit.getY(), village.x, village.y);
		if (distSq < maxDistSq)
			return new Vector2i(unit.getX(), unit.getY());

		for (int attempt = 0; attempt < 32; attempt++) {
			x = village.x + RNG.randomInt(-range, range);
			y = village.y + RNG.randomInt(-range, range);

			if (!world.isObstacle(x, y)) {
				distSq = Maths.distSq(x, y, village.x, village.y);
				if (distSq < maxDistSq)
					return new Vector2i(x, y);
			}
		}

		return null;
	}

	private Vector2i getUnitAttackPoint(Unit unit, WeaponFile weapon, Unit target) {
		int x = unit.getX();
		int y = unit.getY();
		int range = (int) weapon.getRange();
		float maxDistSq = weapon.getRange() * weapon.getRange();
		float distSq;

		for (int i = 0; i < 32; i++) {
			x = target.getX() + RNG.randomInt(-range, range);
			y = target.getY() + RNG.randomInt(-range, range);

			if (!world.isObstacle(x, y)
					&& Pathfinder.getAttackPath(world, player, weapon, x, y, target.getX(), target.getY()) != null) {
				distSq = Maths.distSq(x, y, target.getX(), target.getY());
				if (distSq < maxDistSq)
					return new Vector2i(x, y);
			}
		}

		return null;
	}

	@Override
	public void render(int offX, int offY) {

	}

	private class UnitAI {

		private final Unit unit;
		private final boolean defender;

		private Village targetVillage;
		private Unit targetUnit;

		private int step;
		private int weaponStep;

		public UnitAI(Unit unit, int index) {
			this.unit = unit;
			this.defender = index % 3 != 0;
			this.targetVillage = null;
			this.targetUnit = null;
			this.step = 0;
			this.weaponStep = 0;
		}

		public boolean update(ArrayList<Village> villages, ArrayList<Unit> units) {
			WeaponFile[] weapons = unit.getUnitFile().getWeapons();
			int maxWeaponStep = weapons.length;

			if (defender && !villages.isEmpty()) {
				if (step == 0) {
					if (weaponStep < maxWeaponStep && unit.getAttacks(weaponStep) > 0) {
						processUnitAttack(unit, getUnitTarget(unit, weapons[weaponStep]), weaponStep);
						weaponStep++;
					}
				} else if (step == 1) {
					if (targetVillage == null || targetVillage.getHouses() == 0
							|| targetVillage.getTotalUnits(world, player) > 2) {
						float minDistSq = Float.POSITIVE_INFINITY;
						for (int i = 0; i < villages.size(); i++) {
							Village current = villages.get(i);
							int total = current.getTotalUnits(world, player);
							if (total > 2)
								continue;
							float distSq = Maths.distSq(unit.getX(), unit.getY(), current.x, current.y);
							if (distSq < minDistSq) {
								minDistSq = distSq;
								targetVillage = current;
							}
						}
					}
					if (targetVillage != null) {
						Vector2i point = getVillagePoint(unit, targetVillage);
						processUnitMove(unit, point);
					}
					weaponStep = maxWeaponStep;
				} else if (step == 2) {
					if (weaponStep < maxWeaponStep && unit.getAttacks(weaponStep) > 0)
						processUnitAttack(unit, getUnitTarget(unit, weapons[weaponStep]), weaponStep);
					weaponStep++;
				}
			} else if (!units.isEmpty()) {
				if (step == 0) {
					targetUnit = getClosestUnit(unit);
					if (targetUnit != null && weaponStep < maxWeaponStep && unit.getAttacks(weaponStep) > 0) {
						processUnitAttack(unit, targetUnit, weaponStep);
						weaponStep++;
					}
				} else if (step == 1) {
					if (targetUnit != null) {
						player.setIgnoreVisibility(true);
						Vector2i pos = null;
						for (int i = 0; i < weapons.length; i++) {
							Vector2i current = getUnitAttackPoint(unit, weapons[i], targetUnit);
							if (current != null)
								pos = current;
						}
						processUnitMove(unit, pos);
						player.setIgnoreVisibility(false);
					}
					weaponStep = maxWeaponStep;
				} else if (step == 2) {
					if (targetUnit != null && weaponStep < maxWeaponStep && unit.getAttacks(weaponStep) > 0) {
						processUnitAttack(unit, targetUnit, weaponStep);
						weaponStep++;
					}
				}
			}

			if (weaponStep >= maxWeaponStep) {
				step++;
				weaponStep = 0;
			}
			if (step > 2) {
				step = 0;
				weaponStep = 0;
				return true;
			} else
				return false;
		}

	}

}
