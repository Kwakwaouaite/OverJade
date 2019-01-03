// Package
///////////////
package examples.alice;


// Imports
///////////////
import java.awt.*;
import javax.swing.*;
import java.beans.*;
import javax.swing.event.*;
import java.awt.event.*;

import jade.core.behaviours.OneShotBehaviour;

public class HostSystemUI
    extends JFrame
{

    // Instance variables
    //////////////////////////////////

    BorderLayout borderLayout1 = new BorderLayout();
    JPanel pnl_main = new JPanel();
    JButton btn_Exit = new JButton();
    Component component3;
    JButton btn_stop = new JButton();
    Component component2;
    JButton btn_start = new JButton();
    Box box_buttons;
    JPanel pnl_numGuests = new JPanel();
    BorderLayout borderLayout3 = new BorderLayout();
    JLabel lbl_numGuests = new JLabel();
    Box box_numGuests;
    JLabel lbl_guestCount = new JLabel();
    JSlider slide_numGuests = new JSlider();
    Component component1;
    Component component4;
    GridLayout gridLayout1 = new GridLayout();
    JLabel jLabel1 = new JLabel();
    JLabel jLabel2 = new JLabel();
    JLabel lbl_numIntroductions = new JLabel();
    JLabel jLabel4 = new JLabel();
    JLabel lbl_partyState = new JLabel();
    Box box1;
    Component component6;
    Component component5;
    JLabel lbl_rumourAvg = new JLabel();


    protected HostSystem m_owner;


    // Constructors
    //////////////////////////////////

    public HostSystemUI( HostSystem owner ) {
        try {
            jbInit();
        }
        catch(Exception e) {
            e.printStackTrace();
        }

        m_owner = owner;
    }

    /**
     * Setup the UI. This code generated by JBuilder designer.
     */
    private void jbInit() throws Exception {
        component3 = Box.createHorizontalStrut(10);
        component2 = Box.createHorizontalStrut(5);
        box_buttons = Box.createHorizontalBox();

        box_numGuests = Box.createHorizontalBox();
        component1 = Box.createGlue();
        component4 = Box.createHorizontalStrut(5);
        box1 = Box.createVerticalBox();
        component6 = Box.createGlue();
        component5 = Box.createGlue();
        this.getContentPane().setLayout(borderLayout1);
        pnl_main.setLayout(gridLayout1);
        btn_Exit.setText("Exit");
        btn_Exit.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(ActionEvent e) {
                btn_Exit_actionPerformed(e);
            }
        });
        btn_stop.setEnabled(false);
        btn_stop.setText("Stop");
        btn_stop.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(ActionEvent e) {
                btn_stop_actionPerformed(e);
            }
        });
        btn_start.setText("Start");
        btn_start.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(ActionEvent e) {
                btn_start_actionPerformed(e);
            }
        });
        this.setTitle("Nb de clients");
        this.addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                this_windowClosing(e);
            }
        });
        pnl_numGuests.setLayout(borderLayout3);
        lbl_numGuests.setText("Nb clients total");
        lbl_guestCount.setMaximumSize(new Dimension(30, 17));
        lbl_guestCount.setMinimumSize(new Dimension(30, 17));
        lbl_guestCount.setPreferredSize(new Dimension(30, 17));
        lbl_guestCount.setText("100");
        slide_numGuests.setValue(100);
        slide_numGuests.setMinimum(200);
        slide_numGuests.setMaximum(2000);
        slide_numGuests.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                slide_numGuests_stateChanged(e);
            }
        });
        gridLayout1.setRows(3);
        gridLayout1.setColumns(2);
        jLabel1.setToolTipText("");
        jLabel1.setHorizontalAlignment(SwingConstants.RIGHT);
        jLabel1.setText("App State: ");
        jLabel2.setHorizontalAlignment(SwingConstants.RIGHT);
        jLabel2.setText("Nb connected: ");
        lbl_numIntroductions.setBackground(Color.white);
        lbl_numIntroductions.setText("0");
        jLabel4.setToolTipText("");
        jLabel4.setHorizontalAlignment(SwingConstants.RIGHT);
        jLabel4.setText("Nb without group: ");
        lbl_partyState.setBackground(Color.white);
        lbl_partyState.setText("Not started");
        lbl_rumourAvg.setToolTipText("");
        lbl_rumourAvg.setText("0.0");
        this.getContentPane().add(pnl_main, BorderLayout.CENTER);
        pnl_main.add(jLabel1, null);
        pnl_main.add(lbl_partyState, null);
        pnl_main.add(jLabel2, null);
        pnl_main.add(lbl_numIntroductions, null);
        pnl_main.add(jLabel4, null);
        pnl_main.add(lbl_rumourAvg, null);
        this.getContentPane().add(pnl_numGuests, BorderLayout.NORTH);
        pnl_numGuests.add(box_numGuests, BorderLayout.CENTER);
        pnl_numGuests.setBorder( BorderFactory.createCompoundBorder( BorderFactory.createEtchedBorder(), BorderFactory.createEmptyBorder( 2, 2, 2, 2 ) ) );

        box_numGuests.add(lbl_numGuests, null);
        box_numGuests.add(slide_numGuests, null);
        box_numGuests.add(lbl_guestCount, null);
        this.getContentPane().add(box_buttons, BorderLayout.SOUTH);
        box_buttons.add(component2, null);
        box_buttons.add(btn_start, null);
        box_buttons.add(component3, null);
        box_buttons.add(btn_stop, null);
        box_buttons.add(component1, null);
        box_buttons.add(btn_Exit, null);
        box_buttons.add(component4, null);
        lbl_partyState.setForeground( Color.black );
        lbl_numIntroductions.setForeground( Color.black );
        lbl_rumourAvg.setForeground( Color.black );
    }


    /**
     * When the slider for the num guests changes, we update the label.
     */
    void slide_numGuests_stateChanged(ChangeEvent e) {
        lbl_guestCount.setText( Integer.toString( slide_numGuests.getValue() ) );
    }


    /**
     * When the user clicks on start, notify the host to begin the party.
     */
    void btn_start_actionPerformed(ActionEvent e) {
        enableControls( true );

        // add a behaviour to the host to start the conversation going
        m_owner.addBehaviour( new OneShotBehaviour() {
                                  public void action() {
                                      ((HostSystem) myAgent).inviteGuests( slide_numGuests.getValue() );
                                  }
                              } );
    }


    /**
     * When the user clicks on stop, tell the host to stop the party.
     */
    void btn_stop_actionPerformed(ActionEvent e) {
        enableControls( false );

        // add a behaviour to the host to end the party
        m_owner.addBehaviour( new OneShotBehaviour() {
                                  public void action() {
                                      ((HostSystem) myAgent).endParty();
                                  }
                              } );
    }


    /**
     * Maintains the enbabled/disabled state of key controls, depending
     * on whether the sim is running or stopped.
     */
    void enableControls( boolean starting ) {
        btn_start.setEnabled( !starting );
        btn_stop.setEnabled( starting );
        slide_numGuests.setEnabled( !starting );
        btn_Exit.setEnabled( !starting );
    }


    /**
     * When the user clicks the exit button, tell the host to shut down.
     */
    void btn_Exit_actionPerformed(ActionEvent e) {
        m_owner.addBehaviour( new OneShotBehaviour() {
                                  public void action() {
                                      ((HostSystem) myAgent).terminateHost();
                                  }
                              } );
    }


    /**
     * The window closing event is the same as clicking exit.
     */
    void this_windowClosing(WindowEvent e) {
        // simulate the user having clicked exit
        btn_Exit_actionPerformed( null );
    }

}


