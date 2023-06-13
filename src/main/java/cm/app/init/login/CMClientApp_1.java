import java.io.*;
import java.util.*;

import kr.ac.konkuk.ccslab.cm.event.CMSessionEvent;
import kr.ac.konkuk.ccslab.cm.event.CMUserEvent;
import kr.ac.konkuk.ccslab.cm.info.*;
import kr.ac.konkuk.ccslab.cm.manager.*;
import kr.ac.konkuk.ccslab.cm.stub.CMClientStub;


public class CMClientApp_1 {
    private CMClientStub m_clientStub;
    private CMClientEventHandler_1 m_eventHandler;
    private boolean m_bRun;
    private Scanner m_scan = null;

    public CMClientApp_1()
    {
        m_clientStub = new CMClientStub();
        m_eventHandler = new CMClientEventHandler_1(m_clientStub);
        m_bRun = true;
    }

    public CMClientStub getClientStub()
    {
        return m_clientStub;
    }

    public CMClientEventHandler_1 getClientEventHandler()
    {
        return m_eventHandler;
    }

    public void testSyncLoginDS()
    {
        String strUserName = null;
        String strPassword = null;
        CMSessionEvent loginAckEvent = null;
        Console console = System.console();
        if(console == null)
        {
            System.err.println("Unable to obtain console.");
        }

        System.out.println("====== login to default server");
        System.out.print("user name: ");
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        try {
            strUserName = br.readLine();
            if(console == null)
            {
                System.out.print("password: ");
                strPassword = br.readLine();
            }
            else
                strPassword = new String(console.readPassword("password: "));
        } catch (IOException e) {
            e.printStackTrace();
        }

        loginAckEvent = m_clientStub.syncLoginCM(strUserName, strPassword);
        if(loginAckEvent != null)
        {
            // print login result
            if(loginAckEvent.isValidUser() == 0)
            {
                System.err.println("This client fails authentication by the default server!");
            }
            else if(loginAckEvent.isValidUser() == -1)
            {
                System.err.println("This client is already in the login-user list!");
            }
            else
            {
                System.out.println("This client successfully logs in to the default server.");
            }
        }
        else
        {
            System.err.println("failed the login request!");
        }

        System.out.println("======");
    }

