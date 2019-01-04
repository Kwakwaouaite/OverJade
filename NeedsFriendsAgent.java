package OverJADE;

import jade.core.Agent;
import jade.core.AID;

import jade.domain.AMSService;
import jade.domain.FIPAAgentManagement.*;

public class NeedsFriendsAgent extends PlayerAgent {
	
	
	protected void setup() {
		// TODO : créer un méthode dans Player Agent qui reprend cet partie du code
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
        AMSAgentDescription [] agents = null;
        try {
            SearchConstraints c = new SearchConstraints();
            long random_int = (long)((Math.random() * 10) + 1);
            c.setMaxResults (random_int);
            agents = AMSService.search( this, new AMSAgentDescription (), c );
        }
        catch (Exception e) {
            System.out.println( "Problem searching AMS: " + e );
            e.printStackTrace();
        }
        AID myID = getAID();
        for (int i=0; i<agents.length;i++)
        {
        	AID agentID = agents[i].getName();
        	if (!(agentID.equals( myID ))) {
          	  	friends.add(agentID);
        	}
        }



		addBehaviour( new NeedsFriendsBehaviour(this));
	}
	
}