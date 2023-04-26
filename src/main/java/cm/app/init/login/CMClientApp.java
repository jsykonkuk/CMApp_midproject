package cm.app.init.login;

import kr.ac.konkuk.ccslab.cm.entity.CMList;
import kr.ac.konkuk.ccslab.cm.entity.CMRecvFileInfo;
import kr.ac.konkuk.ccslab.cm.entity.CMSendFileInfo;
import kr.ac.konkuk.ccslab.cm.info.CMFileTransferInfo;
import kr.ac.konkuk.ccslab.cm.info.CMInfo;
import kr.ac.konkuk.ccslab.cm.info.enums.CMFileSyncMode;
import kr.ac.konkuk.ccslab.cm.manager.CMFileSyncManager;
import kr.ac.konkuk.ccslab.cm.stub.CMClientStub;

import javax.swing.*;
import java.awt.*;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Hashtable;
import java.util.Objects;
import java.util.Scanner;
import java.util.Set;

public class CMClientApp {
    private CMClientStub m_clientStub;
    private cm.app.init.login.CMClientEventHandler m_eventHandler;
    public CMClientApp() {
        m_clientStub = new CMClientStub();
        m_eventHandler = new cm.app.init.login.CMClientEventHandler(m_clientStub);
    }

    public CMClientStub getClientStub() {
        return m_clientStub;
    }

    public cm.app.init.login.CMClientEventHandler getClientEventHandler() {
        return m_eventHandler;
    }

    public static void main(String[] args) {

        Scanner scanner = new Scanner(System.in);
        CMClientApp client = new CMClientApp();
        CMClientStub clientStub = client.getClientStub();
        cm.app.init.login.CMClientEventHandler eventHandler = client.getClientEventHandler();
        boolean ret = false;

        // initialize CM
        clientStub.setAppEventHandler(eventHandler);
        ret = clientStub.startCM();

        if(ret)
            System.out.println("CM initialization succeeds.");
        else {
            System.err.println("CM initialization error!");
            return;
        }

        // login CM server
        System.out.println("=== login: ");
        System.out.println("user name: jiseok");
        System.out.println("password: jiseok");
        ret = clientStub.loginCM("jiseok", "jiseok");

        if(ret)
            System.out.println("successfully sent the login request.");
        else {
            System.err.println("failed the login request!");
            return;
        }

        // terminate CM
        System.out.println("Enter to terminate CM and client: ");
        scanner.nextLine();
        clientStub.terminateCM();
    }
    public void SetFilePath()
    {
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        System.out.println("====== set file path");
        String strPath = null;
        System.out.print("file path: ");
        try {
            strPath = br.readLine();
        } catch (IOException e) {
            e.printStackTrace();
        }

        m_clientStub.setTransferedFileHome(Paths.get(strPath));

        System.out.println("======");
    }

    public void RequestFile()
    {
        boolean bReturn = false;
        String strFileName = null;
        String strFileOwner = null;
        String strFileAppend = null;
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        System.out.println("====== request a file");
        try {
            System.out.print("File name: ");
            strFileName = br.readLine();
            System.out.print("File owner(enter for \"SERVER\"): ");
            strFileOwner = br.readLine();
            if(strFileOwner.isEmpty())
                strFileOwner = m_clientStub.getDefaultServerName();
            System.out.print("File append mode('y'(append);'n'(overwrite);''(empty for the default configuration): ");
            strFileAppend = br.readLine();

        } catch (IOException e) {
            e.printStackTrace();
        }

        if(strFileAppend.isEmpty())
            bReturn = m_clientStub.requestFile(strFileName, strFileOwner);
        else if(strFileAppend.equals("y"))
            bReturn = m_clientStub.requestFile(strFileName,  strFileOwner, CMInfo.FILE_APPEND);
        else if(strFileAppend.equals("n"))
            bReturn = m_clientStub.requestFile(strFileName,  strFileOwner, CMInfo.FILE_OVERWRITE);
        else
            System.err.println("wrong input for the file append mode!");

        if(!bReturn)
            System.err.println("Request file error! file("+strFileName+"), owner("+strFileOwner+").");

        System.out.println("======");
    }

