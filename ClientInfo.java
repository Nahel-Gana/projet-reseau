public class ClientInfo {
    private String pseudo ;
    private String adresseIP ;
    private int port ;

    public ClientInfo() {
        this.pseudo = "" ;
        this.adresseIP = "" ;
        this.port = 0 ;
    }

    public ClientInfo(String pseudo, String adresseIP, int port) {
        this.pseudo = pseudo ;
        this.adresseIP = adresseIP ;
        this.port = port ;
    }

    public String getPseudo() {
        return pseudo ;
    }

    public void setPseudo(String pseudo) {
        this.pseudo = pseudo ;
    }

    public String getAdresseIP() {
        return adresseIP ;
    }

    public void setAdresseIP(String adresseIP) {
        this.adresseIP = adresseIP ;
    }

    public int getPort() {
        return port ;
    }

    public void setPort(int port) {
        this.port = port ;
    }
}
