package rtp.audio.mp3;
import java.io.File;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.net.URL;

import javax.media.*;
import javax.media.format.AudioFormat;
/**
 * 
 * This code requires mp3plugin.jar to run. You may find it in lib/mp3plugin.jar
 * @author hoymkot
 *
 */
public class SimpleAudioPlayer {

	public static void main(String args[]) throws NoPlayerException, CannotRealizeException, IOException {
		MediaLocator ml = new MediaLocator((new File("roar_of_future.mp3").toURL()));
		Player player = Manager.createRealizedPlayer(ml);
		player.start();
	}
	
}
