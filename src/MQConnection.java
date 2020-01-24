import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Properties;

import javax.jms.JMSException;
import javax.jms.Session;

import com.ibm.mq.jms.JMSC;
import com.ibm.mq.jms.MQQueueConnection;
import com.ibm.mq.jms.MQQueueConnectionFactory;
import com.ibm.mq.jms.MQQueueSession;

public class MQConnection {
	private static MQQueueConnection connection = null;
	private static MQQueueSession session = null;
	private static Properties props = null;

	public static Properties initProperties() {
		try {
			// Reading details from the property file to read configs
			InputStream fileInpStream = new FileInputStream("properties/MQProperties.properties");
			props = new Properties();
			props.load(fileInpStream);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return props;
	}
	
	public static void initConnection(Properties props) {
		System.out.println("Initializing Connection");
		System.out.println("--------------------------");
		if (connection == null) {
			try {
				// Server details to connect
				MQQueueConnectionFactory connFactory = new MQQueueConnectionFactory();
				connFactory.setHostName(props.getProperty("HOST_NAME"));
				connFactory.setPort(Integer.parseInt(props.getProperty("PORT")));
				connFactory.setTransportType(JMSC.MQJMS_TP_CLIENT_MQ_TCPIP);
				connFactory.setQueueManager(props.getProperty("QUEUE_MGR"));
				connFactory.setChannel(props.getProperty("CHANNEL"));

				System.out.println("Connection setup completed");
				
				
				connection = (MQQueueConnection) connFactory.createQueueConnection(props.getProperty("USER_NAME"),
						props.getProperty("PASSWORD"));

			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	public static MQQueueConnection getMQConnection() {
		return connection;
	}
	
	public static MQQueueSession getMQSession() {
		try {
			session = (MQQueueSession) connection.createQueueSession(false, Session.AUTO_ACKNOWLEDGE);
		} catch (JMSException e) {
			e.printStackTrace();
		}
		return session;
	}
	
	public static void closeConn() {
		try {
			session.close();
			connection.close();
		} catch (JMSException e) {
			e.printStackTrace();
		}
		
	}

	public static Properties getProperties() {
		return props;
	}
}
