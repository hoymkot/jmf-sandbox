package rtp.video.client;

import java.io.IOException;
import java.lang.reflect.Field;
import java.net.InetAddress;
import java.util.logging.Logger;
import javax.media.*;
import javax.media.control.QualityControl;
import javax.media.control.TrackControl;
import javax.media.format.*;
import javax.media.protocol.*;
import javax.media.rtp.RTPManager;
import javax.media.rtp.SendStream;
import javax.media.rtp.rtcp.SourceDescription;
import net.sf.fmj.media.RegistryDefaults;
import net.sf.fmj.media.cdp.GlobalCaptureDevicePlugger;
import net.sf.fmj.media.rtp.RTPSocketAdapter;
import net.sf.fmj.utility.*;




public class RTPVideoWebcam extends ControllerAdapter {
	private static final Logger logger = LoggerSingleton.logger;

	static {
		System.setProperty("java.library.path", "D:/fmj-sf/native/win32-x86/");
		try {
			final Field sysPathsField = ClassLoader.class.getDeclaredField("sys_paths");
			sysPathsField.setAccessible(true);
			sysPathsField.set(null, null);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void main(String args[]) throws IOException, NoPlayerException {
		ClientStart.initUI();
		// The following is require to register the plugins
		if (!ClasspathChecker.checkAndWarn()) {
			// JMF is ahead of us in the classpath. Let's do some things to make this go
			// more smoothly.

			// Let's register our own prefixes, etc, since they won't generally be if JMF is
			// in charge.
			RegistryDefaults.registerAll(RegistryDefaults.FMJ | RegistryDefaults.THIRD_PARTY);
			// RegistryDefaults.unRegisterAll(RegistryDefaults.JMF); // TODO: this can be
			// used to make some things that work in FMJ but not in JMF, work, like
			// streaming mp3/ogg.
			// TODO: what about the removal of some/reordering?
		}

		GlobalCaptureDevicePlugger.addCaptureDevices(); // TODO: this needs to be done globally somewhere.
		final java.util.Vector<CaptureDeviceInfo> vectorDevices = CaptureDeviceManager.getDeviceList(null);
		CaptureDeviceInfo device = (CaptureDeviceInfo) vectorDevices.get(0);

		try {

			dataOutput = Manager.createDataSource(device.getLocator());
			dataClone = Manager.createCloneableDataSource(dataOutput);
			ds = ((SourceCloneable) dataClone).createClone();
			processor = Manager.createProcessor(ds);
			processor.addControllerListener(new StateListener());
			processor.configure();

			Thread t = new Thread(new Runner());
			t.start();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	static Processor processor = null;

	static class StateListener extends ControllerAdapter {

		public void configureComplete(ConfigureCompleteEvent e) {
			ContentDescriptor cd = new ContentDescriptor(ContentDescriptor.RAW_RTP);
			processor.setContentDescriptor(cd);
			processor.realize();
		}

		public void realizeComplete(RealizeCompleteEvent e) {
			Format supported[];
			Format chosen;
			TrackControl[] tracks = ((Processor) (e.getSourceController())).getTrackControls();
			boolean atLeastOneTrack = false;
			for (int i = 0; i < tracks.length; i++) {
				if (tracks[i].isEnabled()) {

					supported = tracks[i].getSupportedFormats();

					for (int j = 0; j < supported.length; j++)
						System.out.println(supported[j]);
					if (supported.length > 0) {
						chosen = supported[0];
						tracks[i].setFormat(chosen);
						System.out.println("Track " + i + " is set to transmit as: " + chosen);
						atLeastOneTrack = true;

					} else {
						tracks[i].setEnabled(false);
					}
				}
			}

			setJPEGQuality(processor, 0.5f);
			dataOutput = processor.getDataOutput();

			createTransmitter();
		}

	}

	static DataSource dataOutput;
	static DataSource dataClone;
	static DataSource ds;

	private static void setJPEGQuality(Player p, float val) {
		Control cs[] = p.getControls();
		QualityControl qc = null;
		VideoFormat jpegFmt = new VideoFormat(VideoFormat.H261_RTP);
		for (int i = 0; i < cs.length; i++) {
			if (cs[i] instanceof QualityControl && cs[i] instanceof Owned) {
				Object owner = ((Owned) cs[i]).getOwner();
				if (owner instanceof Codec) {
					Format fmts[] = ((Codec) owner).getSupportedOutputFormats(null);

					for (int j = 0; j < fmts.length; j++) {
						if (fmts[j].matches(jpegFmt)) {
							qc = (QualityControl) cs[i];
							qc.setQuality(val);
							System.out.println("- Setting quality to " + val + " on" + qc);
							break;
						}
					}
				}
				if (qc != null)
					break;

			}
		}

	}

	static RTPManager[] rtpMgrs;
	static int portBase = 1234;
	static String ipAddress = "192.168.1.86";

	static private String createTransmitter() {
		PushBufferDataSource pbds = (PushBufferDataSource) dataOutput;
		PushBufferStream pbss[] = pbds.getStreams();
		rtpMgrs = new RTPManager[pbss.length];
		SendStream sendStream;
		int port;
		SourceDescription srcDesList[];
		for (int i = 0; i < pbss.length; i++) {
			try {
				rtpMgrs[i] = RTPManager.newInstance();
				port = portBase + 2 * i;
				RTPSocketAdapter RTPSA = new RTPSocketAdapter(InetAddress.getByName(ipAddress), port);
				rtpMgrs[i].initialize(RTPSA);
				System.out.println("create RTP session: " + ipAddress + " " + port);
				sendStream = rtpMgrs[i].createSendStream(dataOutput, i);
				sendStream.start();

				ChatSource cs = new ChatSource(dataClone);
				cs.start();
				processor.start();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		return null;
	}

	static class Runner implements Runnable {
		Processor p;
		DataSink fw;

		public Runner(Processor p, DataSink filewriter) {
			this.p = p;
			fw = filewriter;
		}

		public Runner() {
			// TODO Auto-generated constructor stub
		}

		@Override
		public void run() {
			try {
				System.out.println("before sleep");
				Thread.currentThread().sleep(15000);
				System.out.println("after sleep");
//				p.close();
//				fw.close();
				System.exit(0);

			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

	}

}
