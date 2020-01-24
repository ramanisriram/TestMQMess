import java.io.File;
import java.nio.file.Files;

import javax.jms.MessageConsumer;
import javax.jms.TextMessage;

import com.ibm.jms.JMSTextMessage;
import com.ibm.mq.jms.MQQueue;
import com.ibm.mq.jms.MQQueueReceiver;
import com.ibm.mq.jms.MQQueueSender;
import com.ibm.mq.jms.MQQueueSession;

public class InvokerService {

	
	public static void main(String args[]) {
		try {
			InvokerService ob1 = new InvokerService();

			MQConnection.initProperties();
			MQConnection.initConnection(MQConnection.getProperties());

			System.out.println("Initializing Session");
			MQConnection.getMQConnection().start();

			MQQueue reqQ = new MQQueue(MQConnection.getProperties().getProperty("REQQ"));
			MQQueue resQ = new MQQueue(MQConnection.getProperties().getProperty("RESQ"));
			
			//1. This method waits, receives a message from the queue and then responds with a new message
			// containing the co-relation id as the initial message's message ID.
			ob1.receiveAndSendMsgWithCorel(MQConnection.getMQSession(), reqQ, resQ);
			
			//2. This method reads a file and sends the contents as a message to Q
			File fileMsg = new File("C:\\file.txt");
			byte[] fileContents = Files.readAllBytes(fileMsg.toPath());
			String requestMsg = new String(fileContents, "UTF-8");

			JMSTextMessage message = (JMSTextMessage) MQConnection.getMQSession().createTextMessage(requestMsg);

			ob1.sendMessage(MQConnection.getMQSession(), reqQ, message);
			
			//3. This method waits, receives a message from the queue and send a message to another Q
			ob1.receiveAndSendMsg(MQConnection.getMQSession(), reqQ, resQ);
			
			//4. This method just consumes a message in the Q. If no message exists in the Q, it wont wait.
			ob1.consumeMessageFromQ(MQConnection.getMQSession(), resQ);
		} catch (Exception ex) {
			ex.printStackTrace();
			System.out.println("FAILURE in main");
		}
	}

	public void receiveAndSendMsgWithCorel(MQQueueSession session, MQQueue sendQ, MQQueue receiverQ) {
		try {
			System.out.println("Awaiting Response... from Q:" + receiverQ.toString());
			MQQueueReceiver receiver = (MQQueueReceiver) session.createReceiver(receiverQ);
			System.out.println("--------------------------");
			JMSTextMessage receivedMsg = (JMSTextMessage) receiver.receive();

			System.out.println("message.getText()):" + receivedMsg.getText());
			receivedMsg.setJMSReplyTo(sendQ);
			sendMessageWithCorrelId(session, sendQ, receivedMsg);
		} catch (Exception ex) {
			ex.printStackTrace();
			System.out.println("FAILURE");
		}
	}
	
	public void sendMessageWithCorrelId(MQQueueSession session, MQQueue sendQ,
			JMSTextMessage receivedMsg) {
		try {
			System.out.println("Sending Message...");
			System.out.println("--------------------------");
			MQQueueSender sender = (MQQueueSender) session.createSender(sendQ);
			String responseMessage = "Response Message from Stub";

			// send the same payload received

			System.out.println("responseMessage:" + responseMessage);

			JMSTextMessage message = (JMSTextMessage) session.createTextMessage(responseMessage);

			if (receivedMsg != null) {
				String initialMsgId = receivedMsg.getJMSMessageID();
				System.out.println("---------------------------");
				message.setJMSCorrelationID(initialMsgId);
				System.out.println("correlId:" + message.getJMSCorrelationID());
				System.out.println("Sending message:" + message.getText());
			}
			
			System.out.println("Sending message:" + message.getText());
			message.setJMSType("response");
			message.setIntProperty("messageCount", 8000);

			sender.send(message);
			System.out.println("Message Sent to Q:" + sendQ.toString());
			System.out.println("--------------------------");
			sender.close();

		} catch (Exception ex) {
			ex.printStackTrace();
			System.out.println("FAILURE");
		}
	}
	
	//Simply consume msg from the queue
	public void consumeMessageFromQ(MQQueueSession session, MQQueue queueDet) {
		try {
			System.out.println("Reading Response...");
			System.out.println("--------------------------");
			MessageConsumer responseMsgConsumer = session.createConsumer(queueDet);
			TextMessage message = (TextMessage) responseMsgConsumer.receive(1000);
			System.out.println("message.getText()):" + message.getText());
		} catch (Exception ex) {
			ex.printStackTrace();
			System.out.println("FAILURE");
		}
	}

	public void receiveAndSendMsg(MQQueueSession session, MQQueue sendQ, MQQueue receiverQ) {
		try {
			System.out.println("Awaiting Response... from Q:" + receiverQ.toString());
			MQQueueReceiver receiver = (MQQueueReceiver) session.createReceiver(receiverQ);
			System.out.println("--------------------------");
			JMSTextMessage msg = (JMSTextMessage) receiver.receive();

			System.out.println("message.getText()):" + msg.getText());
			msg.setJMSReplyTo(sendQ);
			sendMessage(session, sendQ, msg);
		} catch (Exception ex) {
			ex.printStackTrace();
			System.out.println("FAILURE");
		}
	}

	//just to send message
	public void sendMessage(MQQueueSession session, MQQueue sendQ, JMSTextMessage message) {
		try {
			System.out.println("Sending Message...");
			System.out.println("--------------------------");
			MQQueueSender sender = (MQQueueSender) session.createSender(sendQ);
			String messageString = message.getText();
			System.out.println("messageString:" + messageString);
			message._setJMSIBMMsgTypeFromInt(8);
			sender.send(message);
			System.out.println("Message Sent to Q:" + sendQ.toString());
			System.out.println("--------------------------");
			sender.close();

		} catch (Exception ex) {
			ex.printStackTrace();
			System.out.println("FAILURE");
		}
	}

}