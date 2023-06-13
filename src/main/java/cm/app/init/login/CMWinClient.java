
import java.util.*;

import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

import javax.swing.*;
import javax.swing.text.*;
import kr.ac.konkuk.ccslab.cm.entity.CMList;;
import kr.ac.konkuk.ccslab.cm.entity.CMSendFileInfo;
import kr.ac.konkuk.ccslab.cm.entity.CMSessionInfo;
import kr.ac.konkuk.ccslab.cm.entity.CMUser;
import kr.ac.konkuk.ccslab.cm.event.CMSessionEvent;
import kr.ac.konkuk.ccslab.cm.event.CMUserEvent;
import kr.ac.konkuk.ccslab.cm.info.*;
import kr.ac.konkuk.ccslab.cm.info.enums.CMFileSyncMode;
import kr.ac.konkuk.ccslab.cm.manager.*;
import kr.ac.konkuk.ccslab.cm.stub.CMClientStub;

public class CMWinClient extends JFrame {

    private static final long serialVersionUID = 1L;
    //private JTextArea m_outTextArea;
    private JTextPane m_outTextPane;
    private JTextField m_inTextField;
    private JButton m_startStopButton;
    private JButton m_loginLogoutButton;
    CMWinClient()
    {
        MyKeyListener cmKeyListener = new MyKeyListener();
        MyActionListener cmActionListener = new MyActionListener();
        MyMouseListener cmMouseListener = new MyMouseListener();
        setTitle("CM Client");
        setSize(600, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        setMenus();
        setLayout(new BorderLayout());

        m_outTextPane = new JTextPane();
        m_outTextPane.setBackground(new Color(245,245,245));
        //m_outTextPane.setForeground(Color.WHITE);
        m_outTextPane.setEditable(false);

        StyledDocument doc = m_outTextPane.getStyledDocument();
        addStylesToDocument(doc);
        add(m_outTextPane, BorderLayout.CENTER);
        JScrollPane centerScroll = new JScrollPane (m_outTextPane,
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        //add(centerScroll);
        getContentPane().add(centerScroll, BorderLayout.CENTER);

        m_inTextField = new JTextField();
        m_inTextField.addKeyListener(cmKeyListener);
        add(m_inTextField, BorderLayout.SOUTH);

        JPanel topButtonPanel = new JPanel();
        topButtonPanel.setBackground(new Color(220,220,220));
        topButtonPanel.setLayout(new FlowLayout());
        add(topButtonPanel, BorderLayout.NORTH);

        m_startStopButton = new JButton("Start Client CM");
        //m_startStopButton.setBackground(Color.LIGHT_GRAY);	// not work on Mac
        m_startStopButton.addActionListener(cmActionListener);
        m_startStopButton.setEnabled(false);
        //add(startStopButton, BorderLayout.NORTH);
        topButtonPanel.add(m_startStopButton);

        m_loginLogoutButton = new JButton("Login");
        m_loginLogoutButton.addActionListener(cmActionListener);
        m_loginLogoutButton.setEnabled(false);
        topButtonPanel.add(m_loginLogoutButton);
        
        setVisible(true);

        // create a CM object and set the event handler
        CMClientStub m_clientStub = new CMClientStub();
        m_eventHandler = new CMWinClientEventHandler(m_clientStub, this);
        m_inTextField.requestFocus();
    }

    private void addStylesToDocument(StyledDocument doc)
    {
        Style defStyle = StyleContext.getDefaultStyleContext().getStyle(StyleContext.DEFAULT_STYLE);

        Style regularStyle = doc.addStyle("regular", defStyle);
        StyleConstants.setFontFamily(regularStyle, "SansSerif");

        Style boldStyle = doc.addStyle("bold", defStyle);
        StyleConstants.setBold(boldStyle, true);

        Style linkStyle = doc.addStyle("link", defStyle);
        StyleConstants.setForeground(linkStyle, Color.BLUE);
        StyleConstants.setUnderline(linkStyle, true);
    }

    private CMClientStub getClientStub()
    {
        return m_clientStub;
    }

    private CMWinClientEventHandler getClientEventHandler()
    {
        return m_eventHandler;
    }

    // set menus
    private void setMenus()
    {
        MyMenuListener menuListener = new MyMenuListener();
        JMenuBar menuBar = new JMenuBar();


        JMenu connectSubMenu = new JMenu("Connection");
        JMenuItem connDefaultMenuItem = new JMenuItem("connect to default server");
        connDefaultMenuItem.addActionListener(menuListener);
        connectSubMenu.add(connDefaultMenuItem);
        JMenuItem disconnDefaultMenuItem = new JMenuItem("disconnect from default server");
        disconnDefaultMenuItem.addActionListener(menuListener);
        connectSubMenu.add(disconnDefaultMenuItem);
        JMenuItem connDesigMenuItem = new JMenuItem("connect to designated server");
        connDesigMenuItem.addActionListener(menuListener);
        connectSubMenu.add(connDesigMenuItem);
        JMenuItem disconnDesigMenuItem = new JMenuItem("disconnect from designated server");
        disconnDesigMenuItem.addActionListener(menuListener);
        connectSubMenu.add(disconnDesigMenuItem);

        JMenu loginSubMenu = new JMenu("Login");
        JMenuItem loginDefaultMenuItem = new JMenuItem("login to default server");
        loginDefaultMenuItem.addActionListener(menuListener);
        loginDefaultMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_L, ActionEvent.ALT_MASK));
        loginSubMenu.add(loginDefaultMenuItem);
        JMenuItem syncLoginDefaultMenuItem = new JMenuItem("synchronously login to default server");
        syncLoginDefaultMenuItem.addActionListener(menuListener);
        loginSubMenu.add(syncLoginDefaultMenuItem);
        JMenuItem logoutDefaultMenuItem = new JMenuItem("logout from default server");
        logoutDefaultMenuItem.addActionListener(menuListener);
        loginSubMenu.add(logoutDefaultMenuItem);
        JMenuItem loginDesigMenuItem = new JMenuItem("login to designated server");
        loginDesigMenuItem.addActionListener(menuListener);
        loginSubMenu.add(loginDesigMenuItem);
        JMenuItem logoutDesigMenuItem = new JMenuItem("logout from designated server");
        logoutDesigMenuItem.addActionListener(menuListener);
        loginSubMenu.add(logoutDesigMenuItem);

        cmServiceMenu.add(channelSubMenu);

        JMenu fileTransferSubMenu = new JMenu("File Transfer");
        JMenuItem setPathMenuItem = new JMenuItem("set file path");
        setPathMenuItem.addActionListener(menuListener);
        fileTransferSubMenu.add(setPathMenuItem);
        JMenuItem reqFileMenuItem = new JMenuItem("request file");
        reqFileMenuItem.addActionListener(menuListener);
        reqFileMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_R, ActionEvent.ALT_MASK));
        fileTransferSubMenu.add(reqFileMenuItem);
        JMenuItem pushFileMenuItem = new JMenuItem("push file");
        pushFileMenuItem.addActionListener(menuListener);
        pushFileMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_P, ActionEvent.ALT_MASK));
        fileTransferSubMenu.add(pushFileMenuItem);
        JMenuItem cancelRecvMenuItem = new JMenuItem("cancel receiving file");
        cancelRecvMenuItem.addActionListener(menuListener);
        fileTransferSubMenu.add(cancelRecvMenuItem);
        JMenuItem cancelSendMenuItem = new JMenuItem("cancel sending file");
        cancelSendMenuItem.addActionListener(menuListener);
        fileTransferSubMenu.add(cancelSendMenuItem);
        JMenuItem printSendRecvFileInfoMenuItem = new JMenuItem("print sending/receiving file info");
        printSendRecvFileInfoMenuItem.addActionListener(menuListener);
        fileTransferSubMenu.add(printSendRecvFileInfoMenuItem);

        cmServiceMenu.add(pubsubSubMenu);

        JMenu fileSyncSubMenu = new JMenu("File Sync");
        JMenuItem startFileSyncManualMenuItem = new JMenuItem("start file-sync with manual mode");
        startFileSyncManualMenuItem.addActionListener(menuListener);
        fileSyncSubMenu.add(startFileSyncManualMenuItem);
        JMenuItem stopFileSyncMenuItem = new JMenuItem("stop file-sync");
        stopFileSyncMenuItem.addActionListener(menuListener);
        fileSyncSubMenu.add(stopFileSyncMenuItem);
        JMenuItem openFileSyncFolderMenuItem = new JMenuItem("open file-sync folder");
        openFileSyncFolderMenuItem.addActionListener(menuListener);
        fileSyncSubMenu.add(openFileSyncFolderMenuItem);
        JMenuItem reqOnlineModeMenuItem = new JMenuItem("request online mode");
        reqOnlineModeMenuItem.addActionListener(menuListener);
        fileSyncSubMenu.add(reqOnlineModeMenuItem);
        JMenuItem reqLocalModeMenuItem = new JMenuItem("request local mode");
        reqLocalModeMenuItem.addActionListener(menuListener);
        fileSyncSubMenu.add(reqLocalModeMenuItem);
        JMenuItem printOnlineFilesMenuItem = new JMenuItem("print online mode files");
        printOnlineFilesMenuItem.addActionListener(menuListener);
        fileSyncSubMenu.add(printOnlineFilesMenuItem);
        JMenuItem printLocalFilesMenuItem = new JMenuItem("print local mode files");
        printLocalFilesMenuItem.addActionListener(menuListener);
        fileSyncSubMenu.add(printLocalFilesMenuItem);
        JMenuItem startFileSyncAutoMenuItem = new JMenuItem("start file-sync with auto mode");
        startFileSyncAutoMenuItem.addActionListener(menuListener);
        fileSyncSubMenu.add(startFileSyncAutoMenuItem);
        JMenuItem printFileSyncModeMenuItem = new JMenuItem("print current file-sync mode");
        printFileSyncModeMenuItem.addActionListener(menuListener);
        fileSyncSubMenu.add(printFileSyncModeMenuItem);

    }

    // initialize button titles
    private void initializeButtons()
    {
        m_startStopButton.setText("Start Client CM");
        m_loginLogoutButton.setText("Login");
        //m_leftButtonPanel.setVisible(false);
        //m_westScroll.setVisible(false);
        revalidate();
        repaint();
    }

    // set button titles
    public void setButtonsAccordingToClientState()
    {
        int nClientState;
        nClientState = m_clientStub.getCMInfo().getInteractionInfo().getMyself().getState();

        // nclientState: CMInfo.CM_INIT, CMInfo.CM_CONNECT, CMInfo.CM_LOGIN, CMInfo.CM_SESSION_JOIN
        switch(nClientState)
        {
            case CMInfo.CM_INIT:
                m_startStopButton.setText("Stop Client CM");
                m_loginLogoutButton.setText("Login");
                //m_leftButtonPanel.setVisible(false);
                //m_westScroll.setVisible(false);
                break;
            case CMInfo.CM_CONNECT:
                m_startStopButton.setText("Stop Client CM");
                m_loginLogoutButton.setText("Login");
                //m_leftButtonPanel.setVisible(false);
                //m_westScroll.setVisible(false);
                break;
            case CMInfo.CM_LOGIN:
                m_startStopButton.setText("Stop Client CM");
                m_loginLogoutButton.setText("Logout");
                //m_leftButtonPanel.setVisible(false);
                //m_westScroll.setVisible(false);
                break;
            case CMInfo.CM_SESSION_JOIN:
                m_startStopButton.setText("Stop Client CM");
                m_loginLogoutButton.setText("Logout");
                //m_leftButtonPanel.setVisible(true);
                //m_westScroll.setVisible(true);
                break;
            default:
                m_startStopButton.setText("Start Client CM");
                m_loginLogoutButton.setText("Login");
                //m_leftButtonPanel.setVisible(false);
                //m_westScroll.setVisible(false);
                break;
        }
        revalidate();
        repaint();
    }

    private void testUserEvent()
    {
        String strReceiver = null;
        int nValueByteNum = -1;
        CMUser myself = m_clientStub.getCMInfo().getInteractionInfo().getMyself();

        if(myself.getState() != CMInfo.CM_SESSION_JOIN)
        {
            printMessage("You should join a session and a group!\n");
            return;
        }

        printMessage("====== test CMUserEvent\n");

        String strFieldNum = null;
        int nFieldNum = -1;

        strFieldNum = JOptionPane.showInputDialog("Field Numbers:");
        if(strFieldNum == null) return;
        try{
            nFieldNum = Integer.parseInt(strFieldNum);
        }catch(NumberFormatException e){
            printMessage("Input must be an integer number greater than 0!");
            return;
        }

        String strID = null;
        JTextField strIDField = new JTextField();
        JTextField strReceiverField = new JTextField();
        String[] dataTypes = {"CM_INT", "CM_LONG", "CM_FLOAT", "CM_DOUBLE", "CM_CHAR", "CH_STR", "CM_BYTES"};
        JComboBox<String>[] dataTypeBoxes = new JComboBox[nFieldNum];
        JTextField[] eventFields = new JTextField[nFieldNum*2];
        Object[] message = new Object[4+nFieldNum*3*2];

        for(int i = 0; i < nFieldNum; i++)
        {
            dataTypeBoxes[i] = new JComboBox<String>(dataTypes);
        }

        for(int i = 0; i < nFieldNum*2; i++)
        {
            eventFields[i] = new JTextField();
        }

        message[0] = "event ID: ";
        message[1] = strIDField;
        message[2] = "Receiver Name: ";
        message[3] = strReceiverField;
        for(int i = 4, j = 0, k = 1; i < 4+nFieldNum*3*2; i+=6, j+=2, k++)
        {
            message[i] = "Data type "+k+":";
            message[i+1] = dataTypeBoxes[k-1];
            message[i+2] = "Field Name "+k+":";
            message[i+3] = eventFields[j];
            message[i+4] = "Field Value "+k+":";
            message[i+5] = eventFields[j+1];
        }
        int option = JOptionPane.showConfirmDialog(null, message, "User Event Input", JOptionPane.OK_CANCEL_OPTION);
        if(option == JOptionPane.OK_OPTION)
        {
            strID = strIDField.getText();
            strReceiver = strReceiverField.getText();

            CMUserEvent ue = new CMUserEvent();
            ue.setStringID(strID);
            ue.setHandlerSession(myself.getCurrentSession());
            ue.setHandlerGroup(myself.getCurrentGroup());

            for(int i = 0, j = 0; i < nFieldNum*2; i+=2, j++)
            {
                if(dataTypeBoxes[j].getSelectedIndex() == CMInfo.CM_BYTES)
                {
                    nValueByteNum = Integer.parseInt(eventFields[i+1].getText());
                    if(nValueByteNum < 0)
                    {
                        printMessage("CMClientApp.testUserEvent(), Invalid nValueByteNum("
                                +nValueByteNum+")\n");
                        ue.removeAllEventFields();
                        ue = null;
                        return;
                    }
                    byte[] valueBytes = new byte[nValueByteNum];
                    for(int k = 0; k < nValueByteNum; k++)
                        valueBytes[k] = 1;	// dummy data
                    ue.setEventBytesField(eventFields[i].getText(), nValueByteNum, valueBytes);
                }
                else
                {
                    ue.setEventField(dataTypeBoxes[j].getSelectedIndex(),
                            eventFields[i].getText(), eventFields[i+1].getText());
                }

            }

            m_clientStub.send(ue, strReceiver);
            ue.removeAllEventFields();
            ue = null;
        }

        printMessage("======\n");

        return;
    }

    private void testRequestFile()
    {
        boolean bReturn = false;
        String strFileName = null;
        String strFileOwner = null;
        byte byteFileAppendMode = -1;
        CMInteractionInfo interInfo = m_clientStub.getCMInfo().getInteractionInfo();

        printMessage("====== request a file\n");

        JTextField fnameField = new JTextField();
        JTextField fownerField = new JTextField();
        String[] fAppendMode = {"Default", "Overwrite", "Append"};
        JComboBox<String> fAppendBox = new JComboBox<String>(fAppendMode);

        Object[] message = {
                "File Name: ", fnameField,
                "File Owner(empty for default server): ", fownerField,
                "File Append Mode: ", fAppendBox
        };
        int option = JOptionPane.showConfirmDialog(null, message, "File Request", JOptionPane.OK_CANCEL_OPTION);
        if(option == JOptionPane.CANCEL_OPTION || option != JOptionPane.OK_OPTION)
        {
            printMessage("canceled!\n");
            return;
        }

        strFileName = fnameField.getText().trim();
        if(strFileName.isEmpty())
        {
            printMessage("File name is empty!\n");
            return;
        }
        strFileOwner = fownerField.getText().trim();
        if(strFileOwner.isEmpty())
            strFileOwner = interInfo.getDefaultServerInfo().getServerName();

        switch(fAppendBox.getSelectedIndex())
        {
            case 0:
                byteFileAppendMode = CMInfo.FILE_DEFAULT;
                break;
            case 1:
                byteFileAppendMode = CMInfo.FILE_OVERWRITE;
                break;
            case 2:
                byteFileAppendMode = CMInfo.FILE_APPEND;
                break;
        }

        bReturn = m_clientStub.requestFile(strFileName, strFileOwner, byteFileAppendMode);

        if(!bReturn)
            printMessage("Request file error! file("+strFileName+"), owner("+strFileOwner+").\n");

        printMessage("======\n");
    }

    private void testPushFile()
    {
        String strFilePath = null;
        File[] files = null;
        String strReceiver = null;
        byte byteFileAppendMode = -1;
        CMInteractionInfo interInfo = m_clientStub.getCMInfo().getInteractionInfo();
        boolean bReturn = false;

        printMessage("====== push a file\n");

		/*
		strReceiver = JOptionPane.showInputDialog("Receiver Name: ");
		if(strReceiver == null) return;
		*/
        JTextField freceiverField = new JTextField();
        String[] fAppendMode = {"Default", "Overwrite", "Append"};
        JComboBox<String> fAppendBox = new JComboBox<String>(fAppendMode);

        Object[] message = {
                "File Receiver(empty for default server): ", freceiverField,
                "File Append Mode: ", fAppendBox
        };
        int option = JOptionPane.showConfirmDialog(null, message, "File Push", JOptionPane.OK_CANCEL_OPTION);
        if(option == JOptionPane.CANCEL_OPTION || option != JOptionPane.OK_OPTION)
        {
            printMessage("canceled.\n");
            return;
        }

        strReceiver = freceiverField.getText().trim();
        if(strReceiver.isEmpty())
            strReceiver = interInfo.getDefaultServerInfo().getServerName();

        switch(fAppendBox.getSelectedIndex())
        {
            case 0:
                byteFileAppendMode = CMInfo.FILE_DEFAULT;
                break;
            case 1:
                byteFileAppendMode = CMInfo.FILE_OVERWRITE;
                break;
            case 2:
                byteFileAppendMode = CMInfo.FILE_APPEND;
                break;
        }

        JFileChooser fc = new JFileChooser();
        fc.setMultiSelectionEnabled(true);
        CMConfigurationInfo confInfo = m_clientStub.getCMInfo().getConfigurationInfo();
        File curDir = new File(confInfo.getTransferedFileHome().toString());
        fc.setCurrentDirectory(curDir);
        int fcRet = fc.showOpenDialog(this);
        if(fcRet != JFileChooser.APPROVE_OPTION) return;
        files = fc.getSelectedFiles();
        if(files.length < 1) return;
        for(int i=0; i < files.length; i++)
        {
            strFilePath = files[i].getPath();
            bReturn = m_clientStub.pushFile(strFilePath, strReceiver, byteFileAppendMode);
            if(!bReturn)
            {
                printMessage("push file error! file("+strFilePath+"), receiver("
                        +strReceiver+")\n");
            }
        }

        printMessage("======\n");
    }
    private void testOpenFileSyncFolder() {
        printMessage("========== open file-sync folder\n");

        Path syncHome = m_clientStub.getFileSyncHome();
        if(syncHome == null) {
            printStyledMessage("File sync home is null!\n", "bold");
            printStyledMessage("Please see error message on console for more information.\n",
                    "bold");
            return;
        }

        // open syncHome folder
        Desktop desktop = Desktop.getDesktop();
        try {
            desktop.open(syncHome.toFile());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void testRequestFileSyncOnlineMode() {
        printMessage("========== request file-sync online mode\n");
        // get sync home
        CMFileSyncManager syncManager = m_clientStub.findServiceManager(CMFileSyncManager.class);
        Objects.requireNonNull(syncManager);
        Path syncHome = syncManager.getClientSyncHome();

        // open file chooser to choose files
        JFileChooser fc = new JFileChooser();
        fc.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
        fc.setMultiSelectionEnabled(true);
        fc.setCurrentDirectory(syncHome.toFile());
        int fcRet = fc.showOpenDialog(this);
        if(fcRet != JFileChooser.APPROVE_OPTION) return;
        File[] files = fc.getSelectedFiles();
        if(CMInfo._CM_DEBUG) {
            for(File file : files)
                System.out.println("file = " + file);
        }
        if(files.length < 1) return;

        // call the request API of the client stub
        boolean ret = m_clientStub.requestFileSyncOnlineMode(files);
        if(!ret) {
            printStyledMessage("request error!\n", "bold");
        }
        return;
    }

    private void testRequestFileSyncLocalMode() {
        printMessage("========== request file-sync local mode\n");
        // get sync home
        CMFileSyncManager syncManager = m_clientStub.findServiceManager(CMFileSyncManager.class);
        Objects.requireNonNull(syncManager);
        Path syncHome = syncManager.getClientSyncHome();

        // open file chooser to choose files
        JFileChooser fc = new JFileChooser();
        fc.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
        fc.setMultiSelectionEnabled(true);
        fc.setCurrentDirectory(syncHome.toFile());
        int fcRet = fc.showOpenDialog(this);
        if(fcRet != JFileChooser.APPROVE_OPTION) return;
        File[] files = fc.getSelectedFiles();
        if(CMInfo._CM_DEBUG) {
            for(File file : files)
                System.out.println("file = " + file);
        }
        if(files.length < 1) return;

        // call the request API of the client stub
        boolean ret = m_clientStub.requestFileSyncLocalMode(files);
        if(!ret) {
            printStyledMessage("request error!\n", "bold");
        }
        return;
    }

    private void testFileAccessForSync() {
        printMessage("========= test file access for file-sync\n");

        // select file-sync mode and access method
        final JRadioButton manualRadioButton = new JRadioButton("Manual");
        manualRadioButton.setSelected(true);
        final JRadioButton autoRadioButton = new JRadioButton("Auto");
        final ButtonGroup buttonGroup = new ButtonGroup();
        buttonGroup.add(manualRadioButton);
        buttonGroup.add(autoRadioButton);
        String[] testTypeArray = {"activation->deactivation", "deactivation->activation"};
        JComboBox<String> testTypeComboBox = new JComboBox<>(testTypeArray);
        final Object[] message = {
                "", manualRadioButton,
                "", autoRadioButton,
                "Test type", testTypeComboBox
        };
        int response = JOptionPane.showConfirmDialog(null, message,
                "file-sync mode", JOptionPane.OK_CANCEL_OPTION);
        // check the response
        if(response != JOptionPane.OK_OPTION) {
            printStyledMessage("Test cancelled.\n", "bold");
            return;
        }

        // selected file-sync mode
        final CMFileSyncMode selectedFileSyncMode;
        if(manualRadioButton.isSelected())
            selectedFileSyncMode = CMFileSyncMode.MANUAL;
        else if(autoRadioButton.isSelected())
            selectedFileSyncMode = CMFileSyncMode.AUTO;
        else
            selectedFileSyncMode = CMFileSyncMode.OFF;

        // selected test type
        int selectedTestTypeIndex = testTypeComboBox.getSelectedIndex();

        // check current file-sync mode
        CMFileSyncInfo syncInfo = Objects.requireNonNull(m_clientStub.getCMInfo().getFileSyncInfo());
        CMFileSyncMode currentFileSyncMode = syncInfo.getCurrentMode();
        if(currentFileSyncMode != CMFileSyncMode.OFF) {
            boolean ret = m_clientStub.stopFileSync();
            if(!ret) {
                printStyledMessage("Error to stop file-sync before test!\n", "bold");
                return;
            }
        }

        // show confirm dialog notifying that all files in the sync home will be deleted
        response = JOptionPane.showConfirmDialog(null,
                "All files in the sync home will be deleted!",
                "Notice", JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.WARNING_MESSAGE);
        if(response != JOptionPane.OK_OPTION) {
            printStyledMessage("Test cancelled!\n", "bold");
            return;
        }

        // clear the sync home
        CMFileSyncManager syncManager = m_clientStub.getCMInfo().getServiceManager(CMFileSyncManager.class);
        Objects.requireNonNull(syncManager);
        syncManager.clearSyncHome();

        // input file-name for test results
        StringBuilder resultNameBuilder = new StringBuilder();
        if(selectedTestTypeIndex == 0) resultNameBuilder.append("ad-");
        else if(selectedTestTypeIndex == 1) resultNameBuilder.append("da-");
        else {
            printStyledMessage("Invalid test type: "+selectedTestTypeIndex+"\n", "bold");
            return;
        }
        resultNameBuilder.append(selectedFileSyncMode.name()).append("-");
        String fileName = JOptionPane.showInputDialog("File name to record test result", resultNameBuilder)
                .trim();
        // check the response
        if(fileName == null || fileName.isEmpty()) {
            printStyledMessage("File name for recording test result is null or empty!\n", "bold");
            return;
        }

        // start the selected file-sync mode
        boolean ret = m_clientStub.startFileSync(selectedFileSyncMode);
        if(!ret) {
            printStyledMessage("Error to start file-sync "+selectedFileSyncMode+"!\n", "bold");
            return;
        }

        // start the file-access test
        if(selectedTestTypeIndex == 0) {
            // activation -> deactivation
            ret = syncManager.simulateDeactivatingFileAccess(fileName);
            if(!ret) {
                printStyledMessage("Error to simulate deactivating file access!\n", "bold");
                return;
            }
        }
        else if(selectedTestTypeIndex == 1) {
            // deactivation -> activation
            ret = syncManager.simulateActivatingFileAccess(fileName);
            if(!ret) {
                printStyledMessage("Error to simulate activating file access!\n", "bold");
                return;
            }
        }
        else {
            printStyledMessage("Invalid test type: "+selectedTestTypeIndex+"\n", "bold");
            return;
        }

    }
}