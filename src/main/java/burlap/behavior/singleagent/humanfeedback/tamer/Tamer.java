package burlap.behavior.singleagent.humanfeedback.tamer;

import burlap.behavior.policy.GreedyQPolicy;
import burlap.behavior.policy.Policy;
import burlap.behavior.singleagent.Episode;
import burlap.behavior.singleagent.MDPSolver;
import burlap.behavior.singleagent.humanfeedback.FeedbackReceiver;
import burlap.behavior.singleagent.humanfeedback.events.ActionEvent;
import burlap.behavior.singleagent.humanfeedback.events.FeedbackEvent;
import burlap.behavior.singleagent.humanfeedback.tamer.creditassignment.CreditAssignmentPDF;
import burlap.behavior.singleagent.humanfeedback.tamer.creditassignment.UniformCreditAssignmentPDF;
import burlap.behavior.singleagent.humanfeedback.tamer.rewardmodel.HumanRewardModel;
import burlap.behavior.singleagent.learning.LearningAgent;
import burlap.behavior.valuefunction.QFunction;
import burlap.behavior.valuefunction.QProvider;
import burlap.behavior.valuefunction.QValue;
import burlap.mdp.core.action.Action;
import burlap.mdp.core.state.State;
import burlap.mdp.singleagent.SADomain;
import burlap.mdp.singleagent.environment.Environment;
import burlap.mdp.singleagent.environment.EnvironmentOutcome;

import java.util.ArrayList;
import java.util.List;

/**
 * The full version of TAMER [1] that performs probabilistic human-reward credit assignment and extrapolation and delay
 * weighted aggregate reward.
 *
 * <br/><br/>
 * 1. W. Bradley Knox. Learning from Human-Generated Reward. Dissertation. September 2012.
 * @author James MacGlashan.
 */
public class Tamer extends MDPSolver implements LearningAgent, FeedbackReceiver, QProvider {

	protected HumanRewardModel hr;
	protected HumanRewardModel transientHR;
	protected CreditAssignmentPDF ca;
	protected Policy learningPolicy = new GreedyQPolicy(this);
	protected List<FeedbackEvent> feedbackEvents = new ArrayList<FeedbackEvent>(100);
	protected double epsilonCredit = 0.;
	protected double cminExtrapolate = 1.;

	protected double beginTime;


	public Tamer(SADomain domain, HumanRewardModel hr, double uniformLowerBound, double uniformUpperBound){
		this.solverInit(domain, 0.0, null);
		this.ca = new UniformCreditAssignmentPDF(uniformLowerBound, uniformUpperBound);
		this.hr = hr;
		this.transientHR = hr.copy();
	}


	/**
	 * Initializes TAMER
	 * @param domain the MDP
	 * @param hr the {@link burlap.behavior.singleagent.humanfeedback.tamer.rewardmodel.HumanRewardModel} used to model feedback signals
	 * @param capdf a {@link burlap.behavior.singleagent.humanfeedback.tamer.creditassignment.CreditAssignmentPDF}: object for assigning probability that a feedback targeted an action event
	 * @param epsilonCredit the minimum probability mass needed to consider an action event as the possible target of feedback
	 * @param cminExtrapolate the minimum amount of probability mass that an action event could have already been target for it to be used for extrapolation
	 */
	public Tamer(SADomain domain, HumanRewardModel hr, CreditAssignmentPDF capdf, double epsilonCredit, double cminExtrapolate){
        this.solverInit(domain, 0.0, null);
		this.ca = capdf;
		this.epsilonCredit = epsilonCredit;
		this.cminExtrapolate = cminExtrapolate;
		this.hr = hr;
		this.transientHR = hr.copy();
	}


	@Override
	public void receiveHumanFeedback(double f) {
		synchronized(this.feedbackEvents) {
			feedbackEvents.add(new FeedbackEvent(f, beginTime));
		}
		//System.out.println("feedback history size: " + feedbackEvents.size());
	}

	@Override
	public Episode runLearningEpisode(Environment env) {
		return this.runLearningEpisode(env, -1);
	}

	@Override
	public Episode runLearningEpisode(Environment env, int maxSteps) {


        Episode ea = new Episode(env.currentObservation());
		List<ActionEvent> es = new ArrayList<ActionEvent>();
		synchronized(this.feedbackEvents){
			this.feedbackEvents.clear();
		}

		beginTime = System.currentTimeMillis() / 1000.;

		int steps = 0;
		while(!env.isInTerminalState() && (steps < maxSteps || maxSteps == -1)){


			State curState = env.currentObservation();
			Action ga = learningPolicy.action(curState);
			ActionEvent e = new ActionEvent(curState, ga, beginTime);
			es.add(e);

			EnvironmentOutcome eo = env.executeAction(ga);
			double tcurr = (System.currentTimeMillis() / 1000.) - beginTime;
			e.t_t = tcurr;

			this.updateModel(es, tcurr, eo.terminated);

			steps++;
		}

		return ea;
	}

