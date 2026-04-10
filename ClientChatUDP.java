import java.net.*;
import java.util.*;

/**
 * Client UDP simple de chat.
 * <p>
 * Ce client permet de se connecter à un serveur UDP, d'envoyer un pseudo,
 * de recevoir un port dédié au chat, puis d’échanger des messages en temps réel.
 */
public class ClientChatUDP {

    /**
     * Pseudo de l'utilisateur (non utilisé directement ici mais prévu pour extension).
     */
    private String pseudo;

    /**
     * Socket UDP du client (non utilisé directement dans ce code actuel).
     */
    private DatagramSocket client;

    private static volatile boolean running = true ;

    /**
     * Point d'entrée du programme.
     * <p>
     * Étapes :
     * <ul>
     *      <li>Connexion au serveur</li>
     *      <li>Envoi du pseudo</li>
     *      <li>Réception du port dédié au chat</li>
     *      <li>Lancement d'un thread d'écoute des messages entrants</li>
     *      <li>Boucle d'envoi des messages utilisateur</li>
     * </ul>
     *
     * @param args arguments de la ligne de commande (non utilisés)
     */
    public static void main(String[] args) {
        String serveurIP = "127.0.0.1";
        int serveurPort = 9000;
        running = false ;

        try {
            DatagramSocket socket = new DatagramSocket();
            Scanner scanner = new Scanner(System.in);
            InetAddress adresseServeur = InetAddress.getByName(serveurIP);

            System.out.println("Pseudo : ");
            String pseudo = scanner.nextLine();

            // Envoi du pseudo au serveur pour rejoindre le chat
            String join = "JOIN:" + pseudo;
            socket.send(new DatagramPacket(join.getBytes(), join.length(), adresseServeur, serveurPort));

            // Réception de la réponse du serveur (port dédié)
            byte[] tmp = new byte[1024];
            DatagramPacket reponse = new DatagramPacket(tmp, tmp.length);
            socket.receive(reponse);

            String message = new String(reponse.getData(), 0, reponse.getLength());
            int portDedie = Integer.parseInt(message.split(":")[1]);
            System.out.println("Port choisi : " + portDedie);

            // Socket dédié au chat
            DatagramSocket socketChat = new DatagramSocket();

            /**
             * Thread d'écoute des messages entrants.
             * Fonctionne en continu et affiche chaque message reçu.
             */
            Thread ecoute = new Thread(() -> {
                try {
                    byte[] temp = new byte[1024];

                    while (running) {
                        DatagramPacket p = new DatagramPacket(temp, temp.length);
                        socketChat.receive(p);

                        String m = new String(p.getData(), 0, p.getLength());
                        System.out.println("\n" + m);
                    }
                } catch (Exception e) {
                    if (running) {
                        e.printStackTrace();
                    }
                }
            });

            ecoute.start();

            // Boucle d'envoi des messages utilisateur
            while (true) {
                System.out.print("> ");
                String msg = scanner.nextLine();

                byte[] donnees = msg.getBytes();
                DatagramPacket packet = new DatagramPacket(donnees, donnees.length, adresseServeur, portDedie);

                socketChat.send(packet);

                // Condition de sortie
                if (msg.equalsIgnoreCase("exit")) {
                    socketChat.close();
                    break;
                }
            }

            socket.close();
            scanner.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
