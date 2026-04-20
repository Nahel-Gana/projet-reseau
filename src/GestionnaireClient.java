import java.net.*;
import java.util.concurrent.*;

/**
 * Gère les échanges avec un client UDP dans un thread dédié.
 * <p>
 * Chaque instance de cette classe écoute les messages d’un client, les traite (commandes, messages privés, messages publics) et les diffuse aux autres clients connectés.
 */
public class GestionnaireClient implements Runnable {
    /** Informations du client associé à ce thread */
    private ClientInfo client;
    /** Socket UDP utilisée pour la communication */
    private DatagramSocket socketClient;
    /** Structure partagée contenant tous les clients connectés */
    private ConcurrentHashMap<String, ClientInfo> clients;

    /**
     * Construit un gestionnaire pour un client donné.
     *
     * @param client le client à gérer
     * @param socketClient la socket UDP utilisée pour envoyer et recevoir les messages
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
     * Fonctionnalités principales :
     * <ul>
     *   <li>Annonce l’arrivée du client</li>
     *   <li>Surveille l’inactivité (timeout)</li>
     *   <li>Reçoit les messages UDP du client</li>
     *   <li>Traite les commandes (/liste, /mp, EXIT)</li>
     *   <li>Diffuse les messages aux autres clients</li>
     * </ul>
     */
    @Override
    public void run() {
        try {
            // Diffuse l'information à tous qu'une certaine personne a rejoint le chat
            diffuser("INFO: " + client.getPseudo() + " a rejoint le chat");

            // Création d'un thread pour vérifier si le temps d'inactivité est dépassé ou non
            new Thread(() -> {
                try {
                    // Tant que la socket n'est pas fermée
                    while (!socketClient.isClosed()) {
                        // Temps actuel
                        long now = System.currentTimeMillis();
                        // Temps d'inactivité
                        long diff = now - client.getDerniereActivite();

                        // Si le temps d'inactivité est supérieur à 60 secondes
                        if (diff > 60000) {
                            // Envoie au client que le temps est écoulé
                            envoyerAuClient("TIMEOUT");

                            // Enlève le client de la liste des clients connectés
                            clients.remove(client.getPseudo());
                            // Diffuse à tous le fait que le client a été déconnecté à cause d'une inactivité
                            diffuser("INFO: " + client.getPseudo() + " a été déconnecté (inactif)");

                            // Ferme la socket
                            socketClient.close();
                            // Sort de la boucle
                            break;
                        }

                        // Vérification toutes les secondes (pour éviter de surcharger le CPU)
                        Thread.sleep(1000);
                    }
                } catch (Exception e) {
                    // Si une exception a été levée, ne rien faire
                }
            }).start(); // Démarre le thread

            // Buffer pour recevoir le message
            byte[] buffer = new byte[1024];
            // Boucle infinie
            while (true) {
                // Création d'un paquet pour recevoir le message
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                // Récéption du message
                socketClient.receive(packet);
                // Conversion du message en String
                String msg = new String(packet.getData(), 0, packet.getLength()).trim();
                // Indique que la dernière activité du client a eu lieu à ce moment
                client.setDerniereActivite(System.currentTimeMillis());

                // Si le message est "EXIT" (quelle que soit la capitalité des lettres)
                if (msg.equalsIgnoreCase("EXIT")) {
                    // Enlève le client des clients connectés
                    clients.remove(client.getPseudo());
                    // Diffuse l'information à tous comme quoi le client a quitté le chat
                    diffuser("INFO: " + client.getPseudo() + " a quitté le chat");
                    // Sort de la boucle
                    break;
                } 
                // Si le message est "/liste" (quelle que soit la capitalité des lettres)
                else if (msg.equalsIgnoreCase("/liste")) {
                    // Liste des pseudos des clients connectés
                    String listePseudos = "Utilisateurs connectés : " + String.join(", ", clients.keySet());
                    // Conversion en bytes
                    byte[] reponseData = listePseudos.getBytes();
                    
                    // Création du paquet UDP contenant les informations à envoyer
                    DatagramPacket reponsePacket = new DatagramPacket(
                        reponseData, // liste des pseudos en bytes
                        reponseData.length, // longueur de la liste des pseudos
                        client.getAdresseIP(), // adresse IP du client demandeur
                        client.getPort() // port du client demandeur
                    );
                    // Envoi du paquet au client demandeur
                    socketClient.send(reponsePacket);
                } 
                // Si le message commence avec "/mp "
                else if (msg.startsWith("/mp ")) {
                    // Traite le message qui suit comme message privé
                    traiterMessagePrive(msg);
                }
                else {
                    // Si le message commence avec "INFO:", arrêter la fonction
                    if (msg.startsWith("INFO:")) return;

                    // Si le message commence avec le pseudo du client, arrête la fonction
                    if (msg.startsWith(client.getPseudo() + " :")) return;

                    // Diffuse à tous le message (précédé du pseudo du client)
                    diffuser(client.getPseudo() + " : " + msg);
                }
            }
        } catch (Exception e) {
            // S'il y a une exception, en informer l'utilisateur
            System.err.println("Erreur thread " + client.getPseudo() + " : " + e.getMessage());
        } finally {
            // Finalement, si la socket n'est ni vide ni fermée, la fermer
            if (socketClient != null && !socketClient.isClosed()) {
                socketClient.close();
            }
        }
    }

