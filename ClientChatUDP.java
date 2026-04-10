import java.net.* ;
import java.util.* ;

public class ClientChatUDP {
    private String pseudo ;
    private DatagramSocket client ;

    public static void main(String[] args) {
        String serveurIP = "127.0.0.1" ;
        int serveurPort = 9000 ;

        try {
            DatagramSocket socket = new DatagramSocket() ;
            Scanner scanner = new Scanner(System.in) ;
            InetAddress adresseServeur = InetAddress.getByName(serveurIP) ;

            System.out.println("Pseudo : ");
            String pseudo = scanner.nextLine() ;

            String join = "JOIN:" + pseudo ;
            socket.send(new DatagramPacket(join.getBytes(), join.length(), adresseServeur, serveurPort)) ;

            byte[] tmp = new byte[1024] ;
            DatagramPacket reponse = new DatagramPacket(tmp, tmp.length) ;
            socket.receive(reponse) ; 

            String message = new String(reponse.getData(), 0, reponse.getLength()) ;
            int portDedie = Integer.parseInt(message.split(":")[1]) ;
            System.out.println("Port choisi : " + portDedie) ;

            DatagramSocket socketChat = new DatagramSocket() ;
            Thread ecoute = new Thread(() -> {
                try {
                    byte[] temp = new byte[1024] ;
                    while (true) {
                        DatagramPacket p = new DatagramPacket(temp, temp.length) ;
                        socketChat.receive(p) ;
                        String m = new String(p.getData(), 0, p.getLength()) ;
                        System.out.println("\n" + m) ;
                    }
                }
                catch (Exception e) {
                    e.printStackTrace() ;
                }
            }) ;
            ecoute.start() ;
            
            while (true) {
                System.out.print("> ") ;
                String msg = scanner.nextLine() ;
                byte[] donnees = msg.getBytes() ;
                DatagramPacket packet = new DatagramPacket(donnees, donnees.length, adresseServeur, portDedie) ;
                socket.send(packet) ;

                if (msg.equalsIgnoreCase("exit")) {
                    socketChat.close() ;
                    break ;
                }
            }
            socket.close() ;
            scanner.close() ;
        }
        catch (Exception e) {
            e.printStackTrace()  ;
        }
    }
}
