/**
 * 
 */
package le2lejosev3.robots.znap;

import java.util.logging.Logger;

import le2lejosev3.logging.Setup;
import le2lejosev3.pblocks.MediumMotor;
import le2lejosev3.pblocks.MoveSteering;
import le2lejosev3.pblocks.Random;
import le2lejosev3.pblocks.Sound;
import le2lejosev3.pblocks.UltrasonicSensor;
import le2lejosev3.pblocks.Wait;
import lejos.hardware.Button;
import lejos.hardware.port.MotorPort;
import lejos.hardware.port.Port;
import lejos.hardware.port.SensorPort;

/**
 * Znap
 * 
 * @author Roland Blochberger
 */
public class Znap {

	private static Class<?> clazz = Znap.class;
	private static final Logger log = Logger.getLogger(clazz.getName());

	// the robot configuration
	static final Port motorPortA = MotorPort.B; // medium motor
	static final Port motorPortB = MotorPort.A; // left motor
	static final Port motorPortC = MotorPort.D; // right motor
	static final Port usSensorPort = SensorPort.S3; // ultrasonic sensor

	// the variables common to both threads
	public static boolean chk = false;

	// loop interrupt flag
	public static boolean interruptROL = false;

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// setup logging to file
		Setup.log2File(clazz);
		log.fine("Starting ...");

		// instantiate and run the ZNP thread
		Thread znp = new ZnpThread();
		znp.start();

		// instantiate and run the MN thread
		Thread mn = new MnThread();
		mn.start();
	}

}

/**
 * The ZNP loop thread.
 */
class ZnpThread extends Thread {

	private static final Logger log = Logger.getLogger(ZnpThread.class.getName());

	private final UltrasonicSensor ultrasonic;
	private final MediumMotor motorB;

	/**
	 * Constructor
	 * (my model robot uses the medium motor on port A)
	 */
	public ZnpThread() {
		log.fine("");
		// instantiate the ultrasonic sensor
		ultrasonic = new UltrasonicSensor(Znap.usSensorPort);
		// instantiate a medium motor
		motorB = new MediumMotor(Znap.motorPortA);
	}

	/**
	 * @see java.lang.Thread#run()
	 */
	@Override
	public void run() {

		float distance = 50;
		// ZNP loop
		while (Button.ESCAPE.isUp()) {

			// Get ultrasonic distance in centimeters
			distance = ultrasonic.measureDistanceCentimeters();
			log.fine("distance: " + distance);

			if ((distance < 40) && Znap.chk) {
				// less than 40cm from obstacle:
				// Interrupt ROL loop
				Znap.interruptROL = true;

				// Get ultrasonic distance in centimeters
				distance = ultrasonic.measureDistanceCentimeters();
				log.fine("distance: " + distance);

				if (distance < 25) {
					// less than 25cm from obstacle:
					log.fine("distance < 25: T-rex roar");

					// Medium motor B on with power -100
					motorB.motorOn(-100);
					// Play sound file "T-rex roar" with volume 100 and wait until done
					Sound.playFile("T-rex roar", 100, Sound.WAIT);
					// Wait for 0.25 seconds
					Wait.time(0.25F);
					// Medium motor B off with brake
					motorB.motorOff(true);

					// Wait for 1 second
					Wait.time(1F);

					// Medium motor B on for 1 second with power 100 then brake
					motorB.motorOnForSeconds(100, 1F, true);

					// Wait for 0.5 seconds
					Wait.time(0.5F);
					
				} else {
					log.fine("distance > 25: Snake hiss");

					// Medium motor B on for 120 degrees and power -100 and brake afterwards
					motorB.motorOnForDegrees(-100, 120, true);

					// Play sound file "Snake hiss" with volume 100 and wait until done
					Sound.playFile("Snake hiss", 100, Sound.WAIT);

					// Medium motor B on for 1 second and power 100 and brake afterwards
					motorB.motorOnForSeconds(100, 1F, true);

					// Wait for 0.5 seconds
					Wait.time(0.5F);
				}

			} else {
				// re-enable the ROL loop
				Znap.interruptROL = false;
			}
		} // End ZNP loop

		log.fine("End");
	}
}

/**
 * The MN loop thread.
 */
class MnThread extends Thread {

	private static final Logger log = Logger.getLogger(MnThread.class.getName());
	
	private final MoveSteering move;

	/**
	 * Constructor
	 */
	public MnThread() {
		log.fine("");
		// instantiate a move steering for left and right motors
		move = new MoveSteering(Znap.motorPortB, Znap.motorPortC);
	}

	/**
	 * @see java.lang.Thread#run()
	 */
	@Override
	public void run() {

		int rotations = 0;
		// MN loop
		while (Button.ESCAPE.isUp()) {

			// ROL loop
			while (Button.ESCAPE.isUp() && !Znap.interruptROL) {

				// set chk to true
				Znap.chk = true;
				log.fine("Znap.chk: " + Znap.chk);

				// get random number between 1 and 3
				rotations = Random.numeric(1, 3);
				log.fine("Steering 100 rotations: " + rotations);
				// move steering on for rotations with power 100 and steering 100 and brake at end
				move.motorsOnForRotations(100, 100, rotations, true);

				// get random number between 1 and 3
				rotations = Random.numeric(1, 3);
				log.fine("Steering -100 rotations: " + rotations);
				// move steering on for rotations with power 100 and steering -100 and brake at end
				move.motorsOnForRotations(-100, 100, rotations, true);

				// get random number between 1 and 3
				rotations = Random.numeric(1, 3);
				log.fine("Steering 0 rotations: " + rotations);
				// move steering on for rotations with power -100 and steering 0 and brake at end
				move.motorsOnForRotations(0, -100, rotations, true);

			} // End ROL loop

			// set chk to false
			Znap.chk = false;
			log.fine("Znap.chk: " + Znap.chk);

			// move steering off and brake at end
			move.motorsOff(true);

			// wait 2 seconds
			Wait.time(2F);
		} // End MN loop

		log.fine("End");
	}
}
