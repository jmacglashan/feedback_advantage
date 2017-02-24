package experiments.advantagecritic;

import burlap.behavior.functionapproximation.TabularFeatures;
import burlap.behavior.functionapproximation.sparse.LinearVFA;
import burlap.behavior.policy.EnumerablePolicy;
import burlap.behavior.policy.EpsilonGreedy;
import burlap.behavior.policy.Policy;
import burlap.behavior.singleagent.Episode;
import burlap.behavior.singleagent.auxiliary.StateReachability;
import burlap.behavior.singleagent.auxiliary.valuefunctionvis.ValueFunctionVisualizerGUI;
import burlap.behavior.singleagent.humanfeedback.tamer.TamerInfrequent;
import burlap.behavior.singleagent.humanfeedback.tamer.rewardmodel.GDHumanRewardModel;
import burlap.behavior.singleagent.planning.stochastic.policyiteration.PolicyEvaluation;
import burlap.behavior.valuefunction.ValueFunction;
import burlap.domain.singleagent.gridworld.GridWorldDomain;
import burlap.domain.singleagent.gridworld.GridWorldTerminalFunction;
import burlap.domain.singleagent.gridworld.GridWorldVisualizer;
import burlap.domain.singleagent.gridworld.state.GridWorldState;
import burlap.mdp.core.action.Action;
import burlap.mdp.core.action.ActionType;
import burlap.mdp.core.action.ActionUtils;
import burlap.mdp.singleagent.SADomain;
import burlap.mdp.singleagent.common.GoalBasedRF;
import burlap.mdp.singleagent.environment.SimulatedEnvironment;
import burlap.statehashing.HashableStateFactory;
import burlap.statehashing.simple.SimpleHashableStateFactory;
import burlap.mdp.core.state.State;

import java.util.ArrayList;
import java.util.List;

/**
 * TAMER will work well with the advantage function on four rooms, because actions with positive advantage conditioned
 * on the initially random policy are the optimal actions
 * @author James MacGlashan
 */
public class TamerAdvantageTest {

    public static void main(String[] args) {

        //setup domain and environment
        GridWorldDomain gwd = new GridWorldDomain(11, 11);
        gwd.setMapToFourRooms();
        gwd.setTf(new GridWorldTerminalFunction(10, 10));
        gwd.setRf(new GoalBasedRF(gwd.getTf(), 1., 0.));

        final SADomain domain = gwd.generateDomain();
        final double gamma = 0.99;
        final HashableStateFactory hashingFactory = new SimpleHashableStateFactory();
        final SimulatedEnvironment env = new SimulatedEnvironment(domain, new GridWorldState(0, 0));


        //set up tamer and function approximation
        TabularFeatures tf = new TabularFeatures(domain, new SimpleHashableStateFactory());
        LinearVFA vfa = new LinearVFA(tf);
        GDHumanRewardModel hrf = new GDHumanRewardModel(vfa, 0.2);
        TamerInfrequent tamer = new TamerInfrequent(domain, hrf);
        Policy tamerPolicy = tamer.getLearningPolicy();

        //create artificial human feedback that gives feedback based on advantage function of current tamer policy
        //Note that the FeedbackReceiver is the TAMER agent itself.
        AdvantageFeedback feedback = new AdvantageFeedback(tamer, (EnumerablePolicy)tamerPolicy, domain, new SimpleHashableStateFactory(), 0.99, 0.01, 100, 0.4);
        //QPiFeedback feedback = new QPiFeedback(tamer, (EnumerablePolicy)tamerPolicy, domain, new SimpleHashableStateFactory(), 0.99, 0.01, 100);
        env.addObservers(feedback); //add our artificial human feedback as an observe to the environment

        List<Episode> episodes = new ArrayList<>();
        for(int i = 0; i < 10000; i++){
            Episode e = tamer.runLearningEpisode(env, 800);
            episodes.add(e);
            System.out.println(i + " " + e.maxTimeStep());
            env.resetEnvironment();
        }

//        List<State> allStates = StateReachability.getReachableStates(env.currentObservation(), domain, hashingFactory);
//        ValueFunctionVisualizerGUI gui = GridWorldDomain.getGridWorldValueFunctionVisualization(allStates, 11, 11, tamer, tamerPolicy);
//        gui.initGUI();


    }

}
