package gov.usgs.identifrog;

import java.awt.image.BufferedImage;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * <p>
 * Title: DigSignature.java
 * </p>
 * <p>
 * Description: makes a signature with "snout spot" discriminator and four features: number of
 * relevant spots, number of relevant spots per quadrant, connected components, and the centroids of
 * relevant spots
 * </p>
 * 
 * @author Oksana V. Kelly 2008
 *         <p>
 *         This software is released into the public domain.
 *         </p>
 */

public class DigSignature {

	private double areathresh = 0.02;
	private int[] pass = new int[5];

	ArrayList<BinaryRegion> relevantSpots = new ArrayList<BinaryRegion>();
	int[][] AdjacencyMatrix;
	ArrayList<ComponentCoor> connectedComponents = new ArrayList<ComponentCoor>();

	// private boolean hasSnoutSpot = false;

	public void makeSignature(BufferedImage binaryImg, String sigFileLoc) {

		/* ----- COMPONENTS AND CENTROID CONSTELLATION ----- */
		findConstellationAndComponents(binaryImg);

		/* --------- PASSES 0 and 1 ----- */
		PassVector passVector = new PassVector();
		pass = passVector.makePassVector(relevantSpots, binaryImg.getWidth(), binaryImg.getHeight());

		/* ----- WRITE SIGNATURE TO FILE ----- */
		signatureToFile(sigFileLoc);
	}

	public void findConstellationAndComponents(BufferedImage binaryImage) {

		/* ----- LABEL SPOTS IN A BINARY IMAGE ------------------------ */
		ContourTracer tracer = new ContourTracer(binaryImage);
		List<BinaryRegion> regions = tracer.getRegions();
		ArrayList<DistancebtwnSpots> SpotDistances = new ArrayList<DistancebtwnSpots>();
		ArrayList<DistancebtwnSpots> selectedSpotDistances = new ArrayList<DistancebtwnSpots>();

		/* --- FIND RELEVANT SPOTS with normalized area >= areathresh --- */
		/* relevant spot's normarea = area/total spot area ----- */
		int totalArea = 0;
		for (int k = 0; k < regions.size(); ++k) {
			totalArea = totalArea + regions.get(k).numberOfPixels;

		}

		for (int r = 0; r < regions.size(); ++r) {
			double mynormA = (double) regions.get(r).numberOfPixels / totalArea;
			double mynormarea = trunc(mynormA, 2);

			if (mynormarea >= areathresh) {
				System.out.println("REL " + regions.get(r).label);
				relevantSpots.add(regions.get(r));
			}
		}

		// if only three relevant spots, they are already connected components
		if (relevantSpots.size() <= 3 && relevantSpots.size() > 0) {
			int currentComponentNum = 1; // the only spots belong to the same components
			for (int s = 0; s < relevantSpots.size(); ++s) {
				ComponentCoor xycoor = new ComponentCoor(relevantSpots.get(s).xc, relevantSpots.get(s).yc, currentComponentNum);
				connectedComponents.add(xycoor);
			}
		} else { // there are more than three relevant spots

			/* ----------------- DISTANCES BETWEEN SPOTS ---------------- */
			for (int i = 0; i < relevantSpots.size(); ++i) {
				for (int j = 0; j < relevantSpots.size(); ++j) {
					int lab1 = relevantSpots.get(i).label;
					int lab2 = relevantSpots.get(j).label;
					// pick unique distances, some nodes repeat
					if (lab1 != lab2 && isUnique(lab1, lab2, SpotDistances)) {
						double dist_spots = Math.sqrt(Math.pow(relevantSpots.get(i).xc - relevantSpots.get(j).xc, 2) + Math.pow(-relevantSpots.get(i).yc + relevantSpots.get(j).yc, 2));
						DistancebtwnSpots distanceSpots = new DistancebtwnSpots(lab1, relevantSpots.get(i).xc, -relevantSpots.get(i).yc, lab2, relevantSpots.get(j).xc, -relevantSpots.get(j).yc,
								dist_spots);
						SpotDistances.add(distanceSpots);
					}
				}
			}

			/* ---------------- CONNECTED COMPONENTS --------------------- */
			/*
			 * m is number of relevant spots N is the number of distances m*(m-1)/2 sort distances
			 * and pick sqrt(N) of them
			 */

			int m = relevantSpots.size();
			float N = m * (m - 1) / 2;
			int n_short_dist = Math.round((float) Math.sqrt(N));

			// sort DistanceSpots
			Collections.sort(SpotDistances, new distanceComparator());

			AdjacencyMatrix = new int[regions.size() + 1][regions.size() + 1];

			/* -- PICK n_short_dist DISTANCE and FORM ADJACENCY MATRIX A ------ */
			/*
			 * spots are labels from 1 to m in List regions / Adjacency matrix starts with O
			 */
			for (int l = 0; l < n_short_dist; ++l) {
				int node1 = SpotDistances.get(l).spotlabel1;
				int node2 = SpotDistances.get(l).spotlabel2;
				AdjacencyMatrix[node1][node2] = 1;
				AdjacencyMatrix[node2][node1] = 1;
				selectedSpotDistances.add(SpotDistances.get(l));
				// System.out.println("A " + " node1= " + node1 + " node2= " + node2 + " "
				// +AdjacencyMatrix[node1][node2]);
			}

			int currentComponentNum = 0; // use to track components
			for (int row = 0; row < AdjacencyMatrix.length; ++row) {
				// row indicates node "from", col indicate "to"
				// find if row node connected to col node
				ArrayList<Node> ConnectedNodes = new ArrayList<Node>();
				ConnectedNodes = findConnected(row);
				if (ConnectedNodes.size() != 0) {
					// record node "from"
					++currentComponentNum; // incounted a component
					int ind = getCentroidCoor(row);
					ComponentCoor xycoor = new ComponentCoor(relevantSpots.get(ind).xc, relevantSpots.get(ind).yc, currentComponentNum);
					connectedComponents.add(xycoor);

					// record node(s) "to" & turn to zero
					for (int s = 0; s < ConnectedNodes.size(); ++s) {
						int index = getCentroidCoor(ConnectedNodes.get(s).node);
						ComponentCoor coor = new ComponentCoor(relevantSpots.get(index).xc, relevantSpots.get(index).yc, currentComponentNum);
						connectedComponents.add(coor);

						AdjacencyMatrix[row][ConnectedNodes.get(s).node] = 0;
						AdjacencyMatrix[ConnectedNodes.get(s).node][row] = 0;
					}

					// find if "to" node connected to someone else
					for (int t = 0; t < ConnectedNodes.size(); ++t) {
						connect(ConnectedNodes.get(t).node, currentComponentNum);
					}

				}
			}
		}
	}

