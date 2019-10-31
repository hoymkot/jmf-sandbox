/////////////////////////////////////////////////////
//		VideoReceive.java
//
//		Project 2: CSc561 - Multimedia Systems Lab
//
//	basic RTP media client that will playback video
//  streamed from a remote RTP server...
//  uses RTPManager...
//
//  
/////////////////////////////////////////////////////
/*
 *
 * Copyright (c) 1999-2001 Sun Microsystems, Inc. All Rights Reserved.
 *
 * Sun grants you ("Licensee") a non-exclusive, royalty free, license to use,
 * modify and redistribute this software in source and binary code form,
 * provided that i) this copyright notice and license appear on all copies of
 * the software; and ii) Licensee does not utilize the software in a manner
 * which is disparaging to Sun.
 *
 * This software is provided "AS IS," without a warranty of any kind. ALL
 * EXPRESS OR IMPLIED CONDITIONS, REPRESENTATIONS AND WARRANTIES, INCLUDING ANY
 * IMPLIED WARRANTY OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE OR
 * NON-INFRINGEMENT, ARE HEREBY EXCLUDED. SUN AND ITS LICENSORS SHALL NOT BE
 * LIABLE FOR ANY DAMAGES SUFFERED BY LICENSEE AS A RESULT OF USING, MODIFYING
 * OR DISTRIBUTING THE SOFTWARE OR ITS DERIVATIVES. IN NO EVENT WILL SUN OR ITS
 * LICENSORS BE LIABLE FOR ANY LOST REVENUE, PROFIT OR DATA, OR FOR DIRECT,
 * INDIRECT, SPECIAL, CONSEQUENTIAL, INCIDENTAL OR PUNITIVE DAMAGES, HOWEVER
 * CAUSED AND REGARDLESS OF THE THEORY OF LIABILITY, ARISING OUT OF THE USE OF
 * OR INABILITY TO USE SOFTWARE, EVEN IF SUN HAS BEEN ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGES.
 *
 * This software is not designed or intended for use in on-line control of
 * aircraft, air traffic, aircraft navigation or aircraft communications; or in
 * the design, construction, operation or maintenance of any nuclear
 * facility. Licensee represents and warrants that it will not use or
 * redistribute the Software for such purposes.
 */

import java.io.*;
import java.awt.*;
import java.net.*;
import java.awt.event.*;

import javax.media.*;
import javax.media.rtp.*;
import javax.media.rtp.event.*;
import javax.media.rtp.rtcp.*;
import javax.media.protocol.*;
import javax.media.format.*;
import javax.media.Format;
import javax.media.control.BufferControl;

/**
 * VideoReceive to receive RTP transmission using the new RTP API.
 */
