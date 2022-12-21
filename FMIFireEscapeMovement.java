/*
 * Copyright 2010 Aalto University, ComNet
 * Released under GPLv3. See LICENSE.txt for details.
 */
package movement;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import core.SettingsError;
import input.WKTReader;
import movement.map.DijkstraPathFinder;
import movement.map.MapNode;
import movement.map.MapRoute;
import core.Settings;
import core.Coord;

/**
 * Map based movement model that uses Dijkstra's algorithm to find shortest
 * paths between two random map nodes and Points Of Interest
 */
public class FMIFireEscapeMovement extends MapBasedMovement implements
        SwitchableMovement {
    /** the Dijkstra shortest path finder */
    private DijkstraPathFinder pathFinder;

    /** next route's index to give by prototype */
    private Integer nextRouteIndex = 1;

    public static final String EXIT_FILE_S = "exit";
    private List<MapNode> Route_exit = null;

    /**
     * Creates a new movement model based on a Settings object's settings.
     * @param settings The Settings object where the settings are read from
     */
    public FMIFireEscapeMovement(Settings settings) {
        super(settings);
        Route_exit = getExitPoints(settings.getSetting(EXIT_FILE_S));
        this.pathFinder = new DijkstraPathFinder(getOkMapNodeTypes());
    }

    /**
     * Copyconstructor.
     * @param mbm The ShortestPathMapBasedMovement prototype to base
     * the new object to
     */
    protected FMIFireEscapeMovement(FMIFireEscapeMovement mbm) {
        super(mbm);
        this.pathFinder = mbm.pathFinder;
        this.Route_exit = mbm.Route_exit;
    }

    @Override
    public Path getPath() {
        Path p = new Path(generateSpeed());
        MapNode to = getClosestExit();
        List<MapNode> nodePath = pathFinder.getShortestPath(lastMapNode, to);
        //System.out.println(nodePath.size());
        // this assertion should never fire if the map is checked in read phase
        assert nodePath.size() > 0 : "No path from " + lastMapNode + " to " +
                to + ". The simulation map isn't fully connected";

        int pathsize = 5;
        if (nodePath.size() > pathsize){
            // because first node in the path is current position, when the pathsize is between 6-7, it will halt where it is
            if(nodePath.size()/4 < 2)pathsize = 2;
            else pathsize = nodePath.size()/4;
        }
        else pathsize = nodePath.size();


        for (int i = 0; i < pathsize; i++){
            p.addWaypoint(nodePath.get(i).getLocation());
        }
        lastMapNode = nodePath.get(pathsize-1);
        return p;
    }

    @Override
    public FMIFireEscapeMovement replicate() {
        return new FMIFireEscapeMovement(this);
    }

    // function that finds the closest exit point among all the exits
    // distance calculated as [(x1-x2)^2 + (y1-y2)^2]^(1/2)
    public MapNode getClosestExit(){
        double distance = 999999999;
        double tempDistance = 0;
        MapNode closestExit = null;
        for(MapNode stop : this.Route_exit){
            tempDistance = lastMapNode.getLocation().distance(stop.getLocation());
            if (tempDistance < distance){
                closestExit = stop;
                distance = tempDistance;
            }
        }
        return closestExit;
    }
    // function that reads all the points from wkt file.
    // original function from  PointOfInterest.java was readPoisOf() which was private
    // we copied and altered the function to our liking
    public List<MapNode> getExitPoints(String filename){

        String exit_fileName = filename;
        Coord curOffSet = getMap().getOffset();

        List<MapNode> nodes = new ArrayList<MapNode>();
        WKTReader reader = new WKTReader();

        File poiFile = null;
        List<Coord> coords = null;
        try {
            poiFile = new File(exit_fileName);
            coords = reader.readPoints(poiFile);
        }
        catch (IOException ioe){
            throw new SettingsError("Couldn't read POI-data from file '" +
                    poiFile + "' defined in setting " +
                    exit_fileName +
                    " (cause: " + ioe.getMessage() + ")");
        }

        if (coords.size() == 0) {
            throw new SettingsError("Read a POI group of size 0 from "+poiFile);
        }

        for (Coord c : coords) {
            if (getMap().isMirrored()) { // mirror POIs if map data is also mirrored
                c.setLocation(c.getX(), -c.getY()); // flip around X axis
            }

            // translate to match map data
            c.translate(curOffSet.getX(), curOffSet.getY());


            MapNode node = getMap().getNodeByCoord(c);
            if (node != null) {
                if (getOkMapNodeTypes() != null && !node.isType(getOkMapNodeTypes())) {
                    throw new SettingsError("POI " + node + " from file " +
                            poiFile + " is on a part of the map that is not "+
                            "allowed for this movement model");
                }
                nodes.add(node);
            }
            else {
                throw new SettingsError("No MapNode in SimMap at location " +
                        c + " (after translation) from file " + poiFile);
            }
        }
        return nodes;
    }
}
