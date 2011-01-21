package db;


import exceptions.MySQLException;
import java.io.BufferedReader;
import java.io.FileReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;


/**
 *
 * @author Stanislaw Tartakowski, Thomas Altmeyer
 *
 * Erstellt mit den Verbindungsdaten aus der pw.txt Datei eine Connection
 * zu einem Datenbank Server.
 *
 */
public class DBConnector {

	private static final String host = "jdbc:mysql://stud-i-pr2.htw-saarland.de:3306/Projectmanager";
	private static final String username = "projectmanager";
	private static final String driver = "com.mysql.jdbc.Driver";


	/**
	 * Erstellt mit den Verbindungsdaten aus db.properties eine Connection
	 * zu einem Datenbank Server.
	 *
	 * @return Connection
	 * @throws SQLException
	 * @throws MySQLException
	 */
	public static Connection getConnection() throws SQLException, MySQLException{
            try {
                String pw = "";
                BufferedReader in = new BufferedReader(new FileReader("/pw.txt"));
                pw = in.readLine();
                Class.forName(driver).newInstance();
                return DriverManager.getConnection(host, username, pw);
            } catch(Exception e) {
                e.printStackTrace();
                return null;
            }
	}

}
