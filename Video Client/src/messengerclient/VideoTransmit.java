package messengerclient;

import java.awt.*;
import javax.media.*;
import javax.media.protocol.*;
import javax.media.protocol.DataSource;
import javax.media.format.*;
import javax.media.control.TrackControl;
import javax.media.control.QualityControl;
import java.io.*;

public class VideoTransmit {

    // Input MediaLocator
    // 파일 또는 http 또는 캡처 소스일 수 있습니다.
    private MediaLocator locator;
    private String ipAddress;
    private String port;

    private Processor processor = null;
    private DataSink  rtptransmitter = null;
    private DataSource dataOutput = null;
    
    public VideoTransmit(MediaLocator locator,
						 String ipAddress,
						 String port) {
		this.locator = locator;
		this.ipAddress = ipAddress;
		this.port = port;
    }

    /**
     * 전송을 시작합니다. 전송이 정상적으로 시작된 경우 null을 반환합니다.
     * 그렇지 않으면 설치에 실패한 이유가 포함된 문자열이 반환됩니다.
     */
    public synchronized String start() {
	String result;

	// 프로세서 생성하기 for the specified media locator하고 JPEG/RTP를 출력하도록 프로그래밍합니다.
	result = createProcessor();
	if (result != null)
	    return result;

	// RTP 세션을 생성하여 프로세서의 출력을 지정된 IP 주소와 포트 번호로 전송합니다.
	result = createTransmitter();
	if (result != null) {
	    processor.close();
	    processor = null;
	    return result;
	}

	// 쓰레드 시작
	processor.start();
	
	return null;
    }

    /**
     * 이미 시작된 경우 전송을 중지합니다.
     */
    public void stop() {
		synchronized (this) {
		    if (processor != null) {
				processor.stop();
				processor.close();
				processor = null;
				rtptransmitter.close();
				rtptransmitter = null;
		    }
		}
    }

    private String createProcessor() {
	if (locator == null)
	    return "Locator is null";

	DataSource ds;
	DataSource clone;

	try {
	    ds = Manager.createDataSource(locator);
	} catch (Exception e) {
	    return "Couldn't create DataSource";
	}

	// Try to create a processor to handle the input media locator
	try {
	    processor = Manager.createProcessor(ds);
	} catch (NoProcessorException npe) {
	    return "Couldn't create processor";
	} catch (IOException ioe) {
	    return "IOException creating processor";
	} 

	// 구성 대기
	boolean result = waitForState(processor, Processor.Configured);
	if (result == false)
	    return "Couldn't configure processor";

	// 프로세서에서 트랙 가져오기
	TrackControl [] tracks = processor.getTrackControls();

	// 트랙이 있는지 확인하기
	if (tracks == null || tracks.length < 1)
	    return "Couldn't find tracks in processor";

	boolean programmed = false;

	// 비디오 트랙을 찾기 위해 트랙을 검색합니다.
	for (int i = 0; i < tracks.length; i++) {
	    Format format = tracks[i].getFormat();
	    if (  tracks[i].isEnabled() &&
		  format instanceof VideoFormat &&
		  !programmed) {
		
		// 비디오 트랙을 찾았습니다. JPEG/RTP를 출력하도록 프로그래밍을 시도해라
		// 사이즈는 8의 배수여야 합니다.
		Dimension size = ((VideoFormat)format).getSize();
		float frameRate = ((VideoFormat)format).getFrameRate();
		int w = (size.width % 8 == 0 ? size.width :
				(int)(size.width / 8) * 8);
		int h = (size.height % 8 == 0 ? size.height :
				(int)(size.height / 8) * 8);
		VideoFormat jpegFormat = new VideoFormat(VideoFormat.JPEG_RTP,
							 new Dimension(w, h),
							 Format.NOT_SPECIFIED,
							 Format.byteArray,
							 frameRate);
		tracks[i].setFormat(jpegFormat);
		System.err.println("Video transmitted as:");
		System.err.println("  " + jpegFormat);
		// 성공했다고 가정하고
		programmed = true;
	    } else
		tracks[i].setEnabled(false);
	}

	if (!programmed)
	    return "Couldn't find video track";

	// Set the output content descriptor to RAW_RTP
	ContentDescriptor cd = new ContentDescriptor(ContentDescriptor.RAW_RTP);
	processor.setContentDescriptor(cd);

	// Realize the processor. This will internally create a flow
	// graph and attempt to create an output datasource for JPEG/RTP
	// video frames.
	result = waitForState(processor, Controller.Realized);
	if (result == false)
	    return "Couldn't realize processor";

	// Set the JPEG quality to .5.
	setJPEGQuality(processor, 1.0f);

	// Get the output data source of the processor
	dataOutput = processor.getDataOutput();
	return null;
    }

