package Mobile;
import java.io.*;
import java.net.*;
import java.rmi.*;
import java.rmi.server.*;
import java.rmi.registry.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Mobile.Place is the our mobile-agent execution platform that accepts an
 * agent transferred by Mobile.Agent.hop( ), deserializes it, and resumes it
 * as an independent thread.
 *
 * @author  Karan Chopra
 */
public class Place extends UnicastRemoteObject implements PlaceInterface {
    private AgentLoader loader = null;  // a loader to define a new agent class
    private int agentSequencer = 0;     // a sequencer to give a unique agentId
    //to store agent with key and their message
    private static Map<String, Map<String, String>> messageMap = new HashMap<>();
    //to store agent messages to pass to agent object
    private static List<String> messageList = new ArrayList<>();
    /**
     * This constructor instantiates a Mobile.AgentLoader object that
     * is used to define a new agent class coming from remotely.
     */
    public Place( ) throws RemoteException {
        super( );
        loader = new AgentLoader( );
    }

    /**
     * deserialize( ) deserializes a given byte array into a new agent.
     *
     * @param buf a byte array to be deserialized into a new Agent object.
     * @return a deserialized Agent object
     */
    private Agent deserialize( byte[] buf )
            throws IOException, ClassNotFoundException {
        // converts buf into an input stream
        ByteArrayInputStream in = new ByteArrayInputStream( buf );

        // AgentInputStream identify a new agent class and deserialize
        // a ByteArrayInputStream into a new object
        AgentInputStream input = new AgentInputStream( in, loader );
        return ( Agent )input.readObject();
    }
    /**
     * transfer( ) accepts an incoming agent and launches it as an independent
     * thread.
     *
     * @param classname The class name of an agent to be transferred.
     * @param bytecode  The byte code of  an agent to be transferred.
     * @param entity    The serialized object of an agent to be transferred.
     * @return true if an agent was accepted in success, otherwise false.
     */
    public boolean transfer( String classname, byte[] bytecode, byte[] entity )
            throws RemoteException {
        // Implement by yourself.
        try {
            // Register the agent's class
            //System.out.println(classname);
            //System.out.println(bytecode);
            loader.loadClass(classname, bytecode);
            // Deserialize the agent
            Agent agent = deserialize(entity);
            // Set the agent's identifier if it hasn't been set yet
            String hostName = InetAddress.getLocalHost().getHostName();
            String hostAddress = InetAddress.getLocalHost( ).getHostAddress( );
            if (agent.getId() == -1) {
                //increment the sequencer by 1
                agentSequencer++;
                //get the hostAddress and append the sequencer to a substring of it
                String newHostAddress = hostAddress.replace(".", "");
                newHostAddress = newHostAddress.substring(newHostAddress.length() - 3)
                        + String.valueOf(agentSequencer);
                int agentId = Integer.parseInt(newHostAddress);
                agent.setId(agentId);
            }
            //get the "messageListForAgent" from agentObj
            List<String> messageListForAgent = getMessagesFromPreviousAgents(agent.keyToKeyMessage , hostName);
            //set the "messageListForAgent" to agent
            agent.setAgentMessageList(messageListForAgent); // reading
            //deposit the messages of the current agent
            //writing
            depositMyMessage(agent.keyToKeyMessage, "(" + String.valueOf(agent.getId()) + ") =====>> " +
                    agent.pingToNextAgent + " <<=====", hostName);
            // Create a new thread with the agent and start it
            Thread thread = new Thread(agent);
            thread.start();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    /**
     * main( ) starts an RMI registry in local, instantiates a Mobile.Place
     * agent execution platform, and registers it into the registry.
     *
     * @param args receives a port, (i.e., 5001-65535).
     */
    public static void main( String args[] ) {
        // Implement by yourself.
        if (args.length < 1) {
            System.err.println("Usage: java Place <port>");
            System.exit(1);
        }
        try {
            int port = Integer.parseInt(args[0]);
            // Start the RMI registry
            startRegistry(port);
            // Instantiate the Place object
            Place place = new Place();
            // Bind the Place object to the registry
            String url = "rmi://localhost:" + port + "/place";
            Naming.rebind(url, place);
            System.out.println("Place is ready to accept agents on port " + port);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    /**
     * startRegistry( ) starts an RMI registry process in local to this Place.
     * @param port the port to which this RMI should listen.
     */
    private static void startRegistry( int port ) throws RemoteException {
        try {
            Registry registry =
                    LocateRegistry.getRegistry( port );
            registry.list( );
        }
        catch(RemoteException e) {
            Registry registry = LocateRegistry.createRegistry( port );
        }
    }
    /**
     * getMessagesFromPreviousAgents( ) gets the messages from previous agents
     * using its key and hostname
     *
     * @param key agentKey to the message related to agent
     * @param :hostname the hostname to read its messages
     */
    public List<String> getMessagesFromPreviousAgents(String key, String hostName) {
        if (messageMap.containsKey(key)) {
            Map<String, String> myMap = messageMap.get(key);
            if (myMap != null && !myMap.isEmpty()) {
                for (Map.Entry<String, String> entry : myMap.entrySet()) {
                    if (entry.getKey().equals(hostName)) {
                        messageList.add(entry.getValue());
                    }
                }
            }
        }
        return messageList;
    }
    /**
     * depositMyMessage( ) deposits the agent message in the hashmap
     * "messageMap" with the agentKey, message and hostname
     * in local to this Place.
     * @param messageKey agentKey to the message related to agent
     * @param msg agent message to be deposited
     * @param hostname the hostname on which message will deposit
     */
    public void depositMyMessage(String messageKey, String msg, String hostname){
        //if the key of agent exists
        if(messageMap.containsKey(messageKey)){
            //get the key's value map
            Map<String,String> temp = messageMap.get(messageKey);
            //put the message to that hostname
            temp.put(hostname,msg);
            System.out.println("Deposited Message for " + hostname);
        }else{
            //create a new hashmap
            Map<String,String> temp = new HashMap<>();
            //put the message to that hostname
            temp.put(hostname, msg);
            //add the key with message details in the tracker
            messageMap.put(messageKey,temp);
            System.out.println("Deposited Message for " + hostname);
        }

    }
}