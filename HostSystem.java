// Package
///////////////
package examples.alice;

// Imports
///////////////
import jade.core.AID;
import jade.core.Agent;
import jade.core.ProfileImpl;
import jade.core.Profile;

import jade.wrapper.PlatformController;
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

    // Instance variables
    //////////////////////////////////
    protected JFrame m_frame = null;
    protected Vector m_guestList = new Vector();    // invitees
    protected int m_guestCount = 0;                 // arrivals
    protected int m_personInGroup = 0;
    protected int m_personConnected = 0;

    protected int m_introductionCount = 0;
    protected boolean m_partyOver = false;
    protected NumberFormat m_avgFormat = NumberFormat.getInstance();
    protected long m_startTime = 0L;

    Random rnd = new Random();


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
        m_guestCount = 0;
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

                int nombre = rnd.nextInt(3);        // création des differents caractères

                if(nombre == 0) {
                    //création de l'agent
                    //AgentController guest = container.createNewAgent(localName, "examples.party.GuestAgent", null);
                }
                       // keep the guest's ID on a local list
                m_guestList.add( new AID(localName, AID.ISLOCALNAME) );
            }
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

}


