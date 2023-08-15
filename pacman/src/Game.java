// PacMan.java
// Simple PacMan implementation
package src;

import ch.aplu.jgamegrid.*;
import src.matachi.mapeditor.grid.GridModel;
import src.utility.GameCallback;

import java.awt.*;
import java.util.ArrayList;
import java.util.Properties;

public class Game extends GameGrid
{
  private final static int nbHorzCells = 20;
  private final static int nbVertCells = 11;
  private GridModel model;
  protected PacManGameGrid grid;

  protected PacActor pacActor = new PacActor(this);
  private Monster troll = new Monster(this, MonsterType.Troll);
  private Monster tx5 = new Monster(this, MonsterType.TX5);

  private ArrayList<Location> pillAndItemLocations = new ArrayList<Location>();
  private ArrayList<Actor> iceCubes = new ArrayList<Actor>();
  private ArrayList<Actor> goldPieces = new ArrayList<Actor>();
  public ArrayList<Portal>portals = new ArrayList<>();
  private GameCallback gameCallback;
  private Properties properties;
  private int seed = 30006;
  private ArrayList<Location> propertyPillLocations = new ArrayList<>();
  private ArrayList<Location> propertyGoldLocations = new ArrayList<>();

  public Game(GameCallback gameCallback, Properties properties,GridModel model)
  {
    //Setup game
    super(nbHorzCells, nbVertCells, 20, false);
    this.gameCallback = gameCallback;
    this.properties = properties;
    this.model = model;
    this.grid = new PacManGameGrid(nbHorzCells,nbVertCells,model);
    setSimulationPeriod(100);
    setTitle("[PacMan in the Multiverse]");

    //Setup for auto test
    pacActor.setPropertyMoves(properties.getProperty("PacMan.move"));
    pacActor.setAuto(Boolean.parseBoolean(properties.getProperty("PacMan.isAuto")));
    loadPillAndItemsLocations();

    GGBackground bg = getBg();
    drawGrid(bg);

    //Setup Random seeds
    seed = Integer.parseInt(properties.getProperty("seed"));
    pacActor.setSeed(seed);
    troll.setSeed(seed);
    tx5.setSeed(seed);
    addKeyRepeatListener(pacActor);
    setKeyRepeatPeriod(150);
    troll.setSlowDown(3);
    tx5.setSlowDown(3);
    pacActor.setSlowDown(3);
    tx5.stopMoving(5);
    setupActorLocations();



    //Run the game
    doRun();
    show();
    // Loop to look for collision in the application thread
    // This makes it improbable that we miss a hit
    boolean hasPacmanBeenHit;
    boolean hasPacmanEatAllPills;
    setupPillAndItemsLocations();
    int maxPillsAndItems = countPillsAndItems();
    
    do {
      hasPacmanBeenHit = troll.getLocation().equals(pacActor.getLocation()) ||
              tx5.getLocation().equals(pacActor.getLocation());
      hasPacmanEatAllPills = pacActor.getNbPills() >= maxPillsAndItems;
      delay(10);
    } while(!hasPacmanBeenHit && !hasPacmanEatAllPills);
    delay(120);

    Location loc = pacActor.getLocation();
    troll.setStopMoving(true);
    tx5.setStopMoving(true);
    pacActor.removeSelf();
    String title = "";
    if (hasPacmanBeenHit) {
      bg.setPaintColor(Color.red);
      title = "GAME OVER";
      addActor(new Actor("sprites/explosion3.gif"), loc);
      this.hide();
    } else if (hasPacmanEatAllPills) {
      bg.setPaintColor(Color.yellow);
      title = "YOU WIN";
      this.hide();
    }
    setTitle(title);
    gameCallback.endOfGame(title);

    doPause();
  }

  public GameCallback getGameCallback() {
    return gameCallback;
  }

  private void setupActorLocations() {
    Location trollLocations = new Location(-1,-1);
    Location tx5Locations = new Location(-1,-1);
    Location pacManLocations = new Location(-1,-1);
    for (int y = 0; y < nbVertCells; y++)
    {
      for (int x = 0; x < nbHorzCells; x++)
      {
        Location location = new Location(x, y);
        int a = grid.getCell(location);
        if (a == 5 ) {
          pacManLocations = location;
        }
        if (a == 6 ) {
          trollLocations = location;
        }
        if (a == 7) {
          tx5Locations = location;
        }
      }
    }
    int trollX = trollLocations.getX();
    int trollY = trollLocations.getY();

    int tx5X = tx5Locations.getX();
    int tx5Y = tx5Locations.getY();

    int pacManX = pacManLocations.getX();
    int pacManY = pacManLocations.getY();

    addActor(troll, new Location(trollX, trollY), Location.NORTH);
    addActor(pacActor, new Location(pacManX, pacManY));
    addActor(tx5, new Location(tx5X, tx5Y), Location.NORTH);
  }

  private int countPillsAndItems() {
    int pillsAndItemsCount = 0;
    for (int y = 0; y < nbVertCells; y++)
    {
      for (int x = 0; x < nbHorzCells; x++)
      {
        Location location = new Location(x, y);
        int a = grid.getCell(location);
        if (a == 1 && propertyPillLocations.size() == 0) { // Pill
          pillsAndItemsCount++;
        } else if (a == 3 && propertyGoldLocations.size() == 0) { // Gold
          pillsAndItemsCount++;
        }
      }
    }
    if (propertyPillLocations.size() != 0) {
      pillsAndItemsCount += propertyPillLocations.size();
    }

    if (propertyGoldLocations.size() != 0) {
      pillsAndItemsCount += propertyGoldLocations.size();
    }

    return pillsAndItemsCount;
  }

