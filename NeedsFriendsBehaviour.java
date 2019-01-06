package OverJADE;

import jade.core.AID;
import jade.core.behaviours.CyclicBehaviour;

import java.util.ArrayList;
import java.util.Iterator;


import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;

public class NeedsFriendsBehaviour extends CyclicBehaviour  {

	public PlayerAgent myAgent;
    public AID[] myfriends;

    public AID[] friendsLeader;

	public boolean inGroup;
    public AID myLeader; 

    public NeedsFriendsBehaviour(PlayerAgent agent) {

        myAgent = agent;

        friendsLeader = new AID[myAgent.friends.size()];
        myfriends = new AID[myAgent.friends.size()];

        //remplissage des arrays AID[] utilisés pour la fonction MatchReceiver
        for (int i = 0; i < myAgent.friends.size(); ++i) {
            myfriends[i] = myAgent.friends.get(i);
        }

        for (int i = 0; i < friendsLeader.length; ++i) {
            friendsLeader[i] = new AID();
        }
    }

    public void SendRequestToFriend(AID friendAID) {
    	// Envoie une requete à un ami pour demander un role
        ACLMessage requestToJoin = new ACLMessage (ACLMessage.REQUEST);
        requestToJoin.setContent(myAgent.preferedRole.toString());
        requestToJoin.addReceiver(friendAID);
        myAgent.send(requestToJoin);       
    }
    
    public void SendLeaderAID(ACLMessage message) {
    	// Envoi une réponse INFORM pour signaler que le message suivanvt sera pour le leader de l'ami
        ACLMessage answer = message.createReply();
        answer.addReplyTo(myLeader);
        answer.setPerformative(ACLMessage.INFORM);
        answer.setContent("You'll reply to my leader");
        myAgent.send(answer);       
    }
    
    public void AcceptProposal(ACLMessage message, Group.Role roleAsked) {
    	// Accepte l'offre proposée
        ACLMessage acceptOffer = message.createReply();
        acceptOffer.setPerformative(ACLMessage.ACCEPT_PROPOSAL);
        acceptOffer.setContent(roleAsked.toString());
        myAgent.send(acceptOffer);
    }

    public void RefuseOffer(ACLMessage message) {
    	// Refuse l'offre proposée
        ACLMessage refuseOffer = message.createReply();
        refuseOffer.setPerformative(ACLMessage.REJECT_PROPOSAL);
        myAgent.send(refuseOffer);
    }

    public void AnswerByAskingPreferedJob(ACLMessage message) {
    	// Répond en redemandant le role préféré
        ACLMessage refuseOffer = message.createReply();
        refuseOffer.setPerformative(ACLMessage.REQUEST);
        refuseOffer.setContent(myAgent.preferedRole.toString());
        myAgent.send(refuseOffer);
    }

    public void AddFriendsLeaderToList(ACLMessage messageReceivedFromFriendWithReplyToLeader){
    	// Ajoute l'AID du leader à la liste

    	// cherche d'abord la position de l'ami dans la liste
        int found = 0;
        for (int i = 0 ; i < myAgent.friends.size(); i++) {
            if (myfriends[i] == messageReceivedFromFriendWithReplyToLeader.getSender()) {
                found = i;
                i = myAgent.friends.size();
            }
        }

        // assigne ensuite l'AID du leader à la même case que l'ami
        for(Iterator iterator = messageReceivedFromFriendWithReplyToLeader.getAllReceiver(); iterator.hasNext();){
            friendsLeader[found] = (AID) iterator.next();
        }
    }
                    
