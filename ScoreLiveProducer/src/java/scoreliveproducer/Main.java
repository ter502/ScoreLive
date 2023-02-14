/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package scoreliveproducer;

import javax.annotation.Resource;
import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.DeliveryMode;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.MessageProducer;
import javax.jms.Queue;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.jms.Topic;
import java.util.Scanner;

/**
 *
 * @author eiwte
 */
public class Main {
    
    /**
     * @param args the command line arguments
     */
    @Resource(mappedName = "jms/SimpleJMSTopic")
    private static Topic topic;
    @Resource(mappedName = "jms/ConnectionFactory")
    private static ConnectionFactory connectionFactory;
    @Resource(mappedName = "jms/SimpleJMSQueue")
    private static Queue queue;
    public static void main(String[] args) {
        Connection connection = null;

        //Check Argument
        if (args.length != 1) {
            System.err.println(
                    "Program takes only one arguments: "
                    + "<dest_type>");
            System.exit(1);
        }

        String destType = args[0];
//        System.out.println("Destination type is " + destType);

        if (!(destType.equals("queue") || destType.equals("topic"))) {
            System.err.println("Argument must be \"queue\" or " + "\"topic\"");
            System.exit(1);
        }

        Destination dest = null;

        try {
            if (destType.equals("queue")) {
                dest = (Destination) queue;
            } else {
                dest = (Destination) topic;
            }
        } catch (Exception e) {
            System.err.println("Error setting destination: " + e.toString());
            System.exit(1);
        }

        
        try {
            // Create Object form Class Connection
            connection = connectionFactory.createConnection();

            Session session = connection.createSession(
                        false, //Transaction Processing 
                        Session.AUTO_ACKNOWLEDGE); //Get Message Then Delete Message in Queue

            MessageProducer producer = session.createProducer(dest);
            TextMessage message = session.createTextMessage();
//            producer.setTimeToLive(10000);  //message live is set to 10 seconds
//            producer.setDeliveryMode(DeliveryMode.NON_PERSISTENT);
            
            //Send first message 
            Scanner input = new Scanner(System.in);
            message.setText("Thailand 1-0 Laos");
            System.out.println(message.getText());
            producer.send(message);
            
            //loop for get live score
            while(true){
                System.out.print("Enter Live Score ");
                message.setText(input.nextLine());
                
                if(message.getText().equals("q")){
                    break;
                }
                
                producer.send(message);
            }

            /*
             * Send a non-text control message indicating end of
             * messages.
             */
//            producer.send(session.createMessage()); //Message null for tell Sender end of meassage
        } catch (JMSException e) {
            System.err.println("Exception occurred: " + e.toString());
        } finally {
            if (connection != null) {
                try {
                    connection.close();
                } catch (JMSException e) {
                }
            }
        }
    }
}
