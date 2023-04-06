package kaba4cow.warfare.game.world;

import java.util.ArrayList;
import java.util.Stack;

import kaba4cow.ascii.toolbox.maths.Easing;
import kaba4cow.ascii.toolbox.maths.Maths;
import kaba4cow.ascii.toolbox.maths.vectors.Vector2f;
import kaba4cow.ascii.toolbox.maths.vectors.Vector2i;
import kaba4cow.ascii.toolbox.maths.vectors.Vectors;
import kaba4cow.ascii.toolbox.noise.Noise;
import kaba4cow.ascii.toolbox.rng.RNG;
import kaba4cow.ascii.toolbox.rng.RandomLehmer;
import kaba4cow.warfare.Game;
import kaba4cow.warfare.files.BiomeFile;
import kaba4cow.warfare.files.TerrainFile;
import kaba4cow.warfare.files.VegetationFile;
import kaba4cow.warfare.game.Village;
import kaba4cow.warfare.states.State;

public class Generator {

	private static final int MIN_BIOME_AREA = 64;
	private static final int MIN_PATH_AREA = 32;

	private static final int PATH_RADIUS = 3;
	private static final int VILLAGE_PATH_RADIUS = 2;

	private static final float PATH_FREQ = 0.058f;
	private static final float RIVER_FREQ = 0.015f;
	private static final float TEMPERATURE_FREQ = 0.012f;

	private static final float RIVER_THRESHOLD = 0.03f;

	private static final int TEMP = 0xFF;
	private static final int RIVER = 0x00;
	private static final int TERRAIN = 0x01;
	private static final int VEGETATION = 0x02;
	private static final int ROAD = 0x03;
	private static final int HOUSE = 0x04;

	private final int season;

	private final int[][] biomeIndexMap;
	private final int[][] terrainTypeMap;
	private final int[][] terrainIndexMap;
	private final int[][] vegetationIndexMap;
	private final float[][] temperatureValueMap;
	private final ArrayList<Village> villages;

	private final RNG rng;
	private final Noise noise;

	public Generator(int season, long seed) {
		this.season = season;
		this.rng = new RandomLehmer(seed);
		this.noise = new Noise(seed);
		this.biomeIndexMap = new int[Game.WORLD_SIZE][Game.WORLD_SIZE];
		this.terrainTypeMap = new int[Game.WORLD_SIZE][Game.WORLD_SIZE];
		this.terrainIndexMap = new int[Game.WORLD_SIZE][Game.WORLD_SIZE];
		this.vegetationIndexMap = new int[Game.WORLD_SIZE][Game.WORLD_SIZE];
		this.temperatureValueMap = new float[Game.WORLD_SIZE][Game.WORLD_SIZE];
		this.villages = new ArrayList<>();
	}

	public ArrayList<Village> populate(TerrainTile[][] terrainMap, VegetationTile[][] vegetationMap,
			float[][] temperatureMap) {
		ArrayList<BiomeFile> biomes = BiomeFile.getBiomes();

		int x, y, terrainType, biomeIndex, terrainIndex, vegetationIndex;
		float temperature;

		for (y = 0; y < Game.WORLD_SIZE; y++)
			for (x = 0; x < Game.WORLD_SIZE; x++) {
				biomeIndex = biomeIndexMap[x][y] % biomes.size();
				BiomeFile biome = biomeIndex == -1 ? BiomeFile.getRiver() : biomes.get(biomeIndex);

				String[] terrainTiles = biome.getTerrain();
				String[] vegetationTiles = biome.getVegetation();

				terrainType = terrainTypeMap[x][y];
				TerrainFile terrainFile;
				VegetationFile vegetationFile;
				if (terrainType == RIVER || biomeIndexMap[x][y] == -1 || terrainIndexMap[x][y] == -1) {
					terrainFile = TerrainFile.getWater();
					vegetationFile = null;
				} else if (terrainType == ROAD) {
					terrainFile = TerrainFile.getRoad();
					vegetationFile = null;
				} else if (terrainType == HOUSE) {
					terrainFile = TerrainFile.getRuins();
					vegetationFile = VegetationFile.getBuilding();
				} else {
					terrainIndex = (terrainIndexMap[x][y] + biomeIndex) % terrainTiles.length;
					terrainFile = TerrainFile.get(terrainTiles[terrainIndex]);
					if (terrainType == VEGETATION && vegetationTiles.length > 0
							&& rng.nextFloat(0f, 1f) < biome.getVegetationDensity()) {
						vegetationIndex = (vegetationIndexMap[x][y] + terrainIndex) % vegetationTiles.length;
						vegetationFile = VegetationFile.get(vegetationTiles[vegetationIndex]);
					} else
						vegetationFile = null;
				}
				temperature = temperatureValueMap[x][y];
				temperatureMap[x][y] = temperature;
				terrainMap[x][y] = new TerrainTile(terrainFile, biome, temperature);
				if (vegetationFile == null || vegetationFile.isDestroyed(temperature))
					vegetationMap[x][y] = null;
				else
					vegetationMap[x][y] = new VegetationTile(vegetationFile, temperature);
			}

		State.PROGRESS = 0.5f;

		return villages;
	}

