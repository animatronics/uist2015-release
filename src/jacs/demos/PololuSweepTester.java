package jacs.demos;

import java.util.Arrays;

import jssc.SerialPort;
import jssc.SerialPortException;
import jssc.SerialPortList;

/**
 * 
 * Program to run through the full range of motion [0,254] on each pin.
 * 
 * <b>Usage: </b>
 * 
 * This tool can be used to
 * <p>
 * <b>Verify</b> the correct port was used to connect. See {@link jacs.config.SystemPortsList} for details and
 * {@link jacs.demos.ViewSerialPortsList} tools.
 * <p>
 * <b>Calibrate</b> the min and max values for each servo. If a servo "grinds" towards the ends of the range rather than
 * continuing to move, the values of [0,254] should be clamped to a narrow range to avoid burning out the motor.
 * <p>
 * <i>Details: </i>
 * <p>
 * Correctly calibrated animatronics show movements, once a show has been properly computed and synchronized in software
 * or hardware, depend on a combination of individual servo characteristics and servo controller card configuration.
 * <p>
 * The Pololu card provided to UIST contestants has been preconfigured so that each of the 6 channels have been set to
 * the widest minimum and maximum microsecond range for the HS-485HB, which is [608, 2400].
 * <p>
 * The code base uses an 8-bit range [0,254] to generate commands that span the [608,2400] to ideally control servo
 * angular position of [0,180].
 * <p>
 * Ideal, however, may damage your servo by pushing it beyond it's actualy operating range, particularly the HS-82MG
 * microservo. Contestants should adjust the actual min and max of the 8-bit range based on their own calibration
 * results, which are easily determined using this tool to experiment with correct ranges.
 * 
 * <p>
 * By modifying the values in these parallel arrays, contestants can easily enable and disable the sweep test per pin
 * and adjust the min and max values per pin, based on individual servos attached to each. In this example, the min and
 * max range for servo on pin 2 is limited to [30, 220] and commands have been disabled for pins 0, 1 and 5.
 * 
 * <p>
 * {@code public static int[] pinsToTest = 0, 1, 2, 3, 4, 5 }
 * <p>
 * {@code static int[] minServoPosition = 0, 0, 30, 0, 0, 0 }
 * <p>
 * {@code public static int[] maxServoPosition = 254, 254, 220, 254, 254,254 }
 * <p>
 * {@code boolean[] testEnabled = false, false, true, true, true, false }
 * 
 * <p>
 * 
 * <p>
 * <b>Experiment</b> with timing values that on platform and data.
 * <p>
 * SERIAL_COMMAND_PAUSE - is used to delay between sending serial commands. Changes to this will affect the speed and
 * smoothness of the servo sweep. In a show mode, when commands to different servos are interleaved per frame, number
 * may need to be adjusted. The equivalent number for show playback is in {@link jacs.player.Timer_Settings}.
 * <p>
 * OBSERVER_PAUSE - slows down the time between each sweep for observation.
 * 
 * 
 * @author Ginger Alford
 *
 *
 */
public class PololuSweepTester {

	private static SerialPort port = null; // Port name depends on combination of servo controller and operating system

	// Defaults
	public static int MIN_SERVO_POSITION = 0;
	public static int MAX_SERVO_POSITION = 254;
	public static int NEUTRAL_SERVO_POSTION = 127;

	public static int MAX_NUMBER_SERVOS = 6;

	// Setting for min and max values per pin to calibrate to each servo individually
	public static int[] pinsToTest = { 0, 1, 2, 3, 4, 5 }; // pins
	public static int[] minServoPosition = { 0, 0, 30, 0, 0, 0 }; // tune range per pin
	public static int[] maxServoPosition = { 254, 254, 220, 254, 254, 254 }; // min and max values
	public static boolean[] testEnabled = { false, false, true, true, true, false }; // enable and disable commands per
																						// servo
	public static int SERIAL_COMMAND_PAUSE = 14; // microseconds to pause between sending serial commands to controller
	public static int OBSERVER_PAUSE = 1500; // 1.5 seconds

