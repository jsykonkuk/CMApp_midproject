
import java.io.*;

import kr.ac.konkuk.ccslab.cm.event.CMEvent;
import kr.ac.konkuk.ccslab.cm.event.CMFileEvent;
import kr.ac.konkuk.ccslab.cm.event.CMMultiServerEvent;
import kr.ac.konkuk.ccslab.cm.event.CMSNSEvent;
import kr.ac.konkuk.ccslab.cm.event.CMSessionEvent;
import kr.ac.konkuk.ccslab.cm.event.CMUserEvent;
import kr.ac.konkuk.ccslab.cm.event.handler.CMAppEventHandler;
import kr.ac.konkuk.ccslab.cm.info.CMConfigurationInfo;
import kr.ac.konkuk.ccslab.cm.info.CMFileTransferInfo;
import kr.ac.konkuk.ccslab.cm.info.CMInfo;
import kr.ac.konkuk.ccslab.cm.manager.CMDBManager;
import kr.ac.konkuk.ccslab.cm.manager.CMFileTransferManager;
import kr.ac.konkuk.ccslab.cm.stub.CMServerStub;

public class CMWinServerEventHandler implements CMAppEventHandler {
    private CMWinServer m_server;
    private CMServerStub m_serverStub;
    private int m_nCheckCount;	// for internal forwarding simulation
    private boolean m_bDistFileProc;	// for distributed file processing

    // information for csc_ftp
    private boolean m_bStartCSCFTPSession;
    private String m_strFileSender;
    private String m_strFileReceiver;
    private int m_nTotalNumFilesPerSession;
    private int m_nCurNumFilesPerSession;

    public CMWinServerEventHandler(CMServerStub serverStub, CMWinServer server)
    {
        m_server = server;
        m_serverStub = serverStub;
        m_nCheckCount = 0;
        m_bDistFileProc = false;

        m_bStartCSCFTPSession = false;
        m_strFileSender = null;
        m_strFileReceiver = null;
        m_nTotalNumFilesPerSession = 0;
        m_nCurNumFilesPerSession = 0;
    }

    @Override
    public void processEvent(CMEvent cme) {
        switch(cme.getType())
        {
            case CMInfo.CM_SESSION_EVENT:
                processSessionEvent(cme);
                break;
            case CMInfo.CM_FILE_EVENT:
                processFileEvent(cme);
                break;
            default:
                return;
        }
    }

