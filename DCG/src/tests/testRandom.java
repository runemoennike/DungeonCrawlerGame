/**
 * Used for testing the random methods supplied by World.
 */

package tests;

import engine.World;

public class testRandom {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		World w = new World();

		int min = 0;
		int max = 1;
		int tests = 100000000;

		int distribution[] = new int[max - min + 1];

		for (int i = 0; i < tests; i++) {
			int r = w.getRndInt(min, max);
			distribution[r - min]++;
		}

		for (int i = min; i <= max; i++) {
			float perc = (float) (distribution[i - min]) / tests * 100;
			System.out.println("" + i + ": " + perc + "%");
		}
	}

}
