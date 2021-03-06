/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * MessagingFrame.java
 *
 * Created on Jul 28, 2010, 10:04:21 AM
 */

package messengerclient;

import java.io.ObjectInputStream;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MessagingFrame extends javax.swing.JFrame {

    /** 메세지 프레임생성 */

    ClientManager clientManager;
    String from,to;
    ObjectInputStream input;
    ExecutorService clientExecutor;
    public MessagingFrame(String getto,String getfrom,ClientManager getClientManager)
    {
        from=getfrom;
        to=getto;
        initComponents();
        setTitle(to);
        clientManager=getClientManager;
        clientExecutor=Executors.newCachedThreadPool();
    }

    /* 폼을 초기화하기 위해 생성자 내에서 이 메서드를 호출합니다.
     * 경고: 이 코드를 수정하지 마십시오. 이 메서드의 내용은 항상 양식 편집기에서 재생성됩니다.
     */
    @SuppressWarnings("unchecked")
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        ta_view_message = new javax.swing.JTextArea();
        jPanel2 = new javax.swing.JPanel();
        jScrollPane2 = new javax.swing.JScrollPane();
        tp_write_message = new javax.swing.JTextPane();
        but_send = new javax.swing.JButton();
        jPanel3 = new javax.swing.JPanel();
        jPanel4 = new javax.swing.JPanel();
        but_file = new javax.swing.JButton();
        but_video = new javax.swing.JButton();
        tf_file = new javax.swing.JTextField();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);

        ta_view_message.setBackground(new java.awt.Color(254, 255, 230));
        ta_view_message.setColumns(20);
        ta_view_message.setEditable(false);
        ta_view_message.setRows(5);
        ta_view_message.setBorder(javax.swing.BorderFactory.createTitledBorder(""));
        jScrollPane1.setViewportView(ta_view_message);

        jPanel2.setBackground(new java.awt.Color(255, 255, 255));
        jPanel2.setBorder(javax.swing.BorderFactory.createTitledBorder(""));

        tp_write_message.setBorder(null);
        jScrollPane2.setViewportView(tp_write_message);

        but_send.setText("보내기");
        but_send.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                but_sendActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 228, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(but_send)
                .addContainerGap())
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 45, Short.MAX_VALUE)
            .addComponent(but_send, javax.swing.GroupLayout.DEFAULT_SIZE, 45, Short.MAX_VALUE)
        );

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel2, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 304, Short.MAX_VALUE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 235, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

		// 비디오 패널 시작!
        
		jPanel4.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createEtchedBorder(), "Vodio", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Tahoma", 0, 11), new java.awt.Color(0, 0, 255))); // NOI18N
        
        
		but_video.setText("비디오 시작하기");
        but_video.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                but_videoActionPerformed(evt);
            }
        });
        
        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(but_video))
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(but_video)))
        );
        
        //started file transfer panel
        jPanel3.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createEtchedBorder(), "File", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Tahoma", 0, 11), new java.awt.Color(0, 0, 255))); // NOI18N
        
        but_file.setText("파일 보내기");
        but_file.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                but_fileActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(but_file)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 27, Short.MAX_VALUE)
                .addComponent(tf_file, javax.swing.GroupLayout.PREFERRED_SIZE, 166, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(but_file)
                    .addComponent(tf_file, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)	
                .addGap(10, 10, 10))
             .addComponent(jPanel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
              
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap()	
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        pack();
    }

    private void but_sendActionPerformed(java.awt.event.ActionEvent evt) {//액션이벤트
        sendMessage();
    }
    
    private void but_fileActionPerformed(java.awt.event.ActionEvent evt) {
    
    String sFileName = tf_file.getText();
    if(sFileName != "")
    {
    	FileSender pFileSend = new FileSender(sFileName);
    	clientExecutor.execute(pFileSend);
    }
    
    	
    }

    private void but_videoActionPerformed(java.awt.event.ActionEvent evt) {
        
	   String st="/";
	   st += tf_file.getText();
	   tf_file.setText(" ");
	   //  VideoTransmit vt = new VideoTransmit(new MediaLocator("vfw://0"),st,"20002");
		
	   //  String result = vt.start();
       clientManager.sendMessage("video"+" "+to +" "+from);  
	/*	if (result != null) {
		    System.out.println("Error : " + result);
		    System.exit(0);
		}
	    
	    
		System.out.println("Start transmission for 60 seconds...");
	*/	
		
	  /* String str[] = new String[2];
	   str[0]  =  st + "/20006";
	   str[1]  =  st + "/20008";
	   AVReceive2 avReceive= new AVReceive2(str);   
	 
	        if (!avReceive.initialize())    
	         {   
	             System.err.println("Failed to initialize the sessions.");   
	             System.exit(-1);   
	         }  
	         	
	         try    
	         {   
	             while (!avReceive.isDone())   
	                 Thread.sleep(60000);   
	         }    
	         catch (Exception ex)    
	         {}   
		     */
		// Transmit for 60 seconds and then close the processor
		// This is a safeguard when using a capture data source
		// so that the capture device will be properly released
		// before quitting.
		// The right thing to do would be to have a GUI with a
		// "Stop" button that would call stop on VideoTransmit
	/*	try {
		    Thread.currentThread().sleep(600000);
		} catch (InterruptedException ie) {
		}
	      
		// Stop the transmission
		vt.stop();
	    */
         
        
    }//GEN-LAST:event_but_fileActionPerformed


    void sendMessage()
    {
        String message=to+" "+from+" : " + tp_write_message.getText();
        clientManager.sendMessage(message);
        tp_write_message.setText(null);
        ta_view_message.append(message.replaceFirst(to,"")+"\n");
    }

    private javax.swing.JButton but_video;
    private javax.swing.JButton but_file;
    public javax.swing.JButton but_send;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    public javax.swing.JTextArea ta_view_message;
    private javax.swing.JTextField tf_file;
    public javax.swing.JTextPane tp_write_message;

}
