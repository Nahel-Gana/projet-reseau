import java.net.*;

/**
 * Représente les informations d'un client connecté.
 * <p>
 * Contient son pseudo, son adresse IP, son port réseau et sa dernière activité.
 */
public class ClientInfo {

    /** Pseudo du client */
    private String pseudo;

    /** Adresse IP du client */
    private InetAddress adresseIP;

    /** Port utilisé par le client */
    private int port;

    /** Horodatage (en millisecondes) de la dernière activité du client */
    private long derniereActivite;

    /**
     * Constructeur par défaut.
     * <p>
     * Initialise uniquement le timestamp de dernière activité à l'instant courant.
     * <p>
     * Les autres attributs restent non initialisés (null ou valeur par défaut).
     */
    public ClientInfo() {
        this.derniereActivite = System.currentTimeMillis();
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
        this.derniereActivite = System.currentTimeMillis();
    }

    /**
     * Retourne le pseudo du client.
     *
     * @return le pseudo du client
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
     * @return l'adresse IP du client
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
     * Retourne le port utilisé par le client.
     *
     * @return le port du client
     */
    public int getPort() {
        return port;
    }

    /**
     * Modifie le port utilisé par le client.
     *
     * @param port le nouveau port
     */
    public void setPort(int port) {
        this.port = port;
    }

    /**
     * Retourne le timestamp de la dernière activité du client.
     *
     * @return la dernière activité en millisecondes
     */
    public long getDerniereActivite() {
        return derniereActivite;
    }

    /**
     * Met à jour le timestamp de la dernière activité du client.
     *
     * @param derniereActivite nouveau timestamp en millisecondes
     */
    public void setDerniereActivite(long derniereActivite) {
        this.derniereActivite = derniereActivite;
    }
}
