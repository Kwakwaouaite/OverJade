package OverJADE;

import jade.core.Agent;
import jade.core.AID;

public class NeedsFriendsAgent extends PlayerAgent {
	
	
	protected void setup() {
		addBehaviour( new NeedsFriendsBehaviour(this));
	}
	
}