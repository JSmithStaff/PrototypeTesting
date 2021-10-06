package PrototypeTesting.PrototypeTesting;

import java.io.IOException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

import com.hopding.jrpicam.RPiCamera;
import com.hopding.jrpicam.enums.Exposure;
import com.hopding.jrpicam.exceptions.FailedToRunRaspistillException;
import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioFactory;
import com.pi4j.io.gpio.GpioPinDigitalInput;
import com.pi4j.io.gpio.GpioPinDigitalOutput;
import com.pi4j.io.gpio.PinPullResistance;
import com.pi4j.io.gpio.RaspiPin;
import com.pi4j.io.gpio.event.GpioPinDigitalStateChangeEvent;
import com.pi4j.io.gpio.event.GpioPinListenerDigital;
import com.pi4j.wiringpi.SoftPwm;



public class TestRobot {

	private static TestRobot TR;
	private String currentVersion = "1.2";


	public static void main(String[] args) {
		TR = new TestRobot();
		System.out.println("Testing the new robot");
		System.out.println("Current version: " + TR.getCurrentVersion());
		TestRightWheel();
		TestLeftWheel();
		TestCamera("/home/pi","testImage",200,200,1000);
		TestLights();
		//Ultrasound is currently not working on new robot, however the hardware has been tested and is working. 
		//TestUltrasound();
		//Two switches work but the other two do not respond to the Java code. They have been tested to work with python however. 
		TestSwitches();
	}

