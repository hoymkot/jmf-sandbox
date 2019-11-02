package video;

import javax.media.*;

import javax.swing.*;

import net.sf.fmj.media.RegistryDefaults;
import net.sf.fmj.utility.ClasspathChecker;

import java.awt.*;
import java.awt.event.*;

import java.io.*;
import java.lang.reflect.Field;

/**
 * An instance of the MediaPlayerFrame may be used to display any media
 * recognized * by JMF. This is intended to be a very simple GUI example,
 * displaying all possible controls for the given media type.
 */
public class MediaPlayerFrame extends JFrame {

	/**
	 * The frame title.
	 */
	private static final String FRAME_TITLE = "developerWorks JMF Tutorial " + "Media Player";

	/**
	 * The panel title of the main control panel.
	 */
	private static final String CONTROL_PANEL_TITLE = "Control Panel";

	// location and size variables for the frame.
	private static final int LOC_X = 100;
	private static final int LOC_Y = 100;
	private static final int HEIGHT = 500;
	private static final int WIDTH = 500;

	/**
	 * The current player.
	 */
	private Player player = null;

	/**
	 * The tabbed pane for displaying controls.
	 */
	private JTabbedPane tabPane = null;

	/**
	 * Create an instance of the media frame. No data will be displayed in the frame
	 * until a player is set.
	 */
	public MediaPlayerFrame() {
		super(FRAME_TITLE);
		setLocation(LOC_X, LOC_Y);
		setSize(WIDTH, HEIGHT);

		tabPane = new JTabbedPane();
		getContentPane().add(tabPane);

		/*
		 * adds a window listener so that the player may be cleaned up before the frame
		 * actually closes.
		 */
		addWindowListener(new WindowAdapter() {
			/**
			 * Invoked when the frame is being closed.
			 */
			public void windowClosing(WindowEvent e) {
				closeCurrentPlayer();

				/*
				 * Closing this frame will cause the entire application to exit. When running
				 * this example as its own application, this is fine - but in general, a closing
				 * frame would not close the entire application. If this behavior is not
				 * desired, comment out the following line:
				 */
				System.exit(0);
			}
		});
	}

	/**
	 * Creates the main panel. This panel will contain the following if they exist:
	 * - The visual component: where any visual data is displayed, i.e. a movie uses
	 * this control to display the video. - The gain component: where the
	 * gain/volume may be changed. This is often * contained in the control panel
	 * component (below.) - The control panel component: time and some extra info
	 * regarding the media.
	 */
	private JPanel createMainPanel() {
		JPanel mainPanel = new JPanel();
//		FlowLayout fl = new FlowLayout();
		GridBagLayout gbl = new GridBagLayout();
		GridBagConstraints gbc = new GridBagConstraints();

		mainPanel.setLayout(gbl);
//		mainPanel.setLayout(fl);

		boolean visualComponentExists = false;

		// if the visual component exists, add it to the newly created panel.
		if (player.getVisualComponent() != null) {
			visualComponentExists = true;
			gbc.gridx = 0;
			gbc.gridy = 0;
//			gbc.weightx = 1;
//			gbc.weighty = 1;
			gbc.weightx = 1;
			gbc.weighty = 1;
			gbc.fill = GridBagConstraints.BOTH;
			mainPanel.add(player.getVisualComponent(), gbc);
//			mainPanel.add(player.getVisualComponent());
		} else {
			System.out.println("No Visual Component");

		}

		// if the gain control component exists, add it to the new panel.
		if ((player.getGainControl() != null) && (player.getGainControl().getControlComponent() != null)) {
			gbc.gridx = 1;
			gbc.gridy = 0;
			gbc.weightx = 0;
			gbc.weighty = 1;
			gbc.gridheight = 2;
			gbc.fill = GridBagConstraints.VERTICAL;
			mainPanel.add(player.getGainControl().getControlComponent(), gbc);
//			mainPanel.add(player.getGainControl().getControlComponent());
		} else {
			System.out.println("No Gain Control");
		}

		// Add the control panel component if it exists (it should exists in
		// all cases.)
		if (player.getControlPanelComponent() != null) {
			gbc.gridx = 0;
			gbc.gridy = 1;
			gbc.weightx = 1;
			gbc.gridheight = 1;

			if (visualComponentExists) {
				gbc.fill = GridBagConstraints.BOTH;
				gbc.weighty = 0.05;
			} else {
				gbc.fill = GridBagConstraints.BOTH;
				gbc.weighty = 1;
			}

			mainPanel.add(player.getControlPanelComponent(), gbc);
//			mainPanel.add(player.getControlPanelComponent());
		} else {
			System.out.println("No Control Panel");
		}

		return mainPanel;
	}

	/**
	 * Sets the media locator. Setting this to a new value effectively discards any
	 * Player which may have already existed.
	 * 
	 * @param locator the new MediaLocator object.
	 * @throws IOException            indicates an IO error in opening the media.
	 * @throws NoPlayerException      indicates no player was found for the media
	 *                                type.
	 * @throws CannotRealizeException indicates an error in realizing the media file
	 *                                or stream.
	 */
	public void setMediaLocator(MediaLocator locator) throws IOException, NoPlayerException, CannotRealizeException {

		// create a new player with the new locator. This will effectively
		// stop and discard any current player.
		setPlayer(Manager.createRealizedPlayer(locator));
	}

	/**
	 * Sets the player reference. Setting this to a new value will discard any
	 * Player which already exists. It will also refresh the contents of the pane
	 * with the components for the new player. A null value will stop the discard
	 * the current player and clear the contents of the frame.
	 */
	public void setPlayer(Player newPlayer) {
		// close the current player
		closeCurrentPlayer();

		player = newPlayer;

		// refresh the tabbed pane.
		tabPane.removeAll();

		if (player == null)
			return;

		// add the new main panel
		tabPane.add(CONTROL_PANEL_TITLE, createMainPanel());

		// add any other controls which may exist in the player. These
		// controls should already contain a name which is used in the
		// tabbed pane.
		Control[] controls = player.getControls();
		for (int i = 0; i < controls.length; i++) {
			if (controls[i].getControlComponent() != null) {
				tabPane.add(controls[i].getControlComponent());
			}
		}
	}

	/**
	 * Stops and closes the current player if one exists.
	 */
	private void closeCurrentPlayer() {
		if (player != null) {
			player.stop();
			player.close();
		}
	}
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


	/**
	 * Allows the user to run the class through the command line. Only one argument
	 * is allowed, which is the media locator.
	 */
	public static void main(String[] args) {
		try {
			
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
			MediaPlayerFrame mpf = new MediaPlayerFrame();

			/*
			 * The following line creates a media locator using the String passed in through
			 * the command line. This version should be used when receiving media streamed
			 * over a network.
			 */
			// mpf.setMediaLocator(new MediaLocator(args[0]));

			/*
			 * the following line may be used to create and set the media locator from a
			 * simple file name. This works fine when playing local media. To play media
			 * streamed over a network, you should use the previous setMediaLocator() line
			 * and comment this one out.
			 */
			MediaLocator ml = new MediaLocator((new File("output.mov")).toURL());
			mpf.setMediaLocator(ml);
			mpf.show();
			
			
//			Player player = Manager.createPlayer(ml);
//			player.start();
			
		} catch (Throwable t) {
			t.printStackTrace();
		}
	}
}