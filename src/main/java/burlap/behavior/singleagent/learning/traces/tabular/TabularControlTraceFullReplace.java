package burlap.behavior.singleagent.learning.traces.tabular;

import burlap.statehashing.HashableStateFactory;

/**
 * @author James MacGlashan.
 */
public class TabularControlTraceFullReplace extends TabularControlTrace{

	public TabularControlTraceFullReplace(HashableStateFactory hashingFactory, double lambda) {
		super(hashingFactory, lambda);
	}

	public TabularControlTraceFullReplace(HashableStateFactory hashingFactory, double lambda, double minVal) {
		super(hashingFactory, lambda, minVal);
	}

	public TracePoll traceVal(SAKey key, double curVal, SAKey lastStep) {
		if(lastStep.equals(key)){
			curVal = 1.;
		}
		else if(lastStep.s.equals(key.s)){
			curVal = 0.;
		}
		double nextVal = this.lambda*curVal;
		return new TracePoll(curVal, nextVal);
	}
}
