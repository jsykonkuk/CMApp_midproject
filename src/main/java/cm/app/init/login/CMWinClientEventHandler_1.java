
import java.util.Iterator;

import javax.swing.JOptionPane;

import java.io.*;
import java.awt.*;

import kr.ac.konkuk.ccslab.cm.entity.CMSessionInfo;
import kr.ac.konkuk.ccslab.cm.event.CMEvent;
import kr.ac.konkuk.ccslab.cm.event.CMFileEvent;
import kr.ac.konkuk.ccslab.cm.event.CMSessionEvent;
import kr.ac.konkuk.ccslab.cm.event.CMUserEvent;
import kr.ac.konkuk.ccslab.cm.event.CMUserEventField;
import kr.ac.konkuk.ccslab.cm.event.filesync.CMFileSyncEvent;
import kr.ac.konkuk.ccslab.cm.event.filesync.CMFileSyncEventCompleteNewFile;
import kr.ac.konkuk.ccslab.cm.event.filesync.CMFileSyncEventCompleteUpdateFile;
import kr.ac.konkuk.ccslab.cm.event.filesync.CMFileSyncEventSkipUpdateFile;
import kr.ac.konkuk.ccslab.cm.event.handler.CMAppEventHandler;

public class CMWinClientEventHandler_1 implements CMAppEventHandler{
    //private JTextArea m_outTextArea;
    private CMWinClient m_client;
    private CMClientStub m_clientStub;
    distributed file processing

    public CMWinClientEventHandle_1(CMClientStub clientStub, CMWinClient client)
    {
        m_client = client;
        //m_outTextArea = textArea;
        m_clientStub = clientStub;
    }
    @Override
    public void processEvent(CMEvent cme) {
        switch(cme.getType())
        {
            Object CMInfo;
            case CMInfo.CM_SESSION_EVENT:
                processSessionEvent(cme);
                break;
            default:
                return;
        }
    }

    private void processFileSyncEvent(CMEvent cme) {
        CMFileSyncEvent fse = (CMFileSyncEvent) cme;
        switch(fse.getID())
        {
            case CMFileSyncEvent.COMPLETE_NEW_FILE:
                CMFileSyncEventCompleteNewFile newFileEvent = (CMFileSyncEventCompleteNewFile) fse;
                printMessage();
                break;
            case CMFileSyncEvent.COMPLETE_UPDATE_FILE:
                CMFileSyncEventCompleteUpdateFile updateFileEvent = (CMFileSyncEventCompleteUpdateFile) fse;
                printMessage();
                break;
            case CMFileSyncEvent.SKIP_UPDATE_FILE:
                CMFileSyncEventSkipUpdateFile skipFileEvent = (CMFileSyncEventSkipUpdateFile) fse;
                printMessage("file skipped: " + skipFileEvent.getSkippedPath() + "\n");
                break;
            case CMFileSyncEvent.COMPLETE_FILE_SYNC:
                printMessage("The file sync completes.\n");
                if(startTimeOfFileSync > 0) {
                    long elapsedTime = System.currentTimeMillis() - startTimeOfFileSync;
                    printMessage("File-sync delay: "+elapsedTime+" ms.\n");
                    startTimeOfFileSync = 0;
                }
                break;
            default:
                return;
        }
    }

