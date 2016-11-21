package burlap.behavior.singleagent.humanfeedback.events;

/**
 * @author James MacGlashan.
 */
public class FeedbackEvent {

	public double r;
	public double timeStamp;

	/**
	 * Initializes.
	 * @param r the reward
	 * @param beginTime
	 */
	public FeedbackEvent(double r, double beginTime) {
		this.r = r;
		this.timeStamp = (System.currentTimeMillis() / 1000.) - beginTime;
	}

}
