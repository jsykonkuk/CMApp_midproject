package cm.app.init.login;

import kr.ac.konkuk.ccslab.cm.entity.CMList;
import kr.ac.konkuk.ccslab.cm.entity.CMRecvFileInfo;
import kr.ac.konkuk.ccslab.cm.entity.CMSendFileInfo;
import kr.ac.konkuk.ccslab.cm.info.CMFileTransferInfo;
import kr.ac.konkuk.ccslab.cm.info.CMInfo;
import kr.ac.konkuk.ccslab.cm.stub.CMServerStub;

import javax.swing.*;
import java.awt.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Hashtable;
import java.util.Scanner;
import java.util.Set;

public class CMServerApp {
    private CMServerStub m_serverStub;
    private cm.app.init.login.CMServerEventHandler m_eventHandler;

    public CMServerApp() {
        m_serverStub = new CMServerStub();
        m_eventHandler = new cm.app.init.login.CMServerEventHandler(m_serverStub);
    }

    public CMServerStub getServerStub() {
        return m_serverStub;
    }

    public cm.app.init.login.CMServerEventHandler getServerEventHandler() {
        return m_eventHandler;
    }

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        CMServerApp server = new CMServerApp();
        CMServerStub serverStub = server.getServerStub();
        serverStub.setAppEventHandler(server.getServerEventHandler());

        // start CM
        boolean ret = serverStub.startCM();

        if(ret) {
            System.out.println("CM initialization succeeds.");
        }
        else {
            System.err.println("CM initialization error!");
        }

        // terminate CM
        System.out.println("Enter to terminate CM and server: ");
        scanner.nextLine();
        serverStub.terminateCM();
    }
    private void openFileSyncFolder() {
        System.out.println("=========== open file-sync folder\n");
        // ask client name
        String userName = JOptionPane.showInputDialog("User Name:");
        if(userName != null) {
            // get the file-sync home of "userName"
            Path syncHome = m_serverStub.getFileSyncHome(userName);
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
    }
    public void setFilePath()
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

        m_serverStub.setTransferedFileHome(Paths.get(strPath));

        System.out.println("======");
    }

    public void requestFile()
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
            System.out.print("File owner(user name): ");
            strFileOwner = br.readLine();
            System.out.print("File append mode('y'(append);'n'(overwrite);''(empty for the default configuration): ");
            strFileAppend = br.readLine();
        } catch (IOException e) {
            e.printStackTrace();
        }

        if(strFileAppend.isEmpty())
            bReturn = m_serverStub.requestFile(strFileName, strFileOwner);
        else if(strFileAppend.equals("y"))
            bReturn = m_serverStub.requestFile(strFileName,  strFileOwner, CMInfo.FILE_APPEND);
        else if(strFileAppend.equals("n"))
            bReturn = m_serverStub.requestFile(strFileName,  strFileOwner, CMInfo.FILE_OVERWRITE);
        else
            System.err.println("wrong input for the file append mode!");

        if(!bReturn)
            System.err.println("Request file error! file("+strFileName+"), owner("+strFileOwner+").");

        System.out.println("======");
    }

    public void pushFile()
    {
        boolean bReturn = false;
        String strFilePath = null;
        String strReceiver = null;
        String strFileAppend = null;
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        System.out.println("====== push a file");

        try {
            System.out.print("File path name: ");
            strFilePath = br.readLine();
            System.out.print("File receiver (user name): ");
            strReceiver = br.readLine();
            System.out.print("File append mode('y'(append);'n'(overwrite);''(empty for the default configuration): ");
            strFileAppend = br.readLine();
        } catch (IOException e) {
            e.printStackTrace();
        }

        if(strFileAppend.isEmpty())
            bReturn = m_serverStub.pushFile(strFilePath, strReceiver);
        else if(strFileAppend.equals("y"))
            bReturn = m_serverStub.pushFile(strFilePath,  strReceiver, CMInfo.FILE_APPEND);
        else if(strFileAppend.equals("n"))
            bReturn = m_serverStub.pushFile(strFilePath,  strReceiver, CMInfo.FILE_OVERWRITE);
        else
            System.err.println("wrong input for the file append mode!");

        if(!bReturn)
            System.err.println("Push file error! file("+strFilePath+"), receiver("+strReceiver+")");

        System.out.println("======");
    }

    public void cancelRecvFile()
    {
        String strSender = null;
        boolean bReturn = false;
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        System.out.println("====== cancel receiving a file");

        System.out.print("Input sender name (enter for all senders): ");
        try {
            strSender = br.readLine();
            if(strSender.isEmpty())
                strSender = null;
        } catch (IOException e) {
            e.printStackTrace();
        }

        bReturn = m_serverStub.cancelPullFile(strSender);

        if(bReturn)
        {
            if(strSender == null)
                strSender = "all senders";
            System.out.println("Successfully requested to cancel receiving a file to ["+strSender+"].");
        }
        else
            System.err.println("Request failed to cancel receiving a file to ["+strSender+"]!");

        return;

    }

    public void cancelSendFile()
    {
        String strReceiver = null;
        boolean bReturn = false;
        System.out.println("====== cancel sending a file");
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        System.out.print("Input receiver name (enter for all receivers): ");

        try {
            strReceiver = br.readLine();
            if(strReceiver.isEmpty())
                strReceiver = null;
        } catch (IOException e) {
            e.printStackTrace();
        }

        bReturn = m_serverStub.cancelPushFile(strReceiver);

        if(bReturn)
            System.out.println("Successfully requested to cancel sending a file to ["+strReceiver+"]");
        else
            System.err.println("Request failed to cancel sending a file to ["+strReceiver+"]!");

        return;
    }

    public void printSendRecvFileInfo()
    {
        CMFileTransferInfo fInfo = m_serverStub.getCMInfo().getFileTransferInfo();
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
}
