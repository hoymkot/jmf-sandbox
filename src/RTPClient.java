import javax.media.*;
import javax.media.format.AudioFormat;

public class RTPClient implements ControllerListener, Runnable {

	Player p;
	MediaLocator src;

	public static void main(String[] args) {
    	Format input1 = new AudioFormat(AudioFormat.MPEGLAYER3);
    	Format input2 = new AudioFormat(AudioFormat.MPEG);
    	Format output = new AudioFormat(AudioFormat.LINEAR);
    	PlugInManager.addPlugIn(
    	        "com.sun.media.codec.audio.mp3.JavaDecoder",
    	        new Format[]{input1, input2},
    	        new Format[]{output},
    	        PlugInManager.CODEC);
		RTPClient rtp = new RTPClient("192.168.1.86");
		Thread t = new Thread(rtp);
		t.start();

	}

	public RTPClient(String ip) {
    	Format input1 = new AudioFormat(AudioFormat.MPEGLAYER3);
    	Format input2 = new AudioFormat(AudioFormat.MPEG);
    	Format output = new AudioFormat(AudioFormat.LINEAR);
    	PlugInManager.addPlugIn(
    	        "com.sun.media.codec.audio.mp3.JavaDecoder",
    	        new Format[]{input1, input2},
    	        new Format[]{output},
    	        PlugInManager.CODEC);
		String srcUrl = "rtp://" + ip + ":49150/audio/1";
		src = new MediaLocator(srcUrl);
	}

	public void run() {
		try {
			p = Manager.createPlayer(src);
			p.addControllerListener(this);
			p.start();
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(1);
		}
	}

	public synchronized void controllerUpdate(ControllerEvent evt) {
		if (evt instanceof EndOfMediaEvent) {
			System.exit(0);
		} else {
			System.out.println(evt.toString());
		}
	}
}