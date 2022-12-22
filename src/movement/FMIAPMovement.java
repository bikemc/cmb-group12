package movement;

import java.util.List;

import movement.map.MapNode;
import core.Coord;
import core.Settings;

public class FMIAPMovement extends MapBasedMovement implements SwitchableMovement{
    private List<MapNode> Route_exit = null;
    public static final String EXIT_FILE = "exit";
    public int clock_spread;
    public static final String FIRESPREADCLOCK = "spread_begin";
    int current_clock = 0;
    int exit_counter = 0;

    List<MapNode> exits = null;

    public FMIAPMovement(Settings settings){
        super(settings);
        clock_spread = Integer.parseInt(settings.getSetting(FIRESPREADCLOCK));
        exits = FMIFireEscapeMovement.getExitPoints(EXIT_FILE, getMap(), getOkMapNodeTypes());
    }

    @Override
    public Path getPath() {
        return null;
    }

    @Override
    public Coord getInitialLocation() {
        Coord placement;
        placement = exits.get(exit_counter).getLocation();
        exit_counter ++;
        return placement;
    }

    @Override
    public MapBasedMovement replicate() {
        return null;
    }

    @Override
    public void setLocation(Coord lastWaypoint) {

    }

    @Override
    public Coord getLastLocation() {
        return null;
    }

    @Override
    public boolean isReady() {
        return false;
    }
}
