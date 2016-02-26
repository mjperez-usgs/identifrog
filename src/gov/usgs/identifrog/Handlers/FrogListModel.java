package gov.usgs.identifrog.Handlers;

import gov.usgs.identifrog.DataObjects.Frog;
import gov.usgs.identifrog.Frames.MainFrame;

import java.util.ArrayList;
import java.util.Collections;

import javax.swing.AbstractListModel;

import org.apache.commons.lang3.ArrayUtils;

public class FrogListModel extends AbstractListModel<Frog> {
	ArrayList<Frog> frogs;

	public FrogListModel(ArrayList<Frog> array) {
		frogs = array;
	}

	public FrogListModel() {
		frogs = new ArrayList<Frog>();
	}

	public int getSize() {
		return frogs.size();
	}

	public void addElement(Frog f) {
		frogs.add(f);
		fireContentsChanged(this, frogs.size() - 1, frogs.size() - 1);
	}

	public Frog get(int index) {
		return getElementAt(index);
	}

	public Frog getElementAt(int index) {
		return frogs.get(index);
	}

	public ArrayList<Frog> getFrogList() {
		return frogs;
	}

	public void set(int i, Frog f) {
		frogs.set(i, f);
	}

	public boolean removeElement(Object o) {
		return frogs.remove(o);
	}

	public Frog removeElement(int i) {
		Frog removed = frogs.remove(i);
		return removed;
	}

	public void setList(ArrayList<Frog> array) {
		this.frogs = array;
	}

	public void sort() {
		Collections.sort(frogs);
		switch (MainFrame.SORTING_METHOD) {
		case MainFrame.SORT_BY_LATEST_CAPTURE:
		case MainFrame.SORT_BY_NUM_SURVEYS:
		case MainFrame.SORT_BY_NUM_IMAGES:
		case MainFrame.SORT_BY_SEARCHABILITY:
			Collections.reverse(frogs);
			break;
		}
		fireContentsChanged(this, 0, frogs.size());
	}
}