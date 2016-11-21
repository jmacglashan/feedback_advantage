package burlap.behavior.singleagent.humanfeedback.environment;

import burlap.behavior.singleagent.humanfeedback.FeedbackReceiver;
import burlap.behavior.singleagent.humanfeedback.events.ActionEvent;
import burlap.behavior.singleagent.humanfeedback.events.FeedbackEvent;
import burlap.mdp.core.action.Action;
import burlap.mdp.core.state.State;
import burlap.mdp.singleagent.environment.Environment;
import burlap.mdp.singleagent.environment.EnvironmentOutcome;
import burlap.mdp.singleagent.environment.extensions.EnvironmentDelegation;
import burlap.mdp.singleagent.environment.extensions.EnvironmentObserver;
import burlap.mdp.singleagent.environment.extensions.EnvironmentServerInterface;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author James MacGlashan.
 */
public class HumanFeedbackEnvironment implements Environment, EnvironmentDelegation, EnvironmentServerInterface, FeedbackReceiver {

	protected Environment delegate;
	protected List<EnvironmentObserver> observers = new ArrayList<EnvironmentObserver>();
	protected List<FeedbackEvent> feedbackEvents = new ArrayList<FeedbackEvent>();
	protected double lastHumanFeedback = 0.;
	protected double humanFeedback = 0.;
	protected boolean terminalSignal = false;

	protected long delay = 0;
	protected boolean delayAfter = true;

	protected double beginTime = System.currentTimeMillis() / 1000.;


	public HumanFeedbackEnvironment(Environment delegate) {
		this.delegate = delegate;
	}

	public Environment getEnvironmentDelegate() {
		return delegate;
	}

	public void setEnvironmentDelegate(Environment delegate) {
		this.delegate = delegate;
	}

	public State currentObservation() {
		return this.delegate.currentObservation();
	}

	public EnvironmentOutcome executeAction(Action a) {
		State cur = this.currentObservation();
		for(EnvironmentObserver ob : this.observers){
			ob.observeEnvironmentActionInitiation(cur, a);
		}

		ActionEvent ae = new ActionEvent(cur, a, this.beginTime);

		if(!delayAfter){
			if(this.delay > 0){
				try {
					Thread.sleep(this.delay);
				} catch(InterruptedException e) {
					e.printStackTrace();
				}
			}
		}

		EnvironmentOutcome eo = this.delegate.executeAction(a);

		HFEnvOutcome heo = new HFEnvOutcome(eo.o, eo.a, eo.op,
				eo.r+this.humanFeedback,
				this.terminalSignal || eo.terminated,
				ae,
				new ArrayList<FeedbackEvent>(this.feedbackEvents));

		if(delayAfter){
			if(this.delay > 0){
				try {
					Thread.sleep(this.delay);
				} catch(InterruptedException e) {
					e.printStackTrace();
				}
			}
		}

		this.feedbackEvents.clear();
		this.lastHumanFeedback = this.humanFeedback;
		this.humanFeedback = 0.;

		for(EnvironmentObserver ob : this.observers){
			ob.observeEnvironmentInteraction(heo);
		}


		return heo;
	}

	public double lastReward() {
		return this.delegate.lastReward() + this.lastHumanFeedback;
	}

	public boolean isInTerminalState() {
		return this.delegate.isInTerminalState() || this.terminalSignal;
	}

	public void resetEnvironment() {
		for(EnvironmentObserver ob : this.observers){
			ob.observeEnvironmentReset(this);
		}
		this.lastHumanFeedback = 0.;
		this.humanFeedback = 0.;
		this.feedbackEvents.clear();
		this.terminalSignal = false;
		delegate.resetEnvironment();
	}

	public void addObservers(EnvironmentObserver... observers) {
		this.observers.addAll(Arrays.asList(observers));
	}

	public void clearAllObservers() {
		this.observers.clear();;
	}

	public void removeObservers(EnvironmentObserver... observers) {
		this.observers.removeAll(Arrays.asList(observers));
	}

	public List<EnvironmentObserver> observers() {
		return observers;
	}

	public void receiveHumanFeedback(double f) {
		this.humanFeedback += f;
		this.feedbackEvents.add(new FeedbackEvent(f, this.beginTime));
	}

	public void setTerminalSignal(boolean val){
		this.terminalSignal = val;
	}

	public boolean getTerminalSignal() {
		return terminalSignal;
	}
}
