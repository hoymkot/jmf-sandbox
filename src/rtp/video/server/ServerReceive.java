package rtp.video.server;

public class ServerReceive {



	public void startReceive() {
		try {
			String[] sessions = new String[1];
			sessions[0] = "192.168.1.86";
			System.out.println("session is: " + sessions[0]);
			VideoReceive video = new VideoReceive(sessions);
			video.initialize();
		} catch (Exception e) {
			e.printStackTrace();
		}

	}
}
