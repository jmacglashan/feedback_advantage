package experiments.turtlebot.states;

import burlap.mdp.core.state.State;
import experiments.turtlebot.datastructures.CameraChannels;

import java.util.Arrays;
import java.util.List;

/**
 * @author James MacGlashan.
 */
public class TurtleChannelsState implements State {


	public CameraChannels channels;

	public TurtleChannelsState() {
	}

	public TurtleChannelsState(CameraChannels channels) {
		this.channels = channels;
	}

	public List<Object> variableKeys() {
		return Arrays.<Object>asList("channels");
	}

	public Object get(Object variableKey) {
		return channels;
	}

	public State copy() {
		return new TurtleChannelsState(this.channels.copy());
	}
}
