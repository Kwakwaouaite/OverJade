package OverJADE;

import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

import jade.core.behaviours.CyclicBehaviour;


public class LeaderBehaviour extends CyclicBehaviour  {

	public Group myGroup;
	public PlayerAgent myAgent;


	public LeaderBehaviour (PlayerAgent agent){
		myAgent = agent;
		myGroup = new Group(myAgent.getAID());
		myAgent.joiningGroup();
	}

	public void action() {
		ACLMessage msg = myAgent.receive();

        if (msg != null) {
            if (msg.getPerformative() == ACLMessage.REQUEST) {
                ACLMessage reply = msg.createReply();
                reply.setPerformative(ACLMessage.PROPOSE);
                Group.Role toPropose = myGroup.tryJoinRole(Group.Role.valueOf(msg.getContent()));
                reply.setContent(toPropose.toString());
                myAgent.send(reply);
            }
            else if (msg.getPerformative() == ACLMessage.ACCEPT_PROPOSAL){

            	ACLMessage reply = msg.createReply();

            	if (!(myGroup.isIn(msg.getSender())) && myGroup.addPlayer(msg.getSender(), Group.Role.valueOf(msg.getContent()))) {
            		reply.setPerformative(ACLMessage.CONFIRM);
            		System.out.println(myAgent.getName() + ": Accepting new people ! wouhou ! We are " + myGroup.size());
            		if (myGroup.size() == 5){
            			System.out.println("We are full, let's go !");
            			myAgent.letsgo();
            		}
            	} else {
            		reply.setPerformative(ACLMessage.DISCONFIRM);
            	}
                myAgent.send(reply);

            }
            else if (msg.getPerformative() == ACLMessage.REJECT_PROPOSAL){

            }
            else if (msg.getPerformative() == ACLMessage.INFORM && msg.getContent().equals("QUIT")) {
            	myGroup.leave(msg.getSender());
            	System.out.println(msg.getSender() + " is quitting");
            }
            else{
                System.out.println( "Leader received unexpected message: " + msg );
            }
        }
        else {
            // if no message is arrived, block the behaviour
            block();
        }
	}
	
	/*
	public boolean done() {
		return true;
	}*/
}
