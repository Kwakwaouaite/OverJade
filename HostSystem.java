// Package
///////////////
package OverJADE;

// Imports
///////////////
import jade.core.AID;
import jade.core.Agent;
import jade.core.ProfileImpl;
import jade.core.Profile;

import jade.wrapper.PlatformController;
import jade.wrapper.StaleProxyException;
import jade.wrapper.AgentController;

import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.OneShotBehaviour;

import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.DFService;
import jade.domain.FIPAException;

import javax.swing.*;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.text.NumberFormat;


/**
 * <code><pre>
 *     java jade.Boot -gui host:examples.party.HostAgent()
 * </pre></code>
 * </p>
**/

public class HostSystem
    extends Agent
{
	
	
    // Constants
    //////////////////////////////////

    public final static String GOODBYE = "GOODBYE";
    public final static String HELLO = "HELLO";
    public final static String JOINGROUP = "JOINGROUP";
    public final static String LEAVEGROUP = "LEAVEGROUP";

    // Instance variables
    //////////////////////////////////
    protected JFrame m_frame = null;
    protected Vector<AgentController> m_guestList = new Vector<AgentController>();    // invite
    protected Vector<AID> m_guestListAID = new Vector<AID>();
    protected Map<AgentController, Boolean> m_guestListAgent = new HashMap<AgentController, Boolean>();
    protected int m_guestCount = 0;                 // arrivals
    protected int m_personInGroup = 0;
    protected int m_personConnected = 0;

    protected int m_introductionCount = 0;
    protected boolean m_partyOver = false;
    protected NumberFormat m_avgFormat = NumberFormat.getInstance();
    protected long m_startTime = 0L;

    Random rnd = new Random();

    protected int timeCreationNewAgents = 10;
    protected int index = 0;


    // Constructors
    //////////////////////////////////

    /**
     * Construct the host agent.  Some tweaking of the UI parameters.
     */
    public HostSystem() {
        m_avgFormat.setMaximumFractionDigits( 2 );
        m_avgFormat.setMinimumFractionDigits( 2 );
    }

    /**
     * Update the state of the party in the UI
     */
    protected void setPartyState( final String state ) {
        SwingUtilities.invokeLater( new Runnable() {
                                        public void run() {
                                            ((HostSystemUI) m_frame).lbl_partyState.setText( state );
                                        }
                                    } );
    }



    // External signature methods
    //////////////////////////////////

    /**
     * Setup the agent.  Registers with the DF, and adds a behaviour to
     * process incoming messages.
     */
    protected void setup() {
        try {
            System.out.println( getLocalName() + " setting up");

            // create the agent descrption of itself
            DFAgentDescription dfd = new DFAgentDescription();
            dfd.setName( getAID() );
            DFService.register( this, dfd );

            // add the GUI
            setupUI();
            
            
            // add a Behaviour to handle messages from guests
            addBehaviour( new CyclicBehaviour( this ) {
                            public void action() {
                                ACLMessage msg = receive();

                                if (msg != null) {
                                    if (HELLO.equals( msg.getContent() )) {		//Un nouveau agent activ?
                                        // a guest has arrived
                                    	m_personConnected++;
                                        System.out.println( "Une nouvelle personne est connect?" );  
                                        SwingUtilities.invokeLater( new Runnable() {
                                            public void run() {
                                                ((HostSystemUI) m_frame).lbl_numIntroductions.setText(Integer.toString(m_personConnected));
                                            }
                                        } );
                                    }
                                    
                                    else if (GOODBYE.equals( msg.getContent() )){
                                    	// a guest has leave
                                    	m_personConnected--;
                                        System.out.println( "Un agent est partis dans un groupe" ); 
                                        SwingUtilities.invokeLater( new Runnable() {
                                            public void run() {
                                                ((HostSystemUI) m_frame).lbl_numIntroductions.setText(Integer.toString(m_personConnected));
                                            }
                                        } );
                                	} 
                                    
                                    else if (JOINGROUP.equals( msg.getContent() )){
                                    	// a guest has leave
                                    	m_personInGroup++;
                                        System.out.println( "Un agent a rejoint un groupe" ); 
                                        SwingUtilities.invokeLater( new Runnable() {
                                            public void run() {
                                                ((HostSystemUI) m_frame).lbl_rumourAvg.setText(Integer.toString(m_personConnected - m_personInGroup));
                                            }
                                        } );
                                    }
                                    
                                    else if (JOINGROUP.equals( msg.getContent() )){
                                    	// a guest has leave
                                    	m_personInGroup--;
                                        System.out.println( "Un agent a quitt?un groupe" );   
                                        SwingUtilities.invokeLater( new Runnable() {
                                            public void run() {
                                                ((HostSystemUI) m_frame).lbl_rumourAvg.setText(Integer.toString(m_personConnected - m_personInGroup));
                                            }
                                        } );
                                    }
                                }
                                else {
                                    // if no message is arrived, block the behaviour
                                    block();
                                }
                            }
            			} );
        }
        catch (Exception e) {
            System.out.println( "Saw exception in HostAgent: " + e );
            e.printStackTrace();
        }

    }


    // Internal implementation methods
    //////////////////////////////////

    /**
     * Setup the UI, which means creating and showing the main frame.
     */
    private void setupUI() {
        m_frame = new HostSystemUI( this );

        m_frame.setSize( 400, 200 );
        m_frame.setLocation( 400, 400 );
        m_frame.setVisible( true );
        m_frame.validate();
    }


    /**
     * Invite a number of guests, as determined by the given parameter.  Clears old
     * state variables, then creates N guest agents.  A list of the agents is maintained,
     * so that the host can tell them all to leave at the end of the party.
     *
     * @param nGuests The number of guest agents to invite.
     */
    protected void inviteGuests( int nGuests ) {
        // remove any old state
        m_guestList.clear();
        m_guestCount = nGuests;
        m_partyOver = false;
        m_personInGroup = 0;
        m_personConnected = 0;
        ((HostSystemUI) m_frame).lbl_numIntroductions.setText( "0" );
        ((HostSystemUI) m_frame).lbl_rumourAvg.setText( "0.0" );

        // notice the start time
        m_startTime = System.currentTimeMillis();

        setPartyState( "Inviting guests" );

        PlatformController container = getContainerController(); // get a container controller for creating new agents
        // create N guest agents
        try {
            for (int i = 0;  i < nGuests;  i++) {
                // create a new agent
                String localName = "guest_"+i;

                int nombre = rnd.nextInt(100);        // création des differents caractères

                if(nombre <= 40) {
                    //création de l'agent Impatient
                    //AgentController guest = container.createNewAgent("Impatient-" + localName, "OverJADE.ImpatientAgent", null);
                    AgentController guest = container.createNewAgent(localName, "OverJADE.ImpatientAgent", null);
                    m_guestList.add(guest);
                    m_guestListAgent.put(guest, false);
                    m_guestListAID.add( new AID(localName, AID.ISLOCALNAME) );
                } else if (nombre <= 80) {
                	//AgentController guest = container.createNewAgent("Leader-" + localName, "OverJADE.LeaderAgent", null);
                    AgentController guest = container.createNewAgent(localName, "OverJADE.LeaderAgent", null);
                	m_guestList.add(guest);
                    m_guestListAgent.put(guest, false);
                    m_guestListAID.add( new AID(localName, AID.ISLOCALNAME) );
                } else {
                	//AgentController guest = container.createNewAgent("Friend-" + localName, "OverJADE.NeedsFriendsAgent", null);
                    AgentController guest = container.createNewAgent(localName, "OverJADE.NeedsFriendsAgent", null);
                	m_guestList.add(guest);
                    m_guestListAgent.put(guest, false);
                    m_guestListAID.add( new AID(localName, AID.ISLOCALNAME) );
                }
                       // keep the guest's ID on a local list
                
                }
            

            updateSystem();
        	}
        catch (Exception e) {
            System.err.println( "Exception while adding guests: " + e );
            e.printStackTrace();
        }
    }


    /**
     * End the party: set the state variables, and tell all the guests to leave.
     */
    protected void endParty() {
        setPartyState( "Party over" );
        m_partyOver = true;

        // log the duration of the run
        System.out.println( "Simulation run complete. NGuests = " + m_guestCount + ", time taken = " +
                            m_avgFormat.format( ((double) System.currentTimeMillis() - m_startTime) / 1000.0 ) + "s" );

        // send a message to all guests to tell them to leave
        for (Iterator i = m_guestList.iterator();  i.hasNext();  ) {
            ACLMessage msg = new ACLMessage( ACLMessage.INFORM );
            msg.setContent( GOODBYE );

            msg.addReceiver( (AID) i.next() );

            send(msg);
        }

        m_guestList.clear();
        m_guestListAgent.clear();
    }


    /**
     * Shut down the host agent, including removing the UI and deregistering
     * from the DF.
     */
    protected void terminateHost() {
        try {
            if (!m_guestList.isEmpty()) {
                endParty();
            }

            DFService.deregister( this );
            m_frame.dispose();
            doDelete();
        }
        catch (Exception e) {
            System.err.println( "Saw FIPAException while terminating: " + e );
            e.printStackTrace();
        }
    }


    /**
    * Boucle d'activation des agents.
    */
    protected void updateSystem() {
    	
    	try {
    		
        while(!m_partyOver) {
        	//s?ection du nb de gens ?inviter en m?e temps 
        	
        	int nombre = rnd.nextInt(20) + 10;
        	
        	for(int i = 0; i < nombre; i++) {
        		
        		index ++;
        		if(index > m_guestCount - 1) index = 0;
        		
        		AgentController guest = m_guestList.get(index);
        		
        		if( m_guestListAgent.get(guest) == false )
        		{	//agent pas actif
        			guest.start();
        			m_guestListAgent.put(guest,true);
        			
        		} else {     //actif deja actif
        			nombre++;
        		}
        	}
        	
        	TimeUnit.SECONDS.sleep(timeCreationNewAgents);
        	
        }
          

        endParty();
			
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (StaleProxyException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

    }
    
    
    
}


