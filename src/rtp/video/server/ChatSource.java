package rtp.video.server;

import javax.media.Manager;
import javax.media.Player;
import javax.media.protocol.DataSource;

public class ChatSource extends Thread {

	private Player player;
	private DataSource dataSource;
	
	
	public ChatSource(DataSource dataSource) {
		this.dataSource = dataSource;
	}
	
	public void run() {
		playSource();
		
	}

	private void playSource() {
		try {
			player = Manager.createPlayer(dataSource);
			MyRTPListener l = new MyRTPListener(player, ServerStart.jf, ServerStart.panel);
			player.addControllerListener(l);
			player.start();
		} catch (Exception e) {
			e.printStackTrace();
		}
	
	}
	

}
