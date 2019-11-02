package rtp.video.client;

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
			MyRTPListener l = new MyRTPListener(player, ClientStart.jf, ClientStart.panel);
			player.addControllerListener(l);
			player.start();
		} catch (Exception e) {
			e.printStackTrace();
		}
	
	}
	

}