	public void generate() {
		int x, y, ix, iy;

		final float pathOffset = rng.nextFloat(-1000f, 1000f);
		final float riverOffset = rng.nextFloat(-1000f, 1000f);
		final float temperatureOffset = rng.nextFloat(-1000f, 1000f);

		float minTemperature = Maths.limit(0.25f * season + rng.nextFloat(-0.1f, 0.1f));
		float maxTemperature = Maths.limit(minTemperature + 0.25f + rng.nextFloat(-0.1f, 0.1f));

		for (y = 0; y < Game.WORLD_SIZE; y++)
			for (x = 0; x < Game.WORLD_SIZE; x++) {
				float temperature_x = TEMPERATURE_FREQ * x - temperatureOffset;
				float temperature_y = TEMPERATURE_FREQ * y - temperatureOffset;
				float temperature = noise.getCombinedValue(temperature_x, temperature_y, 2);
				temperature = Maths.map(temperature, 0f, 1f, minTemperature, maxTemperature);
				temperature = Easing.EASE_IN_OUT_SINE.getValue(temperature);

				float river1_x = RIVER_FREQ * x + riverOffset;
				float river1_y = RIVER_FREQ * y + riverOffset;
				float river2_x = 0.5f * RIVER_FREQ * x - riverOffset;
				float river2_y = 0.5f * RIVER_FREQ * y - riverOffset;
				float river1 = noise.getCombinedValue(river1_x, river1_y, 5);
				float river2 = noise.getCombinedValue(river2_x, river2_y, 5);
				float river = Maths.blend(river1, river2, 0.75f);
				river = Maths.abs(Noise.to11(river));

				temperatureValueMap[x][y] = temperature;
				if (river < RIVER_THRESHOLD)
					terrainTypeMap[x][y] = RIVER;
				else
					terrainTypeMap[x][y] = VEGETATION;
			}

		State.PROGRESS = 0.1f;

		for (y = 0; y < Game.WORLD_SIZE; y++)
			for (x = 0; x < Game.WORLD_SIZE; x++) {
				if (terrainTypeMap[x][y] == RIVER || terrainTypeMap[x][y] == TEMP)
					continue;
				int area = floodFill(terrainTypeMap, x, y, VEGETATION, TEMP);
				if (area <= MIN_BIOME_AREA)
					floodFill(terrainTypeMap, x, y, TEMP, RIVER);
			}
		for (y = 0; y < Game.WORLD_SIZE; y++)
			for (x = 0; x < Game.WORLD_SIZE; x++)
				if (terrainTypeMap[x][y] == TEMP)
					terrainTypeMap[x][y] = VEGETATION;

		populateVoronoiIndices(biomeIndexMap, 3, 1f);
		State.PROGRESS = 0.15f;
		populateVoronoiIndices(terrainIndexMap, 7, 40f);
		State.PROGRESS = 0.2f;
		populateVoronoiIndices(vegetationIndexMap, 7, 50f);
		State.PROGRESS = 0.25f;

		int[][] houseMap_gen = createHouseMap();
		int[][] pathMap_gen = createPathMap();

		State.PROGRESS = 0.3f;

		for (y = 0; y < Game.WORLD_SIZE; y++)
			for (x = 0; x < Game.WORLD_SIZE; x++) {
				if (terrainTypeMap[x][y] == RIVER)
					continue;

				if (houseMap_gen[x][y] == 1)
					terrainTypeMap[x][y] = HOUSE;
				else if (pathMap_gen[x][y] == 1 || houseMap_gen[x][y] == 2)
					terrainTypeMap[x][y] = ROAD;
				else if (terrainTypeMap[x][y] != RIVER && rng.nextFloat(0f, 1f) < 0.9f) {
					float px = -0.5f * PATH_FREQ * x - pathOffset;
					float py = 0.5f * PATH_FREQ * y - pathOffset;
					float value = noise.getCombinedValue(px, py, 2);
					if (value < 0.3f || value > 0.7f)
						terrainTypeMap[x][y] = TERRAIN;
				}
			}

		for (y = 0; y < Game.WORLD_SIZE; y++)
			for (x = 0; x < Game.WORLD_SIZE; x++) {
				if (terrainTypeMap[x][y] != ROAD)
					continue;
				for (iy = y - PATH_RADIUS; iy <= y + PATH_RADIUS; iy++)
					for (ix = x - PATH_RADIUS; ix <= x + PATH_RADIUS; ix++) {
						if (rng.nextBoolean() || ix < 0 || ix >= Game.WORLD_SIZE || iy < 0 || iy >= Game.WORLD_SIZE
								|| terrainTypeMap[ix][iy] == ROAD || terrainTypeMap[ix][iy] == RIVER
								|| terrainTypeMap[ix][iy] == HOUSE)
							continue;
						float dist = Maths.dist(x, y, ix, iy);
						if (dist < PATH_RADIUS)
							terrainTypeMap[ix][iy] = TERRAIN;
					}
			}

		State.PROGRESS = 0.4f;
	}