    private void processSessionEvent(CMEvent cme)
    {
        CMConfigurationInfo confInfo = m_serverStub.getCMInfo().getConfigurationInfo();
        CMSessionEvent se = (CMSessionEvent) cme;
        switch(se.getID())
        {
            case CMSessionEvent.LOGIN:
                //System.out.println("["+se.getUserName()+"] requests login.");
                printMessage("["+se.getUserName()+"] requests login.\n");
                if(confInfo.isLoginScheme())
                {
                    // user authentication...
                    // CM DB must be used in the following authentication..
                    boolean ret = CMDBManager.authenticateUser(se.getUserName(), se.getPassword(),
                            m_serverStub.getCMInfo());
                    if(!ret)
                    {
                        printMessage("["+se.getUserName()+"] authentication fails!\n");
                        m_serverStub.replyEvent(cme, 0);
                    }
                    else
                    {
                        printMessage("["+se.getUserName()+"] authentication succeeded.\n");
                        m_serverStub.replyEvent(cme, 1);
                    }
                }
                break;
            case CMSessionEvent.LOGOUT:
                //System.out.println("["+se.getUserName()+"] logs out.");
                printMessage("["+se.getUserName()+"] logs out.\n");
                break;
            case CMSessionEvent.REQUEST_SESSION_INFO:
                //System.out.println("["+se.getUserName()+"] requests session information.");
                printMessage("["+se.getUserName()+"] requests session information.\n");
                break;
            case CMSessionEvent.CHANGE_SESSION:
                //System.out.println("["+se.getUserName()+"] changes to session("+se.getSessionName()+").");
                printMessage("["+se.getUserName()+"] changes to session("+se.getSessionName()+").\n");
                break;
            case CMSessionEvent.JOIN_SESSION:
                //System.out.println("["+se.getUserName()+"] requests to join session("+se.getSessionName()+").");
                printMessage("["+se.getUserName()+"] requests to join session("+se.getSessionName()+").\n");
                break;
            case CMSessionEvent.LEAVE_SESSION:
                //System.out.println("["+se.getUserName()+"] leaves a session("+se.getSessionName()+").");
                printMessage("["+se.getUserName()+"] leaves a session("+se.getSessionName()+").\n");
                break;
            case CMSessionEvent.ADD_NONBLOCK_SOCKET_CHANNEL:
                //System.out.println("["+se.getChannelName()+"] request to add SocketChannel with index("
                //		+se.getChannelNum()+").");
                printMessage("["+se.getChannelName()+"] request to add a nonblocking SocketChannel with key("
                        +se.getChannelNum()+").\n");
                break;
            case CMSessionEvent.REGISTER_USER:
                //System.out.println("User registration requested by user["+se.getUserName()+"].");
                printMessage("User registration requested by user["+se.getUserName()+"].\n");
                break;
            case CMSessionEvent.DEREGISTER_USER:
                //System.out.println("User deregistration requested by user["+se.getUserName()+"].");
                printMessage("User deregistration requested by user["+se.getUserName()+"].\n");
                break;
            case CMSessionEvent.FIND_REGISTERED_USER:
                //System.out.println("User profile requested for user["+se.getUserName()+"].");
                printMessage("User profile requested for user["+se.getUserName()+"].\n");
                break;
            case CMSessionEvent.UNEXPECTED_SERVER_DISCONNECTION:
                m_server.printStyledMessage("Unexpected disconnection from ["
                        +se.getChannelName()+"] with key["+se.getChannelNum()+"]!\n", "bold");
                break;
            case CMSessionEvent.INTENTIONALLY_DISCONNECT:
                m_server.printStyledMessage("Intentionally disconnected all channels from ["
                        +se.getChannelName()+"]!\n", "bold");
                break;
            default:
                return;
        }
    }
    private void processUserEvent_start_csc_ftp_session(CMUserEvent ue)
    {
        // store relevant information for a csc-ftp session
        m_bStartCSCFTPSession = true;
        m_strFileSender = ue.getEventField(CMInfo.CM_STR, "strFileSender");
        m_strFileReceiver = ue.getEventField(CMInfo.CM_STR, "strFileReceiver");
        String strNumFiles = ue.getEventField(CMInfo.CM_INT, "nNumFilesPerSession");
        try {
            m_nTotalNumFilesPerSession = Integer.parseInt(strNumFiles);
        }catch(NumberFormatException e) {
            System.err.println("string : "+strNumFiles);
            e.printStackTrace();
        }
        m_nCurNumFilesPerSession = 0;

        if(CMInfo._CM_DEBUG)
        {
            System.out.println("CMWinServerEventHandler.processUserEvent_start_csc_ftp_session(): ");
            System.out.println("m_bStartCSCFTPSession: "+m_bStartCSCFTPSession);
            System.out.println("strFileSender: "+m_strFileSender);
            System.out.println("strFileReceiver: "+m_strFileReceiver);
            System.out.println("nNumFilesPerSession: "+m_nTotalNumFilesPerSession);
            System.out.println("m_nCurNumFilesPerSession: "+m_nCurNumFilesPerSession);
        }

        return;
    }

