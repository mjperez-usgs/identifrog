package gov.usgs.identifrog;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.event.MouseEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.CubicCurve2D;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.awt.image.BufferedImageOp;
import java.awt.image.ConvolveOp;
import java.awt.image.Kernel;
import java.awt.image.Raster;
import java.awt.image.RenderedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Vector;
import java.util.prefs.Preferences;
import javax.imageio.ImageIO;
import javax.swing.JPanel;
import gov.usgs.identifrog.Frames.ErrorDialog;
import gov.usgs.identifrog.Handlers.FolderHandler;

/**
 * <p>
 * Title: ImagePanel.java
 * </p>
 * <p>
 * Description: image processing, fingerprint extraction, spot extraction
 * </p>
 * <p>
 * This software is released into the public domain.
 * </p>
 * 
 * @author Oksana V. Kelly 2008
 * @author Oksana V. Kelly used image rotation/alignment by Steven P. Miller from <b>IdentiFrog</b>
 *         <i>2005</i>
 */

@SuppressWarnings("serial")
public class ImagePanel extends JPanel {
	FlowLayout borderLayout1 = new FlowLayout(FlowLayout.CENTER, 0, 50);

	private ImageManipFrame parentFrame;
	@SuppressWarnings("unchecked")
	private Vector operations = new Vector();
	private int operationPlace = 0;
	private int maxOperations = 0;

	// Image
	private File imageFile;
	private BufferedImage image, origImage, previousImage;
	public BufferedImage imageDorsal;
	public double imageX = 0, imageY = 0;
	private boolean shrinkImageToFit = true;
	private boolean stretchImageToFit = false;
	private boolean imageAsDorsal = false;
	public boolean reset = false;
	@SuppressWarnings("unused")
	private boolean noise_slider_active = false;

	// Box
	protected Rectangle boundingBox = new Rectangle();
	private boolean firstRubberBoxOn = false;
	private boolean showBoundingBox = false;
	private boolean secondRubberBoxOn = false;

	// User clicks on spots
	private boolean spotextraction = false;
	public boolean edgeSildersActive = false;
	private boolean pencil = false;
	private boolean fillspot = false;
	private boolean undopencil = false;
	private boolean undofillspot = false;

	// user starts to draw
	private Point2D.Double start;

	// Ridge in Box
	private BasicStroke ridgeBoundaryStroke = new BasicStroke(3, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
	private final double factor = 1.7;// leopard mean 1.88;
	private boolean ridgeInBox = false;
	private boolean oneEyeClick = false;
	private boolean twoEyeClicks = false;
	private boolean ridgeRect = false;
	/*
	 * Northern Leopard Frog :set the right boundary 0.1d to the left from the corners of the eyes d
	 * is the distanceBnEyes
	 */
	private double dist_from_eyes = 0.1;
	private double distanceBnEyes = 0.0;
	private Point2D.Double firstEyeClick = new Point2D.Double();
	private Point2D.Double secondEyeClick = new Point2D.Double();
	private Point2D.Double[] leftBoundLine = new Point2D.Double[pts - 2];
	private Point2D.Double[] leftLimitBoundLine1 = new Point2D.Double[pts - 2];
	private Point2D.Double[] leftLimitBoundLine2 = new Point2D.Double[pts - 2];
	private Point2D.Double[] rightBoundLine = new Point2D.Double[pts - 2];
	private final static int pts = 4; // cubic curve with four contol points
	// first cubic curve: ridge
	private Point2D.Double[] firstCurvePoints = new Point2D.Double[pts];
	// second cubic curve: ridge
	private Point2D.Double[] secondCurvePoints = new Point2D.Double[pts];
	private Point2D.Double selectedPoint;

	// Line
	private Line bandLine = new Line(0, 0, 0, 0);
	private boolean rubberBandLineOn = false;
	private boolean showCenterLine = false;
	private boolean firstClick = false;
	private boolean eraserOn = false;

	public boolean spotfill = false;

	// Parameters adjusted by user to extract spots
	public int default_threshold_edges = 220;
	public int default_dilation_radius = 10;
	private int threshold_edges = 220;
	private int dilation_radius = 10;
	// relationship bettwen high and low threshold
	private double lowThreshFactor = 0.8;
	// Canny edge parameters, see 6*sigma in
	// http://en.wikipedia.org/wiki/Gaussian_blur
	// see Parameters in http://en.wikipedia.org/wiki/Canny_edge_detector
	// bigger sigma produces more smoothing, works better then sigma = 1.17
	private double sigma = 1.5;
	private int window_size = 9;

	// not used
	private int noise_radius = 15;

	// Images: dors_(to display dorsal frog) and tn_ Thumbnail(to display in GUI
	// db row)
	// size, need double for ratio resizing
	double dorImgHeight = 256.0;
	double thumbImgHeight = 64.0;

	// Matrix
	// reductionFactorRGB maps RGB values -1(white) to -16777216(black) into 0
	// to 256 is -65536
	private static final double reductionFactorRGB = -16777216;
	private static final int matrixRow = 128;
	private static final int matrixCol = 256;
	private double[][] valueMatrix = new double[matrixRow][matrixCol];

	// Mouse
	private double mouseX1 = 0;
	private double mouseX2 = -1;
	private double mouseY1 = 0;
	private double mouseY2 = -1;
	private boolean getMouseLocation = false;

	// Center point
	private boolean showCenterPoint = false;
	private boolean centerWithMouseClick = false;
	protected Point center;

	// Colors
	private static final int maxDustPixels = 30;
	private Color boxColor = new Color(240, 230, 220);
	private Color revBoxColor = new Color(015, 025, 035);
	private Color centerColor = new Color(255, 242, 0);
	private Color maskColor = new Color(255, 255, 255);

	private Preferences root = Preferences.userRoot();
	protected final Preferences node = root.node("edu/isu/aadis/defaults");

	// Standardized Rectangle
	// Mapping dimensions
	public static final int rect_width = 320;
	public static final int rect_height = 160;
	// Signature processing dimensions
	public static final int final_rect_width = 256;
	public static final int final_rect_height = 128;
	// Small Binary Image dimensions
	public static final int small_rect_width = 32;
	public static final int small_rect_height = 16;

	private BufferedImage standardRectColor = new BufferedImage(rect_width, rect_height, BufferedImage.TYPE_3BYTE_BGR);
	private BufferedImage standardRectColorCopy = new BufferedImage(rect_width, rect_height, BufferedImage.TYPE_3BYTE_BGR);
	public BufferedImage standardRectGray = new BufferedImage(rect_width, rect_height, BufferedImage.TYPE_3BYTE_BGR);
	public BufferedImage standardRectGrayCopy = new BufferedImage(rect_width, rect_height, BufferedImage.TYPE_3BYTE_BGR);
	public BufferedImage standardRectBinary = new BufferedImage(rect_width, rect_height, BufferedImage.TYPE_3BYTE_BGR);
	public BufferedImage standardRectEdgesDilated = new BufferedImage(rect_width, rect_height, BufferedImage.TYPE_3BYTE_BGR);
	public BufferedImage standardRectFilled = new BufferedImage(rect_width, rect_height, BufferedImage.TYPE_3BYTE_BGR);

	// needed for mapping Shape onto a Standard Rectangle
	private Point2D.Double coorUpperCurve[] = new Point2D.Double[rect_width];
	private Point2D.Double coorLowerCurve[] = new Point2D.Double[rect_width];
	private Point2D.Double a, b, c, d;

	private BinMorpher binMorpher = new BinMorpher();

	// coordinates to draw dorsal portion of the frog
	public static final int dorsalImgX = 10, dorsalImgY = 30;
	// coordinates to draw color fingerprint
	public static final int ColorRectImgX = 110, ColorRectImgY = 50;
	// coordinates to draw Edge Fingerprint
	public static final int EdgelImgX = 110, EdgelImgY = 270;
	// coordinates to draw Resulting Binary image
	public static final int BinaryImgX = 110, BinaryImgY = 490;

	private Color rectColor = new Color(191, 184, 191);

	// Pencil
	ArrayList<Pencil_Line> pencilCoor = new ArrayList<Pencil_Line>();
	public int drawingNumber = 0;
	// Spot Filler
	public ArrayList<SpotFiller> spotFilledCoor = new ArrayList<SpotFiller>();
	public int filledSpotNumber = 0;
	
	private FolderHandler fh;

	/**
	 * Constructor for with an image
	 * 
	 * @param frame
	 *            holding frame
	 * @param filename
	 *            name of the image to be manipulated
	 */
	public ImagePanel(ImageManipFrame frame, FolderHandler fh, File inputfile) {
		parentFrame = frame;
		imageFile = inputfile;
		this.fh = fh;
		
		System.out.println("ImagePanel inputfile = " + inputfile.getName());

		// initialize control points for two curves
		firstCurvePoints[0] = new Point2D.Double();
		firstCurvePoints[1] = new Point2D.Double();
		firstCurvePoints[2] = new Point2D.Double();
		firstCurvePoints[3] = new Point2D.Double();
		secondCurvePoints[0] = new Point2D.Double();
		secondCurvePoints[1] = new Point2D.Double();
		secondCurvePoints[2] = new Point2D.Double();
		secondCurvePoints[3] = new Point2D.Double();
		leftBoundLine[0] = new Point2D.Double();
		leftBoundLine[1] = new Point2D.Double();
		leftLimitBoundLine1[0] = new Point2D.Double();
		leftLimitBoundLine1[1] = new Point2D.Double();
		leftLimitBoundLine2[0] = new Point2D.Double();
		leftLimitBoundLine2[1] = new Point2D.Double();
		rightBoundLine[0] = new Point2D.Double();
		rightBoundLine[1] = new Point2D.Double();
		selectedPoint = null;

		try {
			init();
		} catch (Exception e) {
			System.out.println("ImagePanel.ImagePanel() Exception");
			e.printStackTrace();
		}
	}

	/**
	 * Sets up initial panel
	 * 
	 * @throws Exception
	 */
	private void init() throws Exception {
		// get the jvm heap size.
		long heapSize = Runtime.getRuntime().totalMemory();
		// Print the jvm heap size.
		long Used_memory = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
		System.out.println("\tImagePanel.init() Heap: Size = " + heapSize + " Used = " + Used_memory);
		setBackground(maskColor);
		setVisible(true);
		// initialize start point
		start = new Point2D.Double();
		addMouseMotionListener(new ImagePanel_this_mouseMotionAdapter(this));
		addMouseListener(new ImagePanel_this_mouseAdapter(this));
		try {
			importFile(imageFile);
		} catch (IOException ex) {
			System.err.println("could not import file");
		}

		origImage = image;
	}

	/**
	 * the paint components of the panel
	 * 
	 * @param g
	 *            Graphics
	 */
	@Override
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		Graphics2D G2D = (Graphics2D) g;

		// Use antialiasing.
		G2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

		// DrawImage
		drawTheImage(G2D);

		// if true, draws a crosshair where the center point is located
		if (showCenterPoint) {
			drawCenterPoint(G2D);
		}

		if (showBoundingBox) {
			G2D.draw(boundingBox);
		}

		if (showCenterLine) {
			bandLine.drawLine(G2D, boxColor, convertLineStyle(1, 2));
			bandLine.drawLine(G2D, revBoxColor, convertLineStyle(2, 2));
		}

		if (firstRubberBoxOn) {
			DrawRubberBandBoxFirst(G2D);
		}

		if (secondRubberBoxOn) {
			DrawRubberBandBoxSecond(G2D);
		}

		if (rubberBandLineOn && firstClick) {
			DrawRubberBandLine(G2D);
		}

		if (ridgeInBox && oneEyeClick && !reset) {
			DrawEyeClicks(G2D);//
		}

		if (ridgeInBox && twoEyeClicks) {
			DrawEyeClicks(G2D);//
			DrawRubberRidgeInBox(G2D);
			DrawBoundRidgeLines(G2D);
			DrawCurve(G2D);
		}

		if (!ridgeInBox && twoEyeClicks && ridgeRect) {
			DrawEyeClicks(G2D);//
			DrawBoundRidgeLines(G2D);
			DrawCurve(G2D);
		}
	} // end paint component

