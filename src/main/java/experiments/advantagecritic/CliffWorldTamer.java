package experiments.advantagecritic;

import burlap.behavior.functionapproximation.TabularFeatures;
import burlap.behavior.functionapproximation.sparse.LinearVFA;
import burlap.behavior.policy.EnumerablePolicy;
import burlap.behavior.policy.EpsilonGreedy;
import burlap.behavior.policy.Policy;
import burlap.behavior.singleagent.Episode;
import burlap.behavior.singleagent.auxiliary.EpisodeSequenceVisualizer;
import burlap.behavior.singleagent.humanfeedback.tamer.TamerInfrequent;
import burlap.behavior.singleagent.humanfeedback.tamer.rewardmodel.GDHumanRewardModel;
import burlap.domain.singleagent.gridworld.GridWorldDomain;
import burlap.domain.singleagent.gridworld.GridWorldTerminalFunction;
import burlap.domain.singleagent.gridworld.GridWorldVisualizer;
import burlap.domain.singleagent.gridworld.state.GridWorldState;
import burlap.mdp.core.action.Action;
import burlap.mdp.core.state.State;
import burlap.mdp.singleagent.SADomain;
import burlap.mdp.singleagent.environment.SimulatedEnvironment;
import burlap.mdp.singleagent.model.RewardFunction;
import burlap.shell.visual.VisualExplorer;
import burlap.statehashing.HashableStateFactory;
import burlap.statehashing.simple.SimpleHashableStateFactory;
import burlap.visualizer.Visualizer;

import java.util.ArrayList;
import java.util.List;

/**
 * @author James MacGlashan
 */
public class CliffWorldTamer {

    public static class CliffRF implements RewardFunction{
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
        gwd.setTf(new GridWorldTerminalFunction(7, 0));
        RewardFunction rf = new CliffRF();
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


        //set up tamer and function approximation
        TabularFeatures tf = new TabularFeatures(domain, hashingFactory);
        LinearVFA vfa = new LinearVFA(tf);
        GDHumanRewardModel hrf = new GDHumanRewardModel(vfa, 0.2);
        TamerInfrequent tamer = new TamerInfrequent(domain, hrf);
        tamer.setLearningPolicy(new EpsilonGreedy(tamer, 0.2));
        Policy tamerPolicy = tamer.getLearningPolicy();

        //create artificial human feedback that gives feedback based on advantage function of current tamer policy
        //Note that the FeedbackReceiver is the TAMER agent itself.
        AdvantageFeedback feedback = new AdvantageFeedback(tamer, (EnumerablePolicy)tamerPolicy, domain, new SimpleHashableStateFactory(), 0.99, 0.01, 100, 1.0);
        env.addObservers(feedback); //add our artificial human feedback as an observe to the environment

        List<Episode> episodes = new ArrayList<>();
        for(int i = 0; i < 1000; i++){
            Episode e = tamer.runLearningEpisode(env, 800);
            episodes.add(e);
            System.out.println(i + "  " + e.maxTimeStep() + "  " + objectiveReward(e, rf));
            env.resetEnvironment();
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