	private static void TestCamera(String directory, String imageName, int width, int height, int timeout) {
		//https://github.com/Hopding/JRPiCam
		//leftVelocity. Integer. Speed of left wheel using a number between 1 and 100. 1 being the slowest. 100 being the fastest.
		//directory. String. Folder that you would like to store the photo to be taken
		//imageName. String. Name of the image that you are taking. 
		//Note, image will be stored at directory/imageName once taken.
		//width. Integer. Width of photo to be taken in pixels.
		//height. Integer. Height of photo to be taken in pixels.
		//timeout. Integer. Amount of time to wait before taking the image in milliseconds. If 0, then camera will run indefinitely.
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

	private static void TestRightWheel() {
		System.out.println("Testing the right wheel.");
		System.out.println("Forwards.");
		TR.setWheelVelocities(0, 100, 2000);
		System.out.println("Backwards.");
		TR.setWheelVelocities(0, -100, 2000);
		System.out.println("Finished testing.");
	}
		
		private static void TestLeftWheel() {
		System.out.println("Testing the left wheel.");
		System.out.println("Forward.");
		TR.setWheelVelocities(100, 0, 2000);
		System.out.println("Backwards.");
		TR.setWheelVelocities(-100, 0, 2000);
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
		int RIGHT_MOTOR_PIN_A = 12;
		int RIGHT_MOTOR_PIN_B = 13;



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
	
	
	//Ultrasound

		public double useUltrasoundSensor() {
			double distanceFromObstacle = 0.0;
			//Set up the GPIO controller to be able to access the GPIO (General Purpose Input Output) Pins on the Pi.
			GpioController gpio = GpioFactory.getInstance();
			//Create digital versions of the pins so that you can access them, change their state and read changes in their state
			//Echo
			final GpioPinDigitalInput inputUltrasound = gpio.provisionDigitalInputPin(RaspiPin.GPIO_06,PinPullResistance.PULL_DOWN);
			//Trigger
			final GpioPinDigitalOutput outputUltrasound = gpio.provisionDigitalOutputPin(RaspiPin.GPIO_23); 
			//Max distance will be implemented.
			//int maxDistance = 23200;
			double distanceToObstacle = 0.0;
			//Times for the pulse to be used to calculate distance.
			Instant pulseStart = null;
			Instant pulseEnd = null;
			long pulseDuration;

			//Make sure the output pin is not turned on as it may mess up readings.
			outputUltrasound.low();
			//Uncomment prints for debugging
			System.out.println("Setting up ultrasound sensor");
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			System.out.println("Sensor set up");


			System.out.println("Send pulse.");
			//Send out pulse
			outputUltrasound.high();
			try {
				Thread.sleep((long) 0.01);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			outputUltrasound.low();
			System.out.println("Waiting for response.");
			//Wait until the pulse is back
			while (inputUltrasound.isLow()){
				pulseStart = Instant.now();
			}
			while (inputUltrasound.isHigh()) {
				pulseEnd = Instant.now();
			}
			//Find out the time the pin was held high in microseconds
			pulseDuration = ChronoUnit.MICROS.between(pulseStart, pulseEnd);
			//Calculate the distance  in cm using the time difference. /58 comes from the official code for the sensor
			distanceToObstacle = (pulseDuration)/58;
			System.out.println("Distance from obstacle: " + distanceToObstacle + "cm");


			return distanceFromObstacle;
		}
		
		private static void TestSwitches()  {
			GpioController gpio = GpioFactory.getInstance();
			//Need fixing
			final GpioPinDigitalInput switch1 = gpio.provisionDigitalInputPin(RaspiPin.GPIO_05,PinPullResistance.PULL_UP);
			final GpioPinDigitalInput switch2 = gpio.provisionDigitalInputPin(RaspiPin.GPIO_27,PinPullResistance.PULL_UP);
			//Working
			final GpioPinDigitalInput switch3 = gpio.provisionDigitalInputPin(RaspiPin.GPIO_22,PinPullResistance.PULL_UP);
			final GpioPinDigitalInput switch4 = gpio.provisionDigitalInputPin(RaspiPin.GPIO_21,PinPullResistance.PULL_UP);
			// set shutdown state for this input pin
			switch1.setShutdownOptions(true);
			// set shutdown state for this input pin
			switch2.setShutdownOptions(true);
			// set shutdown state for this input pin
			switch3.setShutdownOptions(true);
			// set shutdown state for this input pin
			switch4.setShutdownOptions(true);
			// create and register gpio pin listener
			switch1.addListener(new GpioPinListenerDigital() {
				public void handleGpioPinDigitalStateChangeEvent(GpioPinDigitalStateChangeEvent event) {
					// display pin state on console
					System.out.println(" --> GPIO PIN STATE CHANGE: " + event.getPin() + " = " + event.getState());
				}
				
			});
			// create and register gpio pin listener
			switch2.addListener(new GpioPinListenerDigital() {
				public void handleGpioPinDigitalStateChangeEvent(GpioPinDigitalStateChangeEvent event) {
					// display pin state on console
					System.out.println(" --> GPIO PIN STATE CHANGE: " + event.getPin() + " = " + event.getState());
				}

			});
			// create and register gpio pin listener
			switch3.addListener(new GpioPinListenerDigital() {
				public void handleGpioPinDigitalStateChangeEvent(GpioPinDigitalStateChangeEvent event) {
					// display pin state on console
					System.out.println(" --> GPIO PIN STATE CHANGE: " + event.getPin() + " = " + event.getState());
				}

			});
			// create and register gpio pin listener
			switch4.addListener(new GpioPinListenerDigital() {
				public void handleGpioPinDigitalStateChangeEvent(GpioPinDigitalStateChangeEvent event) {
					// display pin state on console
					System.out.println(" --> GPIO PIN STATE CHANGE: " + event.getPin() + " = " + event.getState());
				}

			});

			System.out.println(" ... complete the GPIO #02 circuit and see the listener feedback here in the console.");

			// keep program running until user aborts (CTRL-C)
			while(true) {
				try {
					Thread.sleep(500);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}

			// stop all GPIO activity/threads by shutting down the GPIO controller
			// (this method will forcefully shutdown all GPIO monitoring threads and scheduled tasks)
			// gpio.shutdown();  // <--- implement this method call if you wish to terminate the Pi4J GPIO controller
		}
		
		
		
		
		
		
	}
