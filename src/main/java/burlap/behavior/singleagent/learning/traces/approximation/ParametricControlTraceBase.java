package burlap.behavior.singleagent.learning.traces.approximation;

import burlap.behavior.functionapproximation.FunctionGradient;
import burlap.datastructures.HashedAggregator;

import java.util.Map;

/**
 * @author James MacGlashan.
 */
public abstract class ParametricControlTraceBase implements ParametricControlTrace {

	protected HashedAggregator<Integer> trace = new HashedAggregator<Integer>();
	protected double lambda;
	protected double minVal = 1e-6;

	public ParametricControlTraceBase() {
	}

	public ParametricControlTraceBase(double lambda) {
		this.lambda = lambda;
	}

	public ParametricControlTraceBase(double lambda, double minVal) {
		this.lambda = lambda;
		this.minVal = minVal;
	}

	public HashedAggregator<Integer> getTrace() {
		return trace;
	}

	public void setTrace(HashedAggregator<Integer> trace) {
		this.trace = trace;
	}

	public double getLambda() {
		return lambda;
	}

	public void setLambda(double lambda) {
		this.lambda = lambda;
	}

	public double getMinVal() {
		return minVal;
	}

	public void setMinVal(double minVal) {
		this.minVal = minVal;
	}

	public void begin() {
		this.trace.clear();
	}

	protected FunctionGradient traceToGradient(){
		FunctionGradient grad = new FunctionGradient.SparseGradient(this.trace.size());
		for(Map.Entry<Integer, Double> e : trace.entrySet()){
			grad.put(e.getKey(), e.getValue());
		}
		return grad;
	}
}
