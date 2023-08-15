package src;

import ch.aplu.jgamegrid.*;


public class Portal extends Actor {
    private Game game;
    private Location locationMoveTo;
    private PortalType type;
    public Portal(Game game,PortalType type){
        super("sprites/" + type.getImageName());
        this.game = game;
        this.type = type;
    }
    public void setLocationMoveTo(Location locationMoveTo){
        this.locationMoveTo = locationMoveTo;
    }

    public Location getLocationMoveTo() {
        return locationMoveTo;
    }
    public PortalType getType(){return type;}
}