    private void processFileEvent(CMEvent cme)
    {
        CMFileTransferInfo fInfo = m_serverStub.getCMInfo().getFileTransferInfo();
        long lTotalDelay = 0;
        long lTransferDelay = 0;
        boolean bRet = false;

        CMFileEvent fe = (CMFileEvent) cme;
        switch(fe.getID())
        {
            case CMFileEvent.REQUEST_PERMIT_PULL_FILE:
                printMessage("["+fe.getFileReceiver()+"] requests file("+fe.getFileName()+").\n");
                printMessage("The pull-file request is not automatically permitted!\n");
                printMessage("To change to automatically permit the pull-file request, \n");
                printMessage("set the PERMIT_FILE_TRANSFER field to 1 in the cm-server.conf file\n");
                break;
            case CMFileEvent.REPLY_PERMIT_PULL_FILE:
                if(fe.getReturnCode() == -1)
                {
                    printMessage("["+fe.getFileName()+"] does not exist in the owner!\n");
                }
                else if(fe.getReturnCode() == 0)
                {
                    printMessage("["+fe.getFileSender()+"] rejects to send file("
                            +fe.getFileName()+").\n");
                }
                break;
            case CMFileEvent.REQUEST_PERMIT_PUSH_FILE:
                printMessage("["+fe.getFileSender()+"] wants to send a file("+fe.getFilePath()+
                        ").\n");
                printMessage("The push-file request is not automatically permitted!\n");
                printMessage("To change to automatically permit the push-file request, \n");
                printMessage("set the PERMIT_FILE_TRANSFER field to 1 in the cm-server.conf file\n");
                break;
            case CMFileEvent.REPLY_PERMIT_PUSH_FILE:
                if(fe.getReturnCode() == 0)
                {
                    printMessage("["+fe.getFileReceiver()+"] rejected the push-file request!\n");
                    printMessage("file path("+fe.getFilePath()+"), size("+fe.getFileSize()+").\n");
                }
                break;
            case CMFileEvent.START_FILE_TRANSFER:
            case CMFileEvent.START_FILE_TRANSFER_CHAN:
                if(fInfo.getStartRequestTime() != 0) {
                    printMessage("["+fe.getFileSender()+"] starts to send file("+fe.getFileName()+").\n");
                }
                break;
            case CMFileEvent.START_FILE_TRANSFER_ACK:
            case CMFileEvent.START_FILE_TRANSFER_CHAN_ACK:
                if(fInfo.getStartRequestTime() != 0) {
                    printMessage("["+fe.getFileReceiver()+"] starts to receive file("
                            +fe.getFileName()+").\n");
                }
                break;
            case CMFileEvent.END_FILE_TRANSFER:
            case CMFileEvent.END_FILE_TRANSFER_CHAN:
                if(fInfo.getStartRequestTime() != 0) {
                    printMessage("["+fe.getFileSender()+"] completes to send file("
                            +fe.getFileName()+", "+fe.getFileSize()+" Bytes).\n");

                    lTotalDelay = fInfo.getEndRecvTime() - fInfo.getStartRequestTime();
                    lTransferDelay = fInfo.getEndRecvTime() - fInfo.getStartRecvTime();
                    printMessage("total delay("+lTotalDelay+" ms), ");
                    printMessage("file-receiving delay("+lTransferDelay+" ms).\n");
                }

                String strFile = fe.getFileName();
                if(m_bDistFileProc)
                {
                    processFile(fe.getFileSender(), strFile);
                    m_bDistFileProc = false;
                }

                if(m_bStartCSCFTPSession)
                {
                    String strFilePath = m_serverStub.getTransferedFileHome()
                            +File.separator+fe.getFileSender()
                            +File.separator+fe.getFileName();
                    bRet = m_serverStub.pushFile(strFilePath, m_strFileReceiver, CMInfo.FILE_OVERWRITE);
                    if(!bRet)
                    {
                        printMessage("error to send file("+strFilePath+") to ("
                                +m_strFileReceiver+")!\n");
                    }
                }
                break;
            case CMFileEvent.END_FILE_TRANSFER_ACK:
            case CMFileEvent.END_FILE_TRANSFER_CHAN_ACK:
                if(fInfo.getStartRequestTime() != 0) {
                    printMessage("["+fe.getFileReceiver()+"] completes to receive file("
                            +fe.getFileName()+", "+fe.getFileSize()+" Bytes).\n");
                    lTotalDelay = fInfo.getEndSendTime() - fInfo.getStartRequestTime();
                    lTransferDelay = fInfo.getEndSendTime() - fInfo.getStartSendTime();
                    printMessage("total delay("+lTotalDelay+" ms), ");
                    printMessage("file-sending delay("+lTransferDelay+" ms).\n");
                }

                if(m_bStartCSCFTPSession && fe.getFileReceiver().contentEquals(m_strFileReceiver))
                {
                    checkCompleteCSCFTPSession(fe);
                }
                break;
            case CMFileEvent.REQUEST_DIST_FILE_PROC:
                //System.out.println("["+fe.getUserName()+"] requests the distributed file processing.");
                printMessage("["+fe.getFileReceiver()+"] requests the distributed file processing.\n");
                m_bDistFileProc = true;
                break;
            case CMFileEvent.CANCEL_FILE_SEND:
            case CMFileEvent.CANCEL_FILE_SEND_CHAN:
                printMessage("["+fe.getFileSender()+"] cancelled the file transfer.\n");
                break;
            case CMFileEvent.CANCEL_FILE_RECV_CHAN:
                printMessage("["+fe.getFileReceiver()+"] cancelled the file request.\n");
                break;
        }
        return;
    }

