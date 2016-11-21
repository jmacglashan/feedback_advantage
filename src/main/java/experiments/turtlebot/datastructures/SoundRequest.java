package experiments.turtlebot.datastructures;

/**
 * @author James MacGlashan.
 */
public class SoundRequest {

	public int sound=-2;
	public int command=1;
	public String arg="";
	public String arg2="";

	public SoundRequest() {
	}

	public SoundRequest(String arg) {
		this.arg = arg;
	}

	public SoundRequest(int sound, int command, String arg, String arg2) {
		this.sound = sound;
		this.command = command;
		this.arg = arg;
		this.arg2 = arg2;
	}
}
