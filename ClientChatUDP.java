import java.net.* ;
import java.util.* ;

public class ClientChatUDP {
    private String pseudo ;
    private DatagramSocket client ;

    public static void main(String[] args) {
        String serveurIP = "172.28.180.249" ;
        int serveurPort = 40000 ;

        try (DatagramSocket socket = new DatagramSocket() ;
            Scanner scanner = new Scanner(System.in)) {

            System.out.println("Client UDP étendu. Tapez 'exit' pour arrêter.") ;

            while (true) {
                System.out.print("Entrez votre message : ") ;
                String message = scanner.nextLine() ;

                if (message.equalsIgnoreCase("exit")) {
                    byte[] data = message.getBytes() ;
                    InetAddress addr = InetAddress.getByName(serveurIP) ;
                    DatagramPacket packet = new DatagramPacket(data, data.length, addr, serveurPort) ;
                    socket.send(packet) ;
                    System.out.println("Message 'exit' envoyé au serveur. Fermeture du client.") ;
                    break ;
                }

                byte[] donnees = message.getBytes() ;
                InetAddress adresseServeur = InetAddress.getByName(serveurIP) ;
                DatagramPacket packet = new DatagramPacket(donnees, donnees.length, adresseServeur, serveurPort) ;
                socket.send(packet) ;

                byte[] buffer = new byte[1024] ;
                DatagramPacket packetReponse = new DatagramPacket(buffer, buffer.length) ;
                try {
                    socket.setSoTimeout(3000) ;
                    socket.receive(packetReponse) ;
                    String reponse = new String(packetReponse.getData(), 0, packetReponse.getLength()) ;
                    System.out.println("Réponse du serveur : " + reponse) ;
                } catch (java.net.SocketTimeoutException e) {
                    System.out.println("Aucune réponse du serveur après 3 secondes.") ;
                }
            }

        } catch (Exception e) {
            e.printStackTrace()  ;
        }
    }
}