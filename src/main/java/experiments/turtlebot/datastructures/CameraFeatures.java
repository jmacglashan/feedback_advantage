package experiments.turtlebot.datastructures;

/**
 * @author James MacGlashan.
 */
public class CameraFeatures {
	public double [] features;
	public CameraFeatures(){}
	public CameraFeatures(double [] features){
		this.features = features;
	}
	public CameraFeatures copy(){
		return new CameraFeatures(this.features.clone());
	}

	public static class CameraFeaturesHelper{


		public static double getFeature(CameraFeatures features, int r, int c, int factor){

			return features.features[r*factor+c];

		}

	}

}
