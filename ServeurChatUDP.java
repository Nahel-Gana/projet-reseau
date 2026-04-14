import java.util.concurrent.* ;
import java.net.* ;

/**
 * Classe représentant un serveur de chat utilisant le protocole UDP.
 * <p>
 * Ce serveur écoute sur un port fixe (9000) et permet aux clients de se connecter
 * en envoyant un message de type "JOIN:pseudo".
 * </p>
 * <p>
 * Lorsqu'un client se connecte :
 * <ul>
 *     <li>Un port dédié lui est attribué</li>
 *     <li>Une réponse contenant ce port lui est envoyée</li>
 *     <li>Un thread est lancé pour gérer ce client</li>
 * </ul>
 * </p>
 */
public class ServeurChatUDP {

    /**
     * Table concurrente contenant les clients connectés.
     * La clé est le pseudo du client, la valeur contient ses informations.
     */
    private static ConcurrentHashMap<String, ClientInfo> clients = new ConcurrentHashMap<String, ClientInfo>() ;

    /**
     * Constructeur du serveur.
     * Initialise la structure de stockage des clients.
     */
    public ServeurChatUDP() {
        clients = new ConcurrentHashMap<String, ClientInfo>() ;
    }

    /**
     * Retourne la liste des clients connectés.
     *
     * @return une table de hachage concurrente des clients
     */
    public ConcurrentHashMap<String, ClientInfo> getClients() {
        return clients ;
    }

    /**
     * Méthode principale lançant le serveur UDP.
     * <p>
     * Fonctionnement :
     * <ul>
     *     <li>Écoute sur le port 9000</li>
     *     <li>Réception des messages clients</li>
     *     <li>Traitement des demandes de connexion (JOIN)</li>
     *     <li>Création d'un thread dédié pour chaque client</li>
     * </ul>
     * </p>
     *
     * @param args arguments de la ligne de commande (non utilisés)
     */
    public static void main(String[] args) {
        DatagramSocket socket = null ;
        try {
            // Création d'un socket sur le port 9000
            socket = new DatagramSocket(9000) ;
            // Affichage de la fonctionnabilité du socket
            System.out.println("Port 9000 fonctionnel") ;

            // Boucle infinie
            while (true) {
                // Buffer
                byte[] tmp = new byte[1024] ;
                // Création d'un paquet UDP
                DatagramPacket packet = new DatagramPacket(tmp, tmp.length) ;
                // Attente de réception de message
                socket.receive(packet) ;

                // Conversion des données en String
                String message = new String(packet.getData(), 0, packet.getLength()) ;
                // Affichage du message reçu
                System.out.println("Message reçu : " + message) ;
                System.out.println("[SERVEUR] Reçu de " + packet.getAddress() + ":" + packet.getPort() + " -> " + message);
                System.out.println("\n===== DEBUG RECEPTION SERVEUR =====");
                System.out.println("FROM CLIENT RAW:");
                System.out.println("IP source = " + packet.getAddress());
                System.out.println("port source = " + packet.getPort());
                System.out.println("message brut = " + message);
                System.out.println("===================================");

                // Si le message est un message de connexion
                if (message.startsWith("JOIN:")) {
                    // Récupération du pseudo
                    String pseudo = message.split(":")[1] ;
                    // Création d'un socket dédié libre
                    DatagramSocket socketDediee = new DatagramSocket(0) ;
                    // Récupération du port choisi
                    int portClient = socketDediee.getLocalPort() ;

                    // Création réponse
                    String reponse = "PORT:" + portClient ;
                    // Conversion de la réponse en bytes
                    byte[] data = reponse.getBytes() ;
                    // Création du paquet de la réponse
                    DatagramPacket packetReponse = new DatagramPacket(data, data.length, packet.getAddress(), packet.getPort()) ;
                    System.out.println("\n===== DEBUG ENVOI PORT =====");
                    System.out.println("Envoi à IP = " + packet.getAddress());
                    System.out.println("Réponse = " + reponse);
                    System.out.println("===========================");
                    // Envoi du paquet réponse
                    socket.send(packetReponse) ;

                    // Création du client
                    ClientInfo client = new ClientInfo(pseudo, packet.getAddress(), portClient) ;
                    // Ajout du client dans la map
                    clients.put(pseudo, client) ;
                    // Affichage de la connexion du client
                    System.out.println(pseudo + " connecté") ;
                    System.out.println("[SERVEUR] Ajout client " + pseudo + " IP=" + client.getAdresseIP() + " PORT=" + client.getPort());
                    System.out.println("\n===== DEBUG CLIENT REGISTER =====");
                    System.out.println("Pseudo = " + pseudo);
                    System.out.println("IP stockée = " + client.getAdresseIP());
                    System.out.println("PORT stocké = " + client.getPort());
                    System.out.println("PORT socket dédiée = " + portClient);
                    System.out.println("=================================");
                    System.out.println("\n===== DEBUG MAP CLIENTS =====");
                    clients.forEach((k, v) -> {
                        System.out.println(k + " -> " + v.getAdresseIP() + ":" + v.getPort());
                    });
                    System.out.println("============================");

                    // Lancement d'un thread pour gérer le client
                    GestionnaireClient gestionnaire = new GestionnaireClient(client, socketDediee, clients) ;
                    // Exécution d'un thread en parallèle pour éviter les blocages
                    new Thread(gestionnaire).start() ;
                }
            }
        }
        catch (Exception e) {
            // Gestion des exception
            e.printStackTrace() ;
        }
        finally {
            // Fermeture du socket
            if (socket != null) {
                socket.close() ;
            }
        }
    }
}
