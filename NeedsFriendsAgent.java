package OverJADE;

import jade.core.Agent;
import jade.core.AID;

import jade.domain.AMSService;
import jade.domain.FIPAAgentManagement.*;

public class NeedsFriendsAgent extends PlayerAgent {
	
	
	protected void setup() {
        super.setup();
		// définition des preferences
		double rand = Math.random();
        if (rand < 0.7) {
            preferedRole = Group.Role.DPS;
            rand = Math.random();
            if (rand < 0.5) {
                secondRole = Group.Role.HEALER;
                lastRole = Group.Role.TANK;
            }
            else {
                secondRole = Group.Role.TANK;
                lastRole = Group.Role.HEALER;
            }
        }
        else if (rand < 0.85) {
            preferedRole = Group.Role.TANK;
            rand = Math.random();
            if (rand < 0.7) {
                secondRole = Group.Role.DPS;
                lastRole = Group.Role.HEALER;
            }
            else {
                secondRole = Group.Role.HEALER;
                lastRole = Group.Role.DPS;
            }
        }
        else {
            preferedRole = Group.Role.HEALER;
            rand = Math.random();
            if (rand < 0.7) {
                secondRole = Group.Role.DPS;
                lastRole = Group.Role.TANK;
            }
            else {
                secondRole = Group.Role.TANK;
                lastRole = Group.Role.DPS;
            }
        }
        

     // définition de la liste d'amis                    
        long nbFriends = (long)((Math.random() * 10) + 1);
        AID myID = getAID();
        
        for (int ii=0; ii<nbFriends;ii++)
        {
        	int j =((int) Math.random() * (int) nbFriends) + 1;
        	String lName = "guest_"+ j ;
        	AID agentID = new AID(lName, AID.ISLOCALNAME);
        	
        	if (!(agentID.equals( myID ))) {
          	  	friends.add(agentID);
        	}
        }

		addBehaviour( new NeedsFriendsBehaviour(this));
	}
	
}