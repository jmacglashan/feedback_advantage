package burlap.behavior.singleagent.policy;

import burlap.behavior.functionapproximation.DifferentiablePolicy;
import burlap.behavior.functionapproximation.DifferentiableStateActionValue;
import burlap.behavior.functionapproximation.FunctionGradient;
import burlap.behavior.policy.EnumerablePolicy;
import burlap.behavior.policy.support.ActionProb;
import burlap.datastructures.BoltzmannDistribution;
import burlap.mdp.core.action.Action;
import burlap.mdp.core.action.ActionUtils;
import burlap.mdp.core.state.State;
import burlap.mdp.singleagent.SADomain;

import java.util.ArrayList;
import java.util.List;

/**
 * @author James MacGlashan.
 */
public class ParametricBoltzmann implements DifferentiablePolicy, EnumerablePolicy {

	protected DifferentiableStateActionValue vfa;
	protected SADomain domain;
	protected double temperature = 1.;


	public ParametricBoltzmann(SADomain domain, DifferentiableStateActionValue vfa) {
		this.domain = domain;
		this.vfa = vfa;
	}

	public ParametricBoltzmann(SADomain domain, DifferentiableStateActionValue vfa, double temperature) {
		this.domain = domain;
		this.vfa = vfa;
		this.temperature = temperature;
	}

	@Override
	public FunctionGradient gradient(State s, Action a) {
		return this.vfa.gradient(s, a);
	}

	@Override
	public double evaluate(State s, Action a) {
		return this.actionProb(s, a);
	}

	@Override
	public int numParameters() {
		return vfa.numParameters();
	}

	@Override
	public double getParameter(int i) {
		return vfa.getParameter(i);
	}

	@Override
	public void setParameter(int i, double p) {
		this.vfa.setParameter(i, p);
	}

	@Override
	public void resetParameters() {
		this.vfa.resetParameters();
	}

	@Override
	public ParametricBoltzmann copy() {
		return new ParametricBoltzmann(domain, (DifferentiableStateActionValue)vfa.copy(), this.temperature);
	}

	@Override
	public Action action(State s) {

		List<Action> actions = ActionUtils.allApplicableActionsForTypes(domain.getActionTypes(), s);
		BoltzmannDistribution bd = this.generateBD(s, actions);
		int sel = bd.sample();
		Action choice = actions.get(sel);
		return choice;
	}

	@Override
	public double actionProb(State s, Action a) {

		List<Action> actions = ActionUtils.allApplicableActionsForTypes(domain.getActionTypes(), s);
		int ind = -1;
		for(int i = 0; i < actions.size(); i++){
			if(actions.get(i).equals(a)){
				ind = i;
				break;
			}
		}
		if(ind == -1){
			throw new RuntimeException("Action " + a.toString() + " is not defined for the input state.");
		}

		BoltzmannDistribution bd = this.generateBD(s, actions);
		double [] probs = bd.getProbabilities();
		double p = probs[ind];

		return p;

	}

	@Override
	public List<ActionProb> policyDistribution(State s) {

		List<Action> actions = ActionUtils.allApplicableActionsForTypes(domain.getActionTypes(), s);
		BoltzmannDistribution bd = this.generateBD(s, actions);

		double [] probs = bd.getProbabilities();
		List<ActionProb> aps = new ArrayList<>(actions.size());
		for(int i = 0; i < probs.length; i++){
			ActionProb ap = new ActionProb(actions.get(i), probs[i]);
			aps.add(ap);
		}

		return aps;
	}

	@Override
	public boolean definedFor(State s) {
		return true;
	}

	protected BoltzmannDistribution generateBD(State s, List<Action> actions){
		double [] prefs = new double[actions.size()];
		for(int i = 0; i < actions.size(); i++){
			Action a = actions.get(i);
			double v = this.vfa.evaluate(s, a);
			prefs[i] = v;
		}
		BoltzmannDistribution bd = new BoltzmannDistribution(prefs, this.temperature);
		return bd;
	}
}
