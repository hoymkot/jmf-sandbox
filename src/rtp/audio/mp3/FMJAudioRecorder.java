package rtp.audio.mp3;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.net.MalformedURLException;
import java.util.Vector;
import javax.media.*;
import javax.media.control.StreamWriterControl;
import javax.media.control.TrackControl;
import javax.media.format.AudioFormat;
import javax.media.format.RGBFormat;
import javax.media.protocol.FileTypeDescriptor;

import net.sf.fmj.media.cdp.GlobalCaptureDevicePlugger;

public class FMJAudioRecorder extends ControllerAdapter {
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

	public static void main(String args[]) throws NoProcessorException, IOException {
		GlobalCaptureDevicePlugger.addCaptureDevices();
		Vector<CaptureDeviceInfo> deviceList = CaptureDeviceManager
				.getDeviceList(new AudioFormat("linear", 44100, 16, 2));
		System.out.println(deviceList.size());
		for (Object obj : deviceList) {
			System.out.println(obj);
		}

		FMJAudioRecorder listener = new FMJAudioRecorder();
		
		CaptureDeviceInfo di = deviceList.firstElement();
		Processor p = Manager.createProcessor(di.getLocator());
		p.addControllerListener(listener);
		p.configure();
	}

	public void configureComplete(ConfigureCompleteEvent e) {
		Processor p = (Processor) (e.getSourceController());
		p.setContentDescriptor(new FileTypeDescriptor(FileTypeDescriptor.WAVE));
		TrackControl track[] = p.getTrackControls();
		boolean encodingPossible = false;
		for (int i = 0; i < track.length; i++) {
            try {
                track[i].setFormat(new AudioFormat(AudioFormat.IMA4_MS));
                encodingPossible = true;
            } catch (Exception exp) {
                // cannot convert to ima4
            	exp.printStackTrace();
                track[i].setEnabled(false);
            }
        }
	     if (!encodingPossible) {
             System.out.println("unable to do the convertion");
             System.exit(-1);
         } else {
        	 p.realize();
         }
	}

	public void realizeComplete(RealizeCompleteEvent e) {
		Processor p = (Processor) (e.getSourceController());
		try {
			File file =(new File("recording.wav"));
			System.out.println(file.toURL());
			MediaLocator dest = new MediaLocator(file.toURL());
			DataSink filewriter = null;
			filewriter = Manager.createDataSink(p.getDataOutput(), dest);
			filewriter.open();

			StreamWriterControl swc = (StreamWriterControl) p.getControl("javax.media.control.StreamWriterControl");
			if (swc != null)
				swc.setStreamSizeLimit(5000000);
			
			System.out.println("start recording");
			filewriter.start();
			p.start();
			Thread t = new Thread(new Runner(p ,filewriter));
			t.start();
			

		} catch (MalformedURLException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (NoDataSinkException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (NotRealizedError e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (SecurityException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

	}
	
	class Runner implements Runnable {
		Processor p ;
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

			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}
		
	}

}