    public void PushFile()
    {
        String strFilePath = null;
        String strReceiver = null;
        String strFileAppend = null;
        boolean bReturn = false;
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        System.out.println("====== push a file");

        try {
            System.out.print("File path name: ");
            strFilePath = br.readLine();
            System.out.print("File receiver (enter for \"SERVER\"): ");
            strReceiver = br.readLine();
            if(strReceiver.isEmpty())
                strReceiver = m_clientStub.getDefaultServerName();
            System.out.print("File append mode('y'(append);'n'(overwrite);''(empty for the default configuration): ");
            strFileAppend = br.readLine();

        } catch (IOException e) {
            e.printStackTrace();
        }

        if(strFileAppend.isEmpty())
            bReturn = m_clientStub.pushFile(strFilePath, strReceiver);
        else if(strFileAppend.equals("y"))
            bReturn = m_clientStub.pushFile(strFilePath,  strReceiver, CMInfo.FILE_APPEND);
        else if(strFileAppend.equals("n"))
            bReturn = m_clientStub.pushFile(strFilePath,  strReceiver, CMInfo.FILE_OVERWRITE);
        else
            System.err.println("wrong input for the file append mode!");

        if(!bReturn)
            System.err.println("Push file error! file("+strFilePath+"), receiver("+strReceiver+")");

        System.out.println("======");
    }
    public void printSendRecvFileInfo()
    {
        CMFileTransferInfo fInfo = m_clientStub.getCMInfo().getFileTransferInfo();
        Hashtable<String, CMList<CMSendFileInfo>> sendHashtable = fInfo.getSendFileHashtable();
        Hashtable<String, CMList<CMRecvFileInfo>> recvHashtable = fInfo.getRecvFileHashtable();
        Set<String> sendKeySet = sendHashtable.keySet();
        Set<String> recvKeySet = recvHashtable.keySet();

        System.out.print("==== sending file info\n");
        for(String receiver : sendKeySet)
        {
            CMList<CMSendFileInfo> sendList = sendHashtable.get(receiver);
            System.out.print(sendList+"\n");
        }

        System.out.print("==== receiving file info\n");
        for(String sender : recvKeySet)
        {
            CMList<CMRecvFileInfo> recvList = recvHashtable.get(sender);
            System.out.print(recvList+"\n");
        }
    }
    private void StartFileSyncWithManualMode() {
        System.out.println("========== start file-sync with manual mode");

        m_eventHandler.setStartTimeOfFileSync(System.currentTimeMillis());

        boolean ret = m_clientStub.startFileSync(CMFileSyncMode.MANUAL);
        if(!ret) {
            System.err.println("Start error of file sync with manual mode!");
            m_eventHandler.setStartTimeOfFileSync(0);
        }
        else {
            System.out.println("File sync with manual mode starts.");
        }
    }

    private void StartFileSyncWithAutoMode() {
        System.out.println("========== start file-sync with auto mode");

        m_eventHandler.setStartTimeOfFileSync(System.currentTimeMillis());

        boolean ret = m_clientStub.startFileSync(CMFileSyncMode.AUTO);
        if(!ret) {
            System.err.println("Start error of file sync with auto mode!");
            m_eventHandler.setStartTimeOfFileSync(0);
        }
        else {
            System.out.println("File sync with auto mode starts.");
        }
    }

    private void PrintCurrentFileSyncMode() {
        System.out.println("========== print current file-sync mode");
        CMFileSyncMode currentMode = m_clientStub.getCurrentFileSyncMode();
        if(currentMode == null) {
            System.err.println("Error! Please see error message in console for more information!");
            return;
        }
        System.out.println("Current file-sync mode is "+currentMode+".");
    }

    private void StopFileSync() {
        System.out.println("========== stop file-sync");
        boolean ret = m_clientStub.stopFileSync();
        if(!ret) {
            System.err.println("Stop error of file sync!");
        }
        else {
            System.out.println("File sync stops.");
        }
    }

    private void OpenFileSyncFolder() {
        System.out.println("========== open file-sync folder");

        Path syncHome = m_clientStub.getFileSyncHome();
        if(syncHome == null) {
            System.err.println("File sync home is null!");
            System.err.println("Please see error message on console for more information.");
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

    private void RequestFileSyncOnlineMode() {
        System.out.println("========== request file-sync online mode");
        // get sync home
        CMFileSyncManager syncManager = m_clientStub.findServiceManager(CMFileSyncManager.class);
        Objects.requireNonNull(syncManager);
        Path syncHome = syncManager.getClientSyncHome();

        // open file chooser to choose files
        JFileChooser fc = new JFileChooser();
        fc.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
        fc.setMultiSelectionEnabled(true);
        fc.setCurrentDirectory(syncHome.toFile());
        int fcRet = fc.showOpenDialog(null);
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
            System.err.println("request error!");
        }
        return;
    }

    private void RequestFileSyncLocalMode() {
        System.out.println("========== request file-sync local mode");
        // get sync home
        CMFileSyncManager syncManager = m_clientStub.findServiceManager(CMFileSyncManager.class);
        Objects.requireNonNull(syncManager);
        Path syncHome = syncManager.getClientSyncHome();

        // open file chooser to choose files
        JFileChooser fc = new JFileChooser();
        fc.setMultiSelectionEnabled(true);
        fc.setCurrentDirectory(syncHome.toFile());
        int fcRet = fc.showOpenDialog(null);
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
            System.out.println("request error!");
        }
        return;
    }
}