package net.zhuruoling.uraniumconnect;

import com.google.gson.annotations.SerializedName;

public class Server {
    public Server(String serverIP, int serverPort, String serverKey, String serverCryptoKey, String serverName){
        this.serverCryptoKey = serverCryptoKey;
        this.serverKey = serverKey;
        this.serverIP = serverIP;
        this.serverPort = serverPort;
        this.serverName = serverName;
    }
    @SerializedName("server_ip")
    String serverIP = "";
    @SerializedName("server_port")
    int serverPort = 0;
    @SerializedName("server_key")
    String serverKey = "";
    @SerializedName("server_crypto_key")
    String serverCryptoKey = "";
    @SerializedName("server_name")
    String serverName;

    public int getServerPort() {
        return serverPort;
    }

    public String getServerCryptoKey() {
        return serverCryptoKey;
    }

    public String getServerIP() {
        return serverIP;
    }

    public String getServerKey() {
        return serverKey;
    }

    public String getServerName() {
        return serverName;
    }

    public void setServerName(String serverName) {
        this.serverName = serverName;
    }

    public void setServerCryptoKey(String serverCryptoKey) {
        this.serverCryptoKey = serverCryptoKey;
    }

    public void setServerIP(String serverIP) {
        this.serverIP = serverIP;
    }

    public void setServerKey(String serverKey) {
        this.serverKey = serverKey;
    }

    public void setServerPort(int serverPort) {
        this.serverPort = serverPort;
    }
}
