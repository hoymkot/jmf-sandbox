package rtp.audio.mp3;

import java.io.IOException;
import java.util.Vector;

import javax.media.*;
import javax.media.control.TrackControl;
import javax.media.format.AudioFormat;
import javax.media.pim.PlugInManager;
import javax.media.protocol.ContentDescriptor;
/**
 * Client for getting music via RTP 
 * This code requires mp3plugin.jar to run. You may find it in lib/mp3plugin.jar
 * @author hoymkot
 *
 */
public class RTPClientMP3 implements Runnable {

	MediaLocator src;

	public static void main(String[] args) {

		// make sure mp3plugin.jar is in the classpath

		RTPClientMP3 rtp = new RTPClientMP3("192.168.1.86");
		Thread t = new Thread(rtp);
		t.start();

	}

	public RTPClientMP3(String ip) {
		String srcUrl = "rtp://" + ip + ":49150/audio/1";
		src = new MediaLocator(srcUrl);
	}

	Processor processor;

	public void run() {
		try {
			
			Player player = Manager.createRealizedPlayer(src);
			player.start();
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(1);
		}
	}
}
