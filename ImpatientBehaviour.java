package OverJADE;


import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

import jade.core.behaviours.CyclicBehaviour;
import jade.core.Agent;

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
                requestToJoin.SetContent (myAgent.preferedRole.toString());
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
            ACLMessage.GetContent() == myAgent.preferedRole.toString()){//on accepte l offre
                ACLMessage acceptOffer = answerFromLeader.createReply();
                acceptOffer.setPerformative(ACLMessage.ACCEPT_PROPOSAL);
                myAgent.send(acceptOffer);
            }
            else if (answerFromLeader.getPerformative() == ACLMessage.CONFIRM){
                inParty = true;
            }
        }
    }

	/*
	public boolean done() {
		return true;
	}*/
}
