package messengerclient;

import java.awt.Dimension;
import java.io.IOException;
import java.net.InetAddress;

import javax.media.Codec;
import javax.media.Control;
import javax.media.Controller;
import javax.media.ControllerClosedEvent;
import javax.media.ControllerEvent;
import javax.media.ControllerListener;
import javax.media.Format;
import javax.media.MediaLocator;
import javax.media.NoProcessorException;
import javax.media.Owned;
import javax.media.Player;
import javax.media.Processor;
import javax.media.control.QualityControl;
import javax.media.control.TrackControl;
import javax.media.format.VideoFormat;
import javax.media.protocol.ContentDescriptor;
import javax.media.protocol.DataSource;
import javax.media.protocol.PushBufferDataSource;
import javax.media.protocol.PushBufferStream;
import javax.media.rtp.RTPManager;
import javax.media.rtp.SendStream;
import javax.media.rtp.SessionAddress;
import javax.media.rtp.rtcp.SourceDescription;

public class AVTransmit2 {

    // 파일 또는 http 또는 캡처 소스
    private MediaLocator locator;
    private String ipAddress;
    private int portBase;

    private Processor processor = null;
    private RTPManager rtpMgrs[];
    private DataSource dataOutput = null;
    
    public AVTransmit2(MediaLocator locator,
			 String ipAddress,
			 String pb,
			 Format format) {
	
	this.locator = locator;
	this.ipAddress = ipAddress;
	Integer integer = Integer.valueOf(pb);
	if (integer != null)
	    this.portBase = integer.intValue();
    }

