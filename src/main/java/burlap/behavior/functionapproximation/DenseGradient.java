package burlap.behavior.functionapproximation;

import java.util.HashSet;
import java.util.Set;

/**
 * @author James MacGlashan.
 */
public class DenseGradient implements FunctionGradient {

	public double [] gradient;

	public DenseGradient(double[] gradient) {
		this.gradient = gradient;
	}

	@Override
	public void put(int parameterId, double partialDerivative) {
		this.gradient[parameterId] = partialDerivative;
	}

	@Override
	public double getPartialDerivative(int parameterId) {
		return this.gradient[parameterId];
	}

	@Override
	public Set<PartialDerivative> getNonZeroPartialDerivatives() {
		Set<PartialDerivative> pds = new HashSet<>(gradient.length);
		for(int i = 0; i < gradient.length; i++){
			double pd = gradient[i];
			if(pd != 0.){
				pds.add(new PartialDerivative(i, pd));
			}
		}
		return pds;
	}

	@Override
	public int numNonZeroPDs() {
		int sum = 0;
		for(double pd : this.gradient){
			if(pd != 0.){
				sum++;
			}
		}
		return sum;
	}
}