    /**
     * Diffuse un message à tous les clients connectés sauf l’émetteur.
     *
     * @param texte le message à envoyer à tous les clients
     */
    private void diffuser(String texte) {
        // Conversion du message à envoyer en bytes
        byte[] data = texte.getBytes();
        // Itération sur tous les clients connectés
        for (ClientInfo destinataire : clients.values()) {
            // Si le destinataire n'est pas l'envoyeur
            if (!destinataire.getPseudo().equals(client.getPseudo())) {
                try {
                    // Création du paquet UDP contenant les informations à envoyer
                    DatagramPacket p = new DatagramPacket(
                        data, // le message en bytes
                        data.length, // la longueur du message
                        destinataire.getAdresseIP(), // l'adresse IP du destinataire
                        destinataire.getPort() // le port du destinataire
                    );
                    socketClient.send(p);
                } catch (Exception e) {
                    // S'il y a une exception pour un client, on ne fait rien pour lui et on continue vers les autres
                }
            }
        }
    }

    /**
     * Traite une commande de message privé (/mp).
     * <p>
     * Format attendu :
     * <pre>/mp pseudo message</pre>
     *
     * @param msg le message brut reçu du client
     */
    private void traiterMessagePrive(String msg) {
        try {
            // Découpage du message en trois parties (une pour le /mp, une pour la destination et une pour le message)
            String[] parties = msg.split(" ", 3);

            // Si le nombre de parties est inférieur à 3
            if (parties.length < 3) {
                // Envoie au client de refaire le message dans le bon format
                envoyerAuClient("Format invalide. Utilisation : /mp <pseudo> <message>");
                // Arrête la fonction
                return;
            }

            // Pseudo du destinataire
            String destinatairePseudo = parties[1];
            // Message à envoyer
            String message = parties[2];
            // Destinataire
            ClientInfo destinataire = clients.get(destinatairePseudo);

            // Si le destinataire est introuvable, en informer l'envoyeur et arrêter la fonction
            if (destinataire == null) {
                envoyerAuClient("Utilisateur inconnu");
                return;
            }

            // Message reçu par le destinataire
            String messageFinal = "[MP de " + client.getPseudo() + "] : " + message;
            // Conversion du message en bytes
            byte[] data = messageFinal.getBytes();

            // Création du paquet UDP contenant les informations à envoyer
            DatagramPacket packet = new DatagramPacket(
                data, // le message en bytes
                data.length, // la longueur du message
                destinataire.getAdresseIP(), // l'adresse IP du destinataire
                destinataire.getPort() // le port du destinataire
            );

            // Envoi du message au client
            socketClient.send(packet);

        } catch (Exception e) {
            // S'il y a une exception, en informer l'envoyeur
            envoyerAuClient("Erreur lors de l'envoi du message privé");
        }
    }

    /**
     * Envoie un message directement au client courant.
     *
     * @param texte le message à envoyer
     */
    private void envoyerAuClient(String texte) {
        try {
            // Conversion du message en bytes
            byte[] data = texte.getBytes();

            // Création du paquet UDP contenant les informations à envoyer
            DatagramPacket packet = new DatagramPacket(
                data, // le message en bytes
                data.length, // la longueur du message
                client.getAdresseIP(), // l'adresse IP du destinataire
                client.getPort() // le port du destinataire
            );

            // Envoi du message au client
            socketClient.send(packet);
        } catch (Exception e) {
            // S'il y a une exception, ne rien faire
        }
    }
}