public class VideoReceive implements ReceiveStreamListener, SessionListener,
	ControllerListener
{
    private String ipAddress;
    private String portStr;

    private RTPManager mgr = null;
    private PlayerWindow playerWindow = null;

    private boolean dataReceived = false;
    private Object dataSync = new Object();


    public VideoReceive(String ipAddress, String port) {
		this.ipAddress = ipAddress;
		this.portStr = port;
    }

    protected void initialize() {

        try {
	    InetAddress ipAddr;
	    int port = (new Integer(portStr)).intValue();
	    int ttl = 1;
	    SessionAddress localAddr = new SessionAddress();
	    SessionAddress destAddr;

		mgr = (RTPManager) RTPManager.newInstance();
		mgr.addSessionListener(this);
		mgr.addReceiveStreamListener(this);

		ipAddr = InetAddress.getByName(ipAddress);

		if( ipAddr.isMulticastAddress()) {
		    // local and remote address pairs are identical:
		    localAddr= new SessionAddress( ipAddr, port, ttl);
		    destAddr = new SessionAddress( ipAddr, port, ttl);
		} else {
		    localAddr= new SessionAddress( InetAddress.getLocalHost(), port);
            destAddr = new SessionAddress( ipAddr, port);
		}

		System.out.println("Opening RTP session for: addr: " + ipAddress + " port: " + port + " ttl: " + ttl);
		mgr.initialize(localAddr);

		// can try out some other buffer size to see if you can get better smoothness.
		BufferControl bc = (BufferControl)mgr.getControl("javax.media.control.BufferControl");
		if (bc != null)
		    bc.setBufferLength(350);

    	mgr.addTarget(destAddr);  //can have more sessions than just this one if you want

        } catch (Exception e){
            Fatal("Cannot create the RTP Session: " + e.getMessage());
        }

	// Wait for data to arrive before moving on.
	long then = System.currentTimeMillis();
	long waitingPeriod = 30000;  // wait for a maximum of 30 secs.

	try{
	    synchronized (dataSync) {
		while (!dataReceived &&
			System.currentTimeMillis() - then < waitingPeriod) {
		    if (!dataReceived)
			System.out.println("...Waiting for RTP data to arrive...");
		    dataSync.wait(1000);
		}
	    }
	} catch (Exception e) {
		System.err.println("message :"+e.toString());
	}

	if (!dataReceived) {
	    System.out.println("No RTP data was received.");
	    close();
	}

    }


    public boolean isDone() {
		return playerWindow == null;
    }


    /**
     * Close the players and the session managers.
     */
    protected void close() {

		try {
			playerWindow.close();
		} catch (Exception e) {}

		// close the RTP session.
		if (mgr != null) {
			mgr.removeTargets( "Closing session from VideoReceive");
			mgr.dispose();
			mgr = null;
		}
    }


    /**
     * SessionListener.
     */
    public synchronized void update(SessionEvent evt) {
		if (evt instanceof NewParticipantEvent) {
			Participant p = ((NewParticipantEvent)evt).getParticipant();
			System.out.println("Session Listener - A new participant has just joined: " + p.getCNAME());
		}
    }


    /**
     * ReceiveStreamListener
     */
    public synchronized void update( ReceiveStreamEvent evt) {

		RTPManager mgr = (RTPManager)evt.getSource();
		Participant participant = evt.getParticipant();	// could be null.
		ReceiveStream stream = evt.getReceiveStream();  // could be null.

		if (evt instanceof RemotePayloadChangeEvent) {
			Fatal("ReceiveStream Listener - Received an RTP PayloadChangeEvent. Sorry, cannot handle payload change.");
		}

		else if (evt instanceof NewReceiveStreamEvent) {

			try {
				stream = ((NewReceiveStreamEvent)evt).getReceiveStream();
				DataSource ds = stream.getDataSource();

				// Find out the formats.
				RTPControl ctl = (RTPControl)ds.getControl("javax.media.rtp.RTPControl");
				if (ctl != null){
					System.out.println("ReceiveStream Listener - Received new RTP stream: " + ctl.getFormat());
				} else
					System.out.println("ReceiveStream Listener - Received new RTP stream");

				if (participant == null)
					System.out.println("      The sender of this stream had yet to be identified.");
				else {
					System.out.println("      The stream comes from: " + participant.getCNAME());
				}

				// create a player by passing datasource to the Media Manager
				System.out.println("Creating player...");
				Player p = javax.media.Manager.createPlayer(ds);
				if (p == null)
					return;

				System.out.println("adding Listener...");
				p.addControllerListener(this);
				System.out.println("Realizing player...");
				p.realize();
				System.out.println("Creating playerWindow...");
				playerWindow = new PlayerWindow(p, stream);

				System.out.println("notifying initialize()...");
				// Notify initialize() that a new stream had arrived.
				synchronized (dataSync) {
					dataReceived = true;
					dataSync.notifyAll();
				}

			} catch (Exception e) {
				System.err.println("NewReceiveStreamEvent exception " + e.toString());
				return;
			}
		}

		else if (evt instanceof StreamMappedEvent) {

			 if (stream != null && stream.getDataSource() != null) {
				DataSource ds = stream.getDataSource();
				// Find out the formats.
				RTPControl ctl = (RTPControl)ds.getControl("javax.media.rtp.RTPControl");
				System.out.println("ReceiveStream Listener - The previously unidentified stream ");
				if (ctl != null)
					System.out.println("      " + ctl.getFormat());
				System.out.println("      had now been identified as sent by: " + participant.getCNAME());
			 }
		}

		else if (evt instanceof ByeEvent) {

			 System.out.println("ReceiveStream Listener - Got \"bye\" from: " + participant.getCNAME());
			 if (playerWindow != null) {
				playerWindow.close();
			 }
		}

    }


    /**
     * ControllerListener for the Player.
     */
    public synchronized void controllerUpdate(ControllerEvent ce) {

		Player p = (Player)ce.getSourceController();

		if (p == null)
			return;

		// Get this when the internal players are realized.
		if (ce instanceof RealizeCompleteEvent) {
			if (playerWindow == null) {
				// Some strange happened.
				Fatal("Internal error! -- playerWindow is null");
			}
			playerWindow.initialize();
			playerWindow.setVisible(true);
			p.start();
		}

		if (ce instanceof ControllerErrorEvent) {
			p.removeControllerListener(this);
			if (playerWindow != null) {
				playerWindow.close();
			}
			Fatal("VideoReceive internal error: " + ce);
		}

    }

    /**
     * GUI classes for the Player.
     */
    class PlayerWindow extends Frame implements WindowListener {

	Player player;
	ReceiveStream stream;

	PlayerWindow(Player p, ReceiveStream strm) {
		super("Video Client");
	    player = p;
	    stream = strm;
      addWindowListener(new WindowAdapter() {
      public void windowClosing(WindowEvent we) {
		  close();
        dispose();
        System.exit(0);
      }
    });
	}
  public void windowActivated(WindowEvent we) {}
  public void windowClosed(WindowEvent we) {}
  public void windowClosing(WindowEvent we) {}
  public void windowDeactivated(WindowEvent we) {}
  public void windowDeiconified(WindowEvent we) {}
  public void windowIconified(WindowEvent we) {}
  public void windowOpened(WindowEvent we) {}


	public void initialize() {
	    add(new PlayerPanel(player));
	}

	public void close() {
	    player.close();
	    setVisible(false);
	    dispose();
	}

	public void addNotify() {
	    super.addNotify();
	    pack();
	}
    }


    /**
     * GUI classes for the Player.
     */
    class PlayerPanel extends Panel {

	Component vc, cc;

	PlayerPanel(Player p) {
	    setLayout(new BorderLayout());
	    if ((vc = p.getVisualComponent()) != null)
		add("Center", vc);
	    if ((cc = p.getControlPanelComponent()) != null)
		add("South", cc);
	}

	public Dimension getPreferredSize() {
	    int w = 0, h = 0;
	    if (vc != null) {
		Dimension size = vc.getPreferredSize();
		w = size.width;
		h = size.height;
	    }
	    if (cc != null) {
		Dimension size = cc.getPreferredSize();
		if (w == 0)
		    w = size.width;
		h += size.height;
	    }
	    if (w < 160)
		w = 160;
	    return new Dimension(w, h);
	}
    }

    void Fatal (String s) {
	// Applications will make various choices about what
	// to do here. We print a message
		System.err.println("FATAL ERROR: " + s);
		System.exit(-1);
    }


    public static void main(String argv[]) {
		// We need two parameters to receive
		// For example,
		//   java VideoReceive 192.168.1.100 22044


		VideoReceive avReceive = new VideoReceive("192.168.1.86", "42050");

		avReceive.initialize();

		// Check to see if VideoReceive is done.
		try {
			while (!avReceive.isDone())
			Thread.sleep(1000);
		} catch (Exception e) {}

		System.out.println("Exiting VideoReceive");
    }

    static void prUsage() {
		System.err.println("Usage: VideoReceive <address> <port>");
		System.exit(-1);
    }
}