package db;


import exceptions.MySQLException;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;


/**
 *
 * @author Christan Rech, Tim Bartsch, Yassir Klos
 *
 * Erstellt mit den Verbindungsdaten aus db.properties eine Connection
 * zu einem Datenbank Server.
 *
 */
public class DBConnector {

	private static final String host = "jdbc:mysql://stud-i-pr2.htw-saarland.de:3306/Projektmanager";
	private static final String username = "htwmaps";
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
                return null;
            }
	}

}
