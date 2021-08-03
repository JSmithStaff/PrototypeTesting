package PrototypeTesting.PrototypeTesting;

import java.io.IOException;

import com.hopding.jrpicam.RPiCamera;
import com.hopding.jrpicam.enums.Exposure;
import com.hopding.jrpicam.exceptions.FailedToRunRaspistillException;
import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioFactory;
import com.pi4j.io.gpio.GpioPinDigitalOutput;
import com.pi4j.io.gpio.RaspiPin;
import com.pi4j.wiringpi.SoftPwm;



public class TestRobot {

	private static TestRobot TR;
	private String currentVersion = "1.0";


	public static void main(String[] args) {
		TR = new TestRobot();
		System.out.println("Testing the new robot");
		System.out.println("Current version: " + TR.getCurrentVersion());
		TestRightWheel();
		TestLeftWheel();
		TestCamera("/home/pi","testImage",200,200,1000);
		TestLights();
	}

	private static void TestCamera(String directory, String imageName, int width, int height, int timeout) {
		//https://github.com/Hopding/JRPiCam
		//leftVelocity. Integer. Speed of left wheel using a number between 1 and 100. 1 being the slowest. 100 being the fastest.
		//directory. String. Folder that you would like to store the photo to be taken
		//imageName. String. Name of the image that you are taking. 
		//Note, image will be stored at directory/imageName once taken.
		//width. Integer. Width of photo to be taken in pixels.
		//height. Integer. Height of photo to be taken in pixels.
		System.out.println("Taking picture.");
		try {
			RPiCamera piCamera = new RPiCamera(directory);
			piCamera.setWidth(width).setHeight(height) // Set Camera to produce images.
			.setExposure(Exposure.AUTO) // Set Camera's exposure.
			.setQuality(Integer.MAX_VALUE).setTimeout(timeout) // Set Camera's timeout.
			.setAddRawBayer(true); // Add Raw Bayer data to image files created by Camera.
			piCamera.takeStill(imageName);

			piCamera.setToDefaults();
		} catch (FailedToRunRaspistillException | IOException | InterruptedException e) {
			e.printStackTrace();
		}
		System.out.println("Picture taken.");
	}

	private static void TestLeftWheel() {
		System.out.println("Testing the left wheel.");
		System.out.println("Forward.");
		TR.setWheelVelocities(100, 0, 2000);
		System.out.println("Backwards.");
		TR.setWheelVelocities(-100, 0, 2000);
		System.out.println("Finished testing.");
	}

	private static void TestRightWheel() {
		System.out.println("Testing the right wheel.");
		TR.setWheelVelocities(0, 100, 2000);
		System.out.println("Forwards.");
		TR.setWheelVelocities(0, -100, 2000);
		System.out.println("Backwards.");
		System.out.println("Finished testing.");
	}

	private static void TestLights() {
		GpioController gpio = GpioFactory.getInstance();
		//Time to turn on the LEDs for is in milliseconds. 
		int timeToLightUpFor = 2000;

		final GpioPinDigitalOutput light1 = gpio.provisionDigitalOutputPin(RaspiPin.GPIO_04);
		final GpioPinDigitalOutput light2 = gpio.provisionDigitalOutputPin(RaspiPin.GPIO_03);
		final GpioPinDigitalOutput light3 = gpio.provisionDigitalOutputPin(RaspiPin.GPIO_00);
		final GpioPinDigitalOutput light4 = gpio.provisionDigitalOutputPin(RaspiPin.GPIO_02);

		System.out.println("Testing lights");
		TR.controlLED(light1,timeToLightUpFor);
		TR.controlLED(light2,timeToLightUpFor);
		TR.controlLED(light3,timeToLightUpFor);
		TR.controlLED(light4,timeToLightUpFor);

		gpio.unprovisionPin(light1);
		gpio.unprovisionPin(light2);
		gpio.unprovisionPin(light3);
		gpio.unprovisionPin(light4);
	}


