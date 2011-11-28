
import java.awt.Point;
import java.util.List;
import java.util.ArrayList;

import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

public class PaintServer extends UnicastRemoteObject implements PaintServerInterface {

    /**
     * 
     */
    private static final long serialVersionUID = 3237989332058510161L;
    private List<List<Point>> draw = new ArrayList<List<Point>>();
    private ArrayList<PaintClientInterface> clients = new ArrayList<PaintClientInterface>();
    private static int MAX_CLIENTS = 3;
    private int CLIENT_RESPONSES = 0;
    private boolean RESET_DECISION = true;

    protected PaintServer() throws RemoteException {
        super();
        // TODO Auto-generated constructor stub
    }

    public synchronized void setDraw(List<List<Point>> draw) throws RemoteException {
        this.draw = draw;
        this.broadcast();
    }

    public void register(PaintClientInterface client) throws RemoteException {
        clients.add(client);

        if (clients.size() >= MAX_CLIENTS) {
            wakeUpClients();
        }
    }

    public void wakeUpClients() throws RemoteException {
        for (int i = 0; i < clients.size(); i++) {
            clients.get(i).wakeUp();
        }
    }

    public synchronized void broadcast() throws RemoteException {

        for (int i = 0; i < clients.size(); i++) {
            clients.get(i).repaintFrame();
        }
    }

    public synchronized List<List<Point>> getDraw() throws RemoteException {
        return draw;
    }

    public synchronized void resetDraw() throws RemoteException {

        
        askClientsForReset();

        if (RESET_DECISION) {
            setDraw(new ArrayList<List<Point>>());
        }
        
        broadcast();
        RESET_DECISION = true;
        CLIENT_RESPONSES = 0;
    }

    public synchronized void askClientsForReset() throws RemoteException {
        for (int i = 0; i < clients.size(); i++) {
            clients.get(i).askReset();
        }
    }

    public void clientAcceptResetDraw() throws RemoteException {
        voteDecision(true);
    }

    public void clientDenyResetDraw() throws RemoteException {
        voteDecision(false);
    }

    public void voteDecision(boolean vote) {
        RESET_DECISION = (RESET_DECISION && vote);
        CLIENT_RESPONSES++;
    }

    public static void setNumberOfClients(int numberOfClients) {
        MAX_CLIENTS = numberOfClients;
    }

    public static void main(String[] args) {

        if (args != null && args.length > 0 && Integer.parseInt(args[0]) > 0) {
            setNumberOfClients(Integer.parseInt(args[0]));
        }

        try {
            PaintServerInterface server = new PaintServer();
            Naming.rebind("paint", server);
        } catch (RemoteException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (MalformedURLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }
}
