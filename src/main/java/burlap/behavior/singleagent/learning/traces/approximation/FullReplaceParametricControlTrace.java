package burlap.behavior.singleagent.learning.traces.approximation;

import burlap.behavior.functionapproximation.DifferentiablePolicy;
import burlap.behavior.functionapproximation.FunctionGradient;
import burlap.mdp.core.action.Action;
import burlap.mdp.core.action.ActionUtils;
import burlap.mdp.core.state.State;
import burlap.mdp.singleagent.SADomain;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author James MacGlashan.
 */
public class FullReplaceParametricControlTrace extends ParametricControlTraceBase{

	protected SADomain domain;

	public FullReplaceParametricControlTrace(double lambda, SADomain domain) {
		super(lambda);
		this.domain = domain;
	}

	public FullReplaceParametricControlTrace(double lambda, double minVal, SADomain domain) {
		super(lambda, minVal);
		this.domain = domain;
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

		//now replace off-actions for this state
		for(Action ap : ActionUtils.allApplicableActionsForTypes(domain.getActionTypes(), s)){
			if(!a.equals(ap)){
				FunctionGradient offGrad = policy.gradient(s, ap);
				for(FunctionGradient.PartialDerivative pd : offGrad.getNonZeroPartialDerivatives()){
					toRemove.add(pd.parameterId);
				}
			}
		}

		for(int id : toRemove){
			this.trace.remove(id);
		}

		//then replace
		FunctionGradient saGrad = policy.gradient(s, a);
		for(FunctionGradient.PartialDerivative pd : saGrad.getNonZeroPartialDerivatives()){
			this.trace.set(pd.parameterId, pd.value);
		}

		FunctionGradient curTrace = this.traceToGradient();

		return curTrace;
	}
}
