package OverJADE;

public class ImpatientAgent extends PlayerAgent{

    protected void setup() {
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

        addBehaviour( new ImpatientBehaviour(this));
    }
}
