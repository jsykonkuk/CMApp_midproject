import java.awt.*;
import java.io.*;
import java.nio.file.Path;
import java.awt.event.*;
import java.util.*;

import javax.swing.*;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyleContext;
import javax.swing.text.StyledDocument;

import kr.ac.konkuk.ccslab.cm.entity.CMList;
import kr.ac.konkuk.ccslab.cm.entity.CMRecvFileInfo;
import kr.ac.konkuk.ccslab.cm.entity.CMSendFileInfo;
import kr.ac.konkuk.ccslab.cm.entity.CMSession;
import kr.ac.konkuk.ccslab.cm.info.CMConfigurationInfo;
import kr.ac.konkuk.ccslab.cm.info.CMFileTransferInfo;
import kr.ac.konkuk.ccslab.cm.info.CMInfo;
import kr.ac.konkuk.ccslab.cm.info.CMInteractionInfo;
import kr.ac.konkuk.ccslab.cm.sns.CMSNSUserAccessSimulator;
import kr.ac.konkuk.ccslab.cm.stub.CMServerStub;

public class CMWinServer extends JFrame {

    private static final long serialVersionUID = 1L;

    //private JTextArea m_outTextArea;
    private JTextPane m_outTextPane;
    private JTextField m_inTextField;
    private JButton m_startStopButton;
    private CMServerStub m_serverStub;
    private CMWinServerEventHandler m_eventHandler;
    private CMSNSUserAccessSimulator m_uaSim;

    CMWinServer()
    {

        MyKeyListener cmKeyListener = new MyKeyListener();
        MyActionListener cmActionListener = new MyActionListener();
        setTitle("CM Server");
        setSize(500, 500);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        setMenus();
        setLayout(new BorderLayout());

        m_outTextPane = new JTextPane();
        m_outTextPane.setEditable(false);

        StyledDocument doc = m_outTextPane.getStyledDocument();
        addStylesToDocument(doc);

        add(m_outTextPane, BorderLayout.CENTER);
        JScrollPane scroll = new JScrollPane (m_outTextPane,
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);

        add(scroll);

        m_inTextField = new JTextField();
        m_inTextField.addKeyListener(cmKeyListener);
        add(m_inTextField, BorderLayout.SOUTH);

        JPanel topButtonPanel = new JPanel();
        topButtonPanel.setLayout(new FlowLayout());
        add(topButtonPanel, BorderLayout.NORTH);

        m_startStopButton = new JButton("Start Server CM");
        m_startStopButton.addActionListener(cmActionListener);
        m_startStopButton.setEnabled(false);
        //add(startStopButton, BorderLayout.NORTH);
        topButtonPanel.add(m_startStopButton);

        setVisible(true);

        // create CM stub object and set the event handler
        m_serverStub = new CMServerStub();
        m_eventHandler = new CMWinServerEventHandler(m_serverStub, this);
        m_uaSim = new CMSNSUserAccessSimulator();

        // start cm
        startCM();
    }

    private void addStylesToDocument(StyledDocument doc)
    {
        Style defStyle = StyleContext.getDefaultStyleContext().getStyle(StyleContext.DEFAULT_STYLE);

        Style regularStyle = doc.addStyle("regular", defStyle);
        StyleConstants.setFontFamily(regularStyle, "SansSerif");

        Style boldStyle = doc.addStyle("bold", defStyle);
        StyleConstants.setBold(boldStyle, true);
    }

    public CMServerStub getServerStub()
    {
        return m_serverStub;
    }

    public CMWinServerEventHandler getServerEventHandler()
    {
        return m_eventHandler;
    }

