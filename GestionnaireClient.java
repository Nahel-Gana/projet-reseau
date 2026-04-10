import java.net.*;
import java.util.concurrent.*;

/**
 * Gère les échanges avec un client spécifique dans un thread dédié.
 */
public class GestionnaireClient implements Runnable {
    private ClientInfo client;
    private DatagramSocket socketClient; // Socket dédiée sur un port libre
    private ConcurrentHashMap<String, ClientInfo> clients; // Map partagée

    public GestionnaireClient(ClientInfo client, DatagramSocket socketClient, ConcurrentHashMap<String, ClientInfo> clients) {
        this.client = client;
        this.socketClient = socketClient;
        this.clients = clients;
    }

    @Override
    public void run() {
        try {
            // 1. Diffuser le message de bienvenue à tous
            diffuser("INFO: " + client.getPseudo() + " a rejoint le chat");

            byte[] buffer = new byte[1024];
            while (true) {
                // 2. Recevoir le message du client
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                socketClient.receive(packet);
                String msg = new String(packet.getData(), 0, packet.getLength()).trim();

                // 3. Traitement des commandes et messages
                if (msg.equalsIgnoreCase("EXIT")) {
                    clients.remove(client.getPseudo());
                    diffuser("INFO: " + client.getPseudo() + " a quitté le chat");
                    break; // Sortie de la boucle
                } 
                else if (msg.equalsIgnoreCase("/liste")) {
                    // Construction de la liste des pseudos
                    String listePseudos = "Utilisateurs connectés : " + String.join(", ", clients.keySet());
                    byte[] reponseData = listePseudos.getBytes();
                    
                    // Envoi direct au client demandeur
                    DatagramPacket reponsePacket = new DatagramPacket(
                        reponseData,
                        reponseData.length,
                        client.getAdresseIP(),
                        client.getPort()
                    );
                    socketClient.send(reponsePacket);
                } 
                else {
                    // Message normal : diffusion à tout le monde
                    diffuser(client.getPseudo() + " : " + msg);
                }
            }
        } catch (Exception e) {
            System.err.println("Erreur thread " + client.getPseudo() + " : " + e.getMessage());
        } finally {
            // 4. Nettoyage de la socket
            if (socketClient != null && !socketClient.isClosed()) {
                socketClient.close();
            }
        }
    }

    /**
     * Envoie un message à tous les clients présents dans la Map.
     */
    private void diffuser(String texte) {
        byte[] data = texte.getBytes();
        for (ClientInfo destinataire : clients.values()) {
            try {
                DatagramPacket p = new DatagramPacket(
                    data, 
                    data.length, 
                    destinataire.getAdresseIP(), 
                    destinataire.getPort()
                );
                socketClient.send(p);
            } catch (Exception e) {
                // Erreur sur un client, on continue pour les autres
            }
        }
    }
}
