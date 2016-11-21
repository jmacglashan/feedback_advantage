package experiments.turtlebot.datastructures;

/**
 * @author James MacGlashan.
 */
public class CameraChannels {

	public CameraScaleFeatures [] channels;

	public CameraChannels() {
	}

	public CameraChannels(CameraScaleFeatures[] channels) {
		this.channels = channels;
	}

	public CameraChannels copy(){
		CameraScaleFeatures [] nchannels = new CameraScaleFeatures[this.channels.length];
		for(int i = 0; i < this.channels.length; i++){
			nchannels[i] = this.channels[i].copy();
		}

		return new CameraChannels(nchannels);
	}

	public int nChannels(){
		return this.channels.length;
	}

	public CameraScaleFeatures getChannel(int i){
		return this.channels[i];
	}

}
