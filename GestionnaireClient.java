import java.net.* ;
import java.util.concurrent.* ;

public class GestionnaireClient implements Runnable {
    private ClientInfo client ;
    private DatagramSocket socketClient ;
    private ConcurrentHashMap<String, ClientInfo> clients ;

    public GestionnaireClient(ClientInfo client, DatagramSocket socketClient, ConcurrentHashMap<String, ClientInfo> clients) {
        
    }

    @Override
    public void run() {

    }
}