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

    public NeedsFriendsBehaviour(PlayerAgent agent) {
        friendsLeader = new AID[myAgent.friends.size()];
        myfriends = new AID[myAgent.friends.size()];

        myAgent = agent;

        for (int i = 0; i < myAgent.friends.size(); ++i) {
            myfriends[i] = myAgent.friends.get(i);
        }
    }

    public void SendRequestToFriend(AID friendAID) {
        ACLMessage requestToJoin = new ACLMessage (ACLMessage.REQUEST);
        requestToJoin.setContent (myAgent.preferedRole.toString());
        requestToJoin.addReceiver(friendAID);
        myAgent.send(requestToJoin);       
    }

    /*
    public void SendLeaderAID(ACLMessage message) {
        ACLMessage answer = message.createReply();
        answer.addReplyTo(myAgent.leader);
        answer.setPerformative(ACLMessage.INFORM);
        answer.setContent();
        myAgent.send(answer);       
    }
    */

    public void AcceptProposal(ACLMessage message) {
        ACLMessage acceptOffer = message.createReply();
        acceptOffer.setPerformative(ACLMessage.ACCEPT_PROPOSAL);
        myAgent.send(acceptOffer);
    }

    public void RefuseOffer(ACLMessage message) {
        ACLMessage refuseOffer = message.createReply();
        refuseOffer.setPerformative(ACLMessage.REJECT_PROPOSAL);
        myAgent.send(refuseOffer);
    }

    public void AnswerOfferByAskingAnotherJob(ACLMessage message) {
        ACLMessage refuseOffer = message.createReply();
        refuseOffer.setPerformative(ACLMessage.REQUEST);
        refuseOffer.setContent(myAgent.preferedRole.toString());
        myAgent.send(refuseOffer);
    }

    public void SendMessageToFriendsLeader(ACLMessage message) {
        ACLMessage request = message.createReply();
        request.setPerformative(ACLMessage.REQUEST);
        request.setContent (myAgent.preferedRole.toString());
        myAgent.send(request);
    }

    public void AddFriendsLeaderToList(ACLMessage messageReceivedFromFriendWithReplyToLeader){
        int found = 0;
        for (int i = 0 ; i < myAgent.friends.size(); i++) {
            if (myfriends[i] == messageReceivedFromFriendWithReplyToLeader.getSender()) {
                found = i;
                i = myAgent.friends.size();
            }
        }

        for(Iterator iterator = messageReceivedFromFriendWithReplyToLeader.getAllReceiver(); iterator.hasNext();){
            friendsLeader[found] = (AID) iterator.next();
        }
    }
                    

    public void action() {

    	//send request to friends
        for (int i = 0; i < myAgent.friends.size(); ++i) {
            SendRequestToFriend(myAgent.friends.get(i));
        }

        MessageTemplate temp = MessageTemplate.MatchReceiver(myfriends);
        
        //reception of messages
        ACLMessage messageFromFriends = myAgent.receive(temp);
        if (messageFromFriends != null && !inGroup) {

            boolean offerPreferedJob = messageFromFriends.getPerformative() == ACLMessage.PROPOSE && messageFromFriends.getContent().equals(("\"" + myAgent.preferedRole.toString() + "\""));
            boolean answerFromFriendNotPreferedJob = messageFromFriends.getPerformative() == ACLMessage.PROPOSE && messageFromFriends.getContent() != ("\"" + myAgent.preferedRole.toString() + "\"" );

            if (offerPreferedJob){//on accepte l offre
                AcceptProposal(messageFromFriends);
            }

            else if (messageFromFriends.getPerformative() == ACLMessage.CONFIRM){
                inGroup = true;
                myAgent.leader = messageFromFriends.getSender();
            }

            else if (answerFromFriendNotPreferedJob){
                AnswerOfferByAskingAnotherJob(messageFromFriends);
            }

            else if (messageFromFriends.getPerformative() == ACLMessage.INFORM) {
                AddFriendsLeaderToList(messageFromFriends);
                SendMessageToFriendsLeader(messageFromFriends);
            }
        }
        else if (messageFromFriends != null && inGroup) {
            RefuseOffer(messageFromFriends);
            //TODO : quitter le groupe si jamais pas d'amis dans le groupe actuel (ou si plus d'amis dans l'autre groupe)
        }
        else {
            MessageTemplate temp2 = MessageTemplate.MatchReceiver(friendsLeader);
            ACLMessage messageFromFriendsLeader = myAgent.receive(temp2);
            
            if (messageFromFriendsLeader != null && !inGroup) {
                boolean offerPreferedJob = messageFromFriendsLeader.getPerformative() == ACLMessage.PROPOSE && messageFromFriendsLeader.getContent().equals(("\"" + myAgent.preferedRole.toString() + "\""));
                boolean answerFromFriendNotPreferedJob = messageFromFriendsLeader.getPerformative() == ACLMessage.PROPOSE && messageFromFriendsLeader.getContent() != ("\"" + myAgent.preferedRole.toString() + "\"" );

                if (offerPreferedJob){//on accepte l offre
                    AcceptProposal(messageFromFriendsLeader);
                }

                else if (messageFromFriendsLeader.getPerformative() == ACLMessage.CONFIRM){
                    inGroup = true;
                    myAgent.leader = messageFromFriendsLeader.getSender();
                }

                else if (answerFromFriendNotPreferedJob){
                    AnswerOfferByAskingAnotherJob(messageFromFriendsLeader);
                }

                else if (messageFromFriendsLeader.getPerformative() == ACLMessage.INFORM) {
                    AddFriendsLeaderToList(messageFromFriendsLeader);
                    SendMessageToFriendsLeader(messageFromFriendsLeader);
                }
            }
            else if (messageFromFriendsLeader != null && inGroup) {
                RefuseOffer(messageFromFriendsLeader);
                //TODO : quitter le groupe si jamais pas d'amis dans le groupe actuel (ou si plus d'amis dans l'autre groupe)
            }
            else {
                //TODO : cas d'un message d'un inconnu
            }
        }
    }
}