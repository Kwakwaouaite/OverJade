package OverJADE;

import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

public class ImpatientBehaviour extends CyclicBehaviour  {
    public enum Role {
        DPS,
        TANK,
        HEALER
    }

    public String nickname; // Attribut nom
    public Role preferedRole;
    public Role secondRole;
    public Role lastRole;

    public ArrayList<AID> friends;

    public boolean isActive;

    public int impatienceRate;

    public PlayerAgent myAgent;
    public Group myGroup;

    public boolean inParty = false;

    public AID[] potentialLeaders;

    public ImpatientBehaviour(PlayerAgent agent) {
        myAgent = agent;
        myGroup = null;
    }

    public void action() {

        if (inParty) {
            return;
        }
        // connexion aux pages jaunes
        DFAgentDescription template = new DFAgentDescription();
        ServiceDescription sd = new ServiceDescription();
        sd.setType('party-finding');
        template.addServices(sd);


        try {
            DFAgentDescription[] result = DFService.search(myAgent, template);
            potentialLeaders = new AID[result.length];
            for (int i = 0; i < result.length; ++i) {
                //sellerAgents[i] = result[i].getName();
                ACLMessage requestToJoin = new ACLMessage (ACLMessage.REQUEST)
                requestToJoin.SetContent (preferedRole.toString());
                requestToJoin.addReceiver(result[i].getName());
            }
        }
        catch (FIPAException fe) {
            fe.printStackTrace();
        }

        ACLMessage answerFromLeader = myAgent.receive();
        if (answerFromLeader != null) {
            if (answerFromLeader.getPerformative() == ACLMessage.PROPOSE){

            }
        }
    }

	/*
	public boolean done() {
		return true;
	}*/
}