    private void processSessionEvent(CMEvent cme)
    {
        long lDelay = 0;
        CMSessionEvent se = (CMSessionEvent)cme;
        switch(se.getID())
        {
            case CMSessionEvent.LOGIN_ACK:
                lDelay = System.currentTimeMillis() - m_lStartTime;
                printMessage("LOGIN_ACK delay: "+lDelay+" ms.\n");
                if(se.isValidUser() == 0)
                {
                    printMessage("This client fails authentication by the default server!\n");
                }
                else if(se.isValidUser() == -1)
                {
                    printMessage("This client is already in the login-user list!\n");
                }
                else
                {
                    printMessage("This client successfully logs in to the default server.\n");
                    CMInteractionInfo interInfo = m_clientStub.getCMInfo().getInteractionInfo();

                    // Change the title of the client window
                    m_client.setTitle("CM Client ["+interInfo.getMyself().getName()+"]");

                    // Set the appearance of buttons in the client frame window
                    m_client.setButtonsAccordingToClientState();
                }
                break;
            case CMSessionEvent.RESPONSE_SESSION_INFO:
                lDelay = System.currentTimeMillis() - m_lStartTime;
                printMessage("RESPONSE_SESSION_INFO delay: "+lDelay+" ms.\n");
                processRESPONSE_SESSION_INFO(se);
                break;
            case CMSessionEvent.SESSION_TALK:
                //System.out.println("("+se.getHandlerSession()+")");
                printMessage("("+se.getHandlerSession()+")\n");
                //System.out.println("<"+se.getUserName()+">: "+se.getTalk());
                printMessage("<"+se.getUserName()+">: "+se.getTalk()+"\n");
                break;
            case CMSessionEvent.JOIN_SESSION_ACK:
                lDelay = System.currentTimeMillis() - m_lStartTime;
                printMessage("JOIN_SESSION_ACK delay: "+lDelay+" ms.\n");
                m_client.setButtonsAccordingToClientState();
                break;
            case CMSessionEvent.ADD_NONBLOCK_SOCKET_CHANNEL_ACK:
                if(se.getReturnCode() == 0)
                {
                    printMessage("Adding a nonblocking SocketChannel("+se.getChannelName()+","+se.getChannelNum()
                            +") failed at the server!\n");
                }
                else
                {
                    printMessage("Adding a nonblocking SocketChannel("+se.getChannelName()+","+se.getChannelNum()
                            +") succeeded at the server!\n");
                }
                break;
            case CMSessionEvent.ADD_BLOCK_SOCKET_CHANNEL_ACK:
                //lDelay = System.currentTimeMillis() - m_lStartTime;
                //printMessage("ADD_BLOCK_SOCKET_CHANNEL_ACK delay: "+lDelay+" ms.\n");
                if(se.getReturnCode() == 0)
                {
                    printMessage("Adding a blocking socket channel ("+se.getChannelName()+","+se.getChannelNum()
                            +") failed at the server!\n");
                }
                else
                {
                    printMessage("Adding a blocking socket channel("+se.getChannelName()+","+se.getChannelNum()
                            +") succeeded at the server!\n");
                }
                break;
            case CMSessionEvent.REMOVE_BLOCK_SOCKET_CHANNEL_ACK:
                //lDelay = System.currentTimeMillis() - m_lStartTime;
                //printMessage("REMOVE_BLOCK_SOCKET_CHANNEL_ACK delay: "+lDelay+" ms.\n");
                if(se.getReturnCode() == 0)
                {
                    printMessage("Removing a blocking socket channel ("+se.getChannelName()+","+se.getChannelNum()
                            +") failed at the server!\n");
                }
                else
                {
                    printMessage("Removing a blocking socket channel("+se.getChannelName()+","+se.getChannelNum()
                            +") succeeded at the server!\n");
                }
                break;
            case CMSessionEvent.REGISTER_USER_ACK:
                if( se.getReturnCode() == 1 )
                {
                    // user registration succeeded
                    //System.out.println("User["+se.getUserName()+"] successfully registered at time["
                    //			+se.getCreationTime()+"].");
                    printMessage("User["+se.getUserName()+"] successfully registered at time["
                            +se.getCreationTime()+"].\n");
                }
                else
                {
                    // user registration failed
                    //System.out.println("User["+se.getUserName()+"] failed to register!");
                    printMessage("User["+se.getUserName()+"] failed to register!\n");
                }
                break;
            case CMSessionEvent.DEREGISTER_USER_ACK:
                if( se.getReturnCode() == 1 )
                {
                    // user deregistration succeeded
                    //System.out.println("User["+se.getUserName()+"] successfully deregistered.");
                    printMessage("User["+se.getUserName()+"] successfully deregistered.\n");
                }
                else
                {
                    // user registration failed
                    //System.out.println("User["+se.getUserName()+"] failed to deregister!");
                    printMessage("User["+se.getUserName()+"] failed to deregister!\n");
                }
                break;
            case CMSessionEvent.FIND_REGISTERED_USER_ACK:
                if( se.getReturnCode() == 1 )
                {
                    //System.out.println("User profile search succeeded: user["+se.getUserName()
                    //		+"], registration time["+se.getCreationTime()+"].");
                    printMessage("User profile search succeeded: user["+se.getUserName()
                            +"], registration time["+se.getCreationTime()+"].\n");
                }
                else
                {
                    //System.out.println("User profile search failed: user["+se.getUserName()+"]!");
                    printMessage("User profile search failed: user["+se.getUserName()+"]!\n");
                }
                break;
            case CMSessionEvent.UNEXPECTED_SERVER_DISCONNECTION:
                m_client.printStyledMessage("Unexpected disconnection from ["
                        +se.getChannelName()+"] with key["+se.getChannelNum()+"]!\n", "bold");
                m_client.setButtonsAccordingToClientState();
                m_client.setTitle("CM Client");
                break;
            case CMSessionEvent.INTENTIONALLY_DISCONNECT:
                m_client.printStyledMessage("Intentionally disconnected all channels from ["
                        +se.getChannelName()+"]!\n", "bold");
                m_client.setButtonsAccordingToClientState();
                m_client.setTitle("CM Client");
                break;
            default:
                return;
        }
    }