	private void populateVoronoiIndices(int[][] map, int resolution, float offsetScale) {
		int i, x, y, closest;
		float minDistSq, distSq, offset;

		final float offsetFreq = offsetScale * rng.nextFloat(0.042f, 0.064f);
		final float offsetPower = rng.nextFloat(16f, 20f);
		final int cellSize = Game.WORLD_SIZE / resolution;

		Vector2i[] points = new Vector2i[resolution * resolution];
		i = 0;
		for (y = 0; y < resolution; y++)
			for (x = 0; x < resolution; x++)
				points[i++] = new Vector2i(x * cellSize + rng.nextInt(0, cellSize),
						y * cellSize + rng.nextInt(0, cellSize));

		for (y = 0; y < Game.WORLD_SIZE; y++)
			for (x = 0; x < Game.WORLD_SIZE; x++) {
				if (terrainTypeMap[x][y] == RIVER) {
					map[x][y] = -1;
					continue;
				}
				closest = 0;
				minDistSq = Float.POSITIVE_INFINITY;
				for (i = 0; i < points.length; i++) {
					offset = noise.getCombinedValue(offsetFreq * x, offsetFreq * y, 3);
					offset = offsetPower * Noise.to11(offset);

					distSq = Maths.distSq(x - offset, y + offset, points[i].x, points[i].y);
					if (distSq < minDistSq) {
						minDistSq = distSq;
						closest = i;
					}
				}

				map[x][y] = closest;
			}
	}

	private void processVillage(Village village, final int[][] houses) {
		int numHouses = rng.nextInt(10, 20);

		float pathDensity = rng.nextFloat(0.6f, 1f);

		int minX = village.x - village.radius;
		int maxX = village.x + village.radius;
		int minY = village.y - village.radius;
		int maxY = village.y + village.radius;

		float villageRadiusSq = village.radius * village.radius;

		int maxIterations = 16;
		int iterations;

		ArrayList<Vector2i> houseList = new ArrayList<>();

		int x, y, ix, iy;
		for (int i = 0; i < numHouses; i++) {
			iterations = 0;
			while (true) {
				x = rng.nextInt(minX, maxX);
				y = rng.nextInt(minY, maxY);
				float distSq = Maths.distSq(village.x, village.y, x, y);
				if (distSq > villageRadiusSq)
					continue;

				boolean blocked = false;
				for (ix = x - 1; ix <= x + 1; ix++)
					for (iy = y - 1; iy <= y + 1; iy++) {
						if (ix < 0 || ix >= Game.WORLD_SIZE || iy < 0 || iy >= Game.WORLD_SIZE
								|| terrainTypeMap[ix][iy] == RIVER || houses[ix][iy] == 1) {
							blocked = true;
							break;
						}
					}

				iterations++;
				if (iterations > maxIterations || !blocked)
					break;
			}

			if (iterations <= maxIterations) {
				houseList.add(new Vector2i(x, y));
				houses[x][y] = 1;
			}
		}

		float pathRadiusSq = VILLAGE_PATH_RADIUS * VILLAGE_PATH_RADIUS;
		for (y = 0; y < Game.WORLD_SIZE; y++)
			for (x = 0; x < Game.WORLD_SIZE; x++) {
				if (houses[x][y] != 1)
					continue;
				for (ix = x - VILLAGE_PATH_RADIUS; ix <= x + VILLAGE_PATH_RADIUS; ix++)
					for (iy = y - VILLAGE_PATH_RADIUS; iy <= y + VILLAGE_PATH_RADIUS; iy++) {
						if (ix < 0 || ix >= Game.WORLD_SIZE || iy < 0 || iy >= Game.WORLD_SIZE || houses[ix][iy] != 0
								|| terrainTypeMap[ix][iy] == RIVER || rng.nextFloat(0f, 1f) > pathDensity)
							continue;
						float distSq = Maths.distSq(x, y, ix, iy);
						if (distSq < pathRadiusSq)
							houses[ix][iy] = 2;
					}
			}

		village.setHouses(houseList);
	}

