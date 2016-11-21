package burlap.behavior.singleagent.learning.traces.approximation;

import burlap.behavior.functionapproximation.DifferentiablePolicy;
import burlap.behavior.functionapproximation.FunctionGradient;
import burlap.mdp.core.action.Action;
import burlap.mdp.core.state.State;

/**
 * @author James MacGlashan.
 */
public interface ParametricControlTrace {

	void begin();
	FunctionGradient pollTraceWithUpdate(State s, Action a, DifferentiablePolicy policy);

}
