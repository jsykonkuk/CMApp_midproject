package cm.app.init.login;

import kr.ac.konkuk.ccslab.cm.event.CMEvent;
import kr.ac.konkuk.ccslab.cm.event.CMFileEvent;
import kr.ac.konkuk.ccslab.cm.event.CMSessionEvent;
import kr.ac.konkuk.ccslab.cm.event.CMUserEvent;
import kr.ac.konkuk.ccslab.cm.event.handler.CMAppEventHandler;
import kr.ac.konkuk.ccslab.cm.info.CMConfigurationInfo;
import kr.ac.konkuk.ccslab.cm.info.CMInfo;
import kr.ac.konkuk.ccslab.cm.manager.CMFileTransferManager;
import kr.ac.konkuk.ccslab.cm.stub.CMServerStub;

import java.io.*;

public class CMServerEventHandler implements CMAppEventHandler {
    private CMServerStub m_serverStub;

    public CMServerEventHandler(CMServerStub serverStub) {
        m_serverStub = serverStub;
    }

    @Override
    public void processEvent(CMEvent cme) {
        switch (cme.getType()) {
            case CMInfo.CM_SESSION_EVENT:
                processSessionEvent(cme);
                break;
            case CMInfo.CM_USER_EVENT:
                processUserEvent(cme);
                break;
            case CMInfo.CM_FILE_EVENT:
                processFileEvent(cme);
                break;
            default:
                break;
        }
    }

    private void processSessionEvent(CMEvent cme) {
        CMSessionEvent se = (CMSessionEvent) cme;
        switch (se.getID()) {
            case CMSessionEvent.LOGIN:
                System.out.println("--> [" + se.getUserName() + "] requests login.");
                break;
            case CMSessionEvent.SESSION_ADD_USER:
                System.out.println("--> [" + se.getUserName() + "] requests login.");
                break;
            default:
                return;
        }
    }

    private void processUserEvent(CMEvent cme) {
        CMUserEvent ue = (CMUserEvent) cme;
        switch (ue.getStringID()) {
            case "userInfo":
                System.out.println("--> user event ID: " + ue.getStringID());
                String name = ue.getEventField(CMInfo.CM_STR, "name");
                int age = Integer.parseInt(ue.getEventField(CMInfo.CM_INT, "age"));
                double weight = Double.parseDouble(ue.getEventField(CMInfo.CM_DOUBLE, "weight"));
                System.out.println("--> name: " + name);
                System.out.println("--> age: " + age);
                System.out.println("--> weight: " + weight);
                break;
            default:
                System.err.println("--> unknown CMUserEvent ID: " + ue.getStringID());
        }
    }

    private void processFileEvent(CMEvent cme) {
        CMFileEvent fe = (CMFileEvent) cme;
        switch (fe.getID()) {
            case CMFileEvent.REQUEST_PERMIT_PULL_FILE:
                System.out.println("[" + fe.getFileReceiver() + "] requests file(" + fe.getFileName() + ").");
                System.err.print("[" + fe.getFileReceiver() + "] requests file(" + fe.getFileName() + ").\n");
                System.err.print("The pull-file request is not automatically permitted!\n");
                System.err.print("To change to automatically permit the pull-file request, \n");
                System.err.print("set the PERMIT_FILE_TRANSFER field to 1 in the cm-server.conf file\n");
                break;
            case CMFileEvent.REPLY_PERMIT_PULL_FILE:
                if (fe.getReturnCode() == -1) {
                    System.err.print("[" + fe.getFileName() + "] does not exist in the owner!\n");
                } else if (fe.getReturnCode() == 0) {
                    System.err.print("[" + fe.getFileSender() + "] rejects to send file("
                            + fe.getFileName() + ").\n");
                }
                break;
            case CMFileEvent.REQUEST_PERMIT_PUSH_FILE:
                System.out.println("[" + fe.getFileSender() + "] wants to send a file(" + fe.getFilePath() +
                        ").");
                System.err.print("The push-file request is not automatically permitted!\n");
                System.err.print("To change to automatically permit the push-file request, \n");
                System.err.print("set the PERMIT_FILE_TRANSFER field to 1 in the cm-server.conf file\n");
                break;
            case CMFileEvent.REPLY_PERMIT_PUSH_FILE:
                if (fe.getReturnCode() == 0) {
                    System.err.print("[" + fe.getFileReceiver() + "] rejected the push-file request!\n");
                    System.err.print("file path(" + fe.getFilePath() + "), size(" + fe.getFileSize() + ").\n");
                }
                break;
            case CMFileEvent.START_FILE_TRANSFER:
            case CMFileEvent.START_FILE_TRANSFER_CHAN:
                System.out.println("[" + fe.getFileSender() + "] is about to send file(" + fe.getFileName() + ").");
                break;
            case CMFileEvent.END_FILE_TRANSFER:
            case CMFileEvent.END_FILE_TRANSFER_CHAN:
                System.out.println("[" + fe.getFileSender() + "] completes to send file(" + fe.getFileName() + ", "
                        + fe.getFileSize() + " Bytes).");
                String strFile = fe.getFileName();
                boolean m_bDistFileProc = false;
                if (m_bDistFileProc) {
                    processFile(fe.getFileSender(), strFile);
                    m_bDistFileProc = false;
                }
                break;
            case CMFileEvent.REQUEST_DIST_FILE_PROC:
                System.out.println("[" + fe.getFileReceiver() + "] requests the distributed file processing.");
                m_bDistFileProc = true;
                break;
            case CMFileEvent.CANCEL_FILE_SEND:
            case CMFileEvent.CANCEL_FILE_SEND_CHAN:
                System.out.println("[" + fe.getFileSender() + "] cancelled the file transfer.");
                break;
            case CMFileEvent.CANCEL_FILE_RECV_CHAN:
                System.out.println("[" + fe.getFileReceiver() + "] cancelled the file request.");
                break;
        }
        return;
    }

    private void processFile(String strSender, String strFile) {
        CMConfigurationInfo confInfo = m_serverStub.getCMInfo().getConfigurationInfo();
        String strFullSrcFilePath = null;
        String strModifiedFile = null;
        FileInputStream fis = null;
        FileOutputStream fos = null;
        byte[] fileBlock = new byte[CMInfo.FILE_BLOCK_LEN];

        long lStartTime = System.currentTimeMillis();

        // change the modified file name
        strModifiedFile = "m-" + strFile;
        strModifiedFile = confInfo.getTransferedFileHome().toString() + File.separator + strSender +
                File.separator + strModifiedFile;

        // stylize the file
        strFullSrcFilePath = confInfo.getTransferedFileHome().toString() + File.separator + strSender +
                File.separator + strFile;
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

            while (lRemainBytes > 0) {
                if (lRemainBytes >= CMInfo.FILE_BLOCK_LEN) {
                    readBytes = fis.read(fileBlock);
                } else {
                    readBytes = fis.read(fileBlock, 0, (int) lRemainBytes);
                }

                fos.write(fileBlock, 0, readBytes);
                lRemainBytes -= readBytes;
            }
        } catch (IOException e) {
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
        for (long i = 0; i < lFileSize / 50; i++) {
            for (long j = 0; j < lFileSize / 50; j++) {
                //
            }
        }

        long lEndTime = System.currentTimeMillis();
        System.out.println("processing delay: " + (lEndTime - lStartTime) + " ms");

        // send the modified file to the sender
        CMFileTransferManager.pushFile(strModifiedFile, strSender, m_serverStub.getCMInfo());

        return;
    }
}