	private int[][] createHouseMap() {
		int x, y, radius;

		final int[][] houses = new int[Game.WORLD_SIZE][Game.WORLD_SIZE];
		for (y = 0; y < Game.WORLD_SIZE; y++)
			for (x = 0; x < Game.WORLD_SIZE; x++)
				houses[x][y] = 0;

		int maxIterations = 32;
		int iterations;

		int numVillages = rng.nextInt(10, 13);
		if (numVillages <= 0)
			numVillages = 1;
		Village village;
		for (int i = 0; i < numVillages; i++) {
			radius = rng.nextInt(3, 7);
			iterations = 0;
			while (true) {
				x = rng.nextInt(radius, Game.WORLD_SIZE - radius);
				y = rng.nextInt(radius, Game.WORLD_SIZE - radius);

				boolean blocked = false;
				for (int j = 0; j < villages.size(); j++) {
					village = villages.get(j);
					if (Maths.distSq(x, y, village.x, village.y) < 16f * Maths.sqr(radius + village.radius)) {
						blocked = true;
						break;
					}
				}

				iterations++;
				if (iterations > maxIterations || !blocked)
					break;
			}
			if (iterations <= maxIterations)
				villages.add(new Village(x, y, radius));
		}

		for (int i = 0; i < villages.size(); i++)
			processVillage(villages.get(i), houses);

		return houses;
	}

	private int[][] createPathMap() {
		int x, y, ix, iy;

		final int[][] paths = new int[Game.WORLD_SIZE][Game.WORLD_SIZE];
		for (y = 0; y < Game.WORLD_SIZE; y++)
			for (x = 0; x < Game.WORLD_SIZE; x++)
				paths[x][y] = 0;

		final int numLeaves = Game.WORLD_SIZE / 10;
		final float minDist = 10f;
		final float maxDist = Game.WORLD_SIZE;

		int i, j;
		float record;
		Vector2f closestDir, dir;
		Branch branch, newBranch, closest;
		Leaf leaf;

		Village centerVillage = villages.get(rng.nextInt(0, villages.size()));

		ArrayList<Leaf> leaves = new ArrayList<>();
		for (i = 0; i < numLeaves; i++) {
			float lx = rng.nextFloat(0, Game.WORLD_SIZE);
			float ly = rng.nextFloat(0, Game.WORLD_SIZE);
			leaf = new Leaf(lx, ly);
			leaves.add(leaf);
		}
		for (i = 0; i < villages.size(); i++) {
			Village village = villages.get(i);
			leaf = new Leaf(village.x, village.y);
			leaves.add(leaf);
		}

		ArrayList<Branch> branches = new ArrayList<>();
		Branch root = new Branch(centerVillage.x, centerVillage.y, rng.nextFloat(-1f, 1f), rng.nextFloat(-1f, 1f));
		branches.add(root);

		while (!leaves.isEmpty()) {
			for (i = leaves.size() - 1; i >= 0; i--) {
				leaf = leaves.get(i);

				closest = null;
				closestDir = null;
				record = Float.POSITIVE_INFINITY;

				for (j = 0; j < branches.size(); j++) {
					branch = branches.get(j);
					dir = Vectors.sub(leaf.pos, branch.pos, 1f, null);
					float d = dir.length();
					if (d < minDist) {
						leaf.reach();
						closest = null;
						break;
					} else if (d < maxDist && (closest == null || d < record)) {
						closest = branch;
						closestDir = dir;
						record = d;
					}
				}
				if (closest != null) {
					closestDir.normalize();
					Vectors.add(closest.dir, closestDir, 1f, closest.dir);
					closest.count++;
				}
			}

			for (i = leaves.size() - 1; i >= 0; i--)
				if (leaves.get(i).reached)
					leaves.remove(i);

			for (i = branches.size() - 1; i >= 0; i--) {
				branch = branches.get(i);
				if (branch.count > 0) {
					branch.dir.scale(1f / branch.count);
					float mag = rng.nextFloat(0.5f, 1f);
					branch.dir.x += rng.nextFloat(-mag, mag);
					branch.dir.y += rng.nextFloat(-mag, mag);
					branch.dir.normalize();
					newBranch = new Branch(branch);
					branches.add(newBranch);
				}
				branch.reset();
			}
		}

		int x0, y0, x1, y1, dx, dy, sx, sy, e, e2;
		for (i = 0; i < branches.size(); i++) {
			branch = branches.get(i);
			if (branch.parent != null) {
				x0 = (int) branch.pos.x;
				y0 = (int) branch.pos.y;
				x1 = (int) branch.parent.pos.x;
				y1 = (int) branch.parent.pos.y;

				dx = Math.abs(x1 - x0);
				dy = Math.abs(y1 - y0);
				sx = x0 < x1 ? 1 : -1;
				sy = y0 < y1 ? 1 : -1;
				e = dx - dy;
				e2 = 0;
				while (true) {
					if (x0 == x1 && y0 == y1)
						break;
					e2 = 2 * e;
					if (e2 > -1 * dy) {
						e -= dy;
						x0 += sx;
					}
					if (e2 < dx) {
						e += dx;
						y0 += sy;
					}
					if (x0 >= 0 && x0 < Game.WORLD_SIZE && y0 >= 0 && y0 < Game.WORLD_SIZE
							&& terrainTypeMap[x0][y0] != RIVER)
						paths[x0][y0] = 1;
				}
			}
		}

		for (y = 0; y < Game.WORLD_SIZE; y++)
			for (x = 0; x < Game.WORLD_SIZE; x++) {
				if (paths[x][y] == 0)
					continue;

				for (iy = y - 1; iy <= y; iy++)
					for (ix = x - 1; ix <= x; ix++) {
						if (ix < 0 || ix >= Game.WORLD_SIZE || iy < 0 || iy >= Game.WORLD_SIZE)
							continue;
						paths[ix][iy] = 1;
					}
			}

		for (y = 0; y < Game.WORLD_SIZE; y++)
			for (x = 0; x < Game.WORLD_SIZE; x++) {
				if (paths[x][y] != 1)
					continue;
				int area = floodFill(paths, x, y, 1, 2);
				if (area <= MIN_PATH_AREA)
					floodFill(paths, x, y, 2, 0);
			}
		for (y = 0; y < Game.WORLD_SIZE; y++)
			for (x = 0; x < Game.WORLD_SIZE; x++)
				if (paths[x][y] == 2)
					paths[x][y] = 1;

		return paths;
	}

