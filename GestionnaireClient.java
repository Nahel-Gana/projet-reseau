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
            //Diffuser le message de bienvenue à tous les clients
            diffuser("INFO: " + client.getPseudo() + " a rejoint le chat");

            byte[] buffer = new byte[1024];
            while (true) {
                //  Recevoir en boucle les messages du client via la socket dédiée 
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                socketClient.receive(packet);
                String msg = new String(packet.getData(), 0, packet.getLength()).trim();

                if (msg.equalsIgnoreCase("EXIT")) {
                    clients.remove(client.getPseudo()); // Supprimer le client de la map
                    diffuser("INFO: " + client.getPseudo() + " a quitté le chat"); // Annonce du départ de l'utilisateur
                    break; // Sortir de la boucle pour terminer le thread
                }

                // Diffuser chaque message reçu à tous les autres clients
                diffuser(client.getPseudo() + " : " + msg);
            }
        } catch (Exception e) {
            System.err.println("Erreur thread " + client.getPseudo() + " : " + e.getMessage());
        } finally {
            // Fermer la socket dédiée à la fin de l'exécution
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
        // Parcours de la ConcurrentHashMap pour envoyer à chaque client
        for (ClientInfo destinataire : clients.values()) {
            try {
                // Utilisation de InetAddress et du port stockés dans ClientInfo
                DatagramPacket p = new DatagramPacket(
                    data, 
                    data.length, 
                    destinataire.getAdresseIP(), 
                    destinataire.getPort()
                );
                socketClient.send(p);
            } catch (Exception e) {
                // On continue la diffusion même si l'envoi vers un client échoue
            }
        }
    }
}
