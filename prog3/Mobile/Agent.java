package Mobile;
import java.io.*;
import java.rmi.*;
import java.lang.reflect.*;
import java.sql.SQLOutput;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Mobile.Agent is the base class of all user-define mobile agents. It carries
 * an agent identifier, the next host IP and port, the name of the function to
 * invoke at the next host, arguments passed to this function, its class name,
 * and its byte code. It runs as an independent thread that invokes a given
 * function upon migrating the next host.
 *
 * @author Karan Chopra
 */
public class Agent implements Serializable, Runnable {
    // live data to carry with the agent upon a migration
    protected int agentId = -1;    // this agent's identifier
    private String _hostname = null;  // the next host name to migrate
    private String _function = null;  // the function to invoke upon a move
    private int _port = 0;     // the next host port to migrate
    private String[] _arguments = null;  // arguments pass to _function
    private String _classname = null;  // this agent's class name
    private byte[] _bytecode = null;  // this agent's byte code
    //To receive
    protected List<String> agentList = new ArrayList<>();
    //to message to other agents
    protected String pingToNextAgent = null;
    //key to read the message
    protected String keyToKeyMessage = null;



    /**
     * setPort( ) sets a port that is used to contact a remote Mobile.Place.
     *
     * @param port a port to be set.
     */
    public void setPort(int port) {
        this._port = port;
    }

    /**
     * setId( ) sets this agent identifier: agentId.
     *
     * @param id an idnetifier to set to this agent.
     */
    public void setId(int id) {
        this.agentId = id;
    }

    /**
     * getId( ) returns this agent identifier: agentId.
     *
     * @param: this agent's identifier.
     */
    public int getId() {
        return agentId;
    }

    /**
     * getByteCode( ) reads a byte code from the file whosename is given in
     * "classname.class".
     *
     * @param classname the name of a class to read from local disk.
     * @return a byte code of a given class.
     */
    public static byte[] getByteCode(String classname) {
        // create the file name
        String filename = classname + ".class";

        // allocate the buffer to read this agent's bytecode in
        File file = new File(filename);
        byte[] bytecode = new byte[(int) file.length()];

        // read this agent's bytecode from the file.
        try {
            BufferedInputStream bis = new BufferedInputStream(new FileInputStream(filename));
            bis.read(bytecode, 0, bytecode.length);
            bis.close();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

        // now you got a byte code from the file. just return it.
        return bytecode;
    }

    /**
     * getByteCode( ) reads this agent's byte code from the corresponding file.
     *
     * @return a byte code of this agent.
     */
    public byte[] getByteCode() {
        if (_bytecode != null) // bytecode has been already read from a file
            return _bytecode;

        // obtain this agent's class name and file name
        _classname = this.getClass().getName();
        _bytecode = getByteCode(_classname);

        return _bytecode;
    }

    /**
     * run( ) is the body of Mobile.Agent that is executed upon an injection
     * or a migration as an independent thread. run( ) identifies the method
     * with a given function name and arguments and invokes it. The invoked
     * method may include hop( ) that transfers this agent to a remote host or
     * simply returns back to run( ) that terminates the agent.
     */
    public void run() {
        // Implement by yourself.
        //invoke a function specified in hop()
        try {
            if (this._arguments == null) {
                Method method = this.getClass().getMethod(_function);
                // call this method
                method.invoke(this);
            } else {
                //a function call with arguments
                // retrieve classes of the function arguments
                Class[] argClass = new Class[]{_arguments.getClass()};
                //find  the method whose name is _function and argument is String[]
                Method method = this.getClass().getMethod(_function,String[].class);
                //call this method
                method.invoke(this, (Object) _arguments);
            }
        } catch (NoSuchMethodException e) {
            // Handle the case where the specified method is not found
            System.err.println("Method not found: " + _function);
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            // Handle the case where there is illegal access to the method
            System.err.println("Illegal access to method: " + _function);
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            // Check if the exception is "Thread.stop"
            if (e.getTargetException() instanceof ThreadDeath) {
                // Ignore "Thread.stop" exception
                //System.out.println("Ignoring Thread.stop exception");
            } else {
                // Print out the exception
                System.err.println("Exception during method invocation: " + _function);
                e.printStackTrace();
            }
        }
    }

    /**
     * hop( ) transfers this agent to a given host, and invoeks a given
     * function of this agent.
     *
     * @param hostname the IP name of the next host machine to migrate
     * @param function the name of a function to invoke upon a migration
     */
    public void hop(String hostname, String function) {
        hop(hostname, function, null);
    }

    /**
     * hop( ) transfers this agent to a given host, and invoks a given
     * function of this agent as passing given arguments to it.
     *
     * @param hostname the IP name of the next host machine to migrate
     * @param function the name of a function to invoke upon a migration
     * @param args     the arguments passed to a function called upon a
     *                 migration.
     */
//@SuppressWarnings("deprecation")
    public void hop(String hostname, String function, String[] args) {
        // Implement by yourself.
        try {
            this._function = function;
            this._arguments = args;
            // Serialize the agent into a byte array
            byte[] agentBytes = serialize();
            // Construct the RMI URL for the remote place
            String url = "rmi://" + hostname + ":" + _port + "/place";
            byte[] byteCode = getByteCode();
            String className = this._classname;

            // Look up the remote place object using the RMI URL
            PlaceInterface place = (PlaceInterface) Naming.lookup(url);
            // Invoke the transfer method of the remote place to transfer the agent
            //System.out.println(" In the Hop before the transfer");
            //System.out.println(className);
            place.transfer(className, getByteCode(), agentBytes);

            // Terminate the current thread (agent)
            Thread.currentThread().stop();
        } catch (ThreadDeath td) {
            // Ignore the Thread.stop exception
            //System.out.println("Ignoring Thread.stop exception");
        } catch (Exception e) {
            // Print out any other exceptions that occurred during the hop
            System.err.println("Exception during hop: " + e.getMessage());e.printStackTrace();
        }
    }

    /**
     * serialize( ) serializes this agent into a byte array.
     *
     * @return a byte array to contain this serialized agent.
     */
    private byte[] serialize() {
        try {
            // instantiate an object output stream.
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            ObjectOutputStream os = new ObjectOutputStream(out);

            // write myself to this object output stream
            os.writeObject(this);

            return out.toByteArray(); // conver the stream to a byte array
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public void setAgentMessageList(List<String> messageListForAgent) {
        this.agentList = agentList;
    }
}