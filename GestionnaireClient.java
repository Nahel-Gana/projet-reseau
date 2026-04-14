import java.net.*;
import java.util.concurrent.*;

/**
 * Gère les échanges avec un client spécifique dans un thread dédié.
 */
public class GestionnaireClient implements Runnable {
    /** Informations du client associé à ce thread */
    private ClientInfo client;
    /** Socket dédiée sur un port libre [cite: 160] */
    private DatagramSocket socketClient;
    /** Map partagée [cite: 165] */
    private ConcurrentHashMap<String, ClientInfo> clients;

    /**
     * Construit un GestionnaireClient chargé de gérer un client donné.
     *
     * @param client le client associé à ce gestionnaire
     * @param socketClient la socket UDP utilisée pour recevoir et envoyer les messages
     * @param clients la structure partagée contenant tous les clients connectés
     */
    public GestionnaireClient(ClientInfo client, DatagramSocket socketClient, ConcurrentHashMap<String, ClientInfo> clients) {
        this.client = client;
        this.socketClient = socketClient;
        this.clients = clients;
    }

    /**
     * Exécute le thread de gestion du client.
     * <p>
     * - Diffuse un message de bienvenue à tous les clients<br>
     * - Écoute en boucle les messages UDP du client<br>
     * - Diffuse chaque message reçu aux autres clients<br>
     * - Gère la déconnexion du client via le message "EXIT"
     */
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
                System.out.println("[THREAD " + client.getPseudo() + "] reçu de " + packet.getAddress() + ":" + packet.getPort());
                System.out.println("[THREAD " + client.getPseudo() + "] message = " + msg);
                System.out.println("\n===== DEBUG THREAD RECEPTION =====");
                System.out.println("CLIENT = " + client.getPseudo());
                System.out.println("FROM IP = " + packet.getAddress());
                System.out.println("FROM PORT = " + packet.getPort());
                System.out.println("MESSAGE = " + msg);
                System.out.println("SOCKET THREAD PORT = " + socketClient.getLocalPort());
                System.out.println("=================================");
                System.out.println("\n===== TRACE COMPLETE PACKET =====");
                System.out.println("THREAD = " + client.getPseudo());
                System.out.println("SOCKET LOCAL PORT = " + socketClient.getLocalPort());
                System.out.println("SOURCE PACKET IP = " + packet.getAddress());
                System.out.println("SOURCE PACKET PORT = " + packet.getPort());
                System.out.println("EXPECTED IP = " + client.getAdresseIP());
                System.out.println("EXPECTED PORT = " + client.getPort());
                System.out.println("MESSAGE = " + msg);
                System.out.println("===============================");

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
                    if (msg.startsWith("INFO:")) return;

                    if (msg.startsWith(client.getPseudo() + " :")) return;

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
     * Méthode utilitaire pour envoyer un message UDP à tous les clients connectés.
     * 
     * @param texte le message à diffuser à tous les clients
     */
    private void diffuser(String texte) {
        byte[] data = texte.getBytes();
        for (ClientInfo destinataire : clients.values()) {
            if (!destinataire.getPseudo().equals(client.getPseudo())) {
                try {
                    DatagramPacket p = new DatagramPacket(
                        data, 
                        data.length, 
                        destinataire.getAdresseIP(), 
                        destinataire.getPort()
                    );
                    System.out.println("[DIFFUSION] Envoi à " + destinataire.getPseudo() + " -> " + destinataire.getAdresseIP() + ":" + destinataire.getPort() + " | msg = " + texte);
                    System.out.println("\n===== DEBUG DIFFUSION =====");
                    System.out.println("FROM = " + client.getPseudo());
                    System.out.println("TO = " + destinataire.getPseudo());
                    System.out.println("IP cible = " + destinataire.getAdresseIP());
                    System.out.println("PORT cible = " + destinataire.getPort());
                    System.out.println("MSG = " + texte);
                    System.out.println("===========================");
                    socketClient.send(p);
                } catch (Exception e) {
                    // Erreur sur un client, on continue pour les autres
                }
            }
        }
    }
}
