# Mobile-Agent-Execution-Platform
This project implements a mobile-agent execution platform that is in general facilitated with three distributed-computing technologies: RPC, dynamic linking, and object serialization/deserialization.
We exercise how to use these technologies in Java, which correspond to RMI, class loader and reflection, and
Java object input/output streams.


AgentLoader.java Defines the class of an incoming agent and registers it into its local class
hash. 

AgentInputStream.java Reads a byte array from ObjectInputStream and deserializes it into an agent
object, using the AgentLoader class. 

Inject.java Reads a given agent class from local disk, instantiates a new object from it,
and transfers this agent to a given destination IP where the agent starts with
the init( ) function. 

PlaceInterface.java Defines Place's RMI method that will be called from an Mobile.Agent.hop( )
to transfer an agent. 

Process:
Step 1: Injection
The agent is instantiated where a user injects it through the Mobile.Inject program, (i.e. the computing
node local to the user), and receives a String array as the constructor argument. At this time, an agent is
still an ordinary (passive) Java object.
Step 2: System-initiated migration
Upon an instantiation, the agent is dispatched to the computing node that has been specified with the
Mobile.Inject program. Dispatched there, the agent starts to run as an independent thread and
automatically invokes its init( ) method. If init( ) has no hop( ) method call, a return from init( ) means the
termination of this agent.
Step 3: User-initiated migration
If the agent invokes the hop( destination, function, arguments ) function within init( ), it will migrate to
the next computing-node, (i.e., destination) specified in hop( ). Upon each user-initiated migration, the
agent will resume its execution as an independent thread and invoke the function specified in hop( ).
Step 4: Termination
If the agent returns from the function that was invoked upon a migration (including init( )), the thread to
run this agent is stopped and the object is garbage-collected by the system.


public void hop( String host, String function ) Transfers this agent to a given host, and
invokes a given function of this agent.
public void hop( String host, String function,String[] arguments )
Transfers this agent to a given host, and
invokes a given function of this agent as
passing given arguments to it.

public void run( ) Is the body of Mobile.Agent that is
executed upon an injection or a
migration as an independent thread. The
run( ) method identifies the function and
arguments given in hop( ), and invokes
it. The invoked function may include
hop( ) to further transfer the calling agent
to a remote host or simply return back to
run( ) that terminates the agent.

public void setPort( int port ) Sets a port that is used to contact a
remote RMI server when migrating
there.

public void setId( int id ) Sets this agent identifier, (i.e., id).

public int getId( ) Returns this agent identifier, (i.e., id).

public static byte[] getByteCode( String className ) Reads a byte code from the file whose
name is className + “.class”.
public  byte[] getByteCode( ) Reads this agent's byte code from the
corresponding file.
private byte[] serialize( ) Serializes this agent into a byte array.

Agent.run( )  perform the following tasks:
(1) Find the method to invoke, through this.getClass( ).getMethod( ).
(2) Invoke this method through Method.invoke( ).

Agent.hop( String hostname, String function, String[] args ) must perform the following tasks:
(1) Load this agent’s byte code into the memory.
(2) Serialize this agent into a byte array.
(3) Find a remote place through Naming.lookup( ).
(4) Invoke an RMI call.
(5) Kill this agent with Thread.currentThread( ).stop( ), which is deprecated but do so anyway.


public static void main( String[] args ) Starts an RMI registry in local,
instantiates a Mobile.Place object,
(i.e., an agent execution platform),
and registers into the registry. The
main( ) should receive the port #,
(i.e., 5001-65535) to launch its
local rmiresitry.

private static void startRegistry( int port ) throwsRemoteException
Is called from main( ) and starts an
RMI registry in local to this Place.

public Place( ) throws RemoteException Instantiates an AgentLoader object
that should be passed to
AgentInputStream to deserialize an
incoming agent.