    public void setMenus()
    {
        MyMenuListener menuListener = new MyMenuListener();
        JMenuBar menuBar = new JMenuBar();

        JMenu helpMenu = new JMenu("Help");
        //helpMenu.setMnemonic(KeyEvent.VK_H);
        JMenuItem showAllMenuItem = new JMenuItem("show all menus");
        showAllMenuItem.addActionListener(menuListener);
        showAllMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_H, ActionEvent.ALT_MASK));

        helpMenu.add(showAllMenuItem);
        menuBar.add(helpMenu);

        JMenu cmNetworkMenu = new JMenu("Network Participation");

        JMenu startStopSubMenu = new JMenu("Start/Stop");
        JMenuItem startMenuItem = new JMenuItem("start CM");
        startMenuItem.addActionListener(menuListener);
        startStopSubMenu.add(startMenuItem);
        JMenuItem terminateMenuItem = new JMenuItem("terminate CM");
        terminateMenuItem.addActionListener(menuListener);
        startStopSubMenu.add(terminateMenuItem);

        cmNetworkMenu.add(startStopSubMenu);

        JMenu multiServerSubMenu = new JMenu("Multi-server");
        JMenuItem connectDefaultMenuItem = new JMenuItem("connect to default server");
        connectDefaultMenuItem.addActionListener(menuListener);
        multiServerSubMenu.add(connectDefaultMenuItem);
        JMenuItem disconnectDefaultMenuItem = new JMenuItem("disconnect from default server");
        disconnectDefaultMenuItem.addActionListener(menuListener);
        multiServerSubMenu.add(disconnectDefaultMenuItem);
        JMenuItem regDefaultMenuItem = new JMenuItem("register to default server");
        regDefaultMenuItem.addActionListener(menuListener);
        multiServerSubMenu.add(regDefaultMenuItem);
        JMenuItem deregDefaultMenuItem = new JMenuItem("deregister from default server");
        deregDefaultMenuItem.addActionListener(menuListener);
        multiServerSubMenu.add(deregDefaultMenuItem);

        cmNetworkMenu.add(multiServerSubMenu);
        menuBar.add(cmNetworkMenu);

        JMenu serviceMenu = new JMenu("Services");

        JMenu infoSubMenu = new JMenu("Information");
        JMenuItem showSessionMenuItem = new JMenuItem("show session information");
        showSessionMenuItem.addActionListener(menuListener);
        infoSubMenu.add(showSessionMenuItem);
        JMenuItem showGroupMenuItem = new JMenuItem("show group information");
        showGroupMenuItem.addActionListener(menuListener);
        infoSubMenu.add(showGroupMenuItem);
        JMenuItem showChannelMenuItem = new JMenuItem("show current channels");
        showChannelMenuItem.addActionListener(menuListener);
        infoSubMenu.add(showChannelMenuItem);
        JMenuItem showUsersMenuItem = new JMenuItem("show login users");
        showUsersMenuItem.addActionListener(menuListener);
        infoSubMenu.add(showUsersMenuItem);
        JMenuItem inputThroughputMenuItem = new JMenuItem("test input network throughput");
        inputThroughputMenuItem.addActionListener(menuListener);
        infoSubMenu.add(inputThroughputMenuItem);
        JMenuItem outputThroughputMenuItem = new JMenuItem("test output network throughput");
        outputThroughputMenuItem.addActionListener(menuListener);
        infoSubMenu.add(outputThroughputMenuItem);
        JMenuItem showAllConfMenuItem = new JMenuItem("show all configurations");
        showAllConfMenuItem.addActionListener(menuListener);
        infoSubMenu.add(showAllConfMenuItem);
        JMenuItem changeConfMenuItem = new JMenuItem("change configuration");
        changeConfMenuItem.addActionListener(menuListener);
        infoSubMenu.add(changeConfMenuItem);
        JMenuItem showThreadInfoItem = new JMenuItem("show thread information");
        showThreadInfoItem.addActionListener(menuListener);
        infoSubMenu.add(showThreadInfoItem);

        serviceMenu.add(infoSubMenu);

        JMenu eventTransmissionSubMenu = new JMenu("Event Transmission");
        JMenuItem sendDummyEventMenuItem = new JMenuItem("send CMDummyEvent");
        sendDummyEventMenuItem.addActionListener(menuListener);
        eventTransmissionSubMenu.add(sendDummyEventMenuItem);

        serviceMenu.add(eventTransmissionSubMenu);

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

        serviceMenu.add(fileTransferSubMenu);

        JMenu snsSubMenu = new JMenu("Social Network Service");
        JMenuItem attachSchemeMenuItem = new JMenuItem("set attachment download scheme");
        attachSchemeMenuItem.addActionListener(menuListener);
        snsSubMenu.add(attachSchemeMenuItem);

        serviceMenu.add(snsSubMenu);

        JMenu channelSubMenu = new JMenu("Channel");
        JMenuItem addChannelMenuItem = new JMenuItem("add channel");
        addChannelMenuItem.addActionListener(menuListener);
        channelSubMenu.add(addChannelMenuItem);
        JMenuItem removeChannelMenuItem = new JMenuItem("remove channel");
        removeChannelMenuItem.addActionListener(menuListener);
        channelSubMenu.add(removeChannelMenuItem);

        serviceMenu.add(channelSubMenu);

        JMenu pubsubSubMenu = new JMenu("Publish/Subscribe");
        JMenuItem findSessionMenuItem = new JMenuItem("find session info");
        findSessionMenuItem.addActionListener(menuListener);
        pubsubSubMenu.add(findSessionMenuItem);
        JMenuItem printAllSessionMenuItem = new JMenuItem("print all session info");
        printAllSessionMenuItem.addActionListener(menuListener);
        pubsubSubMenu.add(printAllSessionMenuItem);
        JMenuItem printAllRetainInfoMenuItem = new JMenuItem("print all retain info");
        printAllRetainInfoMenuItem.addActionListener(menuListener);
        pubsubSubMenu.add(printAllRetainInfoMenuItem);

        serviceMenu.add(pubsubSubMenu);

        JMenu fileSyncSubMenu = new JMenu("File Sync");
        JMenuItem openSyncFolderMenuItem = new JMenuItem("open file-sync folder");
        openSyncFolderMenuItem.addActionListener(menuListener);
        fileSyncSubMenu.add(openSyncFolderMenuItem);

        serviceMenu.add(fileSyncSubMenu);

        JMenu otherSubMenu = new JMenu("Other CM Tests");
        JMenuItem configUserAccessSimMenuItem = new JMenuItem("configure SNS user access simulation");
        configUserAccessSimMenuItem.addActionListener(menuListener);
        otherSubMenu.add(configUserAccessSimMenuItem);
        JMenuItem startUserAccessSimMenuItem = new JMenuItem("start SNS user access simulation");
        startUserAccessSimMenuItem.addActionListener(menuListener);
        otherSubMenu.add(startUserAccessSimMenuItem);
        JMenuItem prefetchAccSimMenuItem = new JMenuItem("start SNS user access simulation and measure prefetch accuracy");
        prefetchAccSimMenuItem.addActionListener(menuListener);
        otherSubMenu.add(prefetchAccSimMenuItem);
        JMenuItem recentAccHistorySimMenuItem = new JMenuItem("start and write recent SNS access history simulation to CM DB");
        recentAccHistorySimMenuItem.addActionListener(menuListener);
        otherSubMenu.add(recentAccHistorySimMenuItem);

        serviceMenu.add(otherSubMenu);
        menuBar.add(serviceMenu);

        setJMenuBar(menuBar);
    }

    public void printSessionInfo()
    {
        printMessage("------------------------------------------------------\n");
        printMessage(String.format("%-20s%-20s%-10s%-10s%n", "session name", "session addr", "port", "#users"));
        printMessage("------------------------------------------------------\n");

        CMInteractionInfo interInfo = m_serverStub.getCMInfo().getInteractionInfo();
        Iterator<CMSession> iter = interInfo.getSessionList().iterator();
        while(iter.hasNext())
        {
            CMSession session = iter.next();
            printMessage(String.format("%-20s%-20s%-10d%-10d%n", session.getSessionName(), session.getAddress()
                    , session.getPort(), session.getSessionUsers().getMemberNum()));
        }
        return;
    }
    public void requestFile()
    {
        boolean bReturn = false;
        String strFileName = null;
        String strFileOwner = null;
        byte byteFileAppendMode = -1;

        printMessage("====== request a file\n");
        JTextField fileNameField = new JTextField();
        JTextField fileOwnerField = new JTextField();
        String[] fAppendMode = {"Default", "Overwrite", "Append"};
        JComboBox<String> fAppendBox = new JComboBox<String>(fAppendMode);

        Object[] message = {
                "File Name:", fileNameField,
                "File Owner:", fileOwnerField,
                "File Append Mode: ", fAppendBox
        };
        int option = JOptionPane.showConfirmDialog(null, message, "File Request Input", JOptionPane.OK_CANCEL_OPTION);
        if(option == JOptionPane.CANCEL_OPTION || option != JOptionPane.OK_OPTION)
        {
            printMessage("canceled!\n");
            return;
        }

        strFileName = fileNameField.getText().trim();
        if(strFileName.isEmpty())
        {
            printMessage("File name is empty!\n");
            return;
        }

        strFileOwner = fileOwnerField.getText().trim();
        if(strFileOwner.isEmpty())
        {
            printMessage("File owner is empty!\n");
            return;
        }

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

        bReturn = m_serverStub.requestFile(strFileName, strFileOwner, byteFileAppendMode);

        if(!bReturn)
            printMessage("Request file error! file("+strFileName+"), owner("+strFileOwner+").\n");

        printMessage("======\n");
    }

    public void pushFile()
    {
        String strFilePath = null;
        File[] files;
        String strReceiver = null;
        byte byteFileAppendMode = -1;
        boolean bReturn = false;

		/*
		strReceiver = JOptionPane.showInputDialog("Receiver Name: ");
		if(strReceiver == null) return;
		*/
        JTextField freceiverField = new JTextField();
        String[] fAppendMode = {"Default", "Overwrite", "Append"};
        JComboBox<String> fAppendBox = new JComboBox<String>(fAppendMode);

        Object[] message = {
                "File Receiver: ", freceiverField,
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
        {
            printMessage("File receiver is empty!\n");
            return;
        }

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
        CMConfigurationInfo confInfo = m_serverStub.getCMInfo().getConfigurationInfo();
        File curDir = new File(confInfo.getTransferedFileHome().toString());
        fc.setCurrentDirectory(curDir);
        int fcRet = fc.showOpenDialog(this);
        if(fcRet != JFileChooser.APPROVE_OPTION) return;
        files = fc.getSelectedFiles();
        if(files.length < 1) return;
        for(int i=0; i < files.length; i++)
        {
            strFilePath = files[i].getPath();
            bReturn = m_serverStub.pushFile(strFilePath, strReceiver, byteFileAppendMode);
            if(!bReturn)
            {
                printMessage("push file error! file("+strFilePath+"), receiver("
                        +strReceiver+").\n");
            }
        }

        printMessage("======\n");
    }

    public void cancelRecvFile()
    {
        String strSender = null;
        boolean bReturn = false;
        printMessage("====== cancel receiving a file\n");

        strSender = JOptionPane.showInputDialog("Input sender name (enter for all senders)");
        if(strSender.isEmpty())
            strSender = null;

        bReturn = m_serverStub.cancelPullFile(strSender);

        if(bReturn)
        {
            if(strSender == null)
                strSender = "all senders";
            printMessage("Successfully requested to cancel receiving a file to ["+strSender+"].\n");
        }
        else
            printMessage("Request failed to cancel receiving a file to ["+strSender+"]!\n");

        return;
    }

    public void cancelSendFile()
    {
        String strReceiver = null;
        boolean bReturn = false;
        printMessage("====== cancel sending a file\n");

        strReceiver = JOptionPane.showInputDialog("Input receiver name (enter for all receivers)");
        if(strReceiver.isEmpty())
            strReceiver = null;

        bReturn = m_serverStub.cancelPushFile(strReceiver);

        if(bReturn)
            printMessage("Successfully requested to cancel sending a file to ["+strReceiver+"]");
        else
            printMessage("Request failed to cancel sending a file to ["+strReceiver+"]!");

        return;
    }

    public void printSendRecvFileInfo()
    {
        CMFileTransferInfo fInfo = m_serverStub.getCMInfo().getFileTransferInfo();
        Hashtable<String, CMList<CMSendFileInfo>> sendHashtable = fInfo.getSendFileHashtable();
        Hashtable<String, CMList<CMRecvFileInfo>> recvHashtable = fInfo.getRecvFileHashtable();
        Set<String> sendKeySet = sendHashtable.keySet();
        Set<String> recvKeySet = recvHashtable.keySet();

        printMessage("==== sending file info\n");
        for(String receiver : sendKeySet)
        {
            CMList<CMSendFileInfo> sendList = sendHashtable.get(receiver);
            printMessage(sendList+"\n");
        }

        printMessage("==== receiving file info\n");
        for(String sender : recvKeySet)
        {
            CMList<CMRecvFileInfo> recvList = recvHashtable.get(sender);
            printMessage(recvList+"\n");
        }
    }

    public void requestServerReg()
    {
        String strServerName = null;

        printMessage("====== request registration to the default server\n");
        strServerName = JOptionPane.showInputDialog("Enter registered server name");
        if(strServerName != null)
        {
            m_serverStub.requestServerReg(strServerName);
        }

        printMessage("======\n");
        return;
    }

    public void requestServerDereg()
    {
        printMessage("====== request deregistration from the default server\n");
        boolean bRet = m_serverStub.requestServerDereg();
        printMessage("======\n");
        if(bRet)
            updateTitle();

        return;
    }
    private void openFileSyncFolder() {
        printMessage("=========== open file-sync folder\n");
        // ask client name
        String userName = JOptionPane.showInputDialog("User Name:");
        if(userName != null) {
            // get the file-sync home of "userName"
            Path syncHome = m_serverStub.getFileSyncHome(userName);
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
    }

    private void printThreadInfo() {
        String threadInfo = m_serverStub.getThreadInfo();
        printMessage(threadInfo);
    }

    public static void main(String[] args)
    {
        CMWinServer server = new CMWinServer();
        CMServerStub cmStub = server.getServerStub();
        cmStub.setAppEventHandler(server.getServerEventHandler());
    }
}