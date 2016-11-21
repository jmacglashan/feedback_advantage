package burlap.behavior.singleagent.learning.traces.tabular;

import burlap.statehashing.HashableStateFactory;

/**
 * @author James MacGlashan.
 */
public class TabularControlTraceReplace extends TabularControlTrace {

	public TabularControlTraceReplace(HashableStateFactory hashingFactory, double lambda) {
		super(hashingFactory, lambda);
	}

	public TabularControlTraceReplace(HashableStateFactory hashingFactory, double lambda, double minVal) {
		super(hashingFactory, lambda, minVal);
	}

	@Override
	public TracePoll traceVal(SAKey key, double curVal, SAKey lastStep) {
		if(lastStep.equals(key)){
			curVal = 1.;
		}
		double nextVal = this.lambda*curVal;
		return new TracePoll(curVal, nextVal);
	}
}
