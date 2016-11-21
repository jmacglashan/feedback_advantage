package burlap.behavior.singleagent.humanfeedback.tamer.creditassignment;


import burlap.behavior.singleagent.humanfeedback.events.ActionEvent;

/**
 * @author James MacGlashan.
 */
public class UniformCreditAssignmentPDF implements CreditAssignmentPDF {

	protected double l;
	protected double u;

	protected double r;

	protected LowerPiece lp = new LowerPiece();
	protected MiddlePiece mp = new MiddlePiece();
	protected UpperPiece up = new UpperPiece();

	public UniformCreditAssignmentPDF(double l, double u) {
		this.l = l;
		this.u = u;
		this.updateR();
	}

	public double getL() {
		return l;
	}

	public void setL(double l) {
		this.l = l;
		this.updateR();
	}

	public double getU() {
		return u;
	}

	public void setU(double u) {
		this.u = u;
		this.updateR();
	}

	protected void updateR(){
		this.r = 1. / (this.u - this.l);
	}

	@Override
	public double probability(double a, double b) {
		if(a > u || b < l){
			return 0.;
		}
		return r * (Math.min(b, u) - Math.max(a, l));
	}

	@Override
	public double probability(ActionEvent e, double rewardTime) {
		return this.probability(e.t_s-rewardTime, e.t_t - rewardTime);
	}

	@Override
	public double maxPossibleFutureCredit(ActionEvent e, double curTime) {

		double a = e.t_s - curTime;
		double b = e.t_t - curTime;

		if(b < l){
			return 0.;
		}

		if(a < l){
			return probability(e, curTime);
		}

		//then align it for as much mass as possible
		return probability(e, e.t_s - l);
	}

	@Override
	public double usedUpTargetingMass(ActionEvent e, double curTime) {
		double sum = 0.;
		double u1 = e.t_t-u;
		double u2 = e.t_s-l;
		double u3 = e.t_t-l;

		if(u1 < curTime){
			sum += evalF(lp, e, e.t_s - u, u1);

			if(u2 < curTime){
				sum += evalF(mp, e, u1, u2);

				if(u3 < curTime){
					sum += evalF(up, e, u2, u3);
				}
				else{
					sum += evalF(up, e, u2, curTime);
				}
			}
			else{
				sum += evalF(mp, e, u1, curTime);
			}
		}
		else{
			sum += evalF(lp, e, e.t_s - u, curTime);
		}

		return sum;
	}

	@Override
	public double extrapolationSlope(ActionEvent e, double curTime) {

		double num = this.extrapolationNumerator(e);
		double denom = this.usedUpTargetingMass(e, curTime);
		double slope = num/denom;

		return slope;
	}

	protected double extrapolationNumerator(ActionEvent e){
		double sum = evalF(lp, e, e.t_s-u, e.t_t-u) + evalF(mp, e, e.t_t-u, e.t_s-l) + evalF(up, e, e.t_s-l, e.t_t-l);
		return sum;
	}

	protected double evalF(Function f, ActionEvent e, double a, double b){
		return f.f(e, b) - f.f(e, a);
	}



	protected static interface Function{
		double f(ActionEvent e, double t);
	}

	protected class LowerPiece implements Function{
		@Override
		public double f(ActionEvent e, double t) {
			return r*t*( u - e.t_s + 0.5*t );
		}
	}

	protected class MiddlePiece implements Function{
		@Override
		public double f(ActionEvent e, double t) {
			return r*t*( e.t_t - e.t_s );
		}
	}

	protected class UpperPiece implements Function{
		@Override
		public double f(ActionEvent e, double t) {
			return r*t*( e.t_t - 0.5*t - l);
		}
	}


	public static void main(String[] args) {
		UniformCreditAssignmentPDF u = new UniformCreditAssignmentPDF(-0.8, 0.);
		ActionEvent te = new ActionEvent(null, null, 0.);
		te.t_s = 3.25;
		te.t_t = 3.45;

		double slope = u.extrapolationSlope(te, 3.9);
		double ex = slope*0.666;

		System.out.println(slope + "\n" + ex);

	}

}
