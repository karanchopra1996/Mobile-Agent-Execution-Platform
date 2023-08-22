package Mobile;

import java.util.*;

/**
 * Mobile.AgentLoader defines the class of an incoming agent and registers
 * it into its local class hash.
 * 
 * @author  Munehiro Fukuda
 * @version %I% %G%
 * @since   1.0
 */
public class AgentLoader extends ClassLoader {
    // a hash table to register incoming agent classes
    private Hashtable<String,Class> classHash = new Hashtable<String,Class>();

    /**
     * Mobile.AgentLoader defines the class of an incoming agent and registers
     * it into its local class hash.
     *
     * @param name     the name of a given agent.
     * @param bytecode the byte code of a given agent.
     * @return the new class of a given agent.
     */
    public Class loadClass(String name, byte[] bytecode ) {
	Class newClass = findLoadedClass( name );// try to find it from memory
	if ( newClass == null )
	    try {
		newClass = super.loadClass( name ); // try to find it from disk
	    } catch ( ClassNotFoundException e ) { }
	if ( newClass == null ) 
	    newClass = classHash.get( name );    // try to find it from my hash
	if ( newClass == null ) {      // define a new class from byte code
	    newClass = defineClass( name, bytecode, 0, bytecode.length );
	    classHash.put( name, newClass );
	}
	return newClass;
    }
} 
