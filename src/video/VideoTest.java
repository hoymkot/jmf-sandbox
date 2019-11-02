package video;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.net.URL;
import javax.media.MediaLocator;
import javax.media.NoPlayerException;
import javax.media.CannotRealizeException;
import javax.media.Manager;
import javax.media.Player;
import javax.swing.JFrame;

import net.sf.fmj.media.RegistryDefaults;
import net.sf.fmj.utility.ClasspathChecker;
import net.sf.fmj.utility.JmfUtility;

public class VideoTest extends JFrame {
	
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
	private Player player;

	public static void main(String[] args) {
		
		
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
		VideoTest vt = new VideoTest();
		vt.initUI();

	}

	private void initUI() {
		this.setTitle("JMF Video Player Example");
		this.setSize(400, 300);
		this.setDefaultCloseOperation(3);
		this.setVisible(true);

		this.add(player.getVisualComponent());
		player.start();
	}

	public VideoTest() {
		try {
			MediaLocator ml = new MediaLocator((new File("output.mov")).toURL());
			player = Manager.createRealizedPlayer(ml);
			MyControllerListener l = new MyControllerListener(player, this);
			player.addControllerListener(l);
		} catch (NoPlayerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (CannotRealizeException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
