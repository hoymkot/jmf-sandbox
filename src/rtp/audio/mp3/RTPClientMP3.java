package rtp.audio.mp3;

import java.io.IOException;
import javax.media.*;
/**
 * Client for getting music via RTP 
 * This code requires mp3plugin.jar to run. You may find it in lib/mp3plugin.jar
 * @author hoymkot
 */
public class RTPClientMP3  {
	public static void main(String[] args) throws NoPlayerException, CannotRealizeException, IOException {
		String srcUrl = "rtp://192.168.1.86:49150/audio/1";
		MediaLocator src = new MediaLocator(srcUrl);
		Player player = Manager.createRealizedPlayer(src);
		player.start();

	}
}
