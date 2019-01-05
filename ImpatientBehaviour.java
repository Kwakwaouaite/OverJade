package OverJADE;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.*;
import jade.lang.acl.ACLMessage;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;

public class ImpatientBehaviour extends CyclicBehaviour  {

    public PlayerAgent myAgent;

    public boolean inParty = false;

    public AID[] potentialLeaders;
    public AID myLeader = null;

    public ImpatientBehaviour(PlayerAgent agent) {
        myAgent = agent;
    }

    void RejectProposal(ACLMessage msg) {
        ACLMessage refuseOffer = msg.createReply();
        refuseOffer.setPerformative(ACLMessage.REJECT_PROPOSAL);
        refuseOffer.setContent(myAgent.preferedRole.toString());
        myAgent.send(refuseOffer);
        System.out.println( "Impatient " + getAgent().getName().toString() + "received PROPOSE " + msg
                + " and refuse, expect " + myAgent.preferedRole.toString());
    }

    public void action() {

        // connexion aux pages jaunes
        DFAgentDescription template = new DFAgentDescription();
        ServiceDescription sd = new ServiceDescription();
        sd.setType("party-finding");
        template.addServices(sd);

        if (!inParty) {
            try {
                DFAgentDescription[] result = DFService.search(myAgent, template);
                potentialLeaders = new AID[result.length];
                int chosenLeader = (int) Math.round(Math.random() * (result.length - 1));

                ACLMessage requestToJoin = new ACLMessage(ACLMessage.REQUEST);
                requestToJoin.setContent(myAgent.preferedRole.toString());
                requestToJoin.addReceiver(result[chosenLeader].getName());
                myAgent.send(requestToJoin);
            } catch (FIPAException fe) {
                fe.printStackTrace();
            }
        }

        ACLMessage incomingMsg = myAgent.receive();
        if (incomingMsg != null && !inParty) {
            if (incomingMsg.getPerformative() == ACLMessage.PROPOSE &&
            incomingMsg.getContent().equals(myAgent.preferedRole.toString())){//on accepte l offre
                ACLMessage acceptOffer = incomingMsg.createReply();
                acceptOffer.setPerformative(ACLMessage.ACCEPT_PROPOSAL);
                acceptOffer.setContent (myAgent.preferedRole.toString());
                myAgent.send(acceptOffer);
                System.out.println( "Impatient " + getAgent().getName().toString() + "received PROPOSE " + incomingMsg + " and accept");
            }

            else if (incomingMsg.getPerformative() == ACLMessage.PROPOSE &&
                    !incomingMsg.getContent().equals(myAgent.preferedRole.toString())){
                RejectProposal(incomingMsg);
            }

            else if (incomingMsg.getPerformative() == ACLMessage.CONFIRM){
                inParty = true;
                myLeader = incomingMsg.getSender();

                ACLMessage msgToHost = new ACLMessage(ACLMessage.INFORM);
                msgToHost.setContent("JOINGROUP");
                msgToHost.addReceiver(myAgent.hostAgent);
                myAgent.send(msgToHost);

                System.out.println( "Impatient " + getAgent().getName().toString() + "received COMFIRM " + incomingMsg);
            }

            else if (incomingMsg.getPerformative() == ACLMessage.REQUEST) {
                // no answer since not in party
            }

            else {
                System.out.println( "Impatient " + getAgent().getName().toString() + "received unexpected message: " + incomingMsg);
            }

        }
        //Si on est deja dans un groupe
        else if (incomingMsg != null && inParty) {
            if (incomingMsg.getPerformative() == ACLMessage.PROPOSE) {
                RejectProposal(incomingMsg);

            }
            else if (incomingMsg.getPerformative() == ACLMessage.REQUEST) {
                if (myLeader != null) {
                    ACLMessage answer = incomingMsg.createReply();
                    answer.setContent(myLeader.AGENT_CLASSNAME);
                    myAgent.send(answer);
                }
            }
            else if (incomingMsg.getPerformative() == ACLMessage.INFORM &&
            incomingMsg.getContent().equals("GOODBYE")) { //grp complet
                ACLMessage msgToHost = new ACLMessage(ACLMessage.INFORM);
                msgToHost.setContent("GOODBYE");
                msgToHost.addReceiver(myAgent.hostAgent);
                myAgent.send(msgToHost);


            }
        }
    }

	/*
	public boolean done() {
		return true;
	}*/
}
