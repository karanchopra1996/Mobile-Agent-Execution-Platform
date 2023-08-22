package Mobile;

import java.rmi.*;

/**
 * Mobile.PlaceInterface defines Place's RMI method that will be called from
 * an Mobile.Agent.hop( ) to transfer an agent.
 *
 * @author  Munehiro Fukuda
 * @version %I% %G%
 * @since   1.0
 */
public interface PlaceInterface extends Remote {
    /**
     * transfer( ) accepts an incoming agent and launches it as an independent
     * thread.
     *
     * @param classname The class name of an agent to be transferred.
     * @param bytecode  The byte code of  an agent to be transferred.
     * @param entity    The serialized object of an agent to be transferred.
     * @return true if an agent was accepted in success, otherwise false.
     */
    public boolean transfer( String classname, byte[] bytecode,
			     byte[] entity ) throws RemoteException;
}