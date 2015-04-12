package za.co.discoverylife.desktop.host;

public class ServerConnection implements IServerConnection {
	private String url;
	private int port;
	private String user;
	private String password;
	
	public ServerConnection(){
		url = "localhost";
		port=80;
	}

	public ServerConnection(String url, int port, String user, String password) {
		super();
		this.url = url;
		this.port = port;
		this.user = user;
		this.password = password;
	}
	
	public ServerConnection(String url, int port) {
		super();
		this.url = url;
		this.port = port;
	}

	public boolean hasUser() {
		return user!=null;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public String getUser() {
		return user;
	}

	public void setUser(String user) {
		this.user = user;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String toString() {
		return "ServerConnection [url=" + url + ", port=" + port + ", user="
				+ user + ", password=" + password + "]";
	}

}
