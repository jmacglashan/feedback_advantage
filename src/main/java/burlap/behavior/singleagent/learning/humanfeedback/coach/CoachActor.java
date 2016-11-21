package burlap.behavior.singleagent.learning.humanfeedback.coach;

import burlap.behavior.functionapproximation.DifferentiablePolicy;
import burlap.behavior.functionapproximation.FunctionGradient;
import burlap.behavior.learningrate.LearningRate;
import burlap.behavior.singleagent.learning.actorcritic.Actor;
import burlap.behavior.singleagent.learning.humanfeedback.coach.traceselector.TraceSelector;
import burlap.behavior.singleagent.learning.traces.approximation.ParametricControlTrace;
import burlap.mdp.core.action.Action;
import burlap.mdp.core.state.State;
import burlap.mdp.singleagent.environment.EnvironmentOutcome;

import java.util.LinkedList;
import java.util.List;

/**
 * @author James MacGlashan.
 */
public class CoachActor implements Actor {

	protected DifferentiablePolicy policy;
	protected List<ParametricControlTrace> traceSet;
	protected int delay;
	protected LearningRate lr;
	protected TraceSelector traceSelector;

	protected double maxGradientMag = Double.POSITIVE_INFINITY;


	protected LinkedList<EnvironmentOutcome> outcomeQueue = new LinkedList<>();

	protected int totalTimeSteps = 0;

	public CoachActor(DifferentiablePolicy policy, List<ParametricControlTrace> traceSet, TraceSelector traceSelector, int delay, LearningRate lr) {
		this.policy = policy;
		this.traceSet = traceSet;
		this.traceSelector = traceSelector;
		this.delay = delay;
		this.lr = lr;
	}

	@Override
	public void startEpisode(State s) {
		for(ParametricControlTrace trace : traceSet){
			trace.begin();
		}
	}

	@Override
	public void endEpisode() {
		//do nothing
	}

	public double getMaxGradientMag() {
		return maxGradientMag;
	}

	public void setMaxGradientMag(double maxGradientMag) {
		this.maxGradientMag = maxGradientMag;
	}

	@Override
	public void update(EnvironmentOutcome eo, double critique) {
		outcomeQueue.addLast(eo);

		if(outcomeQueue.size() >= delay){
			EnvironmentOutcome deo = outcomeQueue.poll();

			//figure out lambda
			int targetTraceId = this.traceSelector.useTrace(eo);

			//updated traces
			FunctionGradient targetTrace = null;
			for(int i = 0; i < traceSet.size(); i++){
				FunctionGradient trace = traceSet.get(i).pollTraceWithUpdate(deo.o, deo.a, policy);
				if(i == targetTraceId){
					targetTrace = trace;
				}
			}

			//update parameters
			for(FunctionGradient.PartialDerivative pd : targetTrace.getNonZeroPartialDerivatives()){
				double lr = this.lr.pollLearningRate(this.totalTimeSteps, pd.parameterId);
				double change = lr * critique * pd.value;
				double oldVal = policy.getParameter(pd.parameterId);
				double newval = oldVal + change;
				policy.setParameter(pd.parameterId, newval);
			}

		}


		totalTimeSteps++;
	}

	@Override
	public void reset() {
		this.policy.resetParameters();
	}

	@Override
	public Action action(State s) {
		return this.policy.action(s);
	}

	@Override
	public double actionProb(State s, Action a) {
		return this.policy.actionProb(s, a);
	}

	@Override
	public boolean definedFor(State s) {
		return this.policy.definedFor(s);
	}


	protected FunctionGradient rescaleGradient(FunctionGradient gradient){
		double mag = this.gradientMag(gradient);
		if(mag > this.maxGradientMag){
			double scalar = this.maxGradientMag / mag;
			gradient = this.scaleGradient(gradient, scalar);
		}
		return gradient;
	}


	protected double gradientMag(FunctionGradient gradient){
		double sum = 0.;
		for(FunctionGradient.PartialDerivative pd : gradient.getNonZeroPartialDerivatives()){
			double sq = pd.value*pd.value;
			sum += sq;
		}

		double mag = Math.sqrt(sum);
		return mag;
	}

	protected FunctionGradient scaleGradient(FunctionGradient gradient, double scalar){
		FunctionGradient outGrad = new FunctionGradient.SparseGradient(gradient.numNonZeroPDs());
		for(FunctionGradient.PartialDerivative pd : gradient.getNonZeroPartialDerivatives()){
			double v = pd.value * scalar;
			outGrad.put(pd.parameterId, v);
		}

		return outGrad;
	}
}
