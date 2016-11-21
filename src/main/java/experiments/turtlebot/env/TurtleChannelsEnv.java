package experiments.turtlebot.env;

import burlap.mdp.core.action.Action;
import burlap.mdp.core.action.UniversalActionType;
import burlap.mdp.core.action.continuous.ContinuousActionType;
import burlap.mdp.core.action.continuous.SimpleContinuousActionType;
import burlap.mdp.core.state.State;
import burlap.mdp.singleagent.SADomain;
import burlap.ros.RosEnvironment;
import burlap.ros.actionpub.ActionPublisher;
import burlap.ros.actionpub.CentralizedPeriodicActionPub;
import burlap.shell.visual.VisualExplorer;
import burlap.visualizer.Visualizer;
import com.fasterxml.jackson.databind.JsonNode;
import experiments.turtlebot.CameraFeaturesVisualizer;
import experiments.turtlebot.datastructures.CameraChannels;
import experiments.turtlebot.states.TurtleChannelsState;
import ros.msgs.geometry_msgs.Twist;
import ros.msgs.geometry_msgs.Vector3;
import ros.tools.MessageUnpacker;
import ros.tools.PeriodicPublisher;

/**
 * @author James MacGlashan.
 */
public class TurtleChannelsEnv extends RosEnvironment{

	protected Action stopAction;


	public static TurtleChannelsEnv discreteActionConstructor(String rosURI, String stateTopic, String actionTopic){

		//create a new domain with no state representation
		SADomain domain = new SADomain();

		domain.addActionTypes(
				new UniversalActionType("forward"),
				new UniversalActionType("backward"),
				new UniversalActionType("rotate"),
				new UniversalActionType("rotate_ccw"),
				new UniversalActionType("stop"));


		//define the relevant twist messages that we'll use for our actions
		Twist fTwist = new Twist(new Vector3(0.1,0,0.), new Vector3()); //forward
		Twist bTwist = new Twist(new Vector3(-0.1,0,0.), new Vector3()); //backward
		Twist rTwist = new Twist(new Vector3(), new Vector3(0,0,-0.5)); //clockwise rotate
		Twist rccwTwist = new Twist(new Vector3(), new Vector3(0,0,0.5)); //counter-clockwise rotate
		Twist sTwist = new Twist(); //stop/do nothing


		TurtleChannelsEnv env = new TurtleChannelsEnv(domain, rosURI, stateTopic, domain.getAction("stop").associatedAction(""));

		//create periodic publisher
		PeriodicPublisher ppub = new PeriodicPublisher(actionTopic, "geometry_msgs/Twist", env.getRosBridge());
		ppub.beginPublishing(sTwist, 200);

		int delay = 33;
		env.setActionPublisher("forward", new CentralizedPeriodicActionPub(ppub, fTwist, delay));
		env.setActionPublisher("backward", new CentralizedPeriodicActionPub(ppub, bTwist, delay));
		env.setActionPublisher("rotate", new CentralizedPeriodicActionPub(ppub, rTwist, delay));
		env.setActionPublisher("rotate_ccw", new CentralizedPeriodicActionPub(ppub, rccwTwist, delay));
		env.setActionPublisher("stop", new CentralizedPeriodicActionPub(ppub, sTwist, delay));


		return env;


	}

	public static TurtleChannelsEnv continuousActionConstructor(String rosURI, String stateTopic, String actionTopic){

		//create a new domain with no state representation
		SADomain domain = new SADomain();

		domain.addActionType(new SimpleContinuousActionType("a", 2,
				new ContinuousActionType.ParameterDomain(new double[]{-.2, -.9}, new double[]{.2, .9})));

		Twist sTwist = new Twist(); //stop/do nothing


		TurtleChannelsEnv env = new TurtleChannelsEnv(domain, rosURI, stateTopic, domain.getAction("a").associatedAction("0 0"));

		PeriodicPublisher ppub = new PeriodicPublisher(actionTopic, "geometry_msgs/Twist", env.getRosBridge());
		ppub.beginPublishing(sTwist, 200);

		int delay = 500;
		env.setActionPublisher("a", new TurtleTwistPublisher(ppub, delay));


		return env;
	}


	public TurtleChannelsEnv(SADomain domain, String rosBridgeURI, String rosStateTopic, Action stopAction) {
		super(domain, rosBridgeURI, rosStateTopic, "turtle_env/CameraChannels", 1, 1);
		this.stopAction = stopAction;
	}


	@Override
	public State unpackStateFromMsg(JsonNode data, String stringRep) {
		MessageUnpacker<CameraChannels> unpacker = new MessageUnpacker(CameraChannels.class);
		CameraChannels cc = unpacker.unpackRosMessage(data);
		State s = new TurtleChannelsState(cc);

		return s;
	}

	@Override
	public void resetEnvironment() {
		System.out.println("TurtleChannelsEnv: resetting environment (publishing stop action)");
		ActionPublisher ap = this.actionPublishers.get(stopAction.actionName());
		ap.publishAction(stopAction);
	}

	public static void main(String[] args) {

		String rosURI = "ws://138.16.161.195:9090";
		String stateTopic = "/turtle_env/camera_features/channel_features";
		String actionTopic = "/mobile_base/commands/velocity";

		//TurtleChannelsEnv env = TurtleChannelsEnv.discreteActionConstructor(rosURI, stateTopic, actionTopic);
		TurtleChannelsEnv env = TurtleChannelsEnv.continuousActionConstructor(rosURI, stateTopic, actionTopic);

		Visualizer v = CameraFeaturesVisualizer.getChannelsVisualizer(8);
		VisualExplorer exp = new VisualExplorer(env.getDomain(), env, v);
		exp.initGUI();
		exp.startLiveStatePolling(33);

	}
}
