package burlap.behavior.singleagent.humanfeedback.events;

import burlap.mdp.core.action.Action;
import burlap.mdp.core.state.State;

/**
 * @author James MacGlashan.
 */
public class ActionEvent {
	public State s;
	public Action a;
	public double t_s;
	public double t_t;

	/**
	 * Initializes all parameters except the end time, t_t. Begin time acts as a reference point for when all learning began
	 * and the t_s parameter of this object will be set to the time since them. The units of beginTime should be in seconds.
	 * This will set the begin time to the current clock time minus the reference point.
	 * @param s the state in which the action took place
	 * @param a the action taken
	 * @param beginTime the start time of all learning in seconds, NOT the start of this action event.
	 */
	public ActionEvent(State s, Action a, double beginTime) {
		this.s = s;
		this.a = a;
		this.t_s = (System.currentTimeMillis() / 1000.) - beginTime;
	}
}
