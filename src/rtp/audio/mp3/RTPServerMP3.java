package rtp.audio.mp3;
import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.MalformedURLException;

import javax.media.rtp.*;
import javax.media.rtp.rtcp.*;
import javax.media.*;
import javax.media.protocol.*;
import javax.media.control.*;
import javax.media.format.AudioFormat;
/**
 * Server for sending music via RTP 
 * This code requires mp3plugin.jar to run. You may find it in lib/mp3plugin.jar
 * @author hoymkot
 *
 */
public class RTPServerMP3 implements ControllerListener {
	private String ipAddress;
	Processor p;
	public static void main(String[] args) throws NoProcessorException, IOException {
    	Format input1 = new AudioFormat(AudioFormat.MPEGLAYER3);
    	Format input2 = new AudioFormat(AudioFormat.MPEG);
    	Format output = new AudioFormat(AudioFormat.LINEAR);
    	PlugInManager.addPlugIn(
    	        "com.sun.media.codec.audio.mp3.JavaDecoder",
    	        new Format[]{input1, input2},
    	        new Format[]{output},
    	        PlugInManager.CODEC);
		RTPServerMP3 rtp = new RTPServerMP3("192.168.1.86");
		rtp.p = Manager.createProcessor(new MediaLocator((new File( "roar_of_future.mp3")).toURL()));
		rtp.p.addControllerListener(rtp);
		rtp.p.configure();
	}
	public RTPServerMP3(String ip) throws MalformedURLException {
		ipAddress = ip;
	}
	private void setTrackFormat(Processor p) {
		// Get the tracks from the processor
		TrackControl[] tracks = p.getTrackControls();
		// Do we have atleast one track?
		if (tracks == null || tracks.length < 1) {
			System.out.println("Couldn't find tracks in processor");
			System.exit(1);
		}
		// Set the output content descriptor to RAW_RTP
		// This will limit the supported formats reported from
		// Track.getSupportedFormats to only valid RTP formats.
		ContentDescriptor cd = new ContentDescriptor(ContentDescriptor.RAW_RTP);
		p.setContentDescriptor(cd);
		Format supported[];
		Format chosen;
		boolean atLeastOneTrack = false;
		// Program the tracks.
		for (int i = 0; i < tracks.length; i++) {
			Format format = tracks[i].getFormat();
			System.out.println("seeing format " + format.getEncoding() + " for track " + i);
			if (tracks[i].isEnabled()) {
				supported = tracks[i].getSupportedFormats();
				for (int n = 0; n < supported.length; n++)
					System.out.println("Supported format: " + supported[n]);
				// We've set the output content to the RAW_RTP.
				// So all the supported formats should work with RTP.
				// We'll just pick the first one.
				if (supported.length > 0) {
					chosen = supported[0]; // this is where I tried changing formats
					tracks[i].setFormat(new AudioFormat(AudioFormat.DVI_RTP));
					System.err.println("Track " + i + " is set to transmit as: " + chosen);
					atLeastOneTrack = true;
				} else
					tracks[i].setEnabled(false);
			} else
				tracks[i].setEnabled(false);
		}
	}

	private void transmit(Processor p) {
		try {
			DataSource output = p.getDataOutput();
			PushBufferDataSource pbds = (PushBufferDataSource) output;
			RTPManager rtpMgr = RTPManager.newInstance();
			SessionAddress localAddr, destAddr;
			SendStream sendStream;
			int port = 49150;
			SourceDescription srcDesList[];
			localAddr = new SessionAddress(InetAddress.getLocalHost(), port/2+10);
			InetAddress ipAddr = InetAddress.getByName(ipAddress);
			destAddr = new SessionAddress(ipAddr, port);
			rtpMgr.initialize(localAddr);
			rtpMgr.addTarget(destAddr);
			sendStream = rtpMgr.createSendStream(output, 0);
			sendStream.start();
			System.err.println("Created RTP session: " + ipAddress + " " + port);
			p.start();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public synchronized void controllerUpdate(ControllerEvent evt) {
		if (evt instanceof RealizeCompleteEvent) {
			transmit(p);
		} else if (evt instanceof ConfigureCompleteEvent) {
			setTrackFormat(p);
			p.setContentDescriptor(new ContentDescriptor(ContentDescriptor.RAW_RTP));
			p.realize();
		} else if (evt instanceof EndOfMediaEvent) {
			System.exit(0);
		} 
	}
}