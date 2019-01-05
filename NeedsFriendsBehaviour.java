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

        for (int i = 0; i < myAgent.friends.size(); ++i) {
            myfriends[i] = myAgent.friends.get(i);
        }

        for (int i = 0; i < friendsLeader.length; ++i) {
            friendsLeader[i] = new AID();
        }
    }

    public void SendRequestToFriend(AID friendAID) {
        ACLMessage requestToJoin = new ACLMessage (ACLMessage.REQUEST);
        requestToJoin.setContent(myAgent.preferedRole.toString());
        requestToJoin.addReceiver(friendAID);
        myAgent.send(requestToJoin);       
    }
    
    public void SendLeaderAID(ACLMessage message) {
        ACLMessage answer = message.createReply();
        answer.addReplyTo(myLeader);
        answer.setPerformative(ACLMessage.INFORM);
        answer.setContent("You'll reply to my leader");
        myAgent.send(answer);       
    }
    
    public void AcceptProposal(ACLMessage message, Group.Role roleAsked) {
        ACLMessage acceptOffer = message.createReply();
        acceptOffer.setPerformative(ACLMessage.ACCEPT_PROPOSAL);
        acceptOffer.setContent(roleAsked.toString());
        myAgent.send(acceptOffer);
    }

    public void RefuseOffer(ACLMessage message) {
        ACLMessage refuseOffer = message.createReply();
        refuseOffer.setPerformative(ACLMessage.REJECT_PROPOSAL);
        myAgent.send(refuseOffer);
    }

    public void AnswerByAskingPreferedJob(ACLMessage message) {
        ACLMessage refuseOffer = message.createReply();
        refuseOffer.setPerformative(ACLMessage.REQUEST);
        refuseOffer.setContent(myAgent.preferedRole.toString());
        myAgent.send(refuseOffer);
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
        MessageTemplate temp2 = MessageTemplate.MatchReceiver(friendsLeader);

        MessageTemplate tempFinal = MessageTemplate.or(temp, temp2);

        //reception of messages
        ACLMessage messageFriendly = myAgent.receive(tempFinal);
        if (messageFriendly != null && !inGroup) {

            if (messageFriendly.getPerformative() == ACLMessage.PROPOSE) {
                AcceptProposal(messageFriendly, Group.Role.valueOf(messageFriendly.getContent()));
                inGroup = true;
            }

            else if (messageFriendly.getPerformative() == ACLMessage.CONFIRM) {
                myLeader = messageFriendly.getSender();
            }

            else if (messageFriendly.getPerformative() == ACLMessage.INFORM) {
                AddFriendsLeaderToList(messageFriendly);
                AnswerByAskingPreferedJob(messageFriendly);
            }
            else {
                System.out.println( "Friendly received unexpected message: " + messageFriendly );
            }
        }
        else if (messageFriendly != null && inGroup) {
            if (messageFriendly.getPerformative() == ACLMessage.REQUEST) {
                SendLeaderAID(messageFriendly);
            }
            else if (messageFriendly.getPerformative() == ACLMessage.DISCONFIRM) {
                inGroup = false;
                AnswerByAskingPreferedJob(messageFriendly);
            }
            else {
                RefuseOffer(messageFriendly);
            }
        }
        else {
            ACLMessage messageFromInconnu = myAgent.receive();
            
            if (messageFromInconnu != null && !inGroup) {
                
                boolean offerPreferedJob = messageFromInconnu.getPerformative() == ACLMessage.PROPOSE && messageFromInconnu.getContent().equals(("\"" + myAgent.preferedRole.toString() + "\""));
                boolean offerNotPreferedJob = messageFromInconnu.getPerformative() == ACLMessage.PROPOSE && messageFromInconnu.getContent() != ("\"" + myAgent.preferedRole.toString() + "\"" );
                
                if (offerPreferedJob){//on accepte l offre
                    AcceptProposal(messageFromInconnu, myAgent.preferedRole);
                }

                else if (offerNotPreferedJob){
                    RefuseOffer(messageFromInconnu);
                }

                else if (messageFromInconnu.getPerformative() == ACLMessage.CONFIRM){
                    inGroup = true;
                    myLeader = messageFromInconnu.getSender();
                }
                else if (messageFromInconnu.getPerformative() == ACLMessage.REQUEST){
                    // Malheureusement on a pas de groupe
                }

                else {


                    System.out.println( "Friendly received unexpected message: " + messageFromInconnu );
                }

            }
            else if (messageFromInconnu != null && inGroup) {
                if (messageFromInconnu.getPerformative() == ACLMessage.REQUEST) {
                    SendLeaderAID(messageFromInconnu);
                }
                else if (messageFromInconnu.getPerformative() == ACLMessage.DISCONFIRM) {
                    inGroup = false;
                    AnswerByAskingPreferedJob(messageFromInconnu);
                }
                else {
                    RefuseOffer(messageFromInconnu);
                }
            }
            else {
                block();
            }
        }
    }
}