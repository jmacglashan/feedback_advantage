package burlap.behavior.singleagent.learning.actorcritic.actor;

import burlap.behavior.functionapproximation.dense.DenseStateFeatures;
import burlap.behavior.singleagent.learning.traces.approximation.ParametricControlTrace;
import burlap.mdp.core.action.Action;
import burlap.mdp.core.action.continuous.ContinuousActionType;
import burlap.mdp.core.state.State;
import org.apache.commons.math3.distribution.NormalDistribution;

/**
 * @author James MacGlashan.
 */
public class FilteredGaussianActor extends LinearGaussianActor {
	public FilteredGaussianActor(DenseStateFeatures muFeatures, DenseStateFeatures sigmaFeatures, int numMuFeatures, int numSigmaFeatures, ContinuousActionType actionGen, double learningRate, ParametricControlTrace trace) {
		super(muFeatures, sigmaFeatures, numMuFeatures, numSigmaFeatures, actionGen, learningRate, trace);
	}

	@Override
	public Action action(State s) {
		double [] muFeatures = this.muFeatures.features(s);
		double [] sigFeatures = this.muFeatures != this.sigmaFeatures ? this.sigmaFeatures.features(s) : muFeatures;

		ContinuousActionType.ParameterDomain pdomain = actionGen.parameterDomain();

		double [] actionVec = new double[actionGen.numParameters()];

		double [] mus = this.predictMu(muFeatures);
		double [] sigs = this.predictSigs(sigFeatures);

		for(int i = 0; i < actionGen.numParameters(); i++){
			double mu = mus[i];
			double sig = sigs[i];
			NormalDistribution nd = new NormalDistribution(mu, sig);
			double sample = nd.sample();
			double muSign = Math.signum(mu);
			while(Math.signum(sample) != muSign){
				sample = nd.sample();
			}
			double v = Math.max(pdomain.lower[i], Math.min(pdomain.upper[i], sample));
			actionVec[i] = v;
		}

		Action a = actionGen.generate(actionVec);

		return a;
	}
}