    private void checkCompleteCSCFTPSession(CMFileEvent fe)
    {
        boolean bRet = false;
        m_nCurNumFilesPerSession++;
        if(m_nCurNumFilesPerSession == m_nTotalNumFilesPerSession)
        {
            // send end_csc_ftp_session
            CMUserEvent ue = new CMUserEvent();
            ue.setStringID("end_csc_ftp_session");
            ue.setEventField(CMInfo.CM_STR, "strFileSender", m_strFileSender);
            ue.setEventField(CMInfo.CM_STR, "strFileReceiver", m_strFileReceiver);
            ue.setEventField(CMInfo.CM_INT, "nNumFilesPerSession",
                    Integer.toString(m_nTotalNumFilesPerSession));
            bRet = m_serverStub.send(ue, m_strFileSender);

            if(!bRet)
            {
                printMessage("error sending end_csc_ftp_session event to ("
                        +m_strFileSender+")!\n");
                return;

            }
            // initialize the relevant member variables
            m_bStartCSCFTPSession = false;
            m_strFileSender = null;
            m_strFileReceiver = null;
            m_nTotalNumFilesPerSession = 0;
            m_nCurNumFilesPerSession = 0;
        }

        return;
    }

    private void processFile(String strSender, String strFile)
    {
        CMConfigurationInfo confInfo = m_serverStub.getCMInfo().getConfigurationInfo();
        String strFullSrcFilePath = null;
        String strModifiedFile = null;
        FileInputStream fis = null;
        FileOutputStream fos = null;
        byte[] fileBlock = new byte[CMInfo.FILE_BLOCK_LEN];

        long lStartTime = System.currentTimeMillis();

        // change the modified file name
        strModifiedFile = "m-"+strFile;
        strModifiedFile = confInfo.getTransferedFileHome().toString()+File.separator+strSender+
                File.separator+strModifiedFile;

        // stylize the file
        strFullSrcFilePath = confInfo.getTransferedFileHome().toString()+File.separator+strSender+File.separator+strFile;
        File srcFile = new File(strFullSrcFilePath);
        long lFileSize = srcFile.length();
        long lRemainBytes = lFileSize;
        int readBytes = 0;

        try {
            fis = new FileInputStream(strFullSrcFilePath);
            fos = new FileOutputStream(strModifiedFile);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return;
        }

        try {

            while( lRemainBytes > 0 )
            {
                if( lRemainBytes >= CMInfo.FILE_BLOCK_LEN )
                {
                    readBytes = fis.read(fileBlock);
                }
                else
                {
                    readBytes = fis.read(fileBlock, 0, (int)lRemainBytes);
                }

                fos.write(fileBlock, 0, readBytes);
                lRemainBytes -= readBytes;
            }
        } catch(IOException e) {
            e.printStackTrace();
        } finally {
            try {
                fis.close();
                fos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        // add some process delay here
        for(long i = 0; i < lFileSize/50; i++)
        {
            for(long j = 0; j < lFileSize/50; j++)
            {
                //
            }
        }

        long lEndTime = System.currentTimeMillis();
        //System.out.println("processing delay: "+(lEndTime-lStartTime)+" ms");
        printMessage("processing delay: "+(lEndTime-lStartTime)+" ms\n");

        // send the modified file to the sender
        CMFileTransferManager.pushFile(strModifiedFile, strSender, m_serverStub.getCMInfo());

        return;
    }

    private void processSNSEvent(CMEvent cme)
    {
        CMSNSEvent se = (CMSNSEvent) cme;
        switch(se.getID())
        {
            case CMSNSEvent.CONTENT_DOWNLOAD_REQUEST:
                //System.out.println("["+se.getUserName()+"] requests SNS contents starting at: offset("
                //		+se.getContentOffset()+").");
                printMessage("["+se.getUserName()+"] requests SNS contents starting at: offset("
                        +se.getContentOffset()+").\n");
                break;
            case CMSNSEvent.CONTENT_DOWNLOAD_END_RESPONSE:
                if(se.getReturnCode() == 1)
                {
                    //System.out.println("["+se.getUserName()+"] has received SNS contents starting at "
                    //		+se.getContentOffset()+" successfully.");
                    printMessage("["+se.getUserName()+"] has received SNS contents starting at "
                            +se.getContentOffset()+" successfully.\n");
                }
                else
                {
                    //System.out.println("!! ["+se.getUserName()+" had a problem while receiving SNS "
                    //		+ "contents starting at "+se.getContentOffset()+".");
                    printMessage("!! ["+se.getUserName()+" had a problem while receiving SNS "
                            + "contents starting at "+se.getContentOffset()+".\n");
                }
                break;
            case CMSNSEvent.CONTENT_UPLOAD_REQUEST:
                //System.out.println("content upload requested by ("+se.getUserName()+"), attached file path: "
                //			+se.getAttachedFileName()+", message: "+se.getMessage());
                printMessage("content upload requested by ("+se.getUserName()+"), message("+se.getMessage()
                        +"), #attachement("+se.getNumAttachedFiles()+"), replyID("+se.getReplyOf()
                        +"), lod("+se.getLevelOfDisclosure()+")\n");
                break;
            case CMSNSEvent.REQUEST_ATTACHED_FILE:
                printMessage("["+se.getUserName()+"] requests an attached file ["
                        +se.getFileName()+"] of SNS content ID["+se.getContentID()+"] written by ["
                        +se.getWriterName()+"].\n");
                break;
        }
        return;
    }

    private void processMultiServerEvent(CMEvent cme)
    {
        CMConfigurationInfo confInfo = m_serverStub.getCMInfo().getConfigurationInfo();
        CMMultiServerEvent mse = (CMMultiServerEvent) cme;
        switch(mse.getID())
        {
            case CMMultiServerEvent.REQ_SERVER_REG:
                printMessage("server ("+mse.getServerName()+") requests registration: ip("
                        +mse.getServerAddress()+"), port("+mse.getServerPort()+"), udpport("
                        +mse.getServerUDPPort()+").\n");
                break;
            case CMMultiServerEvent.RES_SERVER_REG:
                if( mse.getReturnCode() == 1 )
                {
                    m_server.updateTitle();
                    printMessage("server["+mse.getServerName()+"] is successfully registered "
                            + "to the default server.\n");
                }
                else
                {
                    printMessage("server["+mse.getServerName()+"] is not registered to the "
                            + "default server.\n");
                }
                break;
            case CMMultiServerEvent.REQ_SERVER_DEREG:
                printMessage("server["+mse.getServerName()+"] requests deregistration.\n");
                break;
            case CMMultiServerEvent.RES_SERVER_DEREG:
                if( mse.getReturnCode() == 1 )
                {
                    printMessage("server["+mse.getServerName()+"] is successfully deregistered "
                            + "from the default server.\n");
                }
                else
                {
                    printMessage("server["+mse.getServerName()+"] is not deregistered from the "
                            + "default server.\n");
                }
                break;
            case CMMultiServerEvent.ADD_LOGIN:
                if( confInfo.isLoginScheme() )
                {
                    // user authentication omitted for the login to an additional server
                    //CMInteractionManager.replyToADD_LOGIN(mse, true, m_serverStub.getCMInfo());
                    m_serverStub.replyEvent(mse, 1);
                }
                printMessage("["+mse.getUserName()+"] requests login to this server("
                        +mse.getServerName()+").\n");
                break;
            case CMMultiServerEvent.ADD_LOGOUT:
                printMessage("["+mse.getUserName()+"] log out this server("+mse.getServerName()
                        +").\n");
                break;
            case CMMultiServerEvent.ADD_REQUEST_SESSION_INFO:
                printMessage("["+mse.getUserName()+"] requests session information.\n");
                break;
        }

        return;
    }