	public void controlLED(GpioPinDigitalOutput lightPin,int timeToLightUpFor) {
		lightPin.high();
		try {
			Thread.sleep(timeToLightUpFor);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		lightPin.low();
	}

	//Movement
	public void setWheelVelocities(int leftVelocity, int rightVelocity, int timeToHold) {

		//Parameters: leftVelocity. Integer. Speed of left wheel using a number between 1 and 100. 1 being the slowest. 100 being the fastest.
		//rightVelocity. Integer. Speed of right wheel using a number between 1 and 100. 1 being the slowest. 100 being the fastest.
		//timeToHold. Integer. Integer. Time to turn the wheel in milliseconds.

		int LEFT_MOTOR_PIN_A = 14;
		int LEFT_MOTOR_PIN_B = 10;
		int RIGHT_MOTOR_PIN_A = 13;
		int RIGHT_MOTOR_PIN_B = 12;




		//			Create GPIO Controller
		GpioController gpio = GpioFactory.getInstance();

		//Old Robot pin access
		//GpioPinDigitalOutput motor1pinE = gpio.provisionDigitalOutputPin(RaspiPin.GPIO_06, "m1E");
		//GpioPinDigitalOutput motor2pinE = gpio.provisionDigitalOutputPin(RaspiPin.GPIO_03, "m2E");

		//New Robot Pin access.
		GpioPinDigitalOutput turnOnMotors = gpio.provisionDigitalOutputPin(RaspiPin.GPIO_25, "m1E");	

		// Create Pulse Width Modulation
		SoftPwm.softPwmCreate(LEFT_MOTOR_PIN_A, 0, 100);
		SoftPwm.softPwmCreate(LEFT_MOTOR_PIN_B, 0, 100);
		SoftPwm.softPwmCreate(RIGHT_MOTOR_PIN_A, 0, 100);
		SoftPwm.softPwmCreate(RIGHT_MOTOR_PIN_B, 0, 100);

		// Set Pins to High

		//Old robot pins.
		//motor1pinE.high();
		//motor2pinE.high();

		//New robot pin.
		turnOnMotors.high();

		// Movement
		if (leftVelocity < 0) {
			SoftPwm.softPwmWrite(LEFT_MOTOR_PIN_A, 0);
			SoftPwm.softPwmWrite(LEFT_MOTOR_PIN_B, Math.abs(leftVelocity));
		} else if (leftVelocity <= 100) {
			SoftPwm.softPwmWrite(LEFT_MOTOR_PIN_A, leftVelocity);
			SoftPwm.softPwmWrite(LEFT_MOTOR_PIN_B, 0);
		}

		if (rightVelocity < 0) {
			SoftPwm.softPwmWrite(RIGHT_MOTOR_PIN_A, 0);
			SoftPwm.softPwmWrite(RIGHT_MOTOR_PIN_B, Math.abs(rightVelocity));
		} else if (rightVelocity <= 100) {
			SoftPwm.softPwmWrite(RIGHT_MOTOR_PIN_A, rightVelocity);
			SoftPwm.softPwmWrite(RIGHT_MOTOR_PIN_B, 0);
		}

		if (timeToHold > 0) {
			try {
				Thread.sleep(timeToHold);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}

			SoftPwm.softPwmWrite(RIGHT_MOTOR_PIN_A, 0);
			SoftPwm.softPwmWrite(RIGHT_MOTOR_PIN_B, 0);
			SoftPwm.softPwmWrite(LEFT_MOTOR_PIN_A, 0);
			SoftPwm.softPwmWrite(LEFT_MOTOR_PIN_B, 0);
		}

		// Turn off Pin
		//motor1pinE.low();
		//motor2pinE.low();
		turnOnMotors.low();

		// Shutdown GPIO
		gpio.shutdown();

		// Unprovision Pins
		//gpio.unprovisionPin(motor1pinE);
		//gpio.unprovisionPin(motor2pinE);
		gpio.unprovisionPin(turnOnMotors);
	}




	public String getCurrentVersion() {
		return currentVersion;
	}
}
