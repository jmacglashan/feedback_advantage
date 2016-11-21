package burlap.behavior.singleagent.learning.actorcritic.actor;

import burlap.behavior.functionapproximation.DifferentiablePolicy;
import burlap.behavior.functionapproximation.FunctionGradient;
import burlap.behavior.functionapproximation.ParametricFunction;
import burlap.behavior.functionapproximation.dense.DenseStateFeatures;
import burlap.behavior.singleagent.learning.actorcritic.Actor;
import burlap.behavior.singleagent.learning.traces.approximation.ParametricControlTrace;
import burlap.mdp.core.action.Action;
import burlap.mdp.core.action.continuous.ContinuousAction;
import burlap.mdp.core.action.continuous.ContinuousActionType;
import burlap.mdp.core.state.State;
import burlap.mdp.singleagent.environment.EnvironmentOutcome;
import org.apache.commons.math3.distribution.NormalDistribution;

import java.util.Arrays;

/**
 * @author James MacGlashan.
 */
public class LinearGaussianActor implements Actor, DifferentiablePolicy {

	protected DenseStateFeatures muFeatures;
	protected DenseStateFeatures sigmaFeatures;
	protected double [][] muParameters;
	protected double [][] sigmaParameters;
	protected int numMuFeatures;
	protected int numSigmaFeatures;
	protected ContinuousActionType actionGen;
	protected double learningRate;

	protected double minSigValue = 0.001;


	protected ParametricControlTrace trace;

	protected double [] initialMu;
	protected double [] initialSig;

	protected double maxGradientMag = Double.POSITIVE_INFINITY;

	public LinearGaussianActor(DenseStateFeatures muFeatures, DenseStateFeatures sigmaFeatures, int numMuFeatures, int numSigmaFeatures, ContinuousActionType actionGen, double learningRate, ParametricControlTrace trace) {
		this.muFeatures = muFeatures;
		this.sigmaFeatures = sigmaFeatures;
		this.numMuFeatures = numMuFeatures;
		this.numSigmaFeatures = numSigmaFeatures;
		this.actionGen = actionGen;
		this.learningRate = learningRate;
		this.trace = trace;

		this.muParameters = new double[actionGen.numParameters()][numMuFeatures];
		this.sigmaParameters = new double[actionGen.numParameters()][numSigmaFeatures];

		this.initialMu = new double[actionGen.numParameters()];
		this.initialSig = new double[actionGen.numParameters()];
		for(int i = 0; i < initialSig.length; i++){
			initialSig[i] = minSigValue;
		}


		this.resetParameters();
	}

	public void setSigmaParams(double paramVal){
		for(int i = 0; i < sigmaParameters.length; i++){
			setSigmaParam(i, paramVal);
		}
	}

	public void setSigmaParam(int actionDim, double paramVal){
		for(int j = 0; j < sigmaParameters[actionDim].length; j++){
			sigmaParameters[actionDim][j] = paramVal;
		}
		this.initialSig[actionDim] = paramVal;
	}

	public void setMuParams(double paramVal){
		for(int i = 0; i < muParameters.length; i++){
			setMuParams(i, paramVal);
		}
	}

	public void setMuParams(int actionDim, double paramVal){
		for(int j = 0; j < muParameters[actionDim].length; j++){
			muParameters[actionDim][j] = paramVal;
		}
		this.initialMu[actionDim] = paramVal;
	}


	public FunctionGradient gradient(State s, Action a) {

		double [] actionVec = ((ContinuousAction)a).parameters;

		double [] muFeatures = this.muFeatures.features(s);
		double [] sigFeatures = this.muFeatures != this.sigmaFeatures ? this.sigmaFeatures.features(s) : muFeatures;

		double [] mus = this.predictMu(muFeatures);
		double [] sigs = this.predictSigs(sigFeatures);

		FunctionGradient gradient = new FunctionGradient.SparseGradient((numMuFeatures + numSigmaFeatures) * muParameters.length);

		int paramId = 0;
		for(int i = 0; i < actionVec.length; i++){
			double diff = actionVec[i] - mus[i];
			double sigSq = sigs[i]*sigs[i];
			double muGrad = diff / sigSq;
			double sigGrad = ((diff*diff) / (sigSq)) - 1;
			for(int j = 0; j < muFeatures.length; j++){
				double pd = muGrad * muFeatures[j];
				gradient.put(paramId, pd);
				paramId++;
			}
			for(int j = 0; j < sigFeatures.length; j++){
				double pd = sigGrad * sigFeatures[j];
				gradient.put(paramId, pd);
				paramId++;
			}

		}

		return gradient;
	}

	public double evaluate(State s, Action a) {
		return this.actionProb(s, a);
	}

	public int numParameters() {
		return (this.numMuFeatures + this.numSigmaFeatures) * this.actionGen.numParameters();
	}

	public double getParameter(int i) {

		int numParamPerAction = this.numMuFeatures + this.numSigmaFeatures;

		int aEl = i / numParamPerAction;
		int withinAc = i % numParamPerAction;
		if(withinAc < numMuFeatures){
			return this.muParameters[aEl][withinAc];
		}
		int sigInd = withinAc - numMuFeatures;
		return this.sigmaParameters[aEl][sigInd];
	}

