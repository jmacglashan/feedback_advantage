package burlap.behavior.singleagent.learning.humanfeedback.coach.traceselector;

import burlap.behavior.singleagent.humanfeedback.environment.HFEnvOutcome;
import burlap.behavior.singleagent.humanfeedback.events.FeedbackEvent;
import burlap.mdp.singleagent.environment.EnvironmentOutcome;

import java.util.HashMap;
import java.util.Map;

/**
 * @author James MacGlashan.
 */
public class MaxFeedbackSelector implements TraceSelector {

	protected int defaultChoice = 0;
	protected Map<Double, Integer> map = new HashMap<>();

	public MaxFeedbackSelector() {
	}

	public MaxFeedbackSelector(int defaultChoice) {
		this.defaultChoice = defaultChoice;
	}

	public MaxFeedbackSelector assignTraceId(double feedbackVal, int traceSelection){
		this.map.put(feedbackVal, traceSelection);
		return this;
	}

	@Override
	public int useTrace(EnvironmentOutcome eo) {
		double mxFeedback = this.maxFeedback(eo);
		Integer choice = map.get(mxFeedback);
		if(choice == null){
			return this.defaultChoice;
		}
		return choice;
	}

	protected double maxFeedback(EnvironmentOutcome eo){
		if(eo instanceof HFEnvOutcome){
			double max = Double.NEGATIVE_INFINITY;
			HFEnvOutcome heo = (HFEnvOutcome)eo;
			for(FeedbackEvent fe : heo.feedbackEvents){
				max = Math.max(fe.r, max);
			}
			return max;
		}
		return eo.r;
	}
}