	/**
	 * imports an image file, reduces it to screen size, and displays it on the screen
	 * 
	 * @param inputfile
	 *            File image file to be manipulated
	 * @throws IOException
	 */
	public void importFile(File inputfile) throws IOException {
		try {
			// Modified to operate on more computers...
			Image tempImage = ImageIO.read(inputfile);
			BufferedImage newImage = new BufferedImage(tempImage.getWidth(null), tempImage.getHeight(null), BufferedImage.TYPE_3BYTE_BGR);
			Graphics2D gc = newImage.createGraphics();
			gc.drawImage(tempImage, 0, 0, null);
			image = newImage;
			System.out.println("\tImage File Size: " + image.getWidth() + " x " + image.getHeight());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Exports, resamples, and renames an image file to a desired matrix size
	 * 
	 * @param dir
	 *            String the file name where the downsampled image is to be exported
	 * @return String the actual full path of the exported file
	 */
	public String exportDownsampledImage(String dir) {
		String localFilename = getImageName();
		// next lines create dor_ image ratioW x 256
		double ratioH1 = dorImgHeight / image.getHeight();
		resampleImage(ratioH1 * image.getWidth(), (int) dorImgHeight);

		saveImage(image, dir, getImageName());
		saveImage(image, fh.getDorsalFolder(), localFilename);

		// next 2 lines create thumb nail ratioW x 64
		double ratioH2 = thumbImgHeight / image.getHeight();
		resampleImage(ratioH2 * image.getWidth(), thumbImgHeight);

		saveImage(image, fh.getThumbnailFolder(), localFilename);
		return localFilename;
	}

	/**
	 * Exports a BufferedImage file to a .png file
	 * 
	 * @param newImageFile
	 *            File the file where the image is to be exported
	 * @return File that file
	 */
	public File exportImage(File newImageFile) {
		try {
			ImageIO.write(image, "png", newImageFile);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return newImageFile;
	}

	// Deletes temporary files used in Digital Signature Creation
	protected void deleteTempFiles() {
		String path = imageFile.getParent();
		if (path == null) {
			path = "";
		} else {
			path = path + File.separator;
		}

		for (int i = 0; i < maxOperations; i++) {
			File file = new File(path + "temp_nos" + i + ".PNG");
			try {
				file.delete();
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
	}

	private class Pencil_Line {
		public int drawingNum;
		public Point2D.Double startL;
		public Point2D.Double endL;

		public Pencil_Line(int drawingNum, Point2D.Double startLine, Point2D.Double endLine) {
			this.drawingNum = drawingNum;
			startL = startLine;
			endL = endLine;
		}
	}

	// draw with Pencil on Color Image
	public void drawWithPencil(Point2D.Double end) {

		BasicStroke stroke = new BasicStroke(2, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);

		Point2D.Double startLine = new Point2D.Double(start.getX(), start.getY());
		Point2D.Double endLine = new Point2D.Double(end.getX(), end.getY());
		Pencil_Line pencil_Line = new Pencil_Line(drawingNumber, startLine, endLine);
		pencilCoor.add(pencil_Line);

		// draw on Color fingerprint
		Graphics2D g1 = standardRectColor.createGraphics();
		g1.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g1.setStroke(stroke);
		g1.setColor(Color.red);
		g1.draw(new Line2D.Double(start, end));

		// simultaneously draw on standardRectEdgesDilated image
		Graphics2D g2 = standardRectEdgesDilated.createGraphics();
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g2.setColor(Color.white);
		g2.setStroke(stroke);
		g2.draw(new Line2D.Double(start, end));

		g1.dispose();
		g2.dispose();
		repaint();
	}

	/**
	 * Displays a Graphics2D image on the screen
	 * 
	 * @param G2D
	 *            Graphics2D
	 */
	public void drawTheImage(Graphics2D G2D) {
		// Draw Images

		try {
			if (shrinkImageToFit) {
				shrinkImage();
				shrinkImageToFit = false;
			}
			if (stretchImageToFit) {
				expandImage();
				stretchImageToFit = false;
			}
			if (imageAsDorsal) {
				image = imageDorsal;
				G2D.setColor(rectColor);
				G2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
				G2D.draw(new RoundRectangle2D.Double(dorsalImgX - 3, dorsalImgY - 3, image.getWidth() + 6, image.getHeight() + 6, 8, 8));
				G2D.setColor(Color.black);
				G2D.setFont(new java.awt.Font("SansSerif", Font.BOLD, 15));
				G2D.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_GASP);
				G2D.drawString("Dorsal Pattern", (image.getWidth() / 2) - 20, 22);
				G2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
				G2D.drawImage(image, null, dorsalImgX, dorsalImgY);
			}

			// Spot Extraction
			if (spotextraction || undopencil || undofillspot) {

				int x_title = rect_width / 2;
				// display Color rectangle
				G2D.setColor(rectColor);
				G2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
				G2D.draw(new RoundRectangle2D.Double(ColorRectImgX - 3, ColorRectImgY - 3, rect_width + 6, rect_height + 6, 8, 8));
				G2D.setColor(Color.black);
				G2D.setFont(new java.awt.Font("SansSerif", Font.BOLD, 14));
				G2D.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_GASP);
				G2D.drawString("Color Fingerprint", x_title + 52, ColorRectImgY - 6);
				G2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
				G2D.drawImage(standardRectColor, null, ColorRectImgX, ColorRectImgY);

				// display detected Edges rectangle
				G2D.setColor(rectColor);
				G2D.draw(new RoundRectangle2D.Double(EdgelImgX - 3, EdgelImgY - 3, rect_width + 6, rect_height + 6, 8, 8));
				G2D.setColor(Color.black);
				G2D.setFont(new Font("Default", Font.BOLD, 14));
				G2D.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_GASP);
				G2D.drawString("Spot Extraction", x_title + 57, EdgelImgY - 6);
				G2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
				G2D.drawImage(standardRectEdgesDilated, null, EdgelImgX, EdgelImgY);

				/*
				 * clear standardRectBinary image by means of drawing on it java does not have a
				 * method to clear BufferedImage
				 */
				if (reset) {
					// clear images if back/restart buttons pressed
					BufferedImage cleanImage = new BufferedImage(rect_width, rect_height, BufferedImage.TYPE_3BYTE_BGR);
					Graphics2D g2D = standardRectBinary.createGraphics();
					g2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
					g2D.drawImage(cleanImage, 0, 0, rect_width, rect_height, null);
					reset = false;
				}

				// display resulting binary rectangle
				G2D.setColor(Color.black);
				G2D.setFont(new java.awt.Font("SansSerif", Font.BOLD, 14));
				G2D.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_GASP);
				G2D.drawString("Binary Fingerprint", x_title + 48, BinaryImgY - 6);
				G2D.setColor(rectColor);
				G2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
				G2D.draw(new RoundRectangle2D.Double(BinaryImgX - 3, BinaryImgY - 3, rect_width + 6, rect_height + 6, 5, 5));
				G2D.drawImage(standardRectBinary, null, BinaryImgX, BinaryImgY);
			}

			if (!spotextraction & !imageAsDorsal) {
				G2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
				G2D.drawImage(image, null, 0, 0);
			}
		} catch (Exception ex) {
			System.err.println("Could not import image");
			ex.printStackTrace();
		}
	}

	// Reduces an image to the size of the screen
	private void shrinkImage() {
		double width = image.getWidth();
		double height = image.getHeight();
		// SnoutSpot Panel is next to the dorsal image with width = 140
		if (width > getWidth() && height > getHeight()) {
			double origWidth = width;
			width = getWidth();
			double ratioW = width / origWidth;
			double origHeight = height;
			height = getHeight();
			double ratioH = height / origHeight;

			if (ratioW < ratioH) {
				scaleImage(ratioW, ratioW);
			} else {
				scaleImage(ratioH, ratioH);
			}
		}
	}

	// Streches an image to fit the screen - Not currently used
	public void expandImage() {
		double origWidth = image.getWidth();
		double origHeight = image.getHeight();
		double width = origWidth;
		double height = origHeight;
		double ratioW = width / origWidth;
		double ratioH = height / origHeight;
		if (width < getWidth()) {
			width = getWidth();
			ratioW = width / origWidth;
		}
		if (height < getHeight()) {
			height = getHeight();
			ratioH = height / origHeight;
		}
		if (ratioW < ratioH) {
			resampleImage(ratioW * origWidth, ratioW * origHeight);
		} else {
			resampleImage(ratioH * origWidth, ratioH * origHeight);
		}
	}

	/**
	 * Draws a center point - Not currently Used
	 * 
	 * @param G2D
	 *            Graphics2D
	 */
	public void drawCenterPoint(Graphics2D G2D) {
		G2D.setColor(centerColor);
		G2D.drawOval((int) center.getX() - 5, (int) center.getY() - 5, 10, 10);
		G2D.fillOval((int) center.getX() - 1, (int) center.getY() - 1, 2, 2);
		G2D.drawLine((int) center.getX(), (int) center.getY() - 7, (int) center.getX(), (int) center.getY() + 7);
		G2D.drawLine((int) center.getX() - 7, (int) center.getY(), (int) center.getX() + 7, (int) center.getY());
	}

	// Not currently Used
	public void setCenter() {
		center = new Point(getWidth() / 2, getHeight() / 2);
	}

	// Not currently Used
	public void setImageCenter() {
		center = new Point(image.getWidth() / 2, image.getHeight() / 2);
	}

	// Sets the rotating point of the image
	protected void setCenter(Point Center) {
		center = Center;
	}

	/**
	 * Sets the rotating point of the image
	 * 
	 * @param x
	 *            int x coordinate
	 * @param y
	 *            int y coordinate
	 */
	public void setCenter(int x, int y) {
		center = new Point(x, y);
	}

	// - Not currently Used
	@SuppressWarnings("unused")
	private Point getCenter() {
		return center;
	}

	// Sets image to the original
	protected void resetToOrigImage() {
		image = origImage;
		shrinkImageToFit = true;
		deleteTempFiles();
		repaint();
	}

	/**
	 * Crops the image based on the bounding oval
	 */
	public void cropImage() {
		// Create a cropped version of the image
		if (boundingBox.getX() + boundingBox.getWidth() > image.getWidth()) {
			boundingBox.setSize((int) (image.getWidth() - boundingBox.getX()), (int) boundingBox.getHeight());
		}
		if (boundingBox.getY() + boundingBox.getHeight() > image.getHeight()) {
			boundingBox.setSize((int) boundingBox.getWidth(), (int) (image.getHeight() - boundingBox.getY()));
		}

		BufferedImage filteredImage = image.getSubimage((int) boundingBox.getX(), (int) boundingBox.getY(), (int) boundingBox.getWidth(), (int) boundingBox.getHeight());
		image = filteredImage;
		imageDorsal = image;
	}

	// Creates a matrix based on a the raster of the 2D image, then reduces it
	// by a factor
	// of <reductionFactorRGB>
	@SuppressWarnings("unused")
	public String fillMatrix() {
		String Path = parentFrame.getParentFrame().node.get("installDir", null);
		String fileName = imageFile.getName();
		try {
			FileOutputStream out = new FileOutputStream(Path, true);
			PrintWriter writer = new PrintWriter(out);
			// Create a downsampled version of the image and translate values
			// into a matrix
			double pixel;
			resampleImage(matrixCol, matrixRow);
			for (int row = 0; row < matrixRow; row++) {
				for (int col = 0; col < matrixCol; col++) {
					pixel = image.getRGB(col, row) / reductionFactorRGB;
					valueMatrix[row][col] = pixel;
					writer.print(pixel + " "); // TESTING
				}
				writer.println(); // TESTING
			}
		} catch (Exception e) {
		}
		return Path + File.separator + "tempOutput.txt";
	}

	// Filtering Operations
	private void Filter(BufferedImageOp op) {
		if (image == null) {
			return;
		}

		image = op.filter(image, null);
		repaint();
	}

	private void Filter(BufferedImageOp op, int width, int height) {
		if (image == null) {
			return;
		}

		image = op.filter(image, null);
		repaint();
	}

	@SuppressWarnings("unused")
	private void Convolve(float[] elements) {
		Kernel kernel = new Kernel(3, 3, elements);
		ConvolveOp op = new ConvolveOp(kernel);
		Filter(op);
	}

	/**
	 * Scales an image based on a set of scaling ratios
	 * 
	 * @param w
	 *            double width scaling ratio
	 * @param h
	 *            double height scaling ratio
	 */
	public void scaleImage(double w, double h) {
		if (image == null) {
			return;
		}

		AffineTransform transform = AffineTransform.getScaleInstance(w, h);
		AffineTransformOp op = new AffineTransformOp(transform, AffineTransformOp.TYPE_BICUBIC);
		Filter(op);
	}

	/**
	 * Changes the image size based on desired Dimensions
	 * 
	 * @param w
	 *            double desired width of the new image
	 * @param h
	 *            double desired height of the new image *
	 */
	public void resampleImage(double w, double h) {
		if (image == null) {
			return;
		}
		double width = w / image.getWidth();
		double height = h / image.getHeight();
		System.out.println(" resampleImage image.getWidth() " + image.getWidth() + " height " + image.getHeight());
		System.out.println("resampleImage width " + width + " height " + height);
		AffineTransform transform = AffineTransform.getScaleInstance(width, height);
		AffineTransformOp op = new AffineTransformOp(transform, AffineTransformOp.TYPE_BICUBIC);
		Filter(op, (int) w, (int) h);

	}

	// Rotates the image theta around the point (x, y)
	private void rotateImage(double theta, double x, double y) {
		if (image == null) {
			return;
		}

		AffineTransform transform = AffineTransform.getRotateInstance(theta, x, y);
		AffineTransformOp op = new AffineTransformOp(transform, AffineTransformOp.TYPE_BICUBIC);
		Filter(op);
	}

	/**
	 * Increases image size by 10%
	 */
	public void zoomIn() {
		scaleImage(1.10, 1.10);
	}

	/**
	 * Decreases image size by 10%
	 */
	public void zoomOut() {
		scaleImage(0.90, 0.90);
	}

	// Box Functions
	public void DrawRubberBandBoxFirst(Graphics2D G2D) {
		if (bandLine.getY_1() > mouseY2) {
			boundingBox.setBounds((int) bandLine.getX_1(), (int) mouseY2, (int) bandLine.getLength(), (int) Math.abs(mouseY2 - bandLine.getY_1()));
			G2D.setColor(boxColor);
			G2D.setStroke(convertLineStyle(1, 2));
			G2D.draw(boundingBox);
			G2D.setStroke(convertLineStyle(2, 2));
			G2D.setColor(revBoxColor);
			G2D.draw(boundingBox);
		}
	}

	public void DrawRubberBandBoxSecond(Graphics2D G2D) {
		if (bandLine.getY_1() < mouseY2) {
			boundingBox.setBounds((int) bandLine.getX_1(), (int) mouseY1, (int) bandLine.getLength(), (int) Math.abs(mouseY2 - mouseY1));

		} else {
			boundingBox.setBounds((int) bandLine.getX_1(), (int) mouseY1, (int) bandLine.getLength(), (int) Math.abs(mouseY1 - bandLine.getY_1()));

		}
		G2D.setColor(boxColor);
		G2D.setStroke(convertLineStyle(1, 2));
		G2D.draw(boundingBox);
		G2D.setStroke(convertLineStyle(2, 2));
		G2D.setColor(revBoxColor);
		G2D.draw(boundingBox);
	}

	private Shape controlPoint(Point2D currentPoint) {
		// control point appear like a small square
		int side = 10;

		return new Rectangle2D.Double(currentPoint.getX() - side / 2, currentPoint.getY() - side / 2, side, side);
	}

	public void DrawEyeClicks(Graphics2D G2D) {
		G2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		G2D.setColor(Color.yellow);
		if (oneEyeClick) {
			G2D.fill(controlPoint(firstEyeClick));
		}
		if (twoEyeClicks) {
			G2D.setColor(Color.yellow);
			G2D.fill(controlPoint(firstEyeClick));
			G2D.fill(controlPoint(secondEyeClick));
		}
	}

	public void DrawBoundRidgeLines(Graphics2D G2D) {
		// left, left limit, and right boundary lines for ridge
		Line2D left = new Line2D.Double(leftBoundLine[0], leftBoundLine[1]);
		Line2D right = new Line2D.Double(rightBoundLine[0], rightBoundLine[1]);

		G2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		G2D.setStroke(ridgeBoundaryStroke);
		G2D.setColor(new Color(230, 0, 0));

		G2D.draw(left);
		G2D.draw(right);
	}

	// Ridge in a Box Function
	public void DrawRubberRidgeInBox(Graphics2D G2D) {

		distanceBnEyes = Math.abs(mouseY2 - mouseY1);

		double rectVertex_X1 = mouseX1 - factor * distanceBnEyes;
		double rectVertex_X2 = mouseX2 - factor * distanceBnEyes;

		// points for left/right boundaries for ridge
		leftBoundLine[0].setLocation(rectVertex_X1, mouseY1);
		leftBoundLine[1].setLocation(rectVertex_X2, mouseY2);

		rightBoundLine[0].setLocation(mouseX1 - dist_from_eyes * distanceBnEyes, mouseY1);
		rightBoundLine[1].setLocation(mouseX2 - dist_from_eyes * distanceBnEyes, mouseY2);
		// draw first curve
		firstCurvePoints[0].setLocation(rectVertex_X1, mouseY1);
		firstCurvePoints[3].setLocation(mouseX1 - dist_from_eyes * distanceBnEyes, mouseY1);
		double firstcurveDistance = firstCurvePoints[3].x - firstCurvePoints[0].x;
		firstCurvePoints[1].setLocation((rectVertex_X1 + 0.33 * firstcurveDistance), mouseY1);
		firstCurvePoints[2].setLocation((rectVertex_X1 + 0.67 * firstcurveDistance), mouseY1);

		// draw second curve
		secondCurvePoints[0].setLocation(rectVertex_X2, mouseY2);
		secondCurvePoints[3].setLocation(mouseX2 - dist_from_eyes * distanceBnEyes, mouseY2);
		double secondcurveDistance = secondCurvePoints[3].x - secondCurvePoints[0].x;
		secondCurvePoints[1].setLocation((rectVertex_X2 + 0.33 * secondcurveDistance), mouseY2);
		secondCurvePoints[2].setLocation((rectVertex_X2 + 0.67 * secondcurveDistance), mouseY2);

		ridgeRect = true;
		ridgeInBox = false;
	}

	public void DrawCurve(Graphics2D G2D) {
		// draw first curve
		G2D.setStroke(ridgeBoundaryStroke);
		G2D.setPaint(Color.yellow);
		G2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

		CubicCurve2D firstCurve = new CubicCurve2D.Float();
		firstCurve.setCurve(firstCurvePoints, 0);
		G2D.setColor(new Color(230, 0, 0));
		G2D.draw(firstCurve);

		// draw second curve
		CubicCurve2D secondCurve = new CubicCurve2D.Float();
		secondCurve.setCurve(secondCurvePoints, 0);
		G2D.setColor(new Color(230, 0, 0));
		G2D.draw(secondCurve);

		// change color of control points to red when moved
		for (int i = 0; i < pts; i++) {
			// If the point is selected, use the selected color.
			if (firstCurvePoints[i] == selectedPoint) {
				G2D.setPaint(new Color(255, 0, 0));
			} else {
				G2D.setPaint(new Color(255, 0, 0));
			}
			// draw control point
			G2D.fill(controlPoint(firstCurvePoints[i]));
		}

		for (int i = 0; i < pts; i++) {
			// If the point is selected, use the selected color.
			if (secondCurvePoints[i] == selectedPoint) {
				G2D.setPaint(new Color(255, 0, 0));
			} else {
				G2D.setPaint(new Color(255, 0, 0));
			}
			// draw control point
			G2D.fill(controlPoint(secondCurvePoints[i]));
		}
	}

	// Line Functions
	public void DrawRubberBandLine(Graphics2D G2D) {
		G2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		G2D.setColor(boxColor);
		G2D.setStroke(convertLineStyle(1, 2));
		// when clicked on the tail
		G2D.drawLine((int) mouseX1, (int) mouseY1, (int) mouseX2, (int) mouseY2);
		G2D.setStroke(convertLineStyle(2, 2));
		G2D.setColor(revBoxColor);
		// when clicked on snout
		G2D.drawLine((int) mouseX1, (int) mouseY1, (int) mouseX2, (int) mouseY2);
	}

	public double getCorrectionAngle(double x1, double y1, double x2, double y2) {
		double angle = 0;
		double length = 0;
		double a = y1 - y2;
		double b = x1 - x2;
		if (b != 0) {
			angle = Math.atan(a / b);
		} else {
			angle = 0;
		}

		center = new Point((int) ((x1 + x2) / 2), (int) ((y1 + y2) / 2));

		bandLine.setLineProps(x1, y1, x2, y2);
		length = bandLine.getLength();
		bandLine.setLineProps(center.getX() - length / 2, center.getY(), center.getX() + length / 2, center.getY());

		if (x1 > x2) {
			angle = angle + Math.PI;
		}
		return angle;
	}

	protected static BasicStroke convertLineStyle(int style, int w) {
		BasicStroke bs = new BasicStroke(w);
		switch (style) {
			case 0: // solid
				bs = new BasicStroke(w);
				break;
			case 1: // dotted
				float dotted[] = { 2.0f };
				bs = new BasicStroke(w, BasicStroke.CAP_BUTT, BasicStroke.JOIN_ROUND, 10.0f, dotted, 0.0f);
				break;
			case 2: // reverse dotted
				float revdotted[] = { 2.0f };
				bs = new BasicStroke(w, BasicStroke.CAP_BUTT, BasicStroke.JOIN_ROUND, 0.0f, revdotted, 10.0f);
				break;
			case 3: // long dashed
				float longdashed[] = { 10.0f };
				bs = new BasicStroke(w, BasicStroke.CAP_BUTT, BasicStroke.JOIN_ROUND, 10.0f, longdashed, 0.0f);
				break;
			case 4: // dot dashed
				float dotdashed[] = { 6.0f, 4.0f, 2.0f, 4.0f };
				bs = new BasicStroke(w, BasicStroke.CAP_BUTT, BasicStroke.JOIN_ROUND, 10.0f, dotdashed, 0.0f);
				break;
			case 5: // dashed
				float dashed[] = { 5.0f };
				bs = new BasicStroke(w, BasicStroke.CAP_BUTT, BasicStroke.JOIN_ROUND, 10.0f, dashed, 0.0f);
				break;

		} // end switch
		return bs;
	}

	public void setuptoWorkOnRidges() {
		/*
		 * repare for clicking on the corners of the eyes set ridgeInBox = true
		 */
		imageAsDorsal = true;
		ridgeInBox = true;
		parentFrame.ExamplePanelContainer.setVisible(true);
		parentFrame.ExampleVent.setVisible(false);
		parentFrame.ExampleLSide.setVisible(false);
		parentFrame.ExampleRSide.setVisible(false);
		parentFrame.ExampleRidge.setVisible(true);
		parentFrame.TextAreaStep.setText("Step 5 of 7: CORNERS OF EYES SELECTION");
		parentFrame.TextAreaStatus.setText("Click on the lateral corners of the eyes.");

		parentFrame.setBackOn(true); // set Back button true
		parentFrame.butStartOver.setEnabled(true);
		if (parentFrame.step == 0) {
			parentFrame.increaseStep();
		}

	}

	// ************ MOUSE EVENTS *************************************//

	void this_mouseClicked(MouseEvent e) {

		if (firstRubberBoxOn || secondRubberBoxOn) {
			if (!firstClick) {
				mouseX1 = e.getX();
				mouseY1 = e.getY();
				setFirstRubberBoxOn(false);
				setSecondRubberBoxOn(true);
				firstClick = true;
				parentFrame.ExamplePanelContainer.setVisible(true);
				parentFrame.ExampleVent.setVisible(false);
				parentFrame.ExampleSnout.setVisible(false);
				parentFrame.ExampleLSide.setVisible(false);
				parentFrame.ExampleRSide.setVisible(true);
				parentFrame.ExampleRidge.setVisible(false);

				parentFrame.TextAreaStep.setText("Step 4 of 7: DORSAL PATTERN EXTRACTION");
				parentFrame.TextAreaStatus.setText("Click on the right side of the frog.");

			} else {
				mouseX2 = e.getX();
				mouseY2 = e.getY();
				boundingBox.setBounds((int) bandLine.getX_1(), (int) mouseY1, (int) bandLine.getLength(), (int) Math.abs(mouseY2 - mouseY1));
				setSecondRubberBoxOn(false);
				firstClick = false;
				showCenterLine = false;
				cropImage();
				setuptoWorkOnRidges();
				repaint();
			} // end if else firstClick
		} // end rubber box Line

		if (rubberBandLineOn) {
			if (!firstClick) {
				mouseX1 = e.getX();
				mouseY1 = e.getY();
				if (mouseX1 > image.getWidth() || mouseY1 > image.getHeight()) {
					new ErrorDialog("You are outside the image area. Please try again.");
					return;
				}
				parentFrame.TextAreaStep.setText("Step 2 of 7: BODY ALIGNMENT");
				parentFrame.TextAreaStatus.setText("Click on the frog's snout at the midline\nof the body.");

				parentFrame.ExamplePanelContainer.setVisible(true);
				parentFrame.ExampleVent.setVisible(false);
				parentFrame.ExampleSnout.setVisible(true);
				parentFrame.ExampleLSide.setVisible(false);
				parentFrame.ExampleRSide.setVisible(false);
				parentFrame.ExampleRidge.setVisible(false);

				firstClick = true;
			} else {
				mouseX2 = e.getX();
				mouseY2 = e.getY();
				double rotateAngle = -1 * getCorrectionAngle(mouseX1, mouseY1, mouseX2, mouseY2);
				setRubberBandLineOn(false);
				firstClick = false;
				showCenterLine = true;
				rotateImage(rotateAngle, center.getX(), center.getY());
				setFirstRubberBoxOn(true);
				parentFrame.ExamplePanelContainer.setVisible(true);
				parentFrame.ExampleVent.setVisible(false);
				parentFrame.ExampleSnout.setVisible(false);
				parentFrame.ExampleLSide.setVisible(true);
				parentFrame.ExampleRSide.setVisible(false);
				parentFrame.ExampleRidge.setVisible(false);
				parentFrame.TextAreaStep.setText("Step 3 of 7: DORSAL PATTERN EXTRACTION");
				parentFrame.TextAreaStatus.setText("Click on the left side of the frog.");

				return;
			} // end if else firstClick
		} // end rubber band Line

		if (centerWithMouseClick) {
			center.setLocation(e.getX(), e.getY());
			repaint();
		}

		// ************* USER CLICKS ON SPOTS *******//
		// users clicks inside spots and they are being filled and displayed
		if (fillspot && !pencil) {
			if (e.getX() >= EdgelImgX & e.getX() <= EdgelImgX + rect_width & e.getY() >= EdgelImgY & e.getY() <= EdgelImgY + rect_height) {
				int mouse_x = e.getX();
				int mouse_y = e.getY();

				Point floodpoint = new Point(mouse_x - EdgelImgX, mouse_y - EdgelImgY);
				Color white = new Color(255, 255, 255, 255);

				++filledSpotNumber;
				// System.out.println("press filledSpotNumber " +
				// filledSpotNumber + " dilation_radius " + dilation_radius);

				FillSpot spot = new FillSpot();
				standardRectFilled = spot.floodFill(standardRectEdgesDilated, white, floodpoint);
				BufferedImage imgWithNewSpotFilled = spot.getFilledSpot(dilation_radius, filledSpotNumber);
				updateBinaryImage(imgWithNewSpotFilled);

				ArrayList<SpotFiller> currentSpotFilledCoor = spot.getFilledSpotCoor();
				spotFilledCoor.addAll(currentSpotFilledCoor);
				repaint();

			} else {
				new ErrorDialog("You are outside the image area. Please try again.");
			}
		}

	}

	void this_mouseDragged(MouseEvent e) {

		if (selectedPoint != null) {

			/* moving control points not on the left/right boundaries */
			if (selectedPoint != firstCurvePoints[0] & selectedPoint != firstCurvePoints[3] & selectedPoint != secondCurvePoints[0] & selectedPoint != secondCurvePoints[3]) {
				selectedPoint.setLocation(e.getPoint());
				repaint();
			}
			/*
			 * left boundary line moves up/down while user drags the contol point
			 */
			if (selectedPoint == firstCurvePoints[0]) {
				firstCurvePoints[0].x = leftBoundLine[0].x;
				leftBoundLine[0].y = e.getY();
				selectedPoint.setLocation(leftBoundLine[0].x, e.getY());
				repaint();
			}
			if (selectedPoint == secondCurvePoints[0]) {
				secondCurvePoints[0].x = leftBoundLine[1].x;
				leftBoundLine[1].y = e.getY();
				selectedPoint.setLocation(leftBoundLine[1].x, e.getY());
				repaint();
			}
			/*
			 * right boundary line moves up/down while user drags the contol point
			 */
			if (selectedPoint == firstCurvePoints[3]) {
				firstCurvePoints[3].x = rightBoundLine[0].x;
				rightBoundLine[0].y = selectedPoint.getY();
				selectedPoint.setLocation(rightBoundLine[0].x, e.getY());
				repaint();
			}
			if (selectedPoint == secondCurvePoints[3]) {
				secondCurvePoints[3].x = rightBoundLine[1].x;
				rightBoundLine[1].y = selectedPoint.getY();
				selectedPoint.setLocation(rightBoundLine[1].x, e.getY());
				repaint();
			}
			// } else new
			// ErrorDialog("You are outside the image area. Please try again.");
		}

		if (pencil) {
			// coordinates to draw color fingerprint
			if (e.getX() >= ColorRectImgX & e.getX() <= ColorRectImgX + rect_width & e.getY() >= ColorRectImgY & e.getY() <= ColorRectImgY + rect_height) {
				Point2D.Double p = new Point2D.Double();
				p.setLocation(e.getX() - ColorRectImgX, e.getY() - ColorRectImgY);
				drawWithPencil(p);
				start.setLocation(p.getX(), p.getY());
			}
		}
	}

	void this_mouseReleased(MouseEvent e) {
		if (pencil) {
			// clear start point
			// start = new Point2D.Double(-1, -1);
		}
	}

	void this_mousePressed(MouseEvent e) {
		if (ridgeInBox) {
			reset = false;
			if (mouseX1 != mouseX2 || mouseY1 != mouseY2) {
				mouseX1 = e.getX();
				mouseY1 = e.getY();
				firstEyeClick.setLocation(mouseX1, mouseY1);
				mouseX2 = mouseX1;
				mouseY2 = mouseY1;
				oneEyeClick = true;
			} else {
				mouseX2 = e.getX();
				mouseY2 = e.getY();
				secondEyeClick.setLocation(mouseX2, mouseY2);
				twoEyeClicks = true;
				oneEyeClick = false;
				parentFrame.setNextOn(true);
				parentFrame.TextAreaStep.setText("Step 6 of 7: DORSOLATERAL FOLDS ALIGNMENT");
				parentFrame.TextAreaStatus.setText("Align upper and lower boundaries to inner\ndorsolateral folds by moving the control points.\nThen click Next>>");
			}
			repaint();
			reset = false;
		}

		if (!ridgeInBox && twoEyeClicks && ridgeRect) {
			selectedPoint = null;
			for (int i = 0; i < pts; ++i) {
				Shape s1 = controlPoint(firstCurvePoints[i]);
				Shape s2 = controlPoint(secondCurvePoints[i]);
				if (s1.contains(e.getPoint())) {
					selectedPoint = firstCurvePoints[i];
					break;
				}
				if (s2.contains(e.getX(), e.getY())) {
					selectedPoint = secondCurvePoints[i];
					break;
				}
			}
			repaint();
		}

		if (pencil) {
			if (e.getX() >= ColorRectImgX & e.getX() < ColorRectImgX + rect_width & e.getY() >= ColorRectImgY & e.getY() < ColorRectImgY + rect_height) {
				// new drawing
				++drawingNumber;
				System.out.println("press " + drawingNumber);
				start.setLocation(e.getX() - ColorRectImgX, e.getY() - ColorRectImgY);
			}
		}
	}

	void this_mouseMoved(MouseEvent e) {
		if (rubberBandLineOn || firstRubberBoxOn || secondRubberBoxOn || eraserOn) {
			mouseX2 = e.getX();
			mouseY2 = e.getY();
			repaint();
		}
	}

	protected void dustImage() {
		setUndoPoint();
		// maxDustPixels
		Cursor lastCursor = getCursor();
		setCursor(new Cursor(Cursor.WAIT_CURSOR));
		int width = image.getWidth();
		int height = image.getHeight();
		int mask = maskColor.getRGB();
		int startPixel;
		int testPixel;
		int endPixel = 0;
		boolean foundPixel = false;
		int dustPixels = maxDustPixels;
		for (int y = 0; y < height; y++) {
			if (y + dustPixels + 1 > height) {
				dustPixels = height - y;
			}
			for (int x = 0; x < width; x++) {
				startPixel = image.getRGB(x, y);
				if (startPixel == mask) {
					for (int t = y + 1; t < y + dustPixels; t++) {
						testPixel = image.getRGB(x, t);
						if (t == y + 1 && testPixel == mask) {
							foundPixel = false;
							break;
						}
						testPixel = image.getRGB(x, t);
						if (testPixel == mask) {
							endPixel = t;
							foundPixel = true;
							break;
						} else {
							foundPixel = false;
						}
					}

					if (foundPixel) {
						for (int i = y + 1; i < endPixel; i++) {
							image.setRGB(x, i, mask);
						}
					}
				}
			}
		}
		repaint();
		setCursor(lastCursor);
	}

	/**
	 * Sets a point that can be undone
	 */
	public void setUndoPoint() {
		previousImage = new BufferedImage(image.getWidth(null), image.getHeight(null), BufferedImage.TYPE_3BYTE_BGR);
		Graphics2D gc = previousImage.createGraphics();
		gc.drawImage(image, 0, 0, null);
		parentFrame.setMenuItemUndo(true);
	}

	/**
	 * Undoes last action
	 */
	public void undoPaint() {
		image = previousImage;
		parentFrame.setMenuItemUndo(false);
		repaint();
	}

	/*
	 * Oksana Kelly: Mapping Shape (formed by upper/lower bezier curves) onto Rectangle rect_width,
	 * rect_height
	 */
	public void mapOntoRectangle() {
		if (image == null) {
			return;
		}

		// GET THE POINTS ON CUBIC UPPER/LOWER CURVES
		// which curve is upper, lower
		if (firstCurvePoints[0].getY() < secondCurvePoints[0].getY()) {
			getBezierPoints(firstCurvePoints, "upper");
			getBezierPoints(secondCurvePoints, "lower");
		} else {
			getBezierPoints(secondCurvePoints, "upper");
			getBezierPoints(firstCurvePoints, "lower");
		}

		/* MAP ONTO STANDARD RECTANGLE */
		double norm = (1.0 / (rect_height - 1));
		for (int col = 0; col < rect_width; ++col) {

			double x_uppercurve = coorUpperCurve[col].getX() - dorsalImgX; // shifted
			// on
			// the
			// Panel
			double x_lowercurve = coorLowerCurve[col].getX() - dorsalImgX; // shifted
			// on
			// the
			// Panel
			double y_uppercurve = coorUpperCurve[col].getY() - dorsalImgY; // shifted
			// on
			// the
			// Panel
			double y_lowercurve = coorLowerCurve[col].getY() - dorsalImgY; // shifted
			// on
			// the
			// Panel

			for (int row = 0; row < rect_height; ++row) {

				double x = norm * ((rect_height - 1 - row) * x_uppercurve + row * x_lowercurve);
				double y = norm * ((rect_height - 1 - row) * y_uppercurve + row * y_lowercurve);

				int p = (int) Math.rint(getInterpolatedPixel(x, y)); // bicubic
				// interpolation
				standardRectColor.setRGB(col, row, p);

				/* CONVERT TO GRAYSCALE with weights from ITU-BT.709, Wikipedia */
				int red = (p & 0xff0000) >> 16;
				int green = (p & 0x00ff00) >> 8;
				int blue = p & 0x0000ff;
				int gray = (int) (0.2126 * red + 0.7152 * green + 0.0722 * blue);

				int graypix = (gray & 0xff) << 16 | (gray & 0xff) << 8 | gray & 0xff;
				standardRectGray.setRGB(col, row, graypix);

			}
		}

		/* CONTRAST NORMALIZATION */
		adjustContrast();
	}

	// constrast normalization
	private void adjustContrast() {

		// img_matrix_as_array to do the sorting
		int[] img_matrix_as_array = new int[rect_width * rect_height];

		Raster grayRaster = standardRectGray.getRaster();

		int ind = 0;
		for (int r = 0; r < rect_height; ++r) {
			for (int c = 0; c < rect_width; ++c) {
				img_matrix_as_array[ind] = grayRaster.getSample(c, r, 1);
				++ind;
			}
		}

		// get upper/lower quantile
		Arrays.sort(img_matrix_as_array);
		int lowerQuantile_index = (int) Math.floor(0.05 * (rect_width * rect_height));
		int upperQuantile_index = (int) Math.floor(0.95 * (rect_width * rect_height));
		int a_low = img_matrix_as_array[lowerQuantile_index];
		int a_high = img_matrix_as_array[upperQuantile_index];

		// contrast stretching or normalization
		int a_min = 0;
		int a_max = 255;
		for (int row = 0; row < rect_height; ++row) {
			for (int col = 0; col < rect_width; ++col) {

				if (grayRaster.getSample(col, row, 1) <= a_low) {
					int graypix = (a_min & 0xff) << 16 | (a_min & 0xff) << 8 | a_min & 0xff;
					standardRectGray.setRGB(col, row, graypix);
				}

				if (grayRaster.getSample(col, row, 1) >= a_high) {
					int graypix = (a_max & 0xff) << 16 | (a_max & 0xff) << 8 | a_max & 0xff;
					standardRectGray.setRGB(col, row, graypix);
				}

				if (grayRaster.getSample(col, row, 1) > a_low & grayRaster.getSample(col, row, 1) < a_high) {
					int value = a_min + (int) Math.floor((double) (grayRaster.getSample(col, row, 1) - a_low) * (a_max - a_min) / (a_high - a_low));
					int valuepix = (value & 0xff) << 16 | (value & 0xff) << 8 | value & 0xff;
					standardRectGray.setRGB(col, row, valuepix);
				}
			}

		}
	}

	/*
	 * http://www.cubic.org/docs/bezier.htm Use DeCasteljau algorithm to get coordinates of all the
	 * points on bezier curve, t = [0; 1]
	 */
	public void getBezierPoints(Point2D ctrPointscurve[], String whichCurve) {

		Point2D.Double point = new Point2D.Double();
		a = new Point2D.Double();
		b = new Point2D.Double();
		c = new Point2D.Double();
		d = new Point2D.Double();
		a.setLocation(ctrPointscurve[0].getX(), ctrPointscurve[0].getY());
		b.setLocation(ctrPointscurve[1].getX(), ctrPointscurve[1].getY());
		c.setLocation(ctrPointscurve[2].getX(), ctrPointscurve[2].getY());
		d.setLocation(ctrPointscurve[3].getX(), ctrPointscurve[3].getY());

		// get coordinate on bezier curve at t = 0..rect_width-1
		for (int i = 0; i < rect_width; ++i) {
			float t = (float) i / (rect_width - 1);

			point = getBezierPointAt(t);
			if (whichCurve == "upper") {
				coorUpperCurve[i] = new Point2D.Double();
				coorUpperCurve[i].setLocation(point.getX(), point.getY());
			} else {
				coorLowerCurve[i] = new Point2D.Double();
				coorLowerCurve[i].setLocation(point.getX(), point.getY());
			}
		}
	}

	// linear interpolation between two points
	private Point2D.Double linearInterpol(Point2D.Double a, Point2D.Double b, float t) {

		Point2D.Double dest = new Point2D.Double();
		dest.setLocation((a.getX() + (b.getX() - a.getX()) * t), (a.getY() + (b.getY() - a.getY()) * t));
		// System.out.println("dest" + " " + dest.getX() + " " + dest.getY());
		return dest;

	}

	// get x,y-coor of a point on bezier-curve, t = [0; 1]
	private Point2D.Double getBezierPointAt(float t) {

		Point2D.Double ab, bc, cd, abbc, bccd, curvePoint;
		ab = new Point2D.Double();
		bc = new Point2D.Double();
		cd = new Point2D.Double();
		abbc = new Point2D.Double();
		bccd = new Point2D.Double();

		ab = linearInterpol(a, b, t);
		bc = linearInterpol(b, c, t);
		cd = linearInterpol(c, d, t);
		abbc = linearInterpol(ab, bc, t);
		bccd = linearInterpol(bc, cd, t);
		curvePoint = linearInterpol(abbc, bccd, t);

		return curvePoint;
	}

	/**
	 * modified code from the book "Digital Image Processing - An Algorithmic Introduction using
	 * Java" by Wilhelm Burger and Mark J. Burge, Copyright (C) <i>2005</i>-2008 Springer-Verlag
	 * Berlin, Heidelberg, New York.
	 */
	private int getInterpolatedPixel(double xcoor, double ycoor) {
		// bicubic interpolator
		double x0 = xcoor;
		double y0 = ycoor;
		int u0 = (int) Math.floor(x0); // use floor to handle negative
		// coordinates too
		int v0 = (int) Math.floor(y0);

		double qred = 0;
		double qgreen = 0;
		double qblue = 0;
		int q = 0;
		for (int j = 0; j <= 3; j++) {
			int v = v0 - 1 + j;
			double pred = 0;
			double pgreen = 0;
			double pblue = 0;
			for (int i = 0; i <= 3; i++) {
				int u = u0 - 1 + i;
				int c = image.getRGB(u, v);
				int red = (c & 0xff0000) >> 16;
				int green = (c & 0x00ff00) >> 8;
				int blue = c & 0x0000ff;

				pred = pred + red * cubic(x0 - u);
				pgreen = pgreen + green * cubic(x0 - u);
				pblue = pblue + blue * cubic(x0 - u);
			}
			qred = qred + pred * cubic(y0 - v);
			qgreen = qgreen + pgreen * cubic(y0 - v);
			qblue = qblue + pblue * cubic(y0 - v);
		}
		q = ((int) qred & 0xff) << 16 | ((int) qgreen & 0xff) << 8 | (int) qblue & 0xff;
		return q;
	}

	private double cubic(double x) {
		// bicubic interpolation, standard parameter a=1; Catmull-Rom
		// interpolation a=0.5
		double a = 1; // seems to be shaper, less smoother

		if (x < 0) {
			x = -x;
		}
		double z = 0;
		if (x < 1) {
			z = (-a + 2) * x * x * x + (a - 3) * x * x + 1;
		} else if (x < 2) {
			z = -a * x * x * x + 5 * a * x * x - 8 * a * x + 4 * a;
		}
		return z;
	}

	public BufferedImage detectEdges() {

		BufferedImage standardRectGrayExpanded = new BufferedImage(rect_width + window_size - 1, rect_height + window_size - 1, BufferedImage.TYPE_3BYTE_BGR);
		BufferedImage standardRectEdgesExpanded = new BufferedImage(rect_width + window_size - 1, rect_height + window_size - 1, BufferedImage.TYPE_3BYTE_BGR);
		BufferedImage dilatedImage = new BufferedImage(rect_width + window_size - 1, rect_height + window_size - 1, BufferedImage.TYPE_3BYTE_BGR);

		// pad image borders with the closest border pixels before filtering
		// thus border pixels will be extended beyound the image border
		ImageBorder imageBorder = new ImageBorder();
		standardRectGrayExpanded = imageBorder.expandImageBorder(standardRectGray, window_size);

		// create the detector
		CannyEdgeDetector detector = new CannyEdgeDetector();
		// adjust its parameters as desired
		detector.setHighThreshold(threshold_edges / 10.0);
		detector.setLowThreshold(threshold_edges * lowThreshFactor / 10.0);
		detector.setGaussianKernelRadius(sigma);
		detector.setGaussianKernelWidth(window_size);
		detector.setSourceImage(standardRectGrayExpanded);
		detector.process();
		standardRectEdgesExpanded = detector.getEdgesImage();

		/*
		 * dilate image with already extended border since dilation also needs border padding
		 */

		dilatedImage = binMorpher.dilate(standardRectEdgesExpanded, dilation_radius / 10f);

		int offset = Math.round(window_size / 2);
		for (int col = offset; col < rect_width + offset; ++col) {
			for (int row = offset; row < rect_height + offset; ++row) {
				int edgepix = dilatedImage.getRGB(col, row);
				standardRectEdgesDilated.setRGB(col - offset, row - offset, edgepix);
			}
		}

		// simultaneously draw on standardRectEdgesDilated image
		if (pencilCoor.size() > 0) {

			Graphics2D g = standardRectEdgesDilated.createGraphics();
			g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			g.setColor(Color.white);
			g.setStroke(new BasicStroke(2, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

			for (int k = 0; k < pencilCoor.size(); ++k) {
				g.draw(new Line2D.Double(pencilCoor.get(k).startL, pencilCoor.get(k).endL));
			}
			g.dispose();
		}

		return standardRectEdgesDilated;
	}

	public void updateBinaryImage(BufferedImage imageWithNewSpotFilled) {
		for (int col = 0; col < rect_width; ++col) {
			for (int row = 0; row < rect_height; ++row) {
				int pix = imageWithNewSpotFilled.getRGB(col, row);
				int c = (pix & 0xff0000) >> 16;
				if (c == 255) {
					standardRectBinary.setRGB(col, row, pix);
				}
			}
		}
	}

	/**
	 * This method extracts the image name and then appends ".png" to the image name.
	 * 
	 * @author Hidayatullah Ahsan 2011
	 * 
	 * @return The <i>proper</i> image name.
	 */
	private String getImageName() {
		String temp = imageFile.getName();
		int endPlace = temp.indexOf(".");
		String propername = temp.substring(0, endPlace) + ".png";
		return propername;
	}

	/**
	 * 
	 * @param image
	 * @param folder
	 * @param imageName
	 */
	private void saveImage(RenderedImage image, String folder, String imageName) {
		try {
			ImageIO.write(image, "png", new File(folder + imageName));
		} catch (IOException e) {
			System.out.println("IOException in ImagePanel.saveImage()");
			System.out.println(e.getMessage());
		}
	}

	/**
	 * Saves the binary image to the binary image folder
	 * 
	 * @param binaryFolder
	 *            the location where all binary images will be stored
	 * @return the buffered image
	 */
	public BufferedImage saveBinaryImage(String binaryFolder) {
		// resize to final_rect_width x final_rect_height
		BufferedImage resizedToStandard = new BufferedImage(final_rect_width, final_rect_height, BufferedImage.TYPE_3BYTE_BGR);
		Graphics2D g = resizedToStandard.createGraphics();
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
		g.drawImage(standardRectBinary, 0, 0, final_rect_width, final_rect_height, null);
		g.dispose();

		BufferedImage binaryImage = new BufferedImage(small_rect_width, small_rect_height, BufferedImage.TYPE_3BYTE_BGR);
		Graphics2D g1 = binaryImage.createGraphics();
		g1.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g1.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
		g1.drawImage(standardRectBinary, 0, 0, small_rect_width, small_rect_height, null);
		g1.dispose();

		saveImage(binaryImage, binaryFolder, getImageName());

		// return binaryImage;
		return resizedToStandard;
	}

	public void trackOriginalRectImage() {
		Graphics2D g1 = standardRectColorCopy.createGraphics();
		g1.drawImage(standardRectColor, 0, 0, rect_width, rect_height, null);
		g1.dispose();

		Graphics2D g2 = standardRectGrayCopy.createGraphics();
		g2.drawImage(standardRectGray, 0, 0, rect_width, rect_height, null);
		g2.dispose();
	}

	public void setBacktoOriginalRectImage() {

		if (undopencil) {

			// undo the latest drawing
			int lastestDrawNumber = pencilCoor.get(pencilCoor.size() - 1).drawingNum;
			int i = pencilCoor.size() - 1;
			while (i >= 0 && pencilCoor.get(i).drawingNum == lastestDrawNumber) {
				pencilCoor.remove(i);
				--i;
			}

			Graphics2D gr1 = standardRectColor.createGraphics();
			gr1.drawImage(standardRectColorCopy, 0, 0, rect_width, rect_height, null);
			gr1.dispose();

			Graphics2D gr2 = standardRectGray.createGraphics();
			gr2.drawImage(standardRectGrayCopy, 0, 0, rect_width, rect_height, null);
			gr2.dispose();

			// remove the latest drawing from Color Fingerprint
			if (pencilCoor.size() > 0) {

				Graphics2D g1 = standardRectColor.createGraphics();
				g1.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
				g1.setColor(Color.red);
				g1.setStroke(new BasicStroke(2, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

				for (int k = 0; k < pencilCoor.size(); ++k) {
					g1.draw(new Line2D.Double(pencilCoor.get(k).startL, pencilCoor.get(k).endL));
				}
				g1.dispose();
			}
			detectEdges();
		}

		if (undofillspot) {

			// undo the latest filled spot
			int lastFilledSpotNum = spotFilledCoor.get(spotFilledCoor.size() - 1).filledSpotNum;
			int j = spotFilledCoor.size() - 1;
			while (j >= 0 && spotFilledCoor.get(j).filledSpotNum == lastFilledSpotNum) {
				spotFilledCoor.remove(j);
				--j;
			}

			detectEdges();

			// clear Binary Image and redraw previously filled spot(s)
			BufferedImage cleanImage = new BufferedImage(rect_width, rect_height, BufferedImage.TYPE_3BYTE_BGR);
			Graphics2D g2D = standardRectBinary.createGraphics();
			g2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			g2D.drawImage(cleanImage, 0, 0, rect_width, rect_height, null);

			// redraw previously filled spot(s)
			if (spotFilledCoor.size() > 0) {

				int white = (255 & 0xff) << 16 | (255 & 0xff) << 8 | 255 & 0xff;
				for (int k = 0; k < spotFilledCoor.size(); ++k) {
					// System.out.println(pencilCoor.get(k).startL + " " +
					// pencilCoor.get(k).endL);
					standardRectBinary.setRGB(spotFilledCoor.get(k).pixCoor.x, spotFilledCoor.get(k).pixCoor.y, white);
				}
				g2D.dispose();
			}

			// reset = true; //if true, then Resulting Binary will clear off
		}
		repaint();
	}

	// Mutators
	public void setCenterWithMouseClick(boolean centerWithMouse) {
		centerWithMouseClick = centerWithMouse;
	}

	public void setShrinkImageToFit(boolean shrink) {
		shrinkImageToFit = shrink;
	}

	public void setImageinEllipse(boolean inEllipse) {
		imageAsDorsal = inEllipse;
	}

	public void setGetMouseLocation(boolean mouseLocation) {
		getMouseLocation = mouseLocation;
	}

	public void setShowCenterPoint(boolean centerPoint) {
		showCenterPoint = centerPoint;
	}

	public void setRubberBandLineOn(boolean BandLineOn) {
		rubberBandLineOn = BandLineOn;
		if (BandLineOn) {
			setCursor(new Cursor(Cursor.CROSSHAIR_CURSOR));
		} else {
			setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
		}
	}

	public void setRidgeInBox(boolean RidgeOn) {
		ridgeInBox = RidgeOn;
		if (RidgeOn) {
			setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
		}
	}

	public void setTwoEyeClicks(boolean TwoEyeClicks) {
		twoEyeClicks = TwoEyeClicks;
	}

	public void setOneEyeClick(boolean click) {
		oneEyeClick = click;
	}

	public void setRidgeRect(boolean RidgeRect) {
		ridgeRect = RidgeRect;
	}

	public void setNoise_slider_Active(boolean activity) {
		noise_slider_active = activity;
	}

	public void setFirstRubberBoxOn(boolean firstRubberBox) {
		firstRubberBoxOn = firstRubberBox;
		if (firstRubberBox) {
			setCursor(new Cursor(Cursor.CROSSHAIR_CURSOR));
		} else {
			setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
		}
	}

	public void setSecondRubberBoxOn(boolean secondRubberBox) {
		secondRubberBoxOn = secondRubberBox;
		if (secondRubberBox) {
			setCursor(new Cursor(Cursor.CROSSHAIR_CURSOR));
		} else {
			setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
		}
	}

	protected void setStretchImageToFit(boolean stretchImage) {
		stretchImageToFit = stretchImage;
		if (stretchImage) {
			repaint();
		}
	}

	public void setThreshold_edges(int threshold_value) {
		threshold_edges = threshold_value;
	}

	public void setDilation_radius(int radius) {
		dilation_radius = radius;
	}

	public void setNoise_radius(int radius) {
		noise_radius = radius;
	}

	protected void setShowCenterLine(boolean centerLine) {
		showCenterLine = centerLine;
	}

	protected void setFirstClick(boolean click) {
		firstClick = click;
	}

	protected void setSpotExtraction(boolean click) {
		spotextraction = click;
	}

	@SuppressWarnings("unchecked")
	protected void newOperationStep() {
		Cursor inCursor = parentFrame.getCursor();
		parentFrame.setCursor(new Cursor(Cursor.WAIT_CURSOR));
		String path = imageFile.getParent();
		if (path == null) {
			path = "";
		} else {
			path = path + File.separator;
		}

		File file = exportImage(new File(path + "temp_nos" + operationPlace + ".png"));
		if (operations.size() == operationPlace) {
			operations.add(file);
			operationPlace++;
		} else if (operations.size() > operationPlace) {
			operations.add(operationPlace, file);
			operationPlace++;
			for (int i = operationPlace; i < operations.size(); i++) {
				operations.removeElementAt(i);
			}
		} else {
			System.err.print("There was an error adding a new operation step");
		}
		if (maxOperations < operationPlace) {
			maxOperations = operationPlace;
		}
		parentFrame.setCursor(inCursor);
	}

	private void setOperationStep(File step) {
		// Error checking
		if (step != null) {
			try {
				importFile(step);
			} catch (IOException ex) {
				System.err.println("could not import file");
			}
			repaint();
		}
	}

	protected void backOperationStep() {
		Cursor inCursor = parentFrame.getCursor();
		parentFrame.setCursor(new Cursor(Cursor.WAIT_CURSOR));
		if (operationPlace > 0) {
			operationPlace--;
			setOperationStep((File) operations.get(operationPlace));
		} else {
			System.err.println("Problem with undo " + operationPlace);
		}
		parentFrame.setCursor(inCursor);
	}

	public void setEraserOn(boolean eraserOn) {
		this.eraserOn = eraserOn;
	}

	protected void setPencilOn(boolean on) {
		pencil = on;
	}

	public void setFillSpotOn(boolean on) {
		fillspot = on;
	}

	public void setUndoPencilOn(boolean on) {
		undopencil = on;
		System.out.println("set undopencil " + undopencil);

	}

	public void setUndoFillSpotOn(boolean on) {
		undofillspot = on;

	}

	// Accessors
	public boolean isCenterWithMouseClick() {
		return centerWithMouseClick;
	}

	public boolean isShrinkImageToFit() {
		return shrinkImageToFit;
	}

	public boolean isGetMouseLocation() {
		return getMouseLocation;
	}

	public boolean isShowCenterPoint() {
		return showCenterPoint;
	}

	public boolean isRubberBandLineOn() {
		return rubberBandLineOn;
	}

	public boolean isFirstRubberBoxOn() {
		return firstRubberBoxOn;
	}

	public boolean isSecondRubberBoxOn() {
		return secondRubberBoxOn;
	}

	public void setShowBoundingBox(boolean showBoundingBox) {
		this.showBoundingBox = showBoundingBox;
	}

	public boolean isEraserOn() {
		return eraserOn;
	}

	public Dimension getImageDimensions() {
		return new Dimension(image.getWidth(), image.getHeight());
	}

	public int getdefaultThreshold_edges() {
		return default_threshold_edges;
	}

	public int getdefaultDilation_radius() {
		return default_dilation_radius;
	}

	public int getNoise_radius() {
		return noise_radius;
	}

	public BufferedImage getBinaryImage() {
		return standardRectBinary;
	}

	public void setBinaryImage(BufferedImage img) {
		standardRectBinary = img;
	}

}// end panel

class ImagePanel_this_mouseAdapter extends java.awt.event.MouseAdapter {
	ImagePanel adaptee;

	ImagePanel_this_mouseAdapter(ImagePanel adaptee) {
		this.adaptee = adaptee;
	}

	@Override
	public void mouseClicked(MouseEvent e) {
		adaptee.this_mouseClicked(e);
	}

	@Override
	public void mouseReleased(MouseEvent e) {
		adaptee.this_mouseReleased(e);
	}

	@Override
	public void mousePressed(MouseEvent e) {
		adaptee.this_mousePressed(e);
	}
}

class ImagePanel_this_mouseMotionAdapter extends java.awt.event.MouseMotionAdapter {
	ImagePanel adaptee;
	@SuppressWarnings("unused")
	private Object image;

	ImagePanel_this_mouseMotionAdapter(ImagePanel adaptee) {
		this.adaptee = adaptee;
	}

	@Override
	public void mouseDragged(MouseEvent e) {
		adaptee.this_mouseDragged(e);
	}

	@Override
	public void mouseMoved(MouseEvent e) {
		adaptee.this_mouseMoved(e);
	}
}
