package burlap.behavior.singleagent.learning.humanfeedback.coach.traceselector;

import burlap.mdp.singleagent.environment.EnvironmentOutcome;

/**
 * @author James MacGlashan.
 */
public class ConstantSelector implements TraceSelector {

	protected int traceSelect = 0;

	public ConstantSelector() {
	}

	public ConstantSelector(int traceSelect) {
		this.traceSelect = traceSelect;
	}

	public int getTraceSelect() {
		return traceSelect;
	}

	public void setTraceSelect(int traceSelect) {
		this.traceSelect = traceSelect;
	}

	@Override
	public int useTrace(EnvironmentOutcome eo) {
		return this.traceSelect;
	}
}
