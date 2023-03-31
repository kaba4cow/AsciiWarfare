package kaba4cow.warfare;

import java.util.prefs.Preferences;

public class Settings {

	private static Preferences preferences;
	private static String serverAddress;
	private static String serverPort;
	private static boolean fullscreen;
	private static int worldSize;
	private static int worldSeason;

	private Settings() {

	}

	public static void init() {
		preferences = Preferences.userNodeForPackage(Settings.class);
		fullscreen = preferences.getBoolean("fullscreen", true);
		serverAddress = preferences.get("ip", "");
		serverPort = preferences.get("port", "");
		worldSize = preferences.getInt("size", 0);
		worldSeason = preferences.getInt("season", 2);
	}

	public static void save() {
		preferences.putBoolean("fullscreen", fullscreen);
		preferences.put("ip", serverAddress);
		preferences.put("port", serverPort);
		preferences.putInt("size", worldSize);
		preferences.putInt("season", worldSeason);
	}

	public static boolean isFullscreen() {
		return fullscreen;
	}

	public static void setFullscreen(boolean fullscreen) {
		Settings.fullscreen = fullscreen;
	}

	public static String getServerAddress() {
		return serverAddress;
	}

	public static String getServerPort() {
		return serverPort;
	}

	public static void setServerInfo(String serverAddress, String serverPort) {
		Settings.serverAddress = serverAddress;
		Settings.serverPort = serverPort;
	}

	public static int getWorldSize() {
		return worldSize;
	}

	public static int getWorldSeason() {
		return worldSeason;
	}

	public static void setWorldInfo(int worldSize, int worldSeason) {
		Settings.worldSize = worldSize;
		Settings.worldSeason = worldSeason;
	}

}
