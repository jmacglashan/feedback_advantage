package experiments.advantagecritic;

import burlap.behavior.functionapproximation.TabularFeatures;
import burlap.behavior.functionapproximation.sparse.LinearVFA;
import burlap.behavior.learningrate.ConstantLR;
import burlap.behavior.policy.support.ActionProb;
import burlap.behavior.singleagent.Episode;
import burlap.behavior.singleagent.humanfeedback.environment.HumanFeedbackEnvironment;
import burlap.behavior.singleagent.learning.actorcritic.Actor;
import burlap.behavior.singleagent.learning.actorcritic.ActorCritic;
import burlap.behavior.singleagent.learning.actorcritic.actor.DiffBoltzmann3;
import burlap.behavior.singleagent.learning.humanfeedback.coach.CoachActor;
import burlap.behavior.singleagent.learning.humanfeedback.coach.CoachCritic;
import burlap.behavior.singleagent.learning.humanfeedback.coach.traceselector.ConstantSelector;
import burlap.behavior.singleagent.learning.traces.approximation.CumulativeParametricControlTrace;
import burlap.behavior.singleagent.learning.traces.approximation.ParametricControlTrace;
import burlap.domain.singleagent.gridworld.GridWorldDomain;
import burlap.domain.singleagent.gridworld.GridWorldTerminalFunction;
import burlap.domain.singleagent.gridworld.state.GridWorldState;
import burlap.mdp.singleagent.SADomain;
import burlap.mdp.singleagent.common.GoalBasedRF;
import burlap.mdp.singleagent.environment.SimulatedEnvironment;
import burlap.statehashing.HashableStateFactory;
import burlap.statehashing.simple.SimpleHashableStateFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


/**
 * @author James MacGlashan
 */
public class CoachAdvantageTest {

    public static void main(String[] args) {

        //set up domain and environment
        GridWorldDomain gwd = new GridWorldDomain(11, 11);
        gwd.setMapToFourRooms();
        gwd.setTf(new GridWorldTerminalFunction(10, 10));
        gwd.setRf(new GoalBasedRF(gwd.getTf(), 1., 0.));

        final SADomain domain = gwd.generateDomain();
        final double gamma = 0.99;
        final HashableStateFactory hashingFactory = new SimpleHashableStateFactory();
        final SimulatedEnvironment env = new SimulatedEnvironment(domain, new GridWorldState(0, 0));
        final HumanFeedbackEnvironment hfenv = new HumanFeedbackEnvironment(env);

        //set up coach and function approximation
        TabularFeatures tf = new TabularFeatures(domain, hashingFactory);
        LinearVFA vfa = new LinearVFA(tf);
        DiffBoltzmann3 pPolicy = new DiffBoltzmann3(vfa, domain.getActionTypes());
        Actor actor = new CoachActor(
                pPolicy,
                Arrays.<ParametricControlTrace>asList(new CumulativeParametricControlTrace(0.)),
                new ConstantSelector(),
                0,
                new ConstantLR(1.));

        CoachCritic critic = new CoachCritic();
        ActorCritic agent = new ActorCritic(actor, critic);

        //create artificial human feedback that gives feedback based on advantage function of current agent policy
        //Note that the FeedbackReceiver for AdvantageFeedback is the HumanFeedbackEnvironment, not the agent
        AdvantageFeedback feedback = new AdvantageFeedback(hfenv, pPolicy, domain, new SimpleHashableStateFactory(), 0.99, 0.01, 100);
        env.addObservers(feedback); //add our artificial human feedback as an observer to the base environment

        List<Episode> episodes = new ArrayList<>();
        for(int i = 0; i < 200; i++){
            //Note that for COACH test we run the agent in the human feedback environment hfenv
            Episode e = agent.runLearningEpisode(hfenv, 5000);
            episodes.add(e);
            System.out.println(i + " " + e.maxTimeStep());
            hfenv.resetEnvironment();
        }

    }


    public static void printDist(List<ActionProb> aps){
        for(ActionProb ap : aps){
            System.out.print(ap.pSelection + " ");
        }
        System.out.println();
    }

}
