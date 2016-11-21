package burlap.behavior.singleagent.learning.traces.approximation;

import burlap.behavior.functionapproximation.DifferentiablePolicy;
import burlap.behavior.functionapproximation.FunctionGradient;
import burlap.mdp.core.action.Action;
import burlap.mdp.core.state.State;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author James MacGlashan.
 */
public class CumulativeParametricControlTrace extends ParametricControlTraceBase {

	public CumulativeParametricControlTrace(double lambda) {
		super(lambda);
	}

	public CumulativeParametricControlTrace(double lambda, double minVal) {
		super(lambda, minVal);
	}

	public FunctionGradient pollTraceWithUpdate(State s, Action a, DifferentiablePolicy policy) {

		//first decay
		List<Integer> toRemove = new ArrayList<Integer>();
		for(Map.Entry<Integer, Double> e : this.trace.entrySet()){
			double nv = e.getValue()*this.lambda;
			if(nv < this.minVal){
				toRemove.add(e.getKey());
			}
		}

		for(int id : toRemove){
			this.trace.remove(id);
		}

		//then add gradient
		FunctionGradient saGrad = policy.gradient(s, a);
		for(FunctionGradient.PartialDerivative pd : saGrad.getNonZeroPartialDerivatives()){
			this.trace.add(pd.parameterId, pd.value);
		}

		FunctionGradient curTrace = this.traceToGradient();

		return curTrace;
	}
}
