package video;

import java.awt.Component;

import javax.media.ControllerEvent;
import javax.media.ControllerListener;
import javax.media.Player;
import javax.media.RealizeCompleteEvent;
import javax.swing.JFrame;

public class MyControllerListener implements ControllerListener {

	private Player player;
	private JFrame jf;

	public MyControllerListener(Player player, JFrame jf) {
		this.player = player;
		this.jf = jf;

	}

	@Override
	public synchronized void controllerUpdate(ControllerEvent e) {
		if (e instanceof RealizeCompleteEvent) {
			Component comp;
			if ((comp = player.getVisualComponent()) != null) {
				jf.add("Center", comp);
			}
			if ((comp = player.getControlPanelComponent()) != null) {
				jf.add("South", comp);
			}
		}
		jf.validate();
	}

}