    public void testJoinSession()
    {
        String strSessionName = null;
        boolean bRequestResult = false;
        System.out.println("====== join a session");
        System.out.print("session name: ");
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        try {
            strSessionName = br.readLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
        bRequestResult = m_clientStub.joinSession(strSessionName);
        if(bRequestResult)
            System.out.println("successfully sent the session-join request.");
        else
            System.err.println("failed the session-join request!");
        System.out.println("======");
    }

    public void testSyncJoinSession()
    {
        CMSessionEvent se = null;
        String strSessionName = null;
        System.out.println("====== join a session");
        System.out.print("session name: ");
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        try {
            strSessionName = br.readLine();
        } catch (IOException e) {
            e.printStackTrace();
        }

        se = m_clientStub.syncJoinSession(strSessionName);
        if(se != null)
        {
            System.out.println("successfully joined a session that has ("+se.getGroupNum()+") groups.");
        }
        else
        {
            System.err.println("failed the session-join request!");
        }

        System.out.println("======");
    }

    public void testLeaveSession()
    {
        boolean bRequestResult = false;
        System.out.println("====== leave the current session");
        bRequestResult = m_clientStub.leaveSession();
        if(bRequestResult)
            System.out.println("successfully sent the leave-session request.");
        else
            System.err.println("failed the leave-session request!");
        System.out.println("======");
    }
    public void testAsyncCastRecv()
    {
        CMUserEvent ue = new CMUserEvent();
        boolean bRet = false;
        String strTargetSession = null;
        String strTargetGroup = null;
        String strMinNumReplyEvents = null;
        int nMinNumReplyEvents = 0;

        // a user event: (id, 112) (string id, "testCastRecv")
        // a reply user event: (id, 223) (string id, "testReplyCastRecv")

        System.out.println("====== test asynchronous castrecv");
        // set a user event
        ue.setID(112);
        ue.setStringID("testCastRecv");

        // set event target session and group
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        System.out.println("user event to be sent: (id, 112), (string id, \"testCastRecv\")");
        System.out.println("reply event to be received: (id, 223), (string id, \"testReplyCastRecv\")");

        try {
            System.out.print("Target session(empty for null): ");
            strTargetSession = br.readLine().trim();
            System.out.print("Target group(empty for null): ");
            strTargetGroup = br.readLine().trim();
            System.out.print("Minimum number of reply events(empty for 0): ");
            strMinNumReplyEvents = br.readLine().trim();
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        if(strTargetSession.isEmpty())
            strTargetSession = null;
        if(strTargetGroup.isEmpty())
            strTargetGroup = null;
        if(strMinNumReplyEvents.isEmpty())
            strMinNumReplyEvents = "0";

        try {
            nMinNumReplyEvents = Integer.parseInt(strMinNumReplyEvents);
        }catch(NumberFormatException e) {
            e.printStackTrace();
            System.err.println("Wrong number format!");
            return;
        }

        System.out.println("Target session: "+strTargetSession);
        System.out.println("Target group: "+strTargetGroup);
        System.out.println("Minimum number of reply events: "+nMinNumReplyEvents);

        m_eventHandler.setStartTime(System.currentTimeMillis());
        m_eventHandler.setMinNumWaitedEvents(nMinNumReplyEvents);
        m_eventHandler.setRecvReplyEvents(0);
        bRet = m_clientStub.cast(ue, strTargetSession, strTargetGroup);

        if(!bRet)
        {
            System.err.println("Error in asynchronous castrecv service!");
            return;
        }
        System.out.println("======");

    }
    public void testRequestFile()
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

    public void testPushFile()
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
    public void testSendMultipleFiles()
    {
        String[] strFiles = null;
        String strFileList = null;
        int nMode = -1; // 1: push, 2: pull
        int nFileNum = -1;
        String strTarget = null;
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        System.out.println("====== pull/push multiple files");
        try {
            System.out.print("Select mode (1: push, 2: pull): ");
            nMode = Integer.parseInt(br.readLine());
            if(nMode == 1)
            {
                System.out.print("Input receiver name: ");
                strTarget = br.readLine();
            }
            else if(nMode == 2)
            {
                System.out.print("Input file owner name: ");
                strTarget = br.readLine();
            }
            else
            {
                System.out.println("Incorrect transmission mode!");
                return;
            }

            System.out.print("Number of files: ");
            nFileNum = Integer.parseInt(br.readLine());
            System.out.print("Input file names separated with space: ");
            strFileList = br.readLine();

        } catch (NumberFormatException e) {
            e.printStackTrace();
            return;
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        strFileList.trim();
        strFiles = strFileList.split("\\s+");
        if(strFiles.length != nFileNum)
        {
            System.out.println("The number of files incorrect!");
            return;
        }

        for(int i = 0; i < nFileNum; i++)
        {
            switch(nMode)
            {
                case 1: // push
                    CMFileTransferManager.pushFile(strFiles[i], strTarget, m_clientStub.getCMInfo());
                    break;
                case 2: // pull
                    CMFileTransferManager.requestPermitForPullFile(strFiles[i], strTarget, m_clientStub.getCMInfo());
                    break;
            }
        }

        return;
    }

    public void testSplitFile()
    {
        String strSrcFile = null;
        String strSplitFile = null;
        long lFileSize = -1;
        long lFileOffset = 0;
        long lSplitSize = -1;
        long lSplitRemainder = -1;
        int nSplitNum = -1;
        RandomAccessFile raf = null;
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));

        System.out.println("====== split a file");
        try {
            System.out.print("Input source file name: ");
            strSrcFile = br.readLine();
            System.out.print("Input the number of splitted files: ");
            nSplitNum = Integer.parseInt(br.readLine());
            raf = new RandomAccessFile(strSrcFile, "r");
            lFileSize = raf.length();

            lSplitSize = lFileSize / nSplitNum;
            lSplitRemainder = lFileSize % lSplitSize;

            for(int i = 0; i < nSplitNum; i++)
            {
                // get the name of split file ('srcfile'-i.split)
                int index = strSrcFile.lastIndexOf(".");
                strSplitFile = strSrcFile.substring(0, index)+"-"+(i+1)+".split";

                // update offset
                lFileOffset = i*lSplitSize;

                if(i+1 != nSplitNum)
                    CMFileTransferManager.splitFile(raf, lFileOffset, lSplitSize, strSplitFile);
                else
                    CMFileTransferManager.splitFile(raf, lFileOffset, lSplitSize+lSplitRemainder, strSplitFile);

            }

            raf.close();
        } catch (FileNotFoundException fe) {
            fe.printStackTrace();
            return;
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        return;
    }
    public static void main(String[] args) {
        CMClientApp_1 client = new CMClientApp_1();
        CMClientStub cmStub = client.getClientStub();
        cmStub.setAppEventHandler(client.getClientEventHandler());
        client.testStartCM();

        System.out.println("Client application is terminated.");
    }

}