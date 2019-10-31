
import javax.media.*;
import javax.media.control.*;
import javax.media.protocol.*;
import javax.media.format.*;

import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.util.concurrent.TimeUnit;
import java.io.File;

/**
 * Creates a new media transmitter.  The media transmitter may be used to 
 * transmit a data source over a network.
 */
public class MediaTransmitter {

    /**
     * Output locator - this is the broadcast address for the media.
     */
    private MediaLocator mediaLocator = null;

    /**
     * The data sink object used to broadcast the results from the processor
     * to the network.
     */
    private DataSink dataSink = null;

    /**
     * The processor used to read the media from a local file, and produce an
     * output stream which will be handed to the data sink object for 
     * broadcast.
     */
    private Processor mediaProcessor = null;

    /**
     * The track formats used for all data sources in this transmitter.  It is 
     * assumed that this transmitter will always be associated with the same 
     * RTP stream format, so this is made static.
     */
//    private static final Format[] FORMATS = new Format[] {
//        new AudioFormat(AudioFormat.LINEAR)}; // for .wav audio files.
    private static final Format[] FORMATS = new Format[] {
    		new AudioFormat(AudioFormat.MPEG_RTP)};

    /**
     * The content descriptor for this transmitter.  It is assumed that this
     * transmitter always handles the same type of RTP content, so this is
     * made static.
     */
//    private static final ContentDescriptor CONTENT_DESCRIPTOR = 
//        new ContentDescriptor(ContentDescriptor.RAW_RTP);
    private static final ContentDescriptor CONTENT_DESCRIPTOR = 
    		new ContentDescriptor(ContentDescriptor.RAW_RTP);

    /**
     * Creates a new transmitter with the given outbound locator.
     */
    public MediaTransmitter(MediaLocator locator) {
        mediaLocator = locator;
    }

    /**
     * Starts transmitting the media.
     */
    public void startTransmitting() throws IOException {
        // start the processor
        mediaProcessor.start();

        // open and start the data sink
        dataSink.open();
        dataSink.start();
    }

    /**
     * Stops transmitting the media.
     */
    public void stopTransmitting() throws IOException {
        // stop and close the data sink
        dataSink.stop();
        dataSink.close();

        // stop and close the processor
        mediaProcessor.stop();
        mediaProcessor.close();
    }

    /**
     * Sets the data source.  This is where the transmitter will get the media
     * to transmit.
     */
    public void setDataSource(DataSource ds) throws IOException, 
        NoProcessorException, CannotRealizeException, NoDataSinkException {

        /* Create the realized processor.  By calling the 
           createRealizedProcessor() method on the manager, we are guaranteed 
           that the processor is both configured and realized already.  
           For this reason, this method will block until both of these 
           conditions are true.  In general, the processor is responsible 
           for reading the file from a file and converting it to
           an RTP stream.
        */
        mediaProcessor = Manager.createRealizedProcessor(
            new ProcessorModel(ds, FORMATS, CONTENT_DESCRIPTOR));

        /* Create the data sink.  The data sink is used to do the actual work 
           of broadcasting the RTP data over a network.
        */
        dataSink = Manager.createDataSink(mediaProcessor.getDataOutput(), 
                                          mediaLocator);
    }

    /**
     * Prints a usage message to System.out for how to use this class
     * through the command line.
     */
    public static void printUsage() {
        System.out.println("Usage: java MediaTransmitter mediaLocator " +
                           "mediaFile");
        System.out.println("  example: java MediaTransmitter " +
            "rtp://192.168.1.72:49150/audio mysong.mp3");
        System.out.println("  example: java MediaTransmitter " +
            "rtp://192.168.1.255:49150/audio mysong.mp3");
    }

    /**
     * Allows the user to run the class through the command line.
     * Only two arguments are allowed; these are the output media
     * locator and the mp3 audio file which will be broadcast
     * in the order.
     * @throws InterruptedException 
     */
    public static void main(String[] args) throws InterruptedException {
    	System.out.println(ManagementFactory.getRuntimeMXBean().getName());
    	Format input1 = new AudioFormat(AudioFormat.MPEGLAYER3);
    	Format input2 = new AudioFormat(AudioFormat.MPEG);
    	Format output = new AudioFormat(AudioFormat.LINEAR);
    	PlugInManager.addPlugIn(
    	        "com.sun.media.codec.audio.mp3.JavaDecoder",
    	        new Format[]{input1, input2},
    	        new Format[]{output},
    	        PlugInManager.CODEC);
    	TimeUnit.SECONDS.sleep(5);
    	System.out.println("process proceed after five seconds;");

        try {
                MediaLocator locator = new MediaLocator("rtp://192.168.1.86:49150/audio/1");
                
//                MediaLocator file = new MediaLocator((new File("r2.mp3").toURL()));
                
                MediaTransmitter transmitter = new MediaTransmitter(locator);
//                MediaTransmitter transmitter = new MediaTransmitter(file);
                System.out.println("-> Created media locator: '" + 
                                   locator + "'");

                /* Creates and uses a file reference for the audio file,
                   if a url or any other reference is desired, then this 
                   line needs to change.
                */
                File mediaFile = new File("roar_of_future.mp3");
                DataSource source = Manager.createDataSource(
                    new MediaLocator(mediaFile.toURL()));
                System.out.println("-> Created data source: '" + 
                                   mediaFile.getAbsolutePath() + "'");

                // set the data source.
                transmitter.setDataSource(source);
                System.out.println("-> Set the data source on the transmitter");
                
                // start transmitting the file over the network.
                transmitter.startTransmitting();
                System.out.println("-> Transmitting...");
                System.out.println("   Press the Enter key to exit");
                
                // wait for the user to press Enter to proceed and exit.
                System.in.read();
                System.out.println("-> Exiting");
                transmitter.stopTransmitting();
         
        } catch (Throwable t) {
            t.printStackTrace();
        }

        System.exit(0);
    }
}
