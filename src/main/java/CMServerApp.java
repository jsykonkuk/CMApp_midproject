package cm.app.init.login;

import kr.ac.konkuk.ccslab.cm.stub.CMServerStub;

import java.util.Scanner;

public class CMServerApp {
    private CMServerStub m_serverStub;
    private CMServerEventHandler m_eventHandler;

    public CMServerApp() {
        m_serverStub = new CMServerStub();
        m_eventHandler = new CMServerEventHandler(m_serverStub);
    }

    public CMServerStub getServerStub() {
        return m_serverStub;
    }

    public CMServerEventHandler getServerEventHandler() {
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
}