    public void action() {

    	// Envoie une invitation aux amis
        for (int i = 0; i < myAgent.friends.size(); ++i) {
            SendRequestToFriend(myAgent.friends.get(i));
        }

        // Crée un filtre pour lire d'abord les messages des amis et des leaders des amis
        MessageTemplate temp = MessageTemplate.MatchReceiver(myfriends);
        MessageTemplate temp2 = MessageTemplate.MatchReceiver(friendsLeader);
        MessageTemplate tempFinal = MessageTemplate.or(temp, temp2);

        // Réception des messages
        ACLMessage messageFriendly = myAgent.receive(tempFinal);
        // si on n'est pas dans un groupe
        if (messageFriendly != null && !inGroup) {
        	// s'il reçoit une proposition d'un amis/ leader d'un ami, il accepte, peu importe le poste et s econsidère dans le groupe
            if (messageFriendly.getPerformative() == ACLMessage.PROPOSE) {
                AcceptProposal(messageFriendly, Group.Role.valueOf(messageFriendly.getContent()));
                inGroup = true;
            }
            // s'il reçoit un CONFIRM, il enregistre le leader
            else if (messageFriendly.getPerformative() == ACLMessage.CONFIRM) {
                myLeader = messageFriendly.getSender();
            }
            // s'il reçoit un INFORM, il enregistre l'AID du leader de son ami afin de recevoir ses messages et lui demande son poste préféré
            else if (messageFriendly.getPerformative() == ACLMessage.INFORM) {
                AddFriendsLeaderToList(messageFriendly);
                AnswerByAskingPreferedJob(messageFriendly);
            }
            // sinon, il renvoie une erreur
            else {
                System.out.println( "Friendly received unexpected message: " + messageFriendly );
            }
        }
        // si on est dans un groupe
        else if (messageFriendly != null && inGroup) 
        	// s'il reçoit une requete et qu'il est déjà dans un groupe, il renvoir l'AID de son leader à son ami
            if (messageFriendly.getPerformative() == ACLMessage.REQUEST) {
                SendLeaderAID(messageFriendly);
            }
            // s'il reçoit un DISCONFIRM, il se remet en quete de job
            else if (messageFriendly.getPerformative() == ACLMessage.DISCONFIRM) {
                inGroup = false;
                AnswerByAskingPreferedJob(messageFriendly);
            }
            else {
            	// sinon, il refuse tout offre
                RefuseOffer(messageFriendly);
            }
        }
        else {
        	// on check ensuite les messages des inconnus, une fois ceux des amis triés
            ACLMessage messageFromInconnu = myAgent.receive();
            
            // si on n'est pas dans un groupe
            if (messageFromInconnu != null && !inGroup) {
                
                boolean offerPreferedJob = messageFromInconnu.getPerformative() == ACLMessage.PROPOSE && messageFromInconnu.getContent().equals(("\"" + myAgent.preferedRole.toString() + "\""));
                boolean offerNotPreferedJob = messageFromInconnu.getPerformative() == ACLMessage.PROPOSE && messageFromInconnu.getContent() != ("\"" + myAgent.preferedRole.toString() + "\"" );
                
                //on accepte l offre s'il s'agit de notre role préféré
                if (offerPreferedJob){
                    AcceptProposal(messageFromInconnu, myAgent.preferedRole);
                }
                // on refuse s'il ne s'agit pas de notre offre favorite en redemandant notre job préféré
                else if (offerNotPreferedJob){
                    RefuseOffer(messageFromInconnu);
                }
                // si on reçoit une confimation, on se considère dans un groupe et on enregistre notre leader
                else if (messageFromInconnu.getPerformative() == ACLMessage.CONFIRM){
                    inGroup = true;
                    myLeader = messageFromInconnu.getSender();
                }
                // sinon, il s'agit d'une erreure
                else {
                    System.out.println( "Friendly received unexpected message: " + messageFromInconnu );
                }

            }
            // si on est dans un groupe
            else if (messageFromInconnu != null && inGroup) {
            	// si on reçoit une requete, on renvoie à notre leader
                if (messageFromInconnu.getPerformative() == ACLMessage.REQUEST) {
                    SendLeaderAID(messageFromInconnu);
                }
                // si on reçoit un DISCONFIRM, on repart en quete de job
                else if (messageFromInconnu.getPerformative() == ACLMessage.DISCONFIRM) {
                    inGroup = false;
                    AnswerByAskingPreferedJob(messageFromInconnu);
                }
                // sinon on refuse
                else {
                    RefuseOffer(messageFromInconnu);
                }
            }
            // si le message est vide, on bloque le flux
            else {
                block();
            }
        }
    }
}