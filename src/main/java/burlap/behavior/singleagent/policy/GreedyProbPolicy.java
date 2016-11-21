package burlap.behavior.singleagent.policy;

import burlap.behavior.functionapproximation.DifferentiablePolicy;
import burlap.behavior.functionapproximation.FunctionGradient;
import burlap.behavior.functionapproximation.ParametricFunction;
import burlap.behavior.policy.EnumerablePolicy;
import burlap.behavior.policy.support.ActionProb;
import burlap.mdp.core.action.Action;
import burlap.mdp.core.state.State;

import java.util.List;

/**
 * @author James MacGlashan.
 */
public class GreedyProbPolicy implements DifferentiablePolicy {

	protected DifferentiablePolicy delegate;

	public GreedyProbPolicy(DifferentiablePolicy delegate) {
		this.setDelegate(delegate);
	}

	public DifferentiablePolicy getDelegate() {
		return delegate;
	}

	public void setDelegate(DifferentiablePolicy delegate) {
		if(!(delegate instanceof EnumerablePolicy)){
			throw new RuntimeException("Cannot create GreedyProbPolicy around input delegate policy, because it does not implement EnumerablePolicy");
		}
		this.delegate = delegate;
	}

	@Override
	public FunctionGradient gradient(State s, Action a) {
		return delegate.gradient(s, a);
	}

	@Override
	public double evaluate(State s, Action a) {
		return delegate.evaluate(s ,a);
	}

	@Override
	public int numParameters() {
		return delegate.numParameters();
	}

	@Override
	public double getParameter(int i) {
		return delegate.getParameter(i);
	}

	@Override
	public void setParameter(int i, double p) {
		delegate.setParameter(i, p);
	}

	@Override
	public void resetParameters() {
		delegate.resetParameters();
	}

	@Override
	public ParametricFunction copy() {
		return new GreedyProbPolicy((DifferentiablePolicy)this.delegate.copy());
	}

	@Override
	public Action action(State s) {

		List<ActionProb> aps = ((EnumerablePolicy)delegate).policyDistribution(s);
		Action maxAction = aps.get(0).ga;
		double maxProb = aps.get(0).pSelection;
		for(int i = 1; i < aps.size(); i++){
			ActionProb ap = aps.get(i);
			if(ap.pSelection > maxProb){
				maxAction = ap.ga;
				maxProb = ap.pSelection;
			}
		}

		return maxAction;
	}

	@Override
	public double actionProb(State s, Action a) {
		return delegate.actionProb(s, a);
	}

	@Override
	public boolean definedFor(State s) {
		return delegate.definedFor(s);
	}
}
