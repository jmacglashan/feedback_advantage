package burlap.behavior.singleagent.learning.traces.tabular;

import burlap.mdp.core.action.Action;
import burlap.statehashing.HashableState;

/**
 * @author James MacGlashan.
 */
public class ControlTraceVal {
	public HashableState s;
	public Action a;
	public double val;

	public ControlTraceVal(HashableState s, Action a, double val) {
		this.s = s;
		this.a = a;
		this.val = val;
	}
}