  public ArrayList<Location> getPillAndItemLocations() {
    return pillAndItemLocations;
  }


  private void loadPillAndItemsLocations() {
    String pillsLocationString = properties.getProperty("Pills.location");
    if (pillsLocationString != null) {
      String[] singlePillLocationStrings = pillsLocationString.split(";");
      for (String singlePillLocationString: singlePillLocationStrings) {
        String[] locationStrings = singlePillLocationString.split(",");
        propertyPillLocations.add(new Location(Integer.parseInt(locationStrings[0]), Integer.parseInt(locationStrings[1])));
      }
    }

    String goldLocationString = properties.getProperty("Gold.location");
    if (goldLocationString != null) {
      String[] singleGoldLocationStrings = goldLocationString.split(";");
      for (String singleGoldLocationString: singleGoldLocationStrings) {
        String[] locationStrings = singleGoldLocationString.split(",");
        propertyGoldLocations.add(new Location(Integer.parseInt(locationStrings[0]), Integer.parseInt(locationStrings[1])));
      }
    }
  }
  private void setupPillAndItemsLocations() {
    for (int y = 0; y < nbVertCells; y++)
    {
      for (int x = 0; x < nbHorzCells; x++)
      {
        Location location = new Location(x, y);
        int a = grid.getCell(location);
        if (a == 1 && propertyPillLocations.size() == 0) {
          pillAndItemLocations.add(location);
        }
        if (a == 3 &&  propertyGoldLocations.size() == 0) {
          pillAndItemLocations.add(location);
        }
        if (a == 4) {
          pillAndItemLocations.add(location);
        }
      }
    }


    if (propertyPillLocations.size() > 0) {
      for (Location location : propertyPillLocations) {
        pillAndItemLocations.add(location);
      }
    }
    if (propertyGoldLocations.size() > 0) {
      for (Location location : propertyGoldLocations) {
        pillAndItemLocations.add(location);
      }
    }
  }

  private void drawGrid(GGBackground bg)
  {
    bg.clear(Color.gray);
    bg.setPaintColor(Color.white);
    for (int y = 0; y < nbVertCells; y++)
    {
      for (int x = 0; x < nbHorzCells; x++)
      {

        bg.setPaintColor(Color.white);
        Location location = new Location(x, y);
        int a = grid.getCell(location);
        if (a > 0)
          bg.fillCell(location, Color.lightGray);
        if (a == 1 && propertyPillLocations.size() == 0) { // Pill
          putPill(bg, location);

        } else if (a == 3 && propertyGoldLocations.size() == 0) { // Gold
          putGold(bg, location);
        } else if (a == 4) {
          putIce(bg, location);
        } else if (a == 8) {
          Portal white = new Portal(this,PortalType.White);
          addActor(white,location);
          for(Portal portal : portals){
            if (portal.getType().equals(PortalType.White)){
              portal.setLocationMoveTo(location);
              white.setLocationMoveTo(portal.getLocation());
            }
          }
          this.portals.add(white);
        } else if (a == 9) {
          Portal yellow = new Portal(this, PortalType.Yellow);
          addActor(yellow, location);
          for(Portal portal : portals){
            if (portal.getType().equals(PortalType.Yellow)){
              portal.setLocationMoveTo(location);
              yellow.setLocationMoveTo(portal.getLocation());
            }
          }
          this.portals.add(yellow);
        } else if (a == 10) {
          Portal darkgold = new Portal(this, PortalType.DarkGold);
          addActor(darkgold, location);
          for(Portal portal : portals){
            if (portal.getType().equals(PortalType.DarkGold)){
              portal.setLocationMoveTo(location);
              darkgold.setLocationMoveTo(portal.getLocation());
            }
          }
          this.portals.add(darkgold);
        }else if (a == 11) {
          Portal darkgray = new Portal(this, PortalType.DarkGray);
          addActor(darkgray, location);
          for(Portal portal : portals){
            if (portal.getType().equals(PortalType.DarkGray)){
              portal.setLocationMoveTo(location);
              darkgray.setLocationMoveTo(portal.getLocation());
            }
          }
          this.portals.add(darkgray);
        }
      }

    }

    for (Location location : propertyPillLocations) {
      putPill(bg, location);
    }

    for (Location location : propertyGoldLocations) {
      putGold(bg, location);
    }
  }

  private void putPill(GGBackground bg, Location location){
    bg.fillCircle(toPoint(location), 5);
  }

  private void putGold(GGBackground bg, Location location){
    bg.setPaintColor(Color.yellow);
    bg.fillCircle(toPoint(location), 5);
    Actor gold = new Actor("sprites/gold.png");
    this.goldPieces.add(gold);
    addActor(gold, location);
  }

  private void putIce(GGBackground bg, Location location){
    bg.setPaintColor(Color.blue);
    bg.fillCircle(toPoint(location), 5);
    Actor ice = new Actor("sprites/ice.png");
    this.iceCubes.add(ice);
    addActor(ice, location);
  }
  public void removeItem(String type,Location location){
    if(type.equals("gold")){
      for (Actor item : this.goldPieces){
        if (location.getX() == item.getLocation().getX() && location.getY() == item.getLocation().getY()) {
          item.hide();
        }
      }
    }else if(type.equals("ice")){
      for (Actor item : this.iceCubes){
        if (location.getX() == item.getLocation().getX() && location.getY() == item.getLocation().getY()) {
          item.hide();
        }
      }
    }
  }
  public void removePill(int index){
    this.pillAndItemLocations.remove(index);
  }

  public int getNumHorzCells(){
    return this.nbHorzCells;
  }
  public int getNumVertCells(){
    return this.nbVertCells;
  }
}
