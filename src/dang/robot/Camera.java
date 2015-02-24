package dang.robot;

public class Camera {
	double cameraSpreadAngle = 40;
	double cameraDistance = 100;
	double cameraPitch = 128;
	double cameraHeight = 13.5;
	Robot robot;
	

	public Camera(Robot robot){
		this.robot = robot;
	}

	public double getCameraSpreadAngle() {
		return cameraSpreadAngle;
	}

	public void setCameraSpreadAngle(double cameraSpreadAngle) {
		this.cameraSpreadAngle = cameraSpreadAngle;
	}

	public double getCameraDistance() {
		return cameraDistance;
	}

	public void setCameraDistance(double cameraDistance) {
		this.cameraDistance = cameraDistance;
	}

	public double getCameraPitch() {
		return robot.getHeadPitchAngle();
	}

	public void setCameraPitch(double cameraPitch) {
		this.cameraPitch = cameraPitch;
	}

	public double getCameraHeight() {
		return cameraHeight;
	}

	public void setCameraHeight(double cameraHeight) {
		this.cameraHeight = cameraHeight;
	}
}
