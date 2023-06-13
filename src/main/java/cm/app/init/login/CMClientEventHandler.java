
import javax.swing.JOptionPane;

import java.io.*;

import kr.ac.konkuk.ccslab.cm.event.CMEvent;
import kr.ac.konkuk.ccslab.cm.event.CMFileEvent;
import kr.ac.konkuk.ccslab.cm.event.handler.CMAppEventHandler;
import kr.ac.konkuk.ccslab.cm.info.CMConfigurationInfo;
import kr.ac.konkuk.ccslab.cm.manager.CMFileTransferManager;

import java.io.*;
import java.nio.file.*;

public class FileSynchronizationClient {
    private static final String SERVER_DIRECTORY = "server_directory/";
    private static final String CLIENT_DIRECTORY = "client_directory/";

    public static void main(String[] args) {
        // Start monitoring the client directory for file updates
        startFileMonitoring();
    }

    private static void startFileMonitoring() {
        try {
            WatchService watchService = FileSystems.getDefault().newWatchService();
            Path clientDirectoryPath = Paths.get(CLIENT_DIRECTORY);
            clientDirectoryPath.register(watchService, StandardWatchEventKinds.ENTRY_CREATE,
                    StandardWatchEventKinds.ENTRY_DELETE, StandardWatchEventKinds.ENTRY_MODIFY);

            System.out.println("Monitoring client directory for file updates...");

            while (true) {
                WatchKey key = watchService.take();

                for (WatchEvent<?> event : key.pollEvents()) {
                    WatchEvent.Kind<?> kind = event.kind();

                    if (kind == StandardWatchEventKinds.OVERFLOW) {
                        continue;
                    }

                    WatchEvent<Path> pathEvent = (WatchEvent<Path>) event;
                    Path filePath = clientDirectoryPath.resolve(pathEvent.context());

                    if (kind == StandardWatchEventKinds.ENTRY_CREATE || kind == StandardWatchEventKinds.ENTRY_MODIFY) {
                        handleFileUpdate(filePath);
                    } else if (kind == StandardWatchEventKinds.ENTRY_DELETE) {
                        handleFileDeletion(filePath);
                    }
                }

                if (!key.reset()) {
                    break;
                }
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    private static void handleFileUpdate(Path filePath) {
        System.out.println("Detected file update: " + filePath);

        // Verify if the update conflicts with files on the server
        if (!isUpdateConflicting(filePath)) {
            // Send the updated file to the server to replace the existing file
            sendFileToServer(filePath);
        } else {
            System.out.println("Update conflicts with files on the server. Skipping synchronization.");
        }
    }

    private static boolean isUpdateConflicting(Path filePath) {
        // Perform logical clock comparison with the server's version of the file
        // Return true if there is a conflict, false otherwise
        // Implement your logical clock comparison logic here
        return false;
    }

    private static void sendFileToServer(Path filePath) {
        try {
            Path serverFilePath = Paths.get(SERVER_DIRECTORY + filePath.getFileName());
            Files.copy(filePath, serverFilePath, StandardCopyOption.REPLACE_EXISTING);
            System.out.println("File synchronized with the server: " + filePath);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void handleFileDeletion(Path filePath) {
        System.out.println("Detected file deletion: " + filePath);

        // Delete the corresponding file from the server
        deleteFileFromServer(filePath);
    }

    private static void deleteFileFromServer(Path filePath) {
        try {
            Path serverFilePath = Paths.get(SERVER_DIRECTORY + filePath.getFileName());
            Files.deleteIfExists(serverFilePath);
            System.out.println("File deleted from the server: " + filePath);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
public class CMClientEventHandler implements CMAppEventHandler {

    private void processFileEvent(CMEvent cme)
    {
        CMFileEvent fe = (CMFileEvent) cme;
        int nOption = -1;
        switch(fe.getID())
        {
            case CMFileEvent.REQUEST_PERMIT_PULL_FILE:
                String strReq = "["+fe.getFileReceiver()+"] requests file("+fe.getFileName()+
                        ").\n";
                System.out.print(strReq);
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
                    System.err.print("["+fe.getFileName()+"] does not exist in the owner!\n");
                }
                else if(fe.getReturnCode() == 0)
                {
                    System.err.print("["+fe.getFileSender()+"] rejects to send file("
                            +fe.getFileName()+").\n");
                }
                break;
            case CMFileEvent.REQUEST_PERMIT_PUSH_FILE:
                StringBuffer strReqBuf = new StringBuffer();
                strReqBuf.append("["+fe.getFileSender()+"] wants to send a file.\n");
                strReqBuf.append("file path: "+fe.getFilePath()+"\n");
                strReqBuf.append("file size: "+fe.getFileSize()+"\n");
                System.out.print(strReqBuf.toString());
                nOption = JOptionPane.showConfirmDialog(null, strReqBuf.toString(),
                        "Push File", JOptionPane.YES_NO_OPTION);
                if(nOption == JOptionPane.YES_OPTION)
                {
                    m_clientStub.replyEvent(fe, 1);
                }
                else
                {
                    m_clientStub.replyEvent(fe, 1);
                }
                break;
            case CMFileEvent.REPLY_PERMIT_PUSH_FILE:
                if(fe.getReturnCode() == 0)
                {
                    System.err.print("["+fe.getFileReceiver()+"] rejected the push-file request!\n");
                    System.err.print("file path("+fe.getFilePath()+"), size("+fe.getFileSize()+").\n");
                }
                break;
            case CMFileEvent.START_FILE_TRANSFER:
            case CMFileEvent.START_FILE_TRANSFER_CHAN:
                System.out.println("["+fe.getFileSender()+"] is about to send file("+fe.getFileName()+").");
                break;
            case CMFileEvent.END_FILE_TRANSFER:
            case CMFileEvent.END_FILE_TRANSFER_CHAN:
                System.out.println("["+fe.getFileSender()+"] completes to send file("+fe.getFileName()+", "
                        +fe.getFileSize()+" Bytes).");
                if(m_bDistFileProc)
                    processFile(fe.getFileName());
                break;
            case CMFileEvent.CANCEL_FILE_SEND:
            case CMFileEvent.CANCEL_FILE_SEND_CHAN:
                System.out.println("["+fe.getFileSender()+"] cancelled the file transfer.");
                break;
            case CMFileEvent.CANCEL_FILE_RECV_CHAN:
                System.out.println("["+fe.getFileReceiver()+"] cancelled the file request.");
                break;
        }
        return;
    }

    private void processFile(String strFile)
    {
        CMConfigurationInfo confInfo = m_clientStub.getCMInfo().getConfigurationInfo();
        String strMergeName = null;

        // add file name to list and increase index
        int m_nRecvPieceNum = 0;
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
            System.out.println("total delay for ("+m_nRecvPieceNum+") files: "
                    +(lRecvTime-m_lStartTime)+" ms");

            // reset m_bDistSendRecv, m_nRecvFilePieceNum
            m_bDistFileProc = false;
            m_nRecvPieceNum = 0;
        }

        return;
    }

}