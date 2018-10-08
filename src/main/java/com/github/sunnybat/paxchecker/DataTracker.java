package com.github.sunnybat.paxchecker;

/**
 *
 * @author SunnyBat
 */
public class DataTracker {

	private static long dataUsed;

	/**
	 * Adds an amount of data (in bytes) used by the program. This should be called whenever a network
	 * connection is made.
	 *
	 * @param data The amount of data (in bytes) to add to the total data used
	 */
	public static synchronized void addDataUsed(long data) {
		dataUsed += data;
	}

	/**
	 * Gets the amount of data (in bytes) used by the program.
	 *
	 * @return The amount of data (in bytes) used by the program
	 */
	public static synchronized long getDataUsed() {
		return dataUsed;
	}

	/**
	 * Gets the amount of data in megabytes used by the program. Note that the double only extends out
	 * two decimal places.
	 *
	 * @return The amount of data in megabytes used by the program
	 */
	public static synchronized double getDataUsedMB() {
		return (double) ((int) ((double) getDataUsed() / 1024 / 1024 * 100)) / 100; // *100 to make the double have two extra numbers, round with typecasting to integer, then divide that by 100 and typecast to double to get a double with two decimal places
	}
}
