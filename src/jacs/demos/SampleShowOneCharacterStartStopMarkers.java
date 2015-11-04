package jacs.demos;

import jacs.config.MicrocontrollerConnection;
import jacs.player.AnimatronicsShowPlayer;
import jacs.player.FormattedShowData;
import jacs.utilities.AnimatronicsUtilities;
import jmcc.Microcontroller;

/**
 * This simple program provides a tool for UIST contestants to test basic animatronics connections, understand the data
 * required by the player, observe servo motions and see the simplest API that can be required to use the code base for
 * show playback.
 * <p>
 * *
 * <li>
 * This is a prototypical animatronics single character show with three servo motion tracks. The story told in this show
 * is a short monologue from the Alpaca, who expresses frustration at being misidentified as a llama.
 * <p>
 * The show requires 2 supporting files, a .WAV file for audio and a .CSV file that has corresponding servo motions
 * <p>
 * After the script was written, a student recorded using Audacity, used the pitch shift tool to create a character
 * voice and then exported the recording as a .WAV file to be used for the audio portion of the animatronics show
 * playback.
 * <p>
 * There are three motion servo tracks stored as columns in the .CSV file. One servo motion track, stored in the first
 * column of the CSV file, represents the mouth. This data was programmatically generated by processing the WAV file.
 * The other two servos represent head and neck motion, easily implemented with a pan and tilt mechanism. Columns 2 and
 * 3 of the CSV file store these motions. This data was generated from real time recorded joystick motion.
 * <p>
 * 
 * The show requires an array of pin numbers representing the current physical connection of the servos to the servo
 * controller card. If the servos are moved to different pins, then the corresponding row in the
 * prerecordedShowsPinConfigurations array must be changed to reflect it. No other change is required.
 * <p>
 * Supporting data files are provided in the {@link data } folder.
 **/

public class SampleShowOneCharacterStartStopMarkers {

	public static void main(String[] args) throws Exception {

		// Initialized prerecorded show data
		String audioFile = "data/AlpacaOESISDemo.wav";
		byte[] pins = { (byte) 2, (byte) 3, (byte) 4 }; // List of exact pins to which servos are attached on the card.
		byte[][] servoMotions; // Each row of the array is a series of servo postions to be sent to a single pin.
		servoMotions = AnimatronicsUtilities.getBytes("data/AlpacaOESISDemo.csv", 3); // Read motion tracks from
																						// CSV
		// file
		byte[] dummy = {}; // For recording motion tracks. Unused here.

		// Format data for use in the player using provided code
		FormattedShowData showData = new FormattedShowData(audioFile, pins, servoMotions, dummy);

		// Create a connection
		MicrocontrollerConnection mc = new MicrocontrollerConnection(Microcontroller.POLOLU_MICRO_MAESTRO);

		// Create a player using the connection
		AnimatronicsShowPlayer player = new AnimatronicsShowPlayer(mc);

		// initiate playback with start and stop markers
		long startTime = 2000;
		long stopTime = 1000;
		player.playShow(showData, startTime, stopTime);

	}
}
