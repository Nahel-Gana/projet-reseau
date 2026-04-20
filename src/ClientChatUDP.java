import java.net.*;
import java.util.*;

/**
 * Client UDP de chat en ligne.
 * <p>
 * Ce client permet de se connecter à un serveur UDP, d’envoyer un pseudo, de recevoir un port dédié pour la communication, puis d’échanger des messages en temps réel avec d’autres clients.
 */
public class ClientChatUDP {

    /**
     * Pseudo de l'utilisateur.
     * <p>
     * Actuellement utilisé uniquement lors de la connexion au serveur.
     */
    private String pseudo;

    /**
     * Socket UDP du client.
     * <p>
     * Utilisée pour envoyer et recevoir les messages du chat.
     */
    private DatagramSocket client;

    /**
     * Indique si le client est en cours d’exécution.
     * <p>
     * Permet de gérer l’arrêt propre des threads.
     */
    private static volatile boolean running = true ;

    /**
     * Constructeur par défaut du client.
     */
    public ClientChatUDP() {

    }

    /**
     * Point d'entrée du client UDP.
     * <p>
     * Le déroulement est le suivant :
     * <ul>
     *     <li>Connexion au serveur UDP</li>
     *     <li>Saisie et envoi du pseudo</li>
     *     <li>Réception du port dédié au chat</li>
     *     <li>Lancement d’un thread d’écoute des messages entrants</li>
     *     <li>Boucle principale d’envoi des messages utilisateur</li>
     * </ul>
     *
     * <p>
     * Commandes possibles :
     * <ul>
     *     <li>{@code exit} : déconnexion du serveur</li>
     *     <li>autres messages : envoyés au chat</li>
     * </ul>
     *
     * @param args arguments de la ligne de commande (non utilisés)
     */
    public static void main(String[] args) {
        // Adresse IP du serveur (localhost)
        String serveurIP = "127.0.0.1";
        // Port d'écoute du serveur principal
        int serveurPort = 9000;
        // Indique que le client est en cours d'exécution
        running = true ;

        try {
            // Création d'une socket côté client
            DatagramSocket socket = new DatagramSocket();
            // Lecteur de ce que l'utilisateur écrit
            Scanner scanner = new Scanner(System.in);
            // Conversion de l'adresse IP du serveur en InetAddress
            InetAddress adresseServeur = InetAddress.getByName(serveurIP);

            // Demande le pseudo de l'utilisateur
            System.out.println("Pseudo : ");
            // Pseudo de l'utilisateur
            String pseudo = scanner.nextLine();

            // Message de connexion de l'utilisateur
            String join = "JOIN:" + pseudo;
            // Envoie au serveur le fait que l'utilisateur s'est connecté
            socket.send(new DatagramPacket(join.getBytes(), join.length(), adresseServeur, serveurPort));

            // Buffer pour recevoir le message
            byte[] buffer = new byte[1024];
            // Création d'un paquet pour recevoir le message
            DatagramPacket reponse = new DatagramPacket(buffer, buffer.length);
            // Récéption du message
            socket.receive(reponse);
            // Conversion du message en String
            String message = new String(reponse.getData(), 0, reponse.getLength());
            // Récupération du port dédié à l'utilisateur
            int portDedie = Integer.parseInt(message.split(":")[1]);
            // Indique à l'utilisateur le port choisi pour lui
            System.out.println("Port choisi : " + portDedie);

            // Création d'un thread pour recevoir les message
            Thread ecoute = new Thread(() -> {
                // Buffer pour recevoir le message
                byte[] tmp = new byte[1024];
                // Tant que l'utilisateur est actif et que sa socket n'est pas fermée
                while (running && !socket.isClosed()) {
                    try {
                        // Création d'un paquet pour recevoir le message
                        DatagramPacket p = new DatagramPacket(tmp, tmp.length);
                        // Récéption du message
                        socket.receive(p);
                        // Conversion du message en String
                        String m = new String(p.getData(), 0, p.getLength());

                        // Si le message est "TIMEOUT"
                        if (m.equals("TIMEOUT")) {
                            // Indique à l'utilisateur qu'il a été déconnecté car il a été inactif pendant plus d'une minute
                            System.out.println("\nDéconnecté : inactivité > 60 secondes.");
                            // Désactive l'utilisateur
                            running = false;
                            // Ferme la socket
                            socket.close();
                            // Ferme le lecteur de texte
                            scanner.close();
                            // Arrête la fonction
                            return;
                        }

                        // Efface la ligne actuelle (pour enlever le '>')
                        System.out.print("\r");
                        System.out.print("                    ");
                        System.out.print("\r");
                        // Affiche le message
                        System.out.println(m);
                        // Remet le '>'
                        System.out.print("> ");
                        // Force l'affichage immédiat
                        System.out.flush();

                    } catch (SocketException e) {
                        // S'il y a eu une exception de socket, arrêter la fonction
                        return;

                    } catch (Exception e) {
                        // S'il y a eu une autre exception, si l'utilisateur est actif, afficher l'exception
                        if (running) {
                            e.printStackTrace();
                        }
                    }
                }
            });

            // Démarrage du thread d'écoute
            ecoute.start();

            // Boucle d'envoi des messages utilisateur
            while (running) {
                // Affiche '>' (pour indiquer d'écrire un message)
                System.out.print("> ");
                // Lit ce qu'écrit l'utilisateur
                String msg = scanner.nextLine();
                // Si l'utilisateur n'est pas actif, arrêter la boucle
                if (!running) break;

                // Conversion du message en bytes
                byte[] donnees = msg.getBytes();
                // Création du paquet UDP contenant les données à envoyer
                DatagramPacket packet = new DatagramPacket(donnees, donnees.length, adresseServeur, portDedie);

                // Si l'utilisateur n'est pas actif ou que la socket est fermée, arrêter la boucle
                if (!running || socket.isClosed()) break;
                // Envoi du message
                socket.send(packet);

                // Si le message est "exit" (qu'importe la capitalité des lettres)
                if (msg.equalsIgnoreCase("exit")) {
                    // Désactive l'utilisateur
                    running = false;
                    // Arrête la boucle
                    break;
                }
            }

            // Arrêt de la socket
            socket.close();
            // Arrêt du lecteur de message
            scanner.close();

        } catch (Exception e) {
            // S'il y a une exception, l'afficher
            e.printStackTrace();
        }
    }
}

