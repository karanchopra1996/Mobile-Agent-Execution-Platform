package Mobile;
import Mobile.*;

/**
 * TestAgent is a test mobile agent that is injected to the 1st Mobile.Place
 * platform to print the breath message, migrates to the 2nd platform to
 * say "Hello!", and even moves to the 3rd platform to say "Oi!".
 */
public class TestAgent extends Agent {
    public int hopCount = 0;
    public String[] destination = null;

    /**
     * The consturctor receives a String array as an argument from
     * Mobile.Inject.
     *
     * @param args arguments passed from Mobile.Inject to this constructor
     */
    public TestAgent(String[] args ) {
        destination = args;
        //messageToNextAgent
        pingToNextAgent = "Initial message from TestAgent " ;
        keyToKeyMessage ="K101"; //agentKey
    }

    /**
     * init( ) is the default method called upon an agent inject.
     */
    public void init( ) {
        System.out.println( "agent( " + agentId + ") invoked init: " +
                "hop count = " + hopCount +
                ", next dest = " + destination[hopCount] );
        String[] args = new String[1];
        args[0] = "Hello!";
        hopCount++;
        hop( destination[0], "step", args );
    }

    /**
     * step( ) is invoked upon an agent migration to destination[0] after
     * init( ) calls hop( ).
     *
     * @param args arguments passed from init( ).
     */
    public void step( String[] args ) {
        System.out.println( "agent( " + agentId + ") invoked step: " +
                "hop count = " + hopCount +
                ", next dest = " + destination[hopCount] +
                ", message = " + args[0] );
        args[0] = "Oi!";

        //if there are messages from other agents, read it
        if(agentList.size() > 0){
            System.out.println("Reading messages from other agents :");
            for (String s : agentList) {
                System.out.println("Message from agent " + s);
            }
        }
        //Message to next agent for this method
        pingToNextAgent = "** Dude! This is a Move Ahead message **";
        hopCount++;
        hop( destination[1], "jump", args );

    }
    /**
     * jump( ) is invoked upon an agent migration to destination[1] after
     * step( ) calls hop( ).
     *
     * @param args arguments passed from step( ).
     */
    public void jump( String[] args ) {
        System.out.println( "agent( " + agentId + ") invoked jump: " +
                "hop count = " + hopCount +
                ", message = " + args[0] );

        //if there are messages from other agents, read it
        if(agentList.size() > 0) {
            System.out.println("Reading messages from other agents :");
            for (String s : agentList) {
                System.out.println("Message from agent " + s);
            }
        }
    }
}

