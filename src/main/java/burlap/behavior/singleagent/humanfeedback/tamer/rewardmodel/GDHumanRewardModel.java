package burlap.behavior.singleagent.humanfeedback.tamer.rewardmodel;


import burlap.behavior.functionapproximation.DifferentiableStateActionValue;
import burlap.behavior.functionapproximation.FunctionGradient;
import burlap.mdp.core.action.Action;
import burlap.mdp.core.state.State;

/**
 * @author James MacGlashan.
 */
public class GDHumanRewardModel implements HumanRewardModel {

	protected DifferentiableStateActionValue vfa;
	protected double learningRate;

	public GDHumanRewardModel(DifferentiableStateActionValue vfa, double learningRate) {
		this.vfa = vfa;
		this.learningRate = learningRate;
	}

	@Override
	public void updateModel(State s, Action ga, double reward) {

		double p = this.vfa.evaluate(s, ga);
		double target = reward;
		double delta = target - p;
		//System.out.println("target: " + target + " from " + p);

		FunctionGradient gradient = this.vfa.gradient(s, ga);
		for(FunctionGradient.PartialDerivative pd : gradient.getNonZeroPartialDerivatives()){
			double newParam = this.vfa.getParameter(pd.parameterId) + this.learningRate * delta * pd.value;
			this.vfa.setParameter(pd.parameterId, newParam);
		}

	}


	@Override
	public HumanRewardModel copy() {

		HumanRewardModel hrm = new GDHumanRewardModel((DifferentiableStateActionValue)this.vfa.copy(), this.learningRate);
		return hrm;
	}

	@Override
	public double reward(State s, Action a, State sprime) {
		return this.vfa.evaluate(s, a);
	}

	@Override
	public void resetModel() {
		this.vfa.resetParameters();
	}

	public DifferentiableStateActionValue getVfa() {
		return vfa;
	}

	public void setVfa(DifferentiableStateActionValue vfa) {
		this.vfa = vfa;
	}

	public double getLearningRate() {
		return learningRate;
	}

	public void setLearningRate(double learningRate) {
		this.learningRate = learningRate;
	}
}
