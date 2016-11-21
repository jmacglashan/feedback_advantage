package burlap.behavior.singleagent.humanfeedback.tamer.creditassignment;


import burlap.behavior.singleagent.humanfeedback.events.ActionEvent;

/**
 * @author James MacGlashan.
 */
public interface CreditAssignmentPDF {


	/**
	 * Computes the probability of this density function on the interval (a, b).
	 * This method must be able to handle upper bound queries of positive infinity (Double.POSITIVE_INFINITY)
	 * @param a the lower bound of the interval
	 * @param b the upper bound of the interval
	 * @return the probability of this density function on the interval (a, b)
	 */
	double probability(double a, double b);

	/**
	 * Computes the probability of this density function that a reward at time rewardTime targets
	 * a ActionEvent.
	 * Must be consistent with the {@link #probability(double, double)} method when its arguments
	 * are a=e.t_s - rewardTime and b=e.t_t - rewardTime.
	 * @param e the ActionEvent that is considered for being targeted.
	 * @param rewardTime the time of the human reward
	 * @return the probability that a reward at time rewardTime targets a ActionEvent
	 */
	double probability(ActionEvent e, double rewardTime);


	/**
	 * Computes the maximum possible credit an action event could be given sometime in the future from the current
	 * specified time.
	 * @param e the ActionEvent under consideration.
	 * @param curTime the current time
	 * @return the maximum possible credit (probability)
	 */
	double maxPossibleFutureCredit(ActionEvent e, double curTime);

	/**
	 * Computes the amount of target probability mass for a
	 * ActionEvent that has been "used up" by the current
	 * time.
	 * @param e the potentially targeted ActionEvent
	 * @param curTime the current time
	 * @return the amount of used up targeting probability mass.
	 */
	double usedUpTargetingMass(ActionEvent e, double curTime);


	/**
	 * Computes an extrapolation slope that can be used for estimating how much future reward might target the
	 * input ActionEvent. See page 57 of
	 * Brad Knox's thesis for more information. Returned result should correspond to the ratio:
	 * <br/>
	 * \frac{\int_{-ifnty}^ifnty \int_{e.t_s - t}^{e.t_t -t} f_{delay}(x) dx dt}{\int_{-ifnty}^{t_{curr}} \int_{e.t_s - t}^{e.t_t -t} f_{delay}(x) dx dt}
	 * <br/>
	 * where f_{delay}(x) is this density function
	 * @param e the ActionEvent being targeted
	 * @param curTime the current time
	 * @return The extrapolation slope that can be used for estimating how much future reward might target the input ActionEvent
	 */
	double extrapolationSlope(ActionEvent e, double curTime);

}

