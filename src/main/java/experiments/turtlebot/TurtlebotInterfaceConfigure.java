package experiments.turtlebot;

import burlap.behavior.singleagent.humanfeedback.FeedbackKeyListener;
import burlap.behavior.singleagent.humanfeedback.FeedbackReceiver;
import burlap.behavior.singleagent.humanfeedback.environment.HumanFeedbackEnvironment;
import burlap.behavior.singleagent.learning.LearningAgent;
import burlap.behavior.singleagent.learning.humanfeedback.LearnerKeyControl;
import burlap.ros.RosEnvironment;
import burlap.visualizer.Visualizer;

/**
 * @author James MacGlashan.
 */
public class TurtlebotInterfaceConfigure {

	public static void configureForSlidePresenter(HumanFeedbackEnvironment env, Visualizer v, LearningAgent agent){

        FeedbackReceiver[] receivers = new FeedbackReceiver[]{env};
        if(agent instanceof FeedbackReceiver){
            receivers = new FeedbackReceiver[]{env, (FeedbackReceiver)agent};
        }

		FeedbackKeyListener feedbackListener = new FeedbackKeyListener(receivers);
		feedbackListener.assignFeedback(33, -1.).assignFeedback(34, 1.);

		LearnerKeyControl lkeyListner = new LearnerKeyControl(env, agent, 66, 66, 27);

		TurtlebotSoundInterface sLister = new TurtlebotSoundInterface(((RosEnvironment)env.getEnvironmentDelegate()).getRosBridge(),
				"/home/turtlebot/jm2_ws/src/turtle_env/scripts/Robot_blip.wav",
				"/home/turtlebot/jm2_ws/src/turtle_env/scripts/Robot_blip_2.wav");
		sLister.addRewardKeys(34).addPunishKeys(33);

		v.addKeyListener(feedbackListener);
		v.addKeyListener(lkeyListner);
		v.addKeyListener(sLister);

	}

}
