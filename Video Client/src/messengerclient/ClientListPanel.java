/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * ClientListPanel.java
 *
 * Created on May 31, 2010, 12:13:48 AM
 */

package messengerclient;

import java.awt.BorderLayout;
import javax.swing.DefaultListSelectionModel;
import javax.swing.ListSelectionModel;

public class ClientListPanel extends javax.swing.JPanel {

    /** 생성! new form ClientListPanel */
    public ClientListPanel() {
        initComponents();
        createListModel();
    }

        void createListModel()
    {
        list_model=new javax.swing.DefaultListModel();
        list_online_clients = new javax.swing.JList(list_model);
        list_online_clients.setBorder(javax.swing.BorderFactory.createTitledBorder("접속자 List"));

        dlsm=new DefaultListSelectionModel();
        dlsm.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
	    list_online_clients.setSelectionModel(dlsm);
        this.setLayout(new BorderLayout());
        this.add(list_online_clients,BorderLayout.CENTER);
    }

    /* 폼을 초기화하기 위해 생성자 내에서 이 메서드를 호출합니다.
     * 경고: 이 코드를 수정하지 마십시오. 이 메서드의 내용은 항상 양식 편집기에서 재생성됩니다.
     */
    @SuppressWarnings("unchecked")
    private void initComponents() {

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 182, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 339, Short.MAX_VALUE)
        );
    }

    public javax.swing.DefaultListModel list_model;
    public javax.swing.DefaultListSelectionModel dlsm;
    public javax.swing.JList list_online_clients;
}