	public void setParameter(int i, double p) {

		int numParamPerAction = this.numMuFeatures + this.numSigmaFeatures;

		int aEl = i / numParamPerAction;
		int withinAc = i % numParamPerAction;
		if(withinAc < numMuFeatures){
			this.muParameters[aEl][withinAc] = p;
			return;
		}
		int sigInd = withinAc - numMuFeatures;
		this.sigmaParameters[aEl][sigInd] = Math.max(p, this.minSigValue);
	}

	public void resetParameters() {
		for(int i = 0; i < muParameters.length; i++){
			for(int j = 0; j < muParameters[i].length; j++){
				muParameters[i][j] = this.initialMu[i];
			}
		}
		for(int i = 0; i < sigmaParameters.length; i++){
			for(int j = 0; j < sigmaParameters[i].length; j++){
				sigmaParameters[i][j] = this.initialSig[i];
			}
		}
	}

	public ParametricFunction copy() {
		return null;
	}

	public void startEpisode(State s) {
		this.trace.begin();
	}

	public void endEpisode() {
		//do nothing
	}

	public double getMaxGradientMag() {
		return maxGradientMag;
	}

	public void setMaxGradientMag(double maxGradientMag) {
		this.maxGradientMag = maxGradientMag;
	}

	public void update(EnvironmentOutcome eo, double critique) {
		FunctionGradient t = this.trace.pollTraceWithUpdate(eo.o, eo.a, this);
		t = this.rescaleGradient(t);
		for(FunctionGradient.PartialDerivative pd : t.getNonZeroPartialDerivatives()){
			double nval = this.getParameter(pd.parameterId) + this.learningRate * critique * pd.value;
			this.setParameter(pd.parameterId, nval);
		}

	}

	public void reset() {
		this.resetParameters();
	}

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
			double v = Math.max(pdomain.lower[i], Math.min(pdomain.upper[i], sample));
			actionVec[i] = v;
		}

		Action a = actionGen.generate(actionVec);

		return a;
	}

	public double actionProb(State s, Action a) {
		double [] actionVec = ((ContinuousAction)a).parameters;

		double [] muFeatures = this.muFeatures.features(s);
		double [] sigFeatures = this.muFeatures != this.sigmaFeatures ? this.sigmaFeatures.features(s) : muFeatures;

		double [] mus = this.predictMu(muFeatures);
		double [] sigs = this.predictSigs(sigFeatures);

		double root2pi = Math.sqrt(2*Math.PI);
		double prod = 1.;
		for(int aind = 0; aind < actionVec.length; aind++){
			double diff = actionVec[aind] - mus[aind];
			double diffsq = diff*diff;
			double sigsq = sigs[aind]*sigs[aind];
			double expon = Math.exp(-diffsq / (2 * sigsq));
			double p = expon / (root2pi * sigs[aind]);
			prod *= p;
		}

		return prod;
	}

	public boolean definedFor(State s) {
		return true;
	}

	public double [][] distributionParametersFor(State s){


		double [] muFeatures = this.muFeatures.features(s);
		double [] sigFeatures = this.muFeatures != this.sigmaFeatures ? this.sigmaFeatures.features(s) : muFeatures;

		int adim = actionGen.numParameters();

		double [] mus = this.predictMu(muFeatures);
		double [] sigs = this.predictSigs(sigFeatures);


		double [][] distParams = new double[adim][2];
		for(int i = 0; i < adim; i++){
			distParams[i][0] = mus[i];
			distParams[i][1] = sigs[i];
		}

		return distParams;

	}


	protected double linearComb(double [][] params, double [] fvec, int aEl){
		double sum = 0.;
		for(int i = 0; i < fvec.length; i++){
			sum += fvec[i] * params[aEl][i];
		}
		return sum;
	}

	@Override
	public String toString() {
		StringBuilder buf = new StringBuilder();
		for(int i = 0; i < muParameters.length; i++){
			buf.append(Arrays.toString(muParameters[i])).append("\n");
		}
		buf.append("-\n");
		for(int i = 0; i < sigmaParameters.length; i++){
			buf.append(Arrays.toString(sigmaParameters[i])).append("\n");
		}
		return buf.toString();

	}


	protected double [] predictMu(double [] stateFeatures){
		int adim = actionGen.numParameters();

		double [] mus = new double[adim];

		ContinuousActionType.ParameterDomain pd = this.actionGen.parameterDomain();
		double [] lower = pd.lower;
		double [] upper = pd.upper;

		for(int i = 0; i < adim; i++){
			double mu = this.linearComb(muParameters, stateFeatures, i);
			mus[i] = Math.max(lower[i], Math.min(upper[i], mu));
		}

		return mus;
	}

	protected double [] predictSigs(double [] stateFeatures){
		int adim = actionGen.numParameters();

		double [] sigs = new double[adim];

		for(int i = 0; i < adim; i++){
			double sig = this.linearComb(sigmaParameters, stateFeatures, i);
			sigs[i] = Math.max(this.minSigValue, sig);
		}

		return sigs;
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
