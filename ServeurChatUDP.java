import java.util.concurrent.* ;

public class ServeurChatUDP {
    private ConcurrentHashMap<String, ClientInfo> clients ;

    public ConcurrentHashMap<String, ClientInfo> getClients() {
        return new ConcurrentHashMap<String, ClientInfo>() ;
    }

    public static void main(String[] args) {
        
    }
}