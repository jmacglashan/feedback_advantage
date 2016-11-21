package burlap.behavior.singleagent.humanfeedback.environment;

import burlap.behavior.singleagent.humanfeedback.events.ActionEvent;
import burlap.behavior.singleagent.humanfeedback.events.FeedbackEvent;
import burlap.mdp.core.action.Action;
import burlap.mdp.core.state.State;
import burlap.mdp.singleagent.environment.EnvironmentOutcome;

import java.util.LinkedList;
import java.util.List;

/**
 * @author James MacGlashan.
 */
public class HFEnvOutcome extends EnvironmentOutcome {

	public List<FeedbackEvent> feedbackEvents;
	public ActionEvent ae;

	public HFEnvOutcome(State o, Action a, State op, double r, boolean terminated, ActionEvent ae) {
		super(o, a, op, r, terminated);
		feedbackEvents = new LinkedList<FeedbackEvent>();
		this.ae = ae;
	}

	public HFEnvOutcome(State o, Action a, State op, double r, boolean terminated, ActionEvent ae, List<FeedbackEvent> feedbackEvents) {
		super(o, a, op, r, terminated);
		this.feedbackEvents = feedbackEvents;
		this.ae = ae;
	}
}
