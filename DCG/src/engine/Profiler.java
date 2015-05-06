/**
 * This class is used for profiling the program, used for performance tests.
 */

package engine;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.TreeSet;

import engine.hud.ConsoleLog;

public class Profiler {

	private static HashMap<String, Long> startTime;
	private static HashMap<String, Long> lap;
	private static HashMap<String, Boolean> running;
	private static HashMap<String, Long> total;
	private static long totalTime;
	private static long totalTimeStart;
	private static long cumulativeTime;
	private static int numClocksRunning = 0;
	private static boolean enabled = false;
	private static long initTime;

	public static void init() {
		initTime = System.nanoTime();

		Profiler.startTime = new HashMap<String, Long>();
		Profiler.total = new HashMap<String, Long>();
		Profiler.lap = new HashMap<String, Long>();
		Profiler.running = new HashMap<String, Boolean>();

		lap();
	}

	public static void lap() {
		if (!Profiler.enabled)
			return;

		if (Profiler.lap != null) {
			for (String target : Profiler.lap.keySet()) {
				if (Profiler.running.get(target)) {
					System.out.println("Profiler.lap: Target " + target + " not stopped in all instances.");
				} else {
					if (Profiler.total.containsKey(target)) {
						Profiler.total.put(target, Profiler.total.get(target) + Profiler.lap.get(target));
					} else {
						Profiler.total.put(target, Profiler.lap.get(target));
					}
					Profiler.cumulativeTime += Profiler.lap.get(target);
				}

				Profiler.running.put(target, false);
			}
		}

		Profiler.lap.clear();
	}

	public static void start(String target) {
		if (!Profiler.enabled)
			return;

		if (Profiler.numClocksRunning == 0) {
			Profiler.totalTimeStart = System.nanoTime();
		}

		Profiler.numClocksRunning++;

		if (Profiler.running.containsKey(target) && Profiler.running.get(target)) {
			System.out.println("Profiler.start: Target " + target + " added recursively.");
		} else {
			Profiler.startTime.put(target, System.nanoTime());

			Profiler.running.put(target, true);
		}
	}

	public static void stop(String target) {
		if (!Profiler.enabled)
			return;

		Profiler.numClocksRunning--;

		if (!Profiler.startTime.containsKey(target)) {
			System.out.println("Profiler.stop: Target " + target + " stopped but not started this lap.");
		} else {
			if (Profiler.lap.containsKey(target)) {
				Profiler.lap.put(target, Profiler.lap.get(target) + System.nanoTime() - Profiler.startTime.get(target));
			} else {
				Profiler.lap.put(target, System.nanoTime() - Profiler.startTime.get(target));
			}
			Profiler.running.put(target, false);
		}

		if (Profiler.numClocksRunning == 0) {
			Profiler.totalTime += System.nanoTime() - Profiler.totalTimeStart;
		}
	}

	public static void enable() {
		Profiler.enabled = true;
	}

	public static void disable() {
		Profiler.enabled = false;
	}

	public static void dumpTotals() {
		if (Profiler.total.size() == 0 || !Profiler.enabled)
			return;

		HashMap<String, Long> map = new LinkedHashMap<String, Long>();

		List<String> keys = new ArrayList<String>(Profiler.total.keySet());
		List<Long> values = new ArrayList<Long>(Profiler.total.values());
		TreeSet<Long> sortedSet = new TreeSet<Long>(values);
		Object[] sortedArray = sortedSet.toArray();
		int size = sortedArray.length;

		for (int i = 0; i < size; i++) {
			map.put(keys.get(values.indexOf(sortedArray[i])), (Long) sortedArray[i]);
		}

		float tSecs = 0, tPerc = 0;

		out("Time\t Percentage\t Target");
		for (String target : map.keySet()) {
			float secs = 1e-9f * Profiler.total.get(target);
			float perc = 100f * Profiler.total.get(target) / Profiler.cumulativeTime;
			out(String.format("%1$.1f", secs) + "\t " + String.format("%1$.1f", perc) + "\t " + target);

			tSecs += secs;
			tPerc += perc;
		}

		out("Profiled secs sum: " + String.format("%1$.1f", tSecs));
		out("Profiled percent sum: " + String.format("%1$.1f", tPerc));

		float realtimeSecs = 1e-9f * (System.nanoTime() - Profiler.initTime);
		float measuredSecs = 1e-9f * Profiler.totalTime;
		float measuredPerc = 100f * measuredSecs / realtimeSecs;

		out("Measured secs: " + String.format("%1$.1f", measuredSecs));
		out("Actual runtime secs: " + String.format("%1$.1f", realtimeSecs));
		out("Measured percent: " + String.format("%1$.1f", measuredPerc));
	}

	private static void out(String s) {
		System.out.println(s);
		ConsoleLog.addLine(s);
	}
}
