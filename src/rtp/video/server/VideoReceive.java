package rtp.video.server;

import java.net.InetAddress;

import javax.media.control.BufferControl;
import javax.media.protocol.DataSource;
import javax.media.rtp.Participant;
import javax.media.rtp.RTPControl;
import javax.media.rtp.RTPManager;
import javax.media.rtp.ReceiveStream;
import javax.media.rtp.ReceiveStreamListener;
import javax.media.rtp.SessionListener;
import javax.media.rtp.event.ByeEvent;
import javax.media.rtp.event.NewParticipantEvent;
import javax.media.rtp.event.NewReceiveStreamEvent;
import javax.media.rtp.event.ReceiveStreamEvent;
import javax.media.rtp.event.RemotePayloadChangeEvent;
import javax.media.rtp.event.SessionEvent;
import javax.media.rtp.event.StreamMappedEvent;

import net.sf.fmj.media.rtp.RTPSocketAdapter;

public class VideoReceive implements ReceiveStreamListener, SessionListener {
	String sessions[] = null;
	RTPManager mgrs[] = null;
	boolean dataReceived = false;
	Object dataSync = new Object();

	public VideoReceive(String sessions[]) {
		this.sessions = sessions;
	}

	protected boolean initialize() {
		try {
			mgrs = new RTPManager[sessions.length];
			SessionLabel session;
			for (int i = 0; i < sessions.length; i++) {
				session = new SessionLabel();
				System.out.println(" - Open RTP Session for: addr: " + session.addr + " port: " + session.port
						+ " ttl: " + session.ttl);
				mgrs[i] = (RTPManager) RTPManager.newInstance();
				mgrs[i].addSessionListener(this);
				mgrs[i].addReceiveStreamListener(this);
				mgrs[i].initialize(
						new RTPSocketAdapter(InetAddress.getByName(session.addr), session.port, session.ttl));
				BufferControl bc = (BufferControl) mgrs[i].getControl("javax.media.control.BufferControl");
				if (bc != null) {
					bc.setBufferLength(350);
				}
			}
		} catch (Exception e) {
			System.err.println("Cannot create the RTP session: " + e.getMessage());
			return false;
		}
		long then = System.currentTimeMillis();
		long waitingPeriod = 300000;
		try {
			synchronized (dataSync) {
				while (!dataReceived && System.currentTimeMillis() - then < waitingPeriod) {
					if (!dataReceived) {
						System.err.println(" -- waiting for RTP data to arrive ");
						dataSync.wait(1000);
					}
				}
			}
		} catch (Exception e) {

		}
		if (!dataReceived) {
			System.err.println("No RTP data was received.");
			close();
			return false;
		}
		return true;
	}

	private void close() {
		for (int i = 0; i < mgrs.length; i++) {
			if (mgrs[i] != null) {
				mgrs[i].removeTargets("Closing Session from AVREcceive3");
				mgrs[i].dispose();
				mgrs[i] = null;
			}
		}

	}

	@Override
	public synchronized void update(SessionEvent evt) {
		if (evt instanceof NewParticipantEvent) {
			Participant p = ((NewParticipantEvent) evt).getParticipant();
			System.err.println(" - A new participant had just joined: " + p.getCNAME());
		}

	}

	public synchronized void update(ReceiveStreamEvent evt) {
		Participant participant = evt.getParticipant();
		ReceiveStream stream = evt.getReceiveStream();
		if (evt instanceof RemotePayloadChangeEvent) {
			System.err.println(" -- Received an RTP Payload Change Event.");
			System.err.println("Sorry, cannot handle payload change");
			System.exit(0);
		}
		if (evt instanceof NewReceiveStreamEvent) {
			try {
				stream = ((NewReceiveStreamEvent) evt).getReceiveStream();
				DataSource ds = stream.getDataSource();
				RTPControl ctl = (RTPControl) ds.getControl("javax.media.rtp.RTPControl");
				if (ctl != null) {
					System.err.println(" - Received new RTP Stream: " + ctl.getFormat());

				} else {
					System.err.println(" -- Received new RTP stream");

				}
				if (participant == null) {
					System.err.println(" the sender of this stream had yet to be identified");

				} else {
					System.err.println(" The stream comes from : " + participant.getCNAME());
				}

				ChatSource cs = new ChatSource(ds);
				cs.start();

				synchronized (dataSync) {
					dataReceived = true;
					dataSync.notifyAll();
				}
			} catch (Exception e) {
				e.printStackTrace();
				return;
			}
		} else if (evt instanceof StreamMappedEvent) {
			if (stream != null && stream.getDataSource() != null) {
				DataSource ds = stream.getDataSource();
				RTPControl ctl = (RTPControl) ds.getControl("javax.media.rtp.RTPControl");
				System.err.println(" -- The previously unindentified stream");
				if (ctl != null) {
					System.out.println(" " + ctl.getFormat());
					System.err.println(" had now been identified as sent by: " + participant.getCNAME());
				}

			}
		} else if (evt instanceof ByeEvent) {
			System.err.println(" -- GOT \"BYE\" from: " + participant.getCNAME());
		}

	}

}
