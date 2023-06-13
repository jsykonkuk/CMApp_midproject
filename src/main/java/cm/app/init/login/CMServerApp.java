
import kr.ac.konkuk.ccslab.cm.entity.CMSession;
import kr.ac.konkuk.ccslab.cm.info.CMInfo;
import kr.ac.konkuk.ccslab.cm.info.CMInteractionInfo;
import kr.ac.konkuk.ccslab.cm.sns.CMSNSUserAccessSimulator;
import kr.ac.konkuk.ccslab.cm.stub.CMServerStub;

import java.awt.*;
import java.io.*;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import javax.swing.JOptionPane;

public class CMServerApp {
    private CMServerStub m_serverStub;
    private CMServerEventHandler m_eventHandler;
    private boolean m_bRun;
    private CMSNSUserAccessSimulator m_uaSim;
    private Scanner m_scan = null;

    public CMServerApp()
    {
        m_serverStub = new CMServerStub();
        m_eventHandler = new CMServerEventHandler(m_serverStub);
        m_bRun = true;
        m_uaSim = new CMSNSUserAccessSimulator();
    }

    public CMServerStub getServerStub()
    {
        return m_serverStub;
    }

    public CMServerEventHandler getServerEventHandler()
    {
        return m_eventHandler;
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
    public void printSessionInfo()
    {
        System.out.println("------------------------------------------------------");
        System.out.format("%-20s%-20s%-10s%-10s%n", "session name", "session addr", "port", "#users");
        System.out.println("------------------------------------------------------");

        CMInteractionInfo interInfo = m_serverStub.getCMInfo().getInteractionInfo();
        Iterator<CMSession> iter = interInfo.getSessionList().iterator();
        while(iter.hasNext())
        {
            CMSession session = iter.next();
            System.out.format("%-20s%-20s%-10d%-10d%n", session.getSessionName(), session.getAddress()
                    , session.getPort(), session.getSessionUsers().getMemberNum());
        }
        return;
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
    public void configureUserAccessSimulation()
    {
        int nUserNum = -1;
        int nAvgDayAccCount = -1;
        int nTotalSimDays = -1;
        int nAccPattern = -1;
        double dNormalMean = -1.0;
        double dNormalSD = -1.0;
        String strInput = null;

        // retrieve current values
        nUserNum = m_uaSim.getUserNum();
        nAvgDayAccCount = m_uaSim.getAvgDayAccCount();
        nTotalSimDays = m_uaSim.getTotalSimDays();
        nAccPattern = m_uaSim.getAccPattern();
        dNormalMean = m_uaSim.getNormalMean();
        dNormalSD = m_uaSim.getNormalSD();

        System.out.println("====== Configure variables of user access simulation");
        System.out.println("The value in () is the current value.");
        System.out.println("Enter in each variable to keep the current value.");
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        try {
            System.out.print("Number of users("+nUserNum+"): ");
            strInput = br.readLine();
            if(!strInput.isEmpty())
            {
                nUserNum = Integer.parseInt(strInput);
                m_uaSim.setUserNum(nUserNum);
            }
            System.out.print("Average daily access count("+nAvgDayAccCount+"): ");
            strInput = br.readLine();
            if(!strInput.isEmpty())
            {
                nAvgDayAccCount = Integer.parseInt(strInput);
                m_uaSim.setAvgDayAccCount(nAvgDayAccCount);
            }
            System.out.print("Total number of simulation days("+nTotalSimDays+"): ");
            strInput = br.readLine();
            if(!strInput.isEmpty())
            {
                nTotalSimDays = Integer.parseInt(strInput);
                m_uaSim.setTotalSimDays(nTotalSimDays);
            }
            System.out.print("Access pattern("+nAccPattern+") (0: random, 1: skewed): ");
            strInput = br.readLine();
            if(!strInput.isEmpty())
            {
                nAccPattern = Integer.parseInt(strInput);
                if(nAccPattern < 0 || nAccPattern > 1)
                {
                    System.err.println("Invalid access pattern!");
                    return;
                }
                m_uaSim.setAccPattern(nAccPattern);
            }

            if(nAccPattern == 1) // skewed access pattern
            {
                System.out.print("Mean value("+dNormalMean+"): ");
                strInput = br.readLine();
                if(!strInput.isEmpty())
                {
                    dNormalMean = Double.parseDouble(strInput);
                    m_uaSim.setNormalMean(dNormalMean);
                }
                System.out.println("Standard deviation("+dNormalSD+"): ");
                strInput = br.readLine();
                if(!strInput.isEmpty())
                {
                    dNormalSD = Double.parseDouble(strInput);
                    m_uaSim.setNormalSD(dNormalSD);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return;
    }

    // simulate user access history according to previous configuration
    public void startUserAccessSimulation()
    {
        System.out.println("====== Start user access simulation");
        m_uaSim.start();
        return;
    }
    public static void main(String[] args) {
        CMServerApp server = new CMServerApp();
        CMServerStub cmStub = server.getServerStub();
        cmStub.setAppEventHandler(server.getServerEventHandler());
        server.startCM();

        System.out.println("Server application is terminated.");
    }

}