    /**
     * 전송을 시작합니다. 전송이 정상적으로 시작된 경우 null을 반환합니다.
     * 그렇지 않으면 설정에 실패한 이유가 포함된 문자열을 반환합니다.
     */
    public synchronized String start() {
	String result;

	// 지정된 media locator 에 대한 프로세서 만들기와 JPEG/RTP를 출력하도록 프로그래밍
	result = createProcessor();
	if (result != null)
	    return result;

	// RTP 세션을 생성하여 프로세서의 출력을 지정된 IP 주소 및 포트 번호로 전송합니다.
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

    /*
     * 이미 시작된 경우 전송을 중지합니다.
     */
    public void stop() {
		synchronized (this) {
		    if (processor != null) {
			processor.stop();
			processor.close();
			processor = null;
			for (int i = 0; i < rtpMgrs.length; i++) {
			    rtpMgrs[i].removeTargets( "Session ended.");
			    rtpMgrs[i].dispose();
			}
		    }
		}
    }

    private String createProcessor() {
	if (locator == null)
	    return "Locator is null";

	DataSource ds;
	DataSource clone;

		try {
		    ds = javax.media.Manager.createDataSource(locator);
		} catch (Exception e) {
		    return "Couldn't create DataSource";
		}
	
		// 입력 미디어 로케이션 프로세서 핸들러 생성을 시도한다
		try {
		    processor = javax.media.Manager.createProcessor(ds);
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
	
		// 트랙이 하나이상 있는지 체크하기
		if (tracks == null || tracks.length < 1)
		    return "Couldn't find tracks in processor";
	
		// 출력 내용 설명자를 RAW_RTP로 설정합니다.
		// 이렇게 하면 다음에 보고된 지원되는 형식이 제한됩니다.
		// Track.getSupportedFormats를 유효한 RTP 형식으로만 추적합니다.
		ContentDescriptor cd = new ContentDescriptor(ContentDescriptor.RAW_RTP);
		processor.setContentDescriptor(cd);

	Format supported[];
	Format chosen;
	boolean atLeastOneTrack = false;

		// 트랙 프로그래밍 시작
		for (int i = 0; i < tracks.length; i++) {
		    Format format = tracks[i].getFormat();
		    if (tracks[i].isEnabled()) {
	
			supported = tracks[i].getSupportedFormats();
	
			// 출력 내용을 RAW_RTP로 설정했습니다.
			// 따라서 지원되는 모든 형식은 RTP와 함께 작동해야 합니다.
			// 첫 번째만 뽑도록 하겠습니다.
	
			if (supported.length > 0) {
			    if (supported[0] instanceof VideoFormat) {
			    	
				// 비디오 포맷의 경우 모든 포맷이 모든 사이즈로 동작하는 것은 아니기 때문에 우리는 크기를 다시 확인해야 합니다.
			    //크기 체크하기
				chosen = checkForVideoSizes(tracks[i].getFormat(), 
								supported[0]);
			    } else
				chosen = supported[0];
			    tracks[i].setFormat(chosen);
			    System.err.println("Track " + i + " is set to transmit as:");
			    System.err.println("  " + chosen);
			    atLeastOneTrack = true; 
			} else
			    tracks[i].setEnabled(false);
		    } else
			tracks[i].setEnabled(false);
		}

	if (!atLeastOneTrack)
	    return "Couldn't set any of the tracks to a valid RTP format";

	// 프로세서를 인식합니다. 그러면 내부적으로 흐름 그래프가 작성되고 JPEG/RTP 오디오 프레임에 대한 출력 데이터 소스가 작성됩니다.
	result = waitForState(processor, Controller.Realized);
	if (result == false)
	    return "Couldn't realize processor";

	// JPEG 품질을 .5로 설정합니다
	setJPEGQuality(processor, 0.5f);

	// 프로세서의 출력 데이터 소스를 가져옵니다.
	dataOutput = processor.getDataOutput();

	return null;
    }


    /*
     * RTP Manager API를 사용하여 프로세서의 각 미디어 트랙에 대한 세션을 생성합니다.
     */
    private String createTransmitter() {

	// Cheated.  Should have checked the type.
	PushBufferDataSource pbds = (PushBufferDataSource)dataOutput;
	PushBufferStream pbss[] = pbds.getStreams();

	rtpMgrs = new RTPManager[pbss.length];
	SessionAddress localAddr, destAddr;
	InetAddress ipAddr;
	SendStream sendStream;
	int port;
	SourceDescription srcDesList[];

	for (int i = 0; i < pbss.length; i++) {
	    try {
		rtpMgrs[i] = RTPManager.newInstance();	    

		// 로컬 세션 주소가 에 생성하는데, 대상 포트와 동일한 포트입니다.
		// 이것은 JMS studio와 함께 AVTransmit2를 사용하는 경우에 필요합니다.
		// JMS Studio는 유니캐스트 세션에서 송신기가 수신 중인 동일한 포트에서 송신한다고 가정하고 RTCP 수신기 보고서를 송신 호스트의 이 포트로 다시 보냅니다.
		
		port = portBase + 2*i;
		ipAddr = InetAddress.getByName(ipAddress);

		localAddr = new SessionAddress( InetAddress.getLocalHost(),
						port);
		
		destAddr = new SessionAddress( ipAddr, port);

		rtpMgrs[i].initialize( localAddr);
		
		rtpMgrs[i].addTarget( destAddr);
		
		System.err.println( "Created RTP session: " + ipAddress + " " + port);
		
		sendStream = rtpMgrs[i].createSendStream(dataOutput, i);		
		sendStream.start();
	    } catch (Exception  e) {
		return e.getMessage();
	    }
	}

	return null;
    }


    /**
     * JPEG와 H263의 경우 특정 크기에서만 작동한다는 것을 알고 있습니다.  
     * 따라서 여기서 추가 검사를 수행하여 크기가 적합한지 확인합니다.
     */
    Format checkForVideoSizes(Format original, Format supported) {

	int width, height;
	Dimension size = ((VideoFormat)original).getSize();
	Format jpegFmt = new Format(VideoFormat.JPEG_RTP);
	Format h263Fmt = new Format(VideoFormat.H263_RTP);

	if (supported.matches(jpegFmt)) {
	    // JPEG의 경우 너비와 높이를 8로 나누어야 합니다.
	    width = (size.width % 8 == 0 ? size.width :
				(int)(size.width / 8) * 8);
	    height = (size.height % 8 == 0 ? size.height :
				(int)(size.height / 8) * 8);
	} else if (supported.matches(h263Fmt)) {
	    // H.263의 경우 특정 사이즈만 지원합니다.
	    if (size.width < 128) {
		width = 128;
		height = 96;
	    } else if (size.width < 176) {
		width = 176;
		height = 144;
	    } else {
		width = 352;
		height = 288;
	    }
	} else {
	    // 우리는 이 특정한 형식을 모르니까 그냥 리턴떄림!
	    return supported;
	}

	return (new VideoFormat(null, 
				new Dimension(width, height), 
				Format.NOT_SPECIFIED,
				null,
				Format.NOT_SPECIFIED)).intersects(supported);
    }


    /**
     * 인코딩 품질을 JPEG 인코더의 지정된 값으로 설정합니다.
     * 0.5는 양호한 기본값입니다.
     */
    void setJPEGQuality(Player p, float val) {

	Control cs[] = p.getControls();
	QualityControl qc = null;
	VideoFormat jpegFmt = new VideoFormat(VideoFormat.JPEG);

	// 컨트롤을 루프하여 JPEG 인코더의 품질 컨트롤을 찾습니다.
	for (int i = 0; i < cs.length; i++) {

	    if (cs[i] instanceof QualityControl &&
		cs[i] instanceof Owned) {
		Object owner = ((Owned)cs[i]).getOwner();

		// 소유자가 코덱인지 확인합니다.
		// 그런 다음 출력 형식을 확인합니다.
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
     * 프로세서의 상태 변경을 처리하는 편리한 방법입니다. *
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
	
		// 프로세서에서 필요한 메서드를 호출합니다.
		if (state == Processor.Configured) {
		    p.configure();
		} else if (state == Processor.Realized) {
		    p.realize();
		}
		
		// 확인 이벤트가 발생할 때까지 기다립니다. 	메서드의 성공 또는 실패 이벤트.
		// 상태 수신기 내부 클래스 참조
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
     * 내부 클래스 *
     ****************************************************************/

    class StateListener implements ControllerListener {

	public void controllerUpdate(ControllerEvent ce) {

	    // 구성 또는 실현 중에 오류가 발생한 경우 프로세서가 닫힙니다.
	    if (ce instanceof ControllerClosedEvent)
		setFailed();

	    // 모든 컨트롤러 이벤트는 waitForState 메서드에서 대기 중인 스레드로 알림을 보냅니다.
	    if (ce instanceof ControllerEvent) {
		synchronized (getStateLock()) {
		    getStateLock().notifyAll();
		}
	    }
	}
    }


    /****************************************************************
     * 샘플 AVTransmit2 class
     ****************************************************************/
    
   /* public static void main(String [] args) {
	// We need three parameters to do the transmission
	// For example,
	//   java AVTransmit2 file:/C:/media/test.mov  129.130.131.132 42050
	
	if (args.length < 3) {
	    prUsage();
	}

	Format fmt = null;
	int i = 0;

	// Create a audio transmit object with the specified params.
	AVTransmit2 at = new AVTransmit2(new MediaLocator(args[i]),
					     args[i+1], args[i+2], fmt);
	// Start the transmission
	String result = at.start();

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
	// "Stop" button that would call stop on AVTransmit2
	try {
	    Thread.currentThread().sleep(60000);
	} catch (InterruptedException ie) {
	}

	// Stop the transmission
	at.stop();
	
	System.err.println("...transmission ended.");

	System.exit(0);
    }


    static void prUsage() {
	System.err.println("Usage: AVTransmit2 <sourceURL> <destIP> <destPortBase>");
	System.err.println("     <sourceURL>: input URL or file name");
	System.err.println("     <destIP>: multicast, broadcast or unicast IP address for the transmission");
	System.err.println("     <destPortBase>: network port numbers for the transmission.");
	System.err.println("                     The first track will use the destPortBase.");
	System.err.println("                     The next track will use destPortBase + 2 and so on.\n");
	System.exit(0);
    }*/
}

