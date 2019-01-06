/*****************************************************************
JADE - Java Agent DEvelopment Framework is a framework to develop 
multi-agent systems in compliance with the FIPA specifications.
Copyright (C) 2000 CSELT S.p.A. 

GNU Lesser General Public License

This library is free software; you can redistribute it and/or
modify it under the terms of the GNU Lesser General Public
License as published by the Free Software Foundation, 
version 2.1 of the License. 

This library is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
Lesser General Public License for more details.

You should have received a copy of the GNU Lesser General Public
License along with this library; if not, write to the
Free Software Foundation, Inc., 59 Temple Place - Suite 330,
Boston, MA  02111-1307, USA.
*****************************************************************/

package OverJADE;

import jade.core.Agent;
import java.util.ArrayList;

import jade.lang.acl.ACLMessage;

import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.DFService;
import jade.domain.FIPAException;

import jade.core.AID;

public class PlayerAgent extends Agent {
	
	
	
	public String nickname; // Attribut nom
	public Group.Role preferedRole;
	public Group.Role secondRole;
	public Group.Role lastRole;
	
	public ArrayList<AID> friends;

	public AID leader;
	public AID hostAgent;
	
	public boolean isActive;
	
	public int impatienceRate;
	
	protected void setup() {
		friends = new ArrayList<AID>();

		// Register to the DF
		DFAgentDescription template = new DFAgentDescription();
        ServiceDescription sd = new ServiceDescription();
        sd.setType("managing");
        template.addServices(sd);

		try {
	            DFAgentDescription[] result = DFService.search(this, template);
	            if (result.length > 0) {
	                hostAgent = result[0].getName();
	            }
	        } catch (FIPAException fe) {
	            fe.printStackTrace();
	        }

	    // Inform the host that we are connected
	    ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
        msg.setContent(HostSystem.HELLO);
        msg.addReceiver(hostAgent);
        send(msg);
	}
	
	public void letsgo() { // To call when the group is complete and the player leave
		ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
        msg.setContent(HostSystem.GOODBYE);
        msg.addReceiver(hostAgent);
        send(msg);
	}

	public void joiningGroup() { // To call when the group is complete and the player leave
		ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
        msg.setContent(HostSystem.JOINGROUP);
        msg.addReceiver(hostAgent);
        send(msg);
	}
}