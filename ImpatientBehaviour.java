package OverJADE;

import jade.core.AID;
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

    public ImpatientBehaviour(PlayerAgent agent) {
        myAgent = agent;
    }

    public void action() {

        if (inParty) {
            return;
        }
        // connexion aux pages jaunes
        DFAgentDescription template = new DFAgentDescription();
        ServiceDescription sd = new ServiceDescription();
        sd.setType("party-finding");
        template.addServices(sd);


        try {
            DFAgentDescription[] result = DFService.search(myAgent, template);
            potentialLeaders = new AID[result.length];
            for (int i = 0; i < result.length; ++i) {
                //sellerAgents[i] = result[i].getName();
                ACLMessage requestToJoin = new ACLMessage (ACLMessage.REQUEST);
                requestToJoin.setContent (myAgent.preferedRole.toString());
                requestToJoin.addReceiver(result[i].getName());
                myAgent.send(requestToJoin);
            }
        }
        catch (FIPAException fe) {
            fe.printStackTrace();
        }

        ACLMessage answerFromLeader = myAgent.receive();
        if (answerFromLeader != null && !inParty) {
            if (answerFromLeader.getPerformative() == ACLMessage.PROPOSE &&
            answerFromLeader.getContent() == myAgent.preferedRole.toString()){//on accepte l offre
                ACLMessage acceptOffer = answerFromLeader.createReply();
                acceptOffer.setPerformative(ACLMessage.ACCEPT_PROPOSAL);
                myAgent.send(acceptOffer);
            }
            else if (answerFromLeader.getPerformative() == ACLMessage.CONFIRM){
                inParty = true;
            }
            else {
                System.out.println( "Leader received unexpected message: " + answerFromLeader );
            }
        }
        else if (answerFromLeader != null && inParty) {
            ACLMessage refuseOffer = answerFromLeader.createReply();
            refuseOffer.setPerformative(ACLMessage.REJECT_PROPOSAL);
            myAgent.send(refuseOffer);
        }
    }

	/*
	public boolean done() {
		return true;
	}*/
}
