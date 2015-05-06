/**
 * A special message payload used for returning status from the AI thread.
 * Does nothing but group a number of variables.
 */
package threadMessaging;

public class MTMsgAIStat {
	private float cps;
	private float cpsavg;
	private int cells;
	private int pathingMaps;
	private boolean liveFlag;

	public MTMsgAIStat(float cps, float cpsavg, int cells, int pathingMaps, boolean liveFlag) {
		this.cps = cps;
		this.cpsavg = cpsavg;
		this.cells = cells;
		this.pathingMaps = pathingMaps;
		this.liveFlag = liveFlag;
	}

	public int getPathingMaps() {
		return pathingMaps;
	}

	public boolean isLiveFlag() {
		return liveFlag;
	}

	public float getCps() {
		return cps;
	}

	public float getCpsAvg() {
		return cpsavg;
	}

	public int getCells() {
		return cells;
	}

}
