package experiments.turtlebot;

import burlap.behavior.learningrate.ConstantLR;
import burlap.behavior.singleagent.humanfeedback.environment.HumanFeedbackEnvironment;
import burlap.behavior.singleagent.humanfeedback.shell.SendFeedback;
import burlap.behavior.singleagent.humanfeedback.shell.SendTerminateCommand;
import burlap.behavior.singleagent.learning.actorcritic.ActorCritic;
import burlap.behavior.singleagent.learning.humanfeedback.coach.CoachActor;
import burlap.behavior.singleagent.learning.humanfeedback.coach.CoachCritic;
import burlap.behavior.singleagent.learning.humanfeedback.coach.traceselector.ConstantSelector;
import burlap.behavior.singleagent.learning.traces.approximation.FullReplaceParametricControlTrace;
import burlap.behavior.singleagent.learning.traces.approximation.ParametricControlTrace;
import burlap.behavior.singleagent.policy.GreedyProbPolicy;
import burlap.behavior.singleagent.policy.ParametricBoltzmann;
import burlap.ros.RosShellCommand;
import burlap.shell.visual.VisualExplorer;
import burlap.visualizer.Visualizer;
import experiments.turtlebot.env.TurtleChannelsEnv;
import experiments.turtlebot.shell.ResetSolverCommand;
import experiments.turtlebot.vfa.TileCodedVision;

import java.util.Arrays;

/**
 * @author James MacGlashan.
 */
public class TurtlebotDiscreteTrain {

	public static void main(String[] args) {

		String rosURI = "ws://138.16.161.195:9090";
		String stateTopic = "/turtle_env/camera_features/channel_features";
		String actionTopic = "/mobile_base/commands/velocity";

		TurtleChannelsEnv rootEnv = TurtleChannelsEnv.discreteActionConstructor(rosURI, stateTopic, actionTopic);
		HumanFeedbackEnvironment henv = new HumanFeedbackEnvironment(rootEnv);

		TileCodedVision vfa = new TileCodedVision(3);
		ParametricBoltzmann pb = new ParametricBoltzmann(rootEnv.getDomain(), vfa);
		GreedyProbPolicy greedy = new GreedyProbPolicy(pb);
		CoachActor ha = new CoachActor(
				greedy,
				Arrays.<ParametricControlTrace>asList(new FullReplaceParametricControlTrace(0.95, rootEnv.getDomain())),
				new ConstantSelector(0),
				6,
				new ConstantLR(0.1));
		CoachCritic critic = new CoachCritic();
		ActorCritic ac = new ActorCritic(ha, critic);



		Visualizer v = CameraFeaturesVisualizer.getChannelsVisualizer(8);
		TurtlebotInterfaceConfigure.configureForSlidePresenter(henv, v, ac);




		VisualExplorer exp = new VisualExplorer(rootEnv.getDomain(), rootEnv, v);
		exp.initGUI();
		exp.getShell().addCommand(new RosShellCommand(rootEnv.getRosBridge()));
		exp.getShell().addCommand(new SendFeedback());
		exp.getShell().addCommand(new SendTerminateCommand());
		exp.getShell().addCommand(new ResetSolverCommand(ac));
		exp.startLiveStatePolling(33);

	}


}
