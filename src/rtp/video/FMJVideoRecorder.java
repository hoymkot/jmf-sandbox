package rtp.video;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.logging.LogManager;
import java.util.logging.Logger;
import javax.media.*;
import javax.media.format.*;
import javax.media.protocol.*;
import net.sf.fmj.media.RegistryDefaults;
import net.sf.fmj.media.cdp.GlobalCaptureDevicePlugger;
import net.sf.fmj.utility.*;

public class FMJVideoRecorder extends ControllerAdapter {
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

		// The following is require to register the plugins
		if (!ClasspathChecker.checkAndWarn()) {
			// JMF is ahead of us in the classpath. Let's do some things to make this go
			// more smoothly.
			logger.info("Enabling JMF logging");
			if (!JmfUtility.enableLogging())
				logger.warning("Failed to enable JMF logging");

			// Let's register our own prefixes, etc, since they won't generally be if JMF is
			// in charge.
			logger.info("Registering FMJ prefixes and plugins with JMF");
			RegistryDefaults.registerAll(RegistryDefaults.FMJ | RegistryDefaults.THIRD_PARTY);
			// RegistryDefaults.unRegisterAll(RegistryDefaults.JMF); // TODO: this can be
			// used to make some things that work in FMJ but not in JMF, work, like
			// streaming mp3/ogg.
			// TODO: what about the removal of some/reordering?
		}

		GlobalCaptureDevicePlugger.addCaptureDevices(); // TODO: this needs to be done globally somewhere.
		final java.util.Vector vectorDevices = CaptureDeviceManager.getDeviceList(null);
		CaptureDeviceInfo device = (CaptureDeviceInfo) vectorDevices.get(0);

		Format formats[] = new Format[2];
		formats[0] = new AudioFormat(AudioFormat.LINEAR);
		formats[1] = new VideoFormat(VideoFormat.RGB);
		FileTypeDescriptor outputType = new FileTypeDescriptor(FileTypeDescriptor.QUICKTIME);
//		FileTypeDescriptor outputType = new FileTypeDescriptor(FileTypeDescriptor.WAVE);
		try {
			Processor p = Manager.createRealizedProcessor(new ProcessorModel(formats, outputType));
			// get the output of the processor
			DataSource source = p.getDataOutput();
			// create a File protocol MediaLocator with the location
			// of the file to which bits are to be written
			MediaLocator dest = new MediaLocator((new File("output.mov")).toURL());
			// create a datasink to do the file writing & open the
			// sink to make sure we can write to it.
			DataSink filewriter = null;
			filewriter = Manager.createDataSink(source, dest);
			filewriter.open();
			// now start the filewriter and processor
			filewriter.start();
			p.start();
			
			Thread t = new Thread(new Runner(p ,filewriter));
			t.start();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	static class Runner implements Runnable {
		Processor p;
		DataSink fw;
		public Runner(Processor p, DataSink filewriter) {
			this.p = p;
			fw = filewriter;
		}
		@Override
		public void run() {
			try {
				System.out.println("before sleep");
				Thread.currentThread().sleep(5000);
				System.out.println("after sleep");
				p.close();
				fw.close();
				System.exit(0);

			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

	}

}