public boolean transfer( String classname, byte[]bytecode, byte[] entity ) throws RemoteException
Is called from Agent.hop( )
remotely. The transfer( ) method
receives this calling agent,
deserializes it, sets this agent’s
identifier if it has not yet been set,
instantiates a Thread object as
passing this agent to its constructor,
(in other words, the agent is an
Runnable interface), and invokes
this thread’s start( ) method. If
everything goes well, transfer( )
should return true, otherwise false.

private Agent deserialize( byte[] buf ) Receives a byte array of an agent,
and deserializes it from the array.

Place.main( String args[] )  perform the following tasks:
(1) Read args[0] as the port number and checks its validity.
(2) Invoke startRegistry( int port ).
(3) Instantiate a Place object.
(4) Register it into rmiregistry through Naming,rebind( ).


Place.transfer( String classname, byte[] bytecode, byte[] entity ) perform the following tasks:
(1) Register this calling agent’s classname and bytecode into AgentLoader.
(2) Deserialize this agent’s entity through deserialize( entity ).
(3) Set this agent’s identifier if it has not yet been set. How to give a new agent id is up to your
implementation. An example is to have each Place maintain a sequencer, to generate a unique
agent id with a combination of the Place IP address and this sequence number, and increment the
sequencer.
(4) Instantiate a Thread object as passing the deserialized agent to the constructor.
(5) Invoke this thread’s start( ) method.
(6) Return true if everything is done in success, otherwise false.



AGENT.JAVA
The code represents the Agent class, which serves as the base class for user-defined mobile agents.
The Agent class implements the Serializable and Runnable interfaces. It contains fields to store agent-related information,
such as the agent identifier, next host IP and port, function name to invoke at the next host, arguments for the function, class name, and byte code.
The setPort method sets the port used to contact a remote Mobile.Place. 
The setId method sets the agent identifier, while the getId method returns the agent identifier.
The getByteCode method reads the byte code of a given class from a file on the local disk. 
It is used both as a static method and an instance method to obtain the byte code of the agent.
The run method represents the body of the Agent class, which is executed upon injection or migration as an independent thread. 
It identifies and invokes the specified function using reflection. 
The invoked method can include a call to the hop method, which transfers the agent to a remote host, or simply returns back to run, terminating the agent.
The hop method transfers the agent to a specified host and invokes a given function upon migration. 
It serializes the agent into a byte array and uses RMI (Remote Method Invocation) to transfer the agent to the remote place. 
The current thread (agent) is then terminated. The serialize method serializes the agent into a byte array using object serialization.




Place.java
The code implements the Place class, which serves as a mobile-agent execution platform.
It allows agents to be transferred remotely and executed as independent threads. 
The Place class extends UnicastRemoteObject and implements the PlaceInterface for remote method invocation.
The constructor initializes the loader object for defining new agent classes and sets the agent sequencer to assign unique agent IDs. 
It also creates a messageMap to store agents and their messages, and a messageList to hold agent messages.
The deserialize method deserializes a byte array into an Agent object using an AgentInputStream and the loader.
The transfer method accepts an agent to be transferred and launched. 
It registers the agent's class, deserializes it, and assigns a unique ID if necessary. 
It retrieves messages from previous agents and sets them to the agent. The agent's own message is deposited using depositMyMessage. 
Finally, a new thread is created for the agent, and execution starts. The getMessagesFromPreviousAgents method retrieves messages associated with a key and hostname from the messageMap. It populates the messageList with the matching messages and returns it. The depositMyMessage method stores an agent's message in the messageMap using the agent's key, message, and hostname. If the key exists, the message is added to the existing map. Otherwise, a new map is created, and the message is stored with the corresponding key.The main method initializes the RMI registry, instantiates a Place object, and binds it to the registry. A message is printed to indicate that the Place is ready to accept agents on the specified port.
I have added a TestAgent.java to work on the additional feature of indirect communication between two agents which will be MyAgent and TestAgent. The TestAgent class is a mobile agent that migrates between different platforms, printing messages at each destination and exchanging messages with other agents. It demonstrates the flow of a mobile agent's movement and interaction within a distributed system.



