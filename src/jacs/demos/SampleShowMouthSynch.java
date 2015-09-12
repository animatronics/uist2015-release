package jacs.demos;

import jacs.config.MicrocontrollerConnection;
import jacs.player.AnimatronicsShowPlayer;
import jacs.player.FormattedShowData;
import jacs.utilities.AnimatronicsUtilities;
import jmcc.Microcontroller;

/**
 * This simple program provides an example of playback with no audio.
 * <p>
 * 
 * MOUTH_SYNCH - is a single servo test with motions that correspond to the audio. This code expects the servo to be on
 * pin 2.
 * <p>
 * The audio was recorded in Audacity and exported to a WAV file.
 *
 * One servo motion track, stored in the first column of the CSV file, represents the mouth. This data was
 * programmatically generated by processing the WAV file.
 **/
public class SampleShowMouthSynch {

	public static void main(String[] args) throws Exception {

		// Initialized prerecorded show data
		String audioFile = "data/MouthSynchTestOneMinute.wav";
		byte[] pins = { (byte) 2 }; // List of exact pins to which servos are attached on the card.
		byte[][] servoMotions; // Each row of the array is a series of servo postions to be sent to a single pin.
		servoMotions = AnimatronicsUtilities.getBytes("data/MouthSynchTestOneMinute.csv", 1); // Read motion
		// tracks
		// from
		// CSV
		// file
		byte[] dummy = {}; // For recording motion tracks. Unused here.

		// Format data for use in the player using provided code
		FormattedShowData showData = new FormattedShowData(audioFile, pins, servoMotions, dummy);

		// Create a connection
		MicrocontrollerConnection mc = new MicrocontrollerConnection(Microcontroller.POLOLU_MICRO_MAESTRO);

		// Create a player using the connection
		AnimatronicsShowPlayer player = new AnimatronicsShowPlayer(mc);

		// initiate playback
		player.playShow(showData);

	}

}