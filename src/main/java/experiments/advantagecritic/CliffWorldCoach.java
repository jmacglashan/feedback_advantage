package experiments.advantagecritic;

import burlap.behavior.functionapproximation.TabularFeatures;
import burlap.behavior.functionapproximation.sparse.LinearVFA;
import burlap.behavior.learningrate.ConstantLR;
import burlap.behavior.singleagent.Episode;
import burlap.behavior.singleagent.auxiliary.EpisodeSequenceVisualizer;
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
import burlap.domain.singleagent.gridworld.GridWorldVisualizer;
import burlap.domain.singleagent.gridworld.state.GridWorldState;
import burlap.mdp.core.action.Action;
import burlap.mdp.core.state.State;
import burlap.mdp.singleagent.SADomain;
import burlap.mdp.singleagent.environment.SimulatedEnvironment;
import burlap.mdp.singleagent.model.RewardFunction;
import burlap.statehashing.HashableStateFactory;
import burlap.statehashing.simple.SimpleHashableStateFactory;
import burlap.visualizer.Visualizer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author James MacGlashan
 */
public class CliffWorldCoach {

    public static class CliffRF implements RewardFunction {
        @Override
        public double reward(State s, Action a, State sprime) {
            GridWorldState gwp = (GridWorldState)sprime;
            int x = gwp.agent.x;
            int y = gwp.agent.y;
            if(y == 0 && x > 0 && x < 7){
                return -1;
            }
            if(x == 7 && y == 0){
                return 5.;
            }
            return 0.;
        }
    }


    public static void main(String[] args) {
        GridWorldDomain gwd = new GridWorldDomain(8, 5);
        RewardFunction rf = new CliffWorldTamer.CliffRF();
        gwd.setTf(new GridWorldTerminalFunction(7, 0));
        gwd.setRf(rf);
        SADomain domain = gwd.generateDomain();

//        Visualizer vis = GridWorldVisualizer.getVisualizer(gwd.getMap());
//        VisualExplorer exp = new VisualExplorer(domain, vis, new GridWorldState(0, 0));
//        exp.addKeyAction("w", GridWorldDomain.ACTION_NORTH, "");
//        exp.addKeyAction("s", GridWorldDomain.ACTION_SOUTH, "");
//        exp.addKeyAction("a", GridWorldDomain.ACTION_WEST, "");
//        exp.addKeyAction("d", GridWorldDomain.ACTION_EAST, "");
//        exp.initGUI();

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
                new ConstantLR(0.2));

        CoachCritic critic = new CoachCritic();
        ActorCritic agent = new ActorCritic(actor, critic);

        //create artificial human feedback that gives feedback based on advantage function of current agent policy
        //Note that the FeedbackReceiver for AdvantageFeedback is the HumanFeedbackEnvironment, not the agent
        AdvantageFeedback feedback = new AdvantageFeedback(hfenv, pPolicy, domain, new SimpleHashableStateFactory(), 0.99, 0.01, 100);
        env.addObservers(feedback); //add our artificial human feedback as an observer to the base environment

        List<Episode> episodes = new ArrayList<>();
        for(int i = 0; i < 1000; i++){
            //Note that for COACH test we run the agent in the human feedback environment hfenv
            Episode e = agent.runLearningEpisode(hfenv, 5000);
            episodes.add(e);
            System.out.println(i + "  " + e.maxTimeStep() + "  " + objectiveReward(e, rf));
            hfenv.resetEnvironment();
        }

        Visualizer v = GridWorldVisualizer.getVisualizer(gwd.getMap());
        new EpisodeSequenceVisualizer(v, domain, episodes);

    }


    public static double objectiveReward(Episode e, RewardFunction rf){
        double sumr = 0.;
        for(int i = 1; i <= e.maxTimeStep(); i++){
            sumr += rf.reward(null, null, e.state(i));
        }
        return sumr;
    }

}
