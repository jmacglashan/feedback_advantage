package burlap.behavior.singleagent.learning.traces.tabular;

/**
 * @author James MacGlashan.
 */
public class TracePoll {
	public double traceVal;
	public double nextTraceVal;

	public TracePoll(double traceVal, double nextTraceVal) {
		this.traceVal = traceVal;
		this.nextTraceVal = nextTraceVal;
	}
}