	protected int numUpdates = 0;

	protected void updateModel(List<ActionEvent> es, double tcurr, boolean terminated){

//		if(this.feedbackEvents.size() == 0){
//			return;
//		}

		//find first completed action-event
		int firstComplete = -1;
		if(!terminated) {
			for(int i = es.size() - 1; i >= 0; i--) {
				ActionEvent e = es.get(i);
				//double possibleCredit = this.ca.probability(e, tcurr);
				double possibleCredit = this.ca.maxPossibleFutureCredit(e, tcurr);
				if(possibleCredit <= this.epsilonCredit) {
					firstComplete = i;
					break;
				}

			}
		}
		else{
			firstComplete = es.size()-1;
		}

		//update all completed events
		for(int i = firstComplete; i >= 0; i--){
			ActionEvent e = es.get(i);
			double h = this.computeRewardSignal(e);
			if(h != 0.) {
				this.hr.updateModel(e.s, e.a, h);
				numUpdates++;
			}

		}
		//make an new copy of the fully complete sample reward function
		this.transientHR = this.hr.copy();

		//update partial reward model with extrapolation
		for(int i = firstComplete+1; i < es.size(); i++){
			ActionEvent e = es.get(i);
			//is this non-completed sample a candidate for extrapolation?
			double usedUp = this.ca.usedUpTargetingMass(e, tcurr);
			if(usedUp > this.cminExtrapolate){
				double slope = this.ca.extrapolationSlope(e, tcurr);
				double h = this.computeRewardSignal(e);
				double eh = slope*h;
				if(eh != 0.) {
					//System.out.println("Update extrapolation");
					this.transientHR.updateModel(e.s, e.a, eh);
				}
			}
		}

		//clear completed action-events
		for(int i = firstComplete; i >= 0; i--){
			es.remove(i);
		}

		//clear human reward events
		//System.out.println("event size: " + es.size());
		if(es.size() > 0) {
			synchronized(this.feedbackEvents) {
				for(int i = this.feedbackEvents.size() - 1; i >= 0; i--) {
					FeedbackEvent re = this.feedbackEvents.get(i);
					//can future state-action events be credited?
					double pFuture = this.ca.probability(tcurr - re.timeStamp, Double.POSITIVE_INFINITY);
					if(pFuture <= this.epsilonCredit) {
						//have all previous state-action events been credited?
						boolean complete = true;
						for(ActionEvent e : es) {
							double p = this.ca.probability(e, re.timeStamp);
							if(p > 0) {
								complete = false;
								break;
							}
						}
						if(complete) {
							this.feedbackEvents.remove(i);
						}
					}


				}
			}
		}
		else{
			synchronized(this.feedbackEvents){
				this.feedbackEvents.clear();
			}
		}


	}

	protected double computeRewardSignal(ActionEvent e){
		double sum = 0.;
		int num = 0;
		synchronized(this.feedbackEvents){
			for(FeedbackEvent re : this.feedbackEvents){
				double prob = this.ca.probability(e, re.timeStamp);
				double val = re.r*prob;
				sum += val;
				if(prob > 0.){
					num++;
				}
			}
		}

		if(num > 0){
			//System.out.println("TS: " + sum + " from " + num + " for " + e.ga.toString());
		}

		return sum;
	}

	@Override
	public void resetSolver() {
		this.hr.resetModel();
		this.transientHR.resetModel();
		this.feedbackEvents.clear();
	}

    @Override
    public List<QValue> qValues(State s) {
        List<Action> gas = this.applicableActions(s);
        List<QValue> qs = new ArrayList<QValue>(gas.size());
        for(Action ga : gas){
            qs.add(new QValue(s, ga, this.qValue(s, ga)));
        }
        return qs;
    }

    @Override
    public double qValue(State s, Action a) {
        double q = this.transientHR.reward(s, a, null);
        return q;
    }

    @Override
	public double value(State s) {
		return QProvider.Helper.maxQ(this, s);
	}


	public HumanRewardModel getHr() {
		return hr;
	}

	public void setHr(HumanRewardModel hr) {
		this.hr = hr;
	}

	public void clearHRExtrapolation(){
		this.transientHR = this.hr.copy();
	}

	public double getEpsilonCredit() {
		return epsilonCredit;
	}

	public void setEpsilonCredit(double epsilonCredit) {
		this.epsilonCredit = epsilonCredit;
	}

	public double getCminExtrapolate() {
		return cminExtrapolate;
	}

	public void setCminExtrapolate(double cminExtrapolate) {
		this.cminExtrapolate = cminExtrapolate;
	}

	public CreditAssignmentPDF getCa() {
		return ca;
	}

	public void setCa(CreditAssignmentPDF ca) {
		this.ca = ca;
	}

	public Policy getLearningPolicy() {
		return learningPolicy;
	}

	public void setLearningPolicy(Policy learningPolicy) {
		this.learningPolicy = learningPolicy;
	}
}
