package experiments.turtlebot.datastructures;

/**
 * @author James MacGlashan.
 */
public class CameraScaleFeatures {
	public CameraFeatures[] scales;

	public CameraScaleFeatures() {
	}

	public CameraScaleFeatures(CameraFeatures[] scales) {
		this.scales = scales;
	}

	public CameraScaleFeatures copy(){
		CameraFeatures[] narray = new CameraFeatures[this.scales.length];
		for(int i = 0; i < narray.length; i++){
			narray[i] = this.scales[i].copy();
		}
		return new CameraScaleFeatures(narray);
	}

	public int nScales(){
		return this.scales.length;
	}


	public static class CameraScaleFeaturesHelper {

		public static double getFeatures(CameraScaleFeatures features, int scale, int r, int c, int factor) {
			return CameraFeatures.CameraFeaturesHelper.getFeature(features.scales[scale], r, c, factor);
		}

	}

}
