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
	}
	
}