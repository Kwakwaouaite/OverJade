

package OverJADE;

import jade.core.Agent;
import java.util.ArrayList;

import jade.core.AID;


public class Group {
	
	public enum Role {
			DPS,
			TANK,
			HEALER
		}

	AID leader = null;
	AID firstHealer = null;
	AID secondHealer = null;
	AID firstDPS = null;
	AID secondDPS = null;
	AID firstTank = null;
	AID secondTank = null;

	public Group (AID leaderAID){
		this.leader = leaderAID;
	}

	public Role tryJoinRole(Role roleWanted){
		if (roleWanted != null) {
			switch (roleWanted){
				case DPS :
					if (isDPSNeeded()) return Role.DPS;
					break;
				case TANK :
					if (isTankNeeded()) return Role.TANK;
					break;
				case HEALER :
					if (isHealerNeeded()) return Role.HEALER;
					break;
			}
		}

		if (isDPSNeeded()) return Role.DPS;
		if (isTankNeeded()) return Role.TANK;
		if (isHealerNeeded()) return Role.HEALER;

		return null;
	}

	boolean isHealerNeeded(){
		if (firstHealer != null && secondHealer != null){
			return false;
		}
		return true;
	}

	boolean isDPSNeeded(){
		if (firstDPS != null && secondDPS != null){
			return false;
		}
		return true;
	}

	boolean isTankNeeded(){
		if (firstTank != null && secondTank != null){
			return false;
		}
		return true;
	}

	Role whatNeeded(){
		if (isHealerNeeded()){
			return Role.HEALER;
		}
		if (isDPSNeeded()){
			return Role.DPS;
		}
		if (isTankNeeded()){
			return Role.TANK;
		}
		return null;
	}

	void leave(AID aidLeaver){
		if (firstHealer != null && aidLeaver == firstHealer){
			firstHealer = null;
		}
		else if (secondHealer != null && aidLeaver == secondHealer){
			secondHealer = null;
		}
		else if (firstDPS != null && aidLeaver == firstDPS){
			firstDPS = null;
		}
		else if (secondDPS != null && aidLeaver == secondDPS){
			secondDPS = null;
		}
		else if (firstTank != null && aidLeaver == firstTank){
			firstTank = null;
		}
		else if (secondTank != null && aidLeaver == secondTank){
			secondTank = null;
		}
	}
}
