package burlap.behavior.singleagent.learning.actorcritic.actor;

import burlap.behavior.singleagent.learning.traces.tabular.ControlTraceVal;
import burlap.behavior.singleagent.learning.traces.tabular.TabularControlTrace;
import burlap.behavior.singleagent.learning.traces.tabular.TabularControlTraceFullReplace;
import burlap.mdp.core.state.State;
import burlap.mdp.singleagent.SADomain;
import burlap.mdp.singleagent.environment.EnvironmentOutcome;
import burlap.statehashing.HashableState;
import burlap.statehashing.HashableStateFactory;

import java.util.Iterator;

/**
 * @author James MacGlashan.
 */
public class BoltzmannTraceActor extends BoltzmannActor {

	TabularControlTrace traces;

	public BoltzmannTraceActor(SADomain domain, HashableStateFactory hashingFactory, double learningRate, double lambda) {
		super(domain, hashingFactory, learningRate);
		this.traces = new TabularControlTraceFullReplace(hashingFactory, lambda);
	}

	@Override
	public void startEpisode(State s) {
		super.startEpisode(s);
		this.traces.begin();
	}

	@Override
	public void update(EnvironmentOutcome eo, double critique) {
		double learningRate = this.learningRate.pollLearningRate(this.totalNumberOfSteps, eo.o, eo.a);
		Iterator<ControlTraceVal> trace = this.traces.pollTraceWithUpdate(eo.o, eo.a);
		while(trace.hasNext()){
			ControlTraceVal t = trace.next();

			HashableState sh = t.s;
			PolicyNode node = this.getNode(sh);

			ActionPreference pref = this.getMatchingPreference(sh, t.a, node);
			pref.preference += learningRate * critique * t.val;

		}


		this.totalNumberOfSteps++;
	}
}
