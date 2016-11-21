package experiments.turtlebot.env;

import burlap.mdp.core.action.continuous.ContinuousAction;
import burlap.mdp.core.action.Action;
import ros.msgs.geometry_msgs.Twist;
import ros.msgs.geometry_msgs.Vector3;
import ros.tools.PeriodicPublisher;

/**
 * @author James MacGlashan.
 */
public class TurtleTwistPublisher extends AbstractCentralizedPeriodicActionPub{

	public TurtleTwistPublisher(PeriodicPublisher ppub, int timeDelay) {
		super(ppub, timeDelay);
	}

	@Override
	public Object msg(Action a) {
		ContinuousAction ca = (ContinuousAction)a;
		double [] params = ca.parameters;
		if(params.length != 2){
			throw new RuntimeException("TurtleTwistPublisher expected a continuous action with 2 parameters, got " + params.length + " parameters");
		}
		Twist msg = new Twist(new Vector3(params[0], 0., 0.), new Vector3(0., 0., params[1]));
		return msg;
	}
}
