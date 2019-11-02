package rtp.audio.mp3;
import java.io.File;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.net.URL;
import java.util.Vector;

import javax.media.*;
import javax.media.format.AudioFormat;
import javax.media.format.RGBFormat;
/**
 * 
 * This code requires mp3plugin.jar to run. You may find it in lib/mp3plugin.jar
 * @author hoymkot
 *
 */
public class SimpleAudioPlayer {

	public static void main(String args[]) {
		
		System.out.println("fun");
		Vector deviceInfo = CaptureDeviceManager.getDeviceList(new RGBFormat());
		System.out.println(deviceInfo.size());
		for (Object obj : deviceInfo ) {
			System.out.println(obj);
		}
		

	}
	
}