    // Creates an RTP transmit data sink. This is the easiest way to create
    // an RTP transmitter. The other way is to use the RTPSessionManager API.
    // Using an RTP session manager gives you more control if you wish to
    // fine tune your transmission and set other parameters.
    private String createTransmitter() {
	// Create a media locator for the RTP data sink.
	// For example:
	//    rtp://129.130.131.132:42050/video
	String rtpURL = "rtp:/" + ipAddress + ":" + port + "/video";
	MediaLocator outputLocator = new MediaLocator(rtpURL);

	// Create a data sink, open it and start transmission. It will wait
	// for the processor to start sending data. So we need to start the
	// output data source of the processor. We also need to start the
	// processor itself, which is done after this method returns.
	try {
	    rtptransmitter = Manager.createDataSink(dataOutput, outputLocator);
	    rtptransmitter.open();
	    rtptransmitter.start();
	    dataOutput.start();
	} catch (MediaException me) {
	    return "Couldn't create RTP data sink";
	} catch (IOException ioe) {
	    return "Couldn't create RTP data sink";
	}
	
	return null;
    }


    /**
     * Setting the encoding quality to the specified value on the JPEG encoder.
     * 0.5 is a good default.
     */
    void setJPEGQuality(Player p, float val) {

	Control cs[] = p.getControls();
	QualityControl qc = null;
	VideoFormat jpegFmt = new VideoFormat(VideoFormat.JPEG);

	// Loop through the controls to find the Quality control for
 	// the JPEG encoder.
	for (int i = 0; i < cs.length; i++) {

	    if (cs[i] instanceof QualityControl &&
		cs[i] instanceof Owned) {
		Object owner = ((Owned)cs[i]).getOwner();

		// Check to see if the owner is a Codec.
		// Then check for the output format.
		if (owner instanceof Codec) {
		    Format fmts[] = ((Codec)owner).getSupportedOutputFormats(null);
		    for (int j = 0; j < fmts.length; j++) {
			if (fmts[j].matches(jpegFmt)) {
			    qc = (QualityControl)cs[i];
	    		    qc.setQuality(val);
			    System.err.println("- Setting quality to " + 
					val + " on " + qc);
			    break;
			}
		    }
		}
		if (qc != null)
		    break;
	    }
	}
    }


    /****************************************************************
     * Convenience methods to handle processor's state changes.
     ****************************************************************/
    
    private Integer stateLock = new Integer(0);
    private boolean failed = false;
    
    Integer getStateLock() {
	return stateLock;
    }

    void setFailed() {
	failed = true;
    }
    
    private synchronized boolean waitForState(Processor p, int state) {
	p.addControllerListener(new StateListener());
	failed = false;

	// Call the required method on the processor
	if (state == Processor.Configured) {
	    p.configure();
	} else if (state == Processor.Realized) {
	    p.realize();
	}
	
	// Wait until we get an event that confirms the
	// success of the method, or a failure event.
	// See StateListener inner class
	while (p.getState() < state && !failed) {
	    synchronized (getStateLock()) {
		try {
		    getStateLock().wait();
		} catch (InterruptedException ie) {
		    return false;
		}
	    }
	}

	if (failed)
	    return false;
	else
	    return true;
    }

    /****************************************************************
     * Inner Classes
     ****************************************************************/

    class StateListener implements ControllerListener {

	public void controllerUpdate(ControllerEvent ce) {

	    // If there was an error during configure or
	    // realize, the processor will be closed
	    if (ce instanceof ControllerClosedEvent)
		setFailed();

	    // All controller events, send a notification
	    // to the waiting thread in waitForState method.
	    if (ce instanceof ControllerEvent) {
		synchronized (getStateLock()) {
		    getStateLock().notifyAll();
		}
	    }
	}
    }


    /****************************************************************
     * Sample Usage for VideoTransmit class
     ****************************************************************/
    
  /*  public static void main(String [] args) {
	// We need three parameters to do the transmission
	// For example,
	//   java VideoTransmit file:/C:/media/test.mov  129.130.131.132 42050
	
	if (args.length < 3) {
	    System.err.println("Usage: VideoTransmit <sourceURL> <destIP> <destPort>");
	    System.exit(-1);
	}
//	args[0] = "vfw://0";
//	args[2] = "22222";
	// Create a video transmit object with the specified params.
	VideoTransmit vt = new VideoTransmit(new MediaLocator(args[0]),
					     args[1],
					     args[2]);
	// Start the transmission
	String result = vt.start();

	// result will be non-null if there was an error. The return
	// value is a String describing the possible error. Print it.
	if (result != null) {
	    System.err.println("Error : " + result);
	    System.exit(0);
	}

	System.err.println("Start transmission for 60 seconds...");
	
	// Transmit for 60 seconds and then close the processor
	// This is a safeguard when using a capture data source
	// so that the capture device will be properly released
	// before quitting.
	// The right thing to do would be to have a GUI with a
	// "Stop" button that would call stop on VideoTransmit
	try {
	    Thread.currentThread().sleep(60000);
	} catch (InterruptedException ie) {
	}

	// Stop the transmission
	vt.stop();

	System.err.println("...transmission ended.");
	
	System.exit(0);
    }
    */
}