    private void processRESPONSE_SESSION_INFO(CMSessionEvent se)
    {
        Iterator<CMSessionInfo> iter = se.getSessionInfoList().iterator();

        printMessage(String.format("%-60s%n", "------------------------------------------------------------"));
        printMessage(String.format("%-20s%-20s%-10s%-10s%n", "name", "address", "port", "user num"));
        printMessage(String.format("%-60s%n", "------------------------------------------------------------"));

        while(iter.hasNext())
        {
            CMSessionInfo tInfo = iter.next();
            printMessage(String.format("%-20s%-20s%-10d%-10d%n", tInfo.getSessionName(), tInfo.getAddress(),
                    tInfo.getPort(), tInfo.getUserNum()));
        }
    }

    private void processUserEvent(CMEvent cme)
    {
        int id = -1;
        long lSendTime = 0;
        int nSendNum = 0;

        CMUserEvent ue = (CMUserEvent) cme;

        if(ue.getStringID().equals("testForward"))
        {
            id = Integer.parseInt(ue.getEventField(CMInfo.CM_INT, "id"));
            //System.out.println("Received user event \'testForward\', id: "+id);
            printMessage("Received user event \'testForward\', id: "+id+"\n");
        }
        else if(ue.getStringID().equals("testNotForward"))
        {
            id = Integer.parseInt(ue.getEventField(CMInfo.CM_INT, "id"));
            //System.out.println("Received user event 'testNotForward', id("+id+")");
            printMessage("Received user event 'testNotForward', id("+id+")\n");
        }
        else if(ue.getStringID().equals("testForwardDelay"))
        {
            id = Integer.parseInt(ue.getEventField(CMInfo.CM_INT, "id"));
            lSendTime = Long.parseLong(ue.getEventField(CMInfo.CM_LONG, "stime"));
            long lDelay = System.currentTimeMillis() - lSendTime;
            m_lDelaySum += lDelay;
            //System.out.println("Received user event 'testNotForward', id("+id+"), delay("+lDelay+"), delay_sum("+m_lDelaySum+")");
            printMessage("Received user event 'testNotForward', id("+id+"), delay("+lDelay+"), delay_sum("+m_lDelaySum+")\n");
        }
        else if(ue.getStringID().equals("EndForwardDelay"))
        {
            nSendNum = Integer.parseInt(ue.getEventField(CMInfo.CM_INT, "sendnum"));
            //System.out.println("Received user envet 'EndForwardDelay', avg delay("+m_lDelaySum/nSendNum+" ms)");
            printMessage("Received user envet 'EndForwardDelay', avg delay("+m_lDelaySum/nSendNum+" ms)\n");
            m_lDelaySum = 0;
        }
        else if(ue.getStringID().equals("repRecv"))
        {
            String strReceiver = ue.getEventField(CMInfo.CM_STR, "receiver");
            int nBlockingChannelType = Integer.parseInt(ue.getEventField(CMInfo.CM_INT, "chType"));
            int nBlockingChannelKey = Integer.parseInt(ue.getEventField(CMInfo.CM_INT, "chKey"));
            int nRecvPort = Integer.parseInt(ue.getEventField(CMInfo.CM_INT, "recvPort"));
            int opt = -1;
            if(nBlockingChannelType == CMInfo.CM_SOCKET_CHANNEL)
                opt = CMInfo.CM_STREAM;
            else if(nBlockingChannelType == CMInfo.CM_DATAGRAM_CHANNEL)
                opt = CMInfo.CM_DATAGRAM;

            CMDummyEvent due = new CMDummyEvent();
            due.setDummyInfo("This is a test message to test a blocking channel");
            System.out.println("Sending a dummy event to ("+strReceiver+")..");

            if(opt == CMInfo.CM_STREAM)
                m_clientStub.send(due, strReceiver, opt, nBlockingChannelKey, true);
            else if(opt == CMInfo.CM_DATAGRAM)
                m_clientStub.send(due, strReceiver, opt, nBlockingChannelKey, nRecvPort, true);
            else
                System.err.println("invalid sending option!: "+opt);
        }
        else if(ue.getStringID().equals("testSendRecv"))
        {
            printMessage("Received user event from ["+ue.getSender()+"] to ["+ue.getReceiver()+
                    "], (id, "+ue.getID()+"), (string id, "+ue.getStringID()+")\n");

            if(!m_clientStub.getMyself().getName().equals(ue.getReceiver()))
                return;

            CMUserEvent rue = new CMUserEvent();
            rue.setID(222);
            rue.setStringID("testReplySendRecv");
            boolean ret = m_clientStub.send(rue, ue.getSender());
            if(ret)
                printMessage("Sent reply event: (id, "+rue.getID()+"), (string id, "+rue.getStringID()+")\n");
            else
                printMessage("Failed to send the reply event!\n");
        }
        else if(ue.getStringID().equals("testCastRecv"))
        {
            printMessage("Received user event from ["+ue.getSender()+"], to session["+
                    ue.getEventField(CMInfo.CM_STR, "Target Session")+"] and group["+
                    ue.getEventField(CMInfo.CM_STR,  "Target Group")+"], (id, "+ue.getID()+
                    "), (string id, "+ue.getStringID()+")\n");
            CMUserEvent rue = new CMUserEvent();
            rue.setID(223);
            rue.setStringID("testReplyCastRecv");
            boolean ret = m_clientStub.send(rue, ue.getSender());
            if(ret)
                printMessage("Sent reply event: (id, "+rue.getID()+"), (sting id, "+rue.getStringID()+")\n");
            else
                printMessage("Failed to send the reply event!\n");
        }
        else if(ue.getStringID().equals("testReplySendRecv")) // for testing asynchronous sendrecv service
        {
            long lServerResponseDelay = System.currentTimeMillis() - m_lStartTime;
            printMessage("Asynchronously received reply event from ["+ue.getSender()+"]: (type, "+ue.getType()+
                    "), (id, "+ue.getID()+"), (string id, "+ue.getStringID()+")\n");
            printMessage("Server response delay: "+lServerResponseDelay+"ms.\n");

        }
        else if(ue.getStringID().equals("testReplyCastRecv")) // for testing asynchronous castrecv service
        {
            //printMessage("Asynchronously received reply event from ["+ue.getSender()+"]: (type, "+ue.getType()+
            //		"), (id, "+ue.getID()+"), (string id, "+ue.getStringID()+")\n");
            m_nRecvReplyEvents++;

            if(m_nRecvReplyEvents == m_nMinNumWaitedEvents)
            {
                long lServerResponseDelay = System.currentTimeMillis() - m_lStartTime;
                printMessage("Complete to receive requested number of reply events.\n");
                printMessage("Number of received reply events: "+m_nRecvReplyEvents+"\n");
                printMessage("Server response delay: "+lServerResponseDelay+"ms.\n");
                m_nRecvReplyEvents = 0;
            }

        }
        else if(ue.getStringID().contentEquals("end_csc_ftp_session"))
        {
            processUserEvent_end_csc_ftp_session(ue);
        }
        else
        {
            printMessage("CMUserEvent received from ["+ue.getSender()+"], strID("+ue.getStringID()+")\n");
            printMessage(String.format("%-5s%-20s%-10s%-20s%n", "Type", "Field", "Length", "Value"));
            printMessage("-----------------------------------------------------\n");
            Iterator<CMUserEventField> iter = ue.getAllEventFields().iterator();
            while(iter.hasNext())
            {
                CMUserEventField uef = iter.next();
                if(uef.nDataType == CMInfo.CM_BYTES)
                {
                    printMessage(String.format("%-5s%-20s%-10d", uef.nDataType, uef.strFieldName,
                            uef.nValueByteNum));
                    for(int i = 0; i < uef.nValueByteNum; i++)
                    {
                        //not yet
                    }
                    printMessage("\n");
                }
                else
                {
                    printMessage(String.format("%-5d%-20s%-10d%-20s%n", uef.nDataType, uef.strFieldName,
                            uef.strFieldValue.length(), uef.strFieldValue));
                }
            }
        }
        return;
    }
    private void processFileEvent(CMEvent cme)
    {
        CMFileEvent fe = (CMFileEvent) cme;
        CMConfigurationInfo confInfo = null;
        CMFileTransferInfo fInfo = m_clientStub.getCMInfo().getFileTransferInfo();
        int nOption = -1;
        long lTotalDelay = 0;
        long lTransferDelay = 0;

        switch(fe.getID())
        {
            case CMFileEvent.REQUEST_PERMIT_PULL_FILE:
                String strReq = "["+fe.getFileReceiver()+"] requests file("+fe.getFileName()+
                        ").\n";
                printMessage(strReq);
                nOption = JOptionPane.showConfirmDialog(null, strReq, "Request a file",
                        JOptionPane.YES_NO_OPTION);
                if(nOption == JOptionPane.YES_OPTION)
                {
                    m_clientStub.replyEvent(fe, 1);
                }
                else
                {
                    m_clientStub.replyEvent(fe, 0);
                }
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
                StringBuffer strReqBuf = new StringBuffer();
                strReqBuf.append("["+fe.getFileSender()+"] wants to send a file.\n");
                strReqBuf.append("file path: "+fe.getFilePath()+"\n");
                strReqBuf.append("file size: "+fe.getFileSize()+"\n");
                printMessage(strReqBuf.toString());
                nOption = JOptionPane.showConfirmDialog(null, strReqBuf.toString(),
                        "Permit to receive a file", JOptionPane.YES_NO_OPTION);
                if(nOption == JOptionPane.YES_OPTION)
                {
                    m_clientStub.replyEvent(fe, 1);
                }
                else
                {
                    m_clientStub.replyEvent(fe, 0);
                }
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
                if(fInfo.getStartRequestTime() != 0) {	// request started by app instead of other reasons
                    printMessage("["+fe.getFileSender()+"] starts to send file("+fe.getFileName()+").\n");
                }
                break;
            case CMFileEvent.START_FILE_TRANSFER_ACK:
            case CMFileEvent.START_FILE_TRANSFER_CHAN_ACK:
                if(fInfo.getStartRequestTime() != 0) {	// request started by app instead of other reasons
                    printMessage("["+fe.getFileReceiver()+"] starts to receive file("
                            +fe.getFileName()+").\n");
                }
                break;
            case CMFileEvent.END_FILE_TRANSFER:
            case CMFileEvent.END_FILE_TRANSFER_CHAN:
                if(fInfo.getStartRequestTime() != 0) {	// request started by app instead of other reasons
                    printMessage("["+fe.getFileSender()+"] completes to send file("
                            +fe.getFileName()+", "+fe.getFileSize()+" Bytes).\n");
                    lTotalDelay = fInfo.getEndRecvTime() - fInfo.getStartRequestTime();
                    printMessage("total delay(" + lTotalDelay + " ms), ");
                    lTransferDelay = fInfo.getEndRecvTime() - fInfo.getStartRecvTime();
                    printMessage("File-receiving delay("+lTransferDelay+" ms).\n");
                }

                if(m_bDistFileProc)
                    processFile(fe.getFileName());
                if(m_bReqAttachedFile)
                {
                    confInfo = m_clientStub.getCMInfo().getConfigurationInfo();
                    String strPath = confInfo.getTransferedFileHome().toString() + File.separator + fe.getFileName();
                    File file = new File(strPath);
                    try {
                        Desktop.getDesktop().open(file);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    m_bReqAttachedFile = false;
                }
                break;
            case CMFileEvent.END_FILE_TRANSFER_ACK:
            case CMFileEvent.END_FILE_TRANSFER_CHAN_ACK:
                if(fInfo.getStartRequestTime() != 0) {	// request started by app instead of other reasons
                    printMessage("["+fe.getFileReceiver()+"] completes to receive file("
                            +fe.getFileName()+", "+fe.getFileSize()+" Bytes).\n");
                    lTotalDelay = fInfo.getEndSendTime() - fInfo.getStartRequestTime();
                    printMessage("Total delay(" + lTotalDelay + " ms).\n");
                    lTransferDelay = fInfo.getEndSendTime() - fInfo.getStartSendTime();
                    printMessage("File-sending delay("+lTransferDelay+" ms).\n");
                }

                if(m_bStartC2CFTPSession && fe.getFileReceiver().contentEquals(m_strFileReceiver))
                {
                    checkCompleteC2CFTPSession(fe);
                }
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

    private void processFile(String strFile)
    {
        CMConfigurationInfo confInfo = m_clientStub.getCMInfo().getConfigurationInfo();
        String strMergeName = null;

        // add file name to list and increase index
        if(m_nCurrentServerNum == 1)
        {
            m_filePieces[m_nRecvPieceNum++] = confInfo.getTransferedFileHome().toString()+File.separator+strFile;
        }
        else
        {
            // Be careful to put a file into an appropriate array member (file piece order)
            // extract piece number from file name ('filename'-'number'.split )
            int nStartIndex = strFile.lastIndexOf("-")+1;
            int nEndIndex = strFile.lastIndexOf(".");
            int nPieceIndex = Integer.parseInt(strFile.substring(nStartIndex, nEndIndex))-1;

            m_filePieces[nPieceIndex] = confInfo.getTransferedFileHome().toString()+File.separator+strFile;
            m_nRecvPieceNum++;
        }


        // if the index is the same as the number of servers, merge the split file
        if( m_nRecvPieceNum == m_nCurrentServerNum )
        {
            if(m_nRecvPieceNum > 1)
            {
                // set the merged file name m-'file name'.'ext'
                int index = strFile.lastIndexOf("-");
                strMergeName = confInfo.getTransferedFileHome().toString()+File.separator+
                        strFile.substring(0, index)+"."+m_strExt;

                // merge split pieces
                CMFileTransferManager.mergeFiles(m_filePieces, m_nCurrentServerNum, strMergeName);
            }

            // calculate the total delay
            long lRecvTime = System.currentTimeMillis();
            //System.out.println("total delay for ("+m_nRecvPieceNum+") files: "
            //					+(lRecvTime-m_lStartTime)+" ms");
            printMessage("total delay for ("+m_nRecvPieceNum+") files: "
                    +(lRecvTime-m_lStartTime)+" ms\n");

            // reset m_bDistSendRecv, m_nRecvFilePieceNum
            m_bDistFileProc = false;
            m_nRecvPieceNum = 0;
        }

        return;
    }
}