	public static void main(String[] args) throws InterruptedException {

		// TODO - use system port utility to make best guesss
		String[] portNames = SerialPortList.getPortNames();
		System.out.println(Arrays.toString(portNames));

		if (portNames.length < 1) {
			System.out.println("Empty Port List.");
		} else // create port
		{
			// TODO - handle Windows .v. Mac
			System.out.println("Attempting to open to: " + portNames[0]);
			port = new SerialPort(portNames[0]);

			if (port == null)
				System.out.println("Null Port.");

			else // openPort Connection
			{
				// OPEN CONNECTION
				try {
					port.openPort();
					System.out.println("Opened Port Successfully");
				} catch (SerialPortException e) {
					System.out.println("Error Opening Port: " + e);
				}

				try {
					System.out.println("Beginning series of calibration tests...");

					positionTest(127); // neutral position for all - end with this to relieve stress on servos
					System.out.println("All servos in 127 position (neutral) to avoid burnout");
					Thread.sleep(OBSERVER_PAUSE);

					System.out.println("Sweep full range for each servo that is enabled.");
					sweepTest();
					Thread.sleep(OBSERVER_PAUSE);
					System.out.println("Sweeps complete");

					System.out.println("Calibration tests complete...");

				} catch (Exception e) {
					System.out.println(" Error with Sweep Test " + e);
				}

				// CLOSE CONNECTION
				try {
					port.closePort();
					System.out.println("Closed Port Successfully.  You may disconnect the microcontroller.");

				} catch (SerialPortException e) {
					System.out.println("Error Closing Port: " + e);
				}
			} // else openPort connection
		} // else create port

	} // main

	/**
	 * This performs a sweep test on all pins that are enabled in the static data arrays {@link testEnabled} using
	 * pin-specific min and max values as specified in the {@link minServoPosition} and {@link maxServoPosition}.
	 * 
	 * @throws Exception
	 *             An exception is thrown if there is an error opening or writing to the serial port
	 */
	public static void sweepTest() throws Exception {
		try {

			for (int pin = 0; pin < pinsToTest.length; pin++) {
				if (testEnabled[pin])
					sweepTest(pin, minServoPosition[pin], maxServoPosition[pin], SERIAL_COMMAND_PAUSE);
			}

		} catch (Exception e) {
			System.out.println("Error Opening Port: " + e);
		}

	}

	public static void positionTest(int position) throws Exception {
		try {
			for (int pin = 0; pin < pinsToTest.length; pin++) {
				port.writeByte((byte) 255);
				port.writeByte((byte) pin);
				port.writeByte((byte) position);
				Thread.sleep(SERIAL_COMMAND_PAUSE); // Give servo time to catch up
			}

		} catch (Exception e) {
			System.out.println("Error Opening Port: " + e);
		}

	}

	// TODO - remove this method
	// public static void sweepTest(int pin) throws Exception {
	// sweepTest(pin, MIN_SERVO_POSITION, MAX_SERVO_POSITION, SERIAL_COMMAND_PAUSE);
	// }

	/**
	 * This sends a series of uniformly timed and spaced commands to sweep the servo through a full range of positions
	 * represented in the range [0,254]
	 * 
	 * @param pin
	 *            pin on controller to which servo is attached
	 * @param start
	 *            beginning position of sweep expressed in the range [ 0,254]
	 * @param stop
	 *            ending position of sweep expressed in the range [ 0,254]
	 * @param pause
	 *            delay in microseconds between issues of commands to serial port
	 * @throws Exception
	 */
	public static void sweepTest(int pin, int start, int stop, int pause) throws Exception {

		port.writeByte((byte) 255);
		port.writeByte((byte) pin);
		port.writeByte((byte) start);
		Thread.sleep(OBSERVER_PAUSE); // Pause before beginning sweep for user

		System.out.println("Sweeping " + pin + " from " + start + " to " + stop + " and back");

		for (int i = start; i <= stop; i++) {
			port.writeByte((byte) 255);
			port.writeByte((byte) pin);
			port.writeByte((byte) i);
			Thread.sleep(pause); // Give servo time to catch up
		}
		Thread.sleep(OBSERVER_PAUSE); // Pause before returning to min for user observation time

		for (int i = stop; i >= start; i--) {
			port.writeByte((byte) 255);
			port.writeByte((byte) pin);
			port.writeByte((byte) i);
			Thread.sleep(pause); // Give servo time to catch up
		}
		Thread.sleep(1000); // Pause before resetting to neutral

		port.writeByte((byte) 255);
		port.writeByte((byte) pin);
		port.writeByte((byte) 127);
		Thread.sleep(pause); // Give servo time to catch up
		Thread.sleep(OBSERVER_PAUSE); // Pause before beginning sweep for user observation time
	}
}
