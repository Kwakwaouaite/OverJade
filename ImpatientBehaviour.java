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

    //fonction qui refuse l'offre proposee dans le message
    void RejectProposal(ACLMessage msg) {
        ACLMessage refuseOffer = msg.createReply();
        refuseOffer.setPerformative(ACLMessage.REJECT_PROPOSAL);
        refuseOffer.setContent(myAgent.preferedRole.toString());
        myAgent.send(refuseOffer);
        //System.out.println( "Impatient " + getAgent().getName().toString() + "received PROPOSE " + msg
        //        + " and refuse, expect " + myAgent.preferedRole.toString());
    }

    public void action() {

        // connexion aux pages jaunes
        DFAgentDescription template = new DFAgentDescription();
        ServiceDescription sd = new ServiceDescription();
        sd.setType("party-finding");
        template.addServices(sd);
        //recherche dans les pages jaunes des leaders, et envoi d'un message à l'un d'entre eux, pour de mander à rejoindre
        //son groupe, uniquement si on en a pas deja un
        if (!inParty) {
            try {
                DFAgentDescription[] result = DFService.search(myAgent, template);
                potentialLeaders = new AID[result.length];
                if (result.length > 0) {
                    int chosenLeader = (int) Math.round(Math.random() * (result.length - 1));

                    ACLMessage requestToJoin = new ACLMessage(ACLMessage.REQUEST);
                    requestToJoin.setContent(myAgent.preferedRole.toString());
                    requestToJoin.addReceiver(result[chosenLeader].getName());
                    myAgent.send(requestToJoin);
                }
            } catch (FIPAException fe) {
                fe.printStackTrace();
            }
        }

        //phase de reponse aux messages recus
        ACLMessage incomingMsg = myAgent.receive();

        //cas ou l'on est pas dans un groupe
        if (incomingMsg != null && !inParty) {
            //si c est une proposition du role que l on veut, on accepte l offre
            if (incomingMsg.getPerformative() == ACLMessage.PROPOSE &&
            incomingMsg.getContent().equals(myAgent.preferedRole.toString())){//on accepte l offre
                ACLMessage acceptOffer = incomingMsg.createReply();
                acceptOffer.setPerformative(ACLMessage.ACCEPT_PROPOSAL);
                acceptOffer.setContent (myAgent.preferedRole.toString());
                myAgent.send(acceptOffer);
                //System.out.println( "Impatient " + getAgent().getName().toString() + "received PROPOSE " + incomingMsg + " and accept");
            }
            //si c est une proposition d'un autre role, on refuse
            else if (incomingMsg.getPerformative() == ACLMessage.PROPOSE &&
                    !incomingMsg.getContent().equals(myAgent.preferedRole.toString())){
                RejectProposal(incomingMsg);
            }
            //si on nous confirme la presence dans un groupe, on se considere comme etant dans ce groupe, et on enregistre le leader
            else if (incomingMsg.getPerformative() == ACLMessage.CONFIRM){
                inParty = true;
                myLeader = incomingMsg.getSender();

                ACLMessage msgToHost = new ACLMessage(ACLMessage.INFORM);
                msgToHost.setContent("JOINGROUP");
                msgToHost.addReceiver(myAgent.hostAgent);
                myAgent.send(msgToHost);

                //System.out.println( "Impatient " + getAgent().getName().toString() + "received COMFIRM " + incomingMsg);
            }
            //si on nous envoi une requete, on ne repond pas car nous ne sommes pas encore dans un groupe, on ne peut donc pas
            //indiquer un leader a notre ami
            else if (incomingMsg.getPerformative() == ACLMessage.REQUEST) {
                // no answer since not in party
            }
            //sinon message non gere
            else {
                //System.out.println( "Impatient " + getAgent().getName().toString() + "received unexpected message: " + incomingMsg);
            }

        }
        //Si on est deja dans un groupe
        else if (incomingMsg != null && inParty) {
            //si l'on recoit une proposition, on la refuse
            if (incomingMsg.getPerformative() == ACLMessage.PROPOSE) {
                RejectProposal(incomingMsg);
            }
            //si l'on recoit une request, on envoi a notre ami le nom du leader de notre groupe
            else if (incomingMsg.getPerformative() == ACLMessage.REQUEST) {
                if (myLeader != null) {
                    ACLMessage answer = incomingMsg.createReply();
                    answer.setContent(myLeader.AGENT_CLASSNAME);
                    myAgent.send(answer);
                }
            }

            //si l'on nous informe que le groupe est complet, on le signale a l'host
            else if (incomingMsg.getPerformative() == ACLMessage.INFORM &&
            incomingMsg.getContent().equals("GOODBYE")) { //grp complet
                ACLMessage msgToHost = new ACLMessage(ACLMessage.INFORM);
                msgToHost.setContent("GOODBYE");
                msgToHost.addReceiver(myAgent.hostAgent);
                myAgent.send(msgToHost);


            }
        }
        else {
            block();
        }
    }

	/*
	public boolean done() {
		return true;
	}*/
}