	private int floodFill(int[][] data, int startX, int startY, int prevValue, int newValue) {
		if (startX < 0 || startX >= Game.WORLD_SIZE || startY < 0 || startY >= Game.WORLD_SIZE
				|| data[startX][startY] != prevValue)
			return 0;
		boolean[][] visited = new boolean[Game.WORLD_SIZE][Game.WORLD_SIZE];
		int area = 0;
		int x, y, value;
		int[] currentPosition;
		Stack<int[]> stack = new Stack<>();
		stack.push(new int[] { startX, startY });
		while (!stack.empty()) {
			currentPosition = stack.pop();
			x = currentPosition[0];
			y = currentPosition[1];
			if (x < 0 || x >= Game.WORLD_SIZE || y < 0 || y >= Game.WORLD_SIZE || visited[x][y])
				continue;
			value = data[x][y];
			if (value == prevValue) {
				data[x][y] = newValue;
				area++;
				stack.push(new int[] { x - 1, y });
				stack.push(new int[] { x + 1, y });
				stack.push(new int[] { x, y - 1 });
				stack.push(new int[] { x, y + 1 });
			}
			visited[x][y] = true;
		}
		return area;
	}

	private class Leaf {

		public final Vector2f pos;

		public boolean reached;

		public Leaf(float x, float y) {
			this.pos = new Vector2f(x, y);
			this.reached = false;
		}

		public void reach() {
			reached = true;
		}

	}

	private class Branch {

		public final Branch parent;
		public final ArrayList<Branch> children;

		public final Vector2f pos;
		public final Vector2f dir;
		public final Vector2f origDir;

		public int count;

		public Branch(float x, float y, float dirX, float dirY) {
			this.parent = null;
			this.children = new ArrayList<>();
			this.pos = new Vector2f(x, y);
			this.origDir = new Vector2f(dirX, dirY);
			this.dir = new Vector2f(origDir);
		}

		public Branch(Branch parent) {
			this.parent = parent.addChild(this);
			this.children = new ArrayList<>();
			this.pos = parent.next();
			this.origDir = new Vector2f(parent.dir);
			this.dir = new Vector2f(origDir);
		}

		public void reset() {
			count = 0;
			dir.set(origDir);
		}

		public Branch addChild(Branch child) {
			children.add(child);
			return this;
		}

		public Vector2f next() {
			return new Vector2f(pos.x + 5f * dir.x, pos.y + 5f * dir.y);
		}

	}

}