	private void connect(int node, int currentComponent) {
		ArrayList<Node> next_node = new ArrayList<Node>();
		next_node = findConnected(node);

		if (next_node.size() != 0) {
			// record node(s) & turn to zero
			for (int i = 0; i < next_node.size(); ++i) {
				int ind = getCentroidCoor(next_node.get(i).node);
				ComponentCoor mycoor = new ComponentCoor(relevantSpots.get(ind).xc, relevantSpots.get(ind).yc, currentComponent);
				connectedComponents.add(mycoor);
				AdjacencyMatrix[node][next_node.get(i).node] = 0;
				AdjacencyMatrix[next_node.get(i).node][node] = 0;
			}

			// find if the node connected to someone else
			for (int j = 0; j < next_node.size(); ++j) {
				connect(next_node.get(j).node, currentComponent);
			}
		}
	}

	private void signatureToFile(String sigLoc) {
		String str = sigLoc;
		String temp = str.substring(0, (str.length() - 3));
		String sigfile = temp + "dsg";

		try {
			BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(sigfile));

			// write Passes Info
			for (int p = 0; p < pass.length; ++p) {
				String str1 = "" + pass[p] + ",";
				bufferedWriter.write(str1);
			}

			// write Component coordinates, each new component indicate with "A" in the file
			int i = 0;
			int num = connectedComponents.get(0).componentNumber;
			boolean new_comp = true;
			while (i < connectedComponents.size()) {
				if (num != connectedComponents.get(i).componentNumber) {
					new_comp = true;
					num = connectedComponents.get(i).componentNumber;
				}
				if (num == connectedComponents.get(i).componentNumber & new_comp) {
					bufferedWriter.write("A,");
					new_comp = false;
				}
				if (!new_comp) {
					String str2 = "" + connectedComponents.get(i).xc + "," + connectedComponents.get(i).yc + ",";
					bufferedWriter.write(str2);
				}
				++i;
			}
			// write coordinates of Centroids, indicate with "Z" in the file
			bufferedWriter.write("Z,");
			for (int j = 0; j < relevantSpots.size(); ++j) {
				String str3 = "" + relevantSpots.get(j).xc + "," + relevantSpots.get(j).yc + ",";
				bufferedWriter.write(str3);
			}
			bufferedWriter.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private class DistancebtwnSpots {
		public int spotlabel1;
		// public double xc1, yc1;
		public int spotlabel2;
		// public double xc2, yc2;
		public double dist;

		public DistancebtwnSpots(int label1, double xc_1, double yc_1, int label2, double xc_2, double yc_2, double distance) {
			spotlabel1 = label1;
			spotlabel2 = label2;
			dist = distance;
		}
	}

	private class ComponentCoor {
		public double xc, yc;
		int componentNumber;

		public ComponentCoor(double x, double y, int mycomponentNumber) {
			xc = x;
			yc = y;
			componentNumber = mycomponentNumber;
		}
	}

	private class Node {
		public int node;

		public Node(int mynode) {
			node = mynode;
		}
	}

	private class distanceComparator implements Comparator<DistancebtwnSpots> {
		public int compare(DistancebtwnSpots spotpair1, DistancebtwnSpots spotpair2) {
			// comparing double values, sort in ascending order
			if (spotpair1.dist > spotpair2.dist) {
				return 1;
			} else if (spotpair1.dist < spotpair2.dist) {
				return -1;
			} else {
				return 0;
			}
		}
	}

	private int getCentroidCoor(int mylabel) {
		int i = 0;
		int index = 0;
		boolean found = false;

		while (i < relevantSpots.size() && !found) {
			if (relevantSpots.get(i).label == mylabel) {
				index = i;
				found = true;
			} else {
				++i;
			}
		}
		return index;
	}

	private ArrayList<Node> findConnected(int currentrow) {
		int i = 0;
		ArrayList<Node> ConNodes = new ArrayList<Node>();
		while (i < AdjacencyMatrix[0].length) {
			if (AdjacencyMatrix[currentrow][i] == 1) {
				Node node = new Node(i);
				ConNodes.add(node);
			}
			++i;
		}
		return ConNodes;
	}

	private boolean isUnique(int label1, int label2, ArrayList<DistancebtwnSpots> spotDistances) {
		int i = 0;
		boolean unique = true;
		if (spotDistances.size() != 0) {
			while (i < spotDistances.size() && unique) {
				if (spotDistances.get(i).spotlabel1 == label2 && spotDistances.get(i).spotlabel2 == label1) {
					unique = false;
				}
				++i;
			}
		}
		return unique;
	}

	double trunc(double d, int precision) {
		double m = Math.pow(10, precision);
		long k = Math.round(d * m);
		double v = k / m;
		return v;
	}

} // end of DigSignature
