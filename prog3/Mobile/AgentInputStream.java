package Mobile;

import java.io.*;
import java.util.List;
import java.util.ArrayList;

/**
 * AgentInputStream is used to read a byte array from ObjectInputStream and 
 * deserialize it into an agent object. For this purpose, 
 * ObjectInputStream.resolveClass( ) was overwritten to search AgentClassLoader
 * for a given agent.
 */
public class AgentInputStream extends ObjectInputStream {
    private ClassLoader classloader;          // Agent ClassLoader

    /**
     * The constructor initialzes the InputStream super class and maintains
     * a class loader that includes the class of an agent to be read from
     * this intpu stream.
     *
     * @param in          an input stream to create an agent
     * @param classloader a loader that includes the class of an agent to read
     */
    public AgentInputStream( InputStream in, ClassLoader classloader ) 
	throws IOException {
        super( in );                      // initialize ObjectIntputStream
        this.classloader = classloader;   // use this agent classloader later
    }

    /**
     * resolveClass( ) is automatically called whe reading and deserializing
     * a new agent.
     *
     * @param agent the class found in ObjectInputStream
     */
    protected Class resolveClass( ObjectStreamClass agent ) 
	throws IOException {
        String className = agent.getName();  // a class name found in a stream

        try {
            // load the corresponding class from the agent class loader
            Class loadedClass = classloader.loadClass( className );
            if (loadedClass != null) {
                return loadedClass; // found it
            }
        } catch ( Exception e ) {
	    // agent is not a mobile-agent user-defined class.
	    // go to super.resolveClass( agent );
        }
        try {
            // try the super class loader for \[java.lang.*;
            return super.resolveClass( agent );
        } catch (ClassNotFoundException e) {
            e.printStackTrace(); // no class round
        }
        return null;
    }
}
