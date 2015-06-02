package gov.usgs.identifrog.Handlers;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

import gov.usgs.identifrog.DataObjects.Frog;
import gov.usgs.identifrog.DataObjects.Location;
import gov.usgs.identifrog.DataObjects.Personel;

/**
 * Used to store the backing data for the jtable displayed on the screen. Essentially holds the database in memory and can perform queries and operations on it in memory.
 * @author (not) mjperez
 *
 */
public class DataHandler {
  private ArrayList<Frog> frogs;

  public DataHandler() {
  }

  public DataHandler(ArrayList<Frog> frogs) {
    this.frogs = frogs;
  }

  
 
/*
  private int searchFrogByFrogID(String ID) {
    int index = -1;
    for (int i = 0; i < frogs.size(); i++) {
      if (frogs.get(i).getID() == ID) {
        index = i;
      }
    }
    return index;
  }*/


  /*public Frog searchFrog(int ID) {
    int index = searchFrogByID(ID);
    if (index == -1) {
      return null;
    }
    return frogs.get(index);
  }*/



  public ArrayList<Personel> uniquePersonels(String type) {
    ArrayList<Personel> personel = new ArrayList<Personel>();
    if (type.equals("observer")) {
      for (int i = 0; i < frogs.size(); i++) {
        personel.add(frogs.get(i).getObserver());
      }
    } else if (type.equals("recorder")) {
      for (int i = 0; i < frogs.size(); i++) {
        personel.add(frogs.get(i).getRecorder());
      }
    }
    HashSet<Personel> uniquePersonel = new HashSet<Personel>(personel);
    personel.clear();
    personel.addAll(uniquePersonel);
    return personel;
  }

  public ArrayList<Location> uniqueLocations() {
    ArrayList<Location> locations = new ArrayList<Location>();
    for (int i = 0; i < frogs.size(); i++) {
      locations.add(frogs.get(i).getLocation());
    }
    HashSet<Location> uniqueLocations = new HashSet<Location>(locations);
    locations.clear();
    locations.addAll(uniqueLocations);
    return locations;
  }

  public Object[][] frogsArray(boolean allFrogs) {
    ArrayList<Frog> localFrogs = new ArrayList<Frog>();
    boolean allFrogsLocal = true;
    if (allFrogsLocal) {
      localFrogs = frogs;
    } else {
      localFrogs = getUniqueFrogs().getFrogs();
    }
    // NOTE: replace frogList with frogs
    Object[][] array = new Object[localFrogs.size()][];
    for (int i = 0; i < localFrogs.size(); i++) {
      array[i] = localFrogs.get(i).toArray();
    }
    return array;
  }
  
  public DataHandler getUniqueFrogs() {
    ArrayList<Frog> uniqueFrogs;
    ArrayList<Integer> frogIDs = new ArrayList<Integer>();
    for (int i = 0; i < frogs.size(); i++) {
      frogIDs.add(frogs.get(i).getID());
    }
    HashSet<Integer> uniqueFrogIDSet = new HashSet<Integer>(frogIDs);
    frogIDs.clear();
    frogIDs.addAll(uniqueFrogIDSet);
    uniqueFrogs = new ArrayList<Frog>();
    for (int i = 0; i < frogIDs.size(); i++) {
      uniqueFrogs.add(XMLFrogDatabase.searchFrogByID(frogIDs.get(i)));
    }
    return new DataHandler(uniqueFrogs);
  }

  public Object[][] personelArray(String type) {
    ArrayList<Personel> personel = uniquePersonels(type);
    Object[][] array = new Object[personel.size()][];
    for (int i = 0; i < personel.size(); i++) {
      array[i] = personel.get(i).toArray();
    }
    return array;
  }

  public Object[][] locationArray() {
    ArrayList<Location> locations = uniqueLocations();

    Object[][] array = new Object[locations.size()][];
    for (int i = 0; i < locations.size(); i++) {
      array[i] = locations.get(i).toArray();
    }
    return array;
  }

  public ArrayList<Frog> getFrogs() {
    return frogs;
  }

  public void setFrogs(ArrayList<Frog> frogs) {
    this.frogs = frogs;
  }

}
