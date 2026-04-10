import java.net.* ;

/**
 * Classe représentant les informations d'un client.
 * Elle contient le pseudo, l'adresse IP et le port du client.
 */
public class ClientInfo {
    
    /** Pseudo du client */
    private String pseudo;
    
    /** Adresse IP du client */
    private InetAddress adresseIP;
    
    /** Port utilisé par le client */
    private int port;

    /**
     * Constructeur par défaut.
     * Initialise les attributs avec des valeurs vides ou nulles.
     */
    public ClientInfo() {
        
    }

    /**
     * Constructeur avec paramètres.
     *
     * @param pseudo le pseudo du client
     * @param adresseIP l'adresse IP du client
     * @param port le port utilisé par le client
     */
    public ClientInfo(String pseudo, InetAddress adresseIP, int port) {
        this.pseudo = pseudo;
        this.adresseIP = adresseIP;
        this.port = port;
    }

    /**
     * Retourne le pseudo du client.
     *
     * @return le pseudo
     */
    public String getPseudo() {
        return pseudo;
    }

    /**
     * Modifie le pseudo du client.
     *
     * @param pseudo le nouveau pseudo
     */
    public void setPseudo(String pseudo) {
        this.pseudo = pseudo;
    }

    /**
     * Retourne l'adresse IP du client.
     *
     * @return l'adresse IP
     */
    public InetAddress getAdresseIP() {
        return adresseIP;
    }

    /**
     * Modifie l'adresse IP du client.
     *
     * @param adresseIP la nouvelle adresse IP
     */
    public void setAdresseIP(InetAddress adresseIP) {
        this.adresseIP = adresseIP;
    }

    /**
     * Retourne le port du client.
     *
     * @return le port
     */
    public int getPort() {
        return port;
    }

    /**
     * Modifie le port du client.
     *
     * @param port le nouveau port
     */
    public void setPort(int port) {
        this.port = port;
    }
}
