/**
* <h1>Langton's Ant</h1>
* HCI 2018-2019 Programming Assignment
* <p>
* @author Cristiano Gelli
* @version 1.0
* @since 2019-01-14
*/

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.text.NumberFormat;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.Box.Filler;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFormattedTextField;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.WindowConstants;
import javax.swing.border.TitledBorder;
import javax.swing.text.NumberFormatter;

/**
 * Main class of the project, which follows the MVC architectural pattern.
 * It implements the runAnt() algorithm (Model), is responsible of GUI 
 * managing (View) and events handling (Controllers).
 */
public class Langton {
	private static final int minRate = 1, maxRate = 10000, defaultRate = 100;
	private static final Color[] colors =  { Color.WHITE, Color.BLACK, Color.BLUE, Color.CYAN, Color.DARK_GRAY, Color.RED, Color.GRAY,
											Color.GREEN, Color.YELLOW, Color.LIGHT_GRAY, Color.MAGENTA, Color.ORANGE, Color.PINK };
	// input parameters
	private int width = 100;
	private int height = 100;
	private int nSteps = 0;
	private int rate;
	private String scheme;
	
	// ant position
	int antX, antY;
	// plane matrix
	private int [][] plane;
	// running runAnt() thread
	private RunningThread runningThread;
	// listener attached to the graphic panel
	private ZoomAndPanListener zoomAndPanListener;
	
	private JTextField widthField;
	private JTextField heightField;
	private JFormattedTextField rateField;
	private JTextField schemeField;
	private JLabel stepLabel;
	private DrawPanel dp;
	
	public static void main(String[] args) {  
		new Langton();
    }
	
	public Langton() {
		initGUI();
	}
	
	/**
	 * Initializes the GUI with all its components and handles events associated to them.
	 */
	public void initGUI() {
		JFrame f = new JFrame("Langton's ant");
	    f.setLayout(new BorderLayout());
	    
	    // header title
	    JLabel title = new JLabel("Langton's Ant");
	    title.setHorizontalAlignment(JLabel.CENTER);
	    title.setFont(new Font("Verdana", Font.BOLD, 28));
	    
	    // Main left panel
	    JPanel ctrlPanel = new JPanel();
	    ctrlPanel.setLayout(new BoxLayout(ctrlPanel, BoxLayout.Y_AXIS));
	    
	    // Step
	    stepLabel = new JLabel("Step: "+nSteps);
	    ctrlPanel.add(stepLabel);
	    stepLabel.setAlignmentX(JLabel.CENTER_ALIGNMENT);
	    stepLabel.setHorizontalTextPosition(JLabel.CENTER);
	    ctrlPanel.add(Box.createRigidArea(new Dimension(0,15)));
	    // Inputs panel
	    JPanel inputPanel = new JPanel();
	    inputPanel.setLayout(new BoxLayout(inputPanel, BoxLayout.Y_AXIS));
	    TitledBorder inputsBorder = BorderFactory.createTitledBorder("Inputs");
	    inputsBorder.setTitleJustification(TitledBorder.CENTER);
	    inputPanel.setBorder(inputsBorder);
	    // Width and Height
	    JPanel widthRow = new JPanel();
	    widthRow.setLayout(new FlowLayout(FlowLayout.LEFT));
	    JLabel widthLabel = new JLabel("Width: ");
	    widthRow.add(widthLabel);
	    widthField = new JTextField(""+width, 5);
	    widthRow.add(widthField);
	    inputPanel.add(widthRow);
	    JPanel heightRow = new JPanel();
	    heightRow.setLayout(new FlowLayout(FlowLayout.LEFT));
	    JLabel heightLabel = new JLabel("Height:");
	    heightRow.add(heightLabel);
	    heightField = new JTextField(""+height, 5);
	    heightRow.add(heightField);
	    inputPanel.add(heightRow);
	    // Scheme
	    JPanel schemeRow = new JPanel();
	    schemeRow.setLayout(new FlowLayout(FlowLayout.LEFT));
	    JLabel schemeLabel = new JLabel("Scheme: ");
	    schemeRow.add(schemeLabel);
	    schemeField = new JTextField("RL", 9);
	    schemeField.addKeyListener(new KeyListener() {
			@Override
			public void keyTyped(KeyEvent e) {}
			@Override
			public void keyPressed(KeyEvent e) {}
			@Override
			public void keyReleased(KeyEvent e) {
				//char c = e.getKeyChar();
				String scheme = schemeField.getText();
				if( scheme.length() <= 13 ) {
					scheme = scheme.toUpperCase();
					for( int i = 0; i < scheme.length(); i++ ) {
						char c = scheme.charAt(i);
						if( c!= 'L' && c != 'R' )
							scheme = scheme.replace(Character.toString(c), "");
					}
				}
				else {
					scheme = scheme.substring(0, 13);
				}
				schemeField.setText(scheme);
			}
		});
	    schemeRow.add(schemeField);
	    inputPanel.add(schemeRow);
	    
	    ctrlPanel.add(inputPanel);
	    ctrlPanel.add(Box.createRigidArea(new Dimension(0,10)));
	    
	    // Commands panel
	    JPanel commandPanel = new JPanel();
	    commandPanel.setLayout(new BoxLayout(commandPanel, BoxLayout.Y_AXIS));
	    TitledBorder commandsBorder = BorderFactory.createTitledBorder("Commands");
	    commandsBorder.setTitleJustification(TitledBorder.CENTER);
	    commandPanel.setBorder(commandsBorder);
	    // Frame rate
	    JPanel rateRow = new JPanel();
	    rateRow.setLayout(new FlowLayout(FlowLayout.LEFT));
	    JLabel rateLabel = new JLabel("Frame rate (fps):");
	    rateRow.add(rateLabel);
	    NumberFormat format = NumberFormat.getInstance();
	    NumberFormatter formatter = new NumberFormatter(format);
	    formatter.setValueClass(Integer.class);
	    formatter.setMinimum(minRate);
	    formatter.setMaximum(maxRate);
	    formatter.setAllowsInvalid(false);
	    // If you want the value to be committed on each keystroke instead of focus lost
	    formatter.setCommitsOnValidEdit(true);
	    rateField = new JFormattedTextField(formatter);
	    rateField.setColumns(5);
	    rateField.setText(""+defaultRate);
		rateRow.add(rateField);
	    commandPanel.add(rateRow);
	    commandPanel.add(Box.createRigidArea(new Dimension(0,5)));
	    // Buttons
	    JPanel btnGroup = new JPanel();
	    btnGroup.setLayout(new FlowLayout(FlowLayout.LEFT));
	    JButton startButton = new JButton("Start");
	    startButton.addActionListener(new ActionListener() {
	    	public void actionPerformed(ActionEvent e) {	    			
	    		if( runningThread != null && runningThread.getRunning() ) {		// resume
	    			runningThread.wake();
	    		}
	    		// start a new run after the end of the previous one
	    		else if( runningThread != null && !runningThread.getRunning() ) {
	    			runningThread.die();
	    			plane = new int[Integer.parseInt(heightField.getText())][Integer.parseInt(widthField.getText())];
	    			zoomAndPanListener.resetZoom();
		    		dp.repaint();
	    			widthField.setEditable(false);
	    			heightField.setEditable(false);
	    			schemeField.setEditable(false);
	    			runningThread = new RunningThread();
	    			runningThread.start();
	    		}
	    		else {							// start
	    			widthField.setEditable(false);
	    			heightField.setEditable(false);
	    			schemeField.setEditable(false);
	    			runningThread = new RunningThread();
	    			runningThread.start();
	    		}
	    	}
	    });
	    btnGroup.add(startButton);
	    JButton pauseButton = new JButton("Pause");
	    pauseButton.addActionListener(new ActionListener() {
	    	public void actionPerformed(ActionEvent e) {
	    		if(runningThread != null)
	    			runningThread.pause();
	    	}
	    });
	    btnGroup.add(pauseButton);
	    JButton clearButton = new JButton("Clear");
	    btnGroup.add(clearButton);
	    clearButton.addActionListener(new ActionListener() {
	    	public void actionPerformed(ActionEvent e) {
	    		if(runningThread != null) {
		    		runningThread.die();
		    		runningThread = null;
		    		plane = new int[height][width];
		    		nSteps = 0;
		    		width = 100;
		    		height = 100;
		    		widthField.setText(Integer.toString(width));
		    		heightField.setText(Integer.toString(height));
		    		rate = defaultRate;
		    		rateField.setValue(defaultRate);
		    		schemeField.setText("RL");
		    		stepLabel.setText("Step: "+nSteps);
		    		zoomAndPanListener.resetZoom();
		    		dp.repaint();
		    		widthField.setEditable(true);
	    			heightField.setEditable(true);
	    			schemeField.setEditable(true);
	    		}
	    	}
	    });
	    commandPanel.add(btnGroup);
	    commandPanel.add(Box.createRigidArea(new Dimension(0,5)));
	    
	    ctrlPanel.add(commandPanel);
	    // Fill space
	    Box.Filler glue = (Filler) Box.createVerticalGlue();
	    glue.changeShape(glue.getMinimumSize(), 
	                    new Dimension(0, Short.MAX_VALUE), // make glue greedy
	                    glue.getMaximumSize());
	    ctrlPanel.add(glue);
	    
	    f.add(title, BorderLayout.PAGE_START);
	    f.add(ctrlPanel, BorderLayout.LINE_START);
	    dp = new DrawPanel();
	    dp.setBackground(Color.WHITE);

	    f.add(dp, BorderLayout.CENTER);
	    
	    f.setSize(Toolkit.getDefaultToolkit().getScreenSize().width*3/5, Toolkit.getDefaultToolkit().getScreenSize().height*3/4);
	    f.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
	    f.setVisible(true);
	}

	/**
	 * Implementation of the Langton's ant algorithm.
	 * @see <a href="https://en.wikipedia.org/wiki/Langton%27s_ant">Langton's ant (Wikipedia)</a>
	 */
	public void runAnt() {
		// get input parameters
		width = Integer.parseInt(widthField.getText());
		height = Integer.parseInt(heightField.getText());
		rate = (int)rateField.getValue();
		scheme = schemeField.getText();
		
		// matrix initialized with all zeros (eg white)
		plane = new int[height][width];
		// start in the middle
		antX = width/2;
		antY = height/2;
		// start moving up
		int xChange = 0, yChange = -1;
		
		nSteps = 0;
		while( antX < width && antY < height && antX >= 0 && antY >= 0 && runningThread != null && runningThread.getRunning() ) {
			// At the start of each iteration, we check if the execution (of the thread) has been paused by the user
			if(runningThread.getPaused()) {
				synchronized(runningThread) {
					while(runningThread.getPaused()) {
						try {
							// wait until getPaused() will come back false (resume)
							runningThread.wait();
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					}
				}
			}
			// left turn for square's color
			if( scheme.charAt(plane[antY][antX]) == 'L' ) {
				if(xChange == 0) {			// moving up or down
					xChange = yChange;
					yChange = 0;
				} else {					// moving left or right
					yChange = -xChange;
					xChange = 0;
				}
			} else {		// right turn for square's color
				if(xChange == 0) { 			// moving up or down
					xChange = -yChange;
					yChange = 0;
				} else { 					// moving left or right
					yChange = xChange;
					xChange = 0;
				}
			}
			// shift square color and move
			plane[antY][antX] = (plane[antY][antX]+1)%scheme.length();
			antX += xChange;
			antY += yChange;
			
			nSteps++;
			stepLabel.setText("Step: "+nSteps);
			dp.repaint();
			try {
				// Frame rate management
				rate = (int)rateField.getValue();
				Thread.sleep((Integer) 1000/rate);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		widthField.setEditable(true);
		heightField.setEditable(true);
		schemeField.setEditable(true);
	}
	
	/**
	 * Inner class that that manages the painting process (nothing else).
	 */
	public class DrawPanel extends JPanel {
		public DrawPanel() {
			zoomAndPanListener = new ZoomAndPanListener(this);
	        addMouseListener(zoomAndPanListener);
	        addMouseMotionListener(zoomAndPanListener);
	        addMouseWheelListener(zoomAndPanListener);
		}
		@Override
	    public void paint(Graphics g) {
			super.paint(g);
			if(plane != null) 
				doDrawing(g);
		}
		private void doDrawing(Graphics g) {
			Graphics2D g2 = (Graphics2D) g;
			g2.setTransform(zoomAndPanListener.getCoordTransform());
			
			float uX = (float)this.getWidth()/width;
			float uY = (float)this.getHeight()/height;
			
			for(int y = 0; y < plane.length;y++) {
				for(int x = 0; x < plane[0].length;x++) {
					g2.setColor(colors[plane[y][x]]);
					g2.fillRect((int)(x *uX), (int)(y * uY), (int)uX, (int)uY);
				}
			}
			
			if( runningThread != null) {
				// mark the starting point
				g2.setColor(Color.GREEN);
				g2.fillRect((int)(plane[0].length / 2 * uX),
						(int)(plane.length / 2 * uY), (int)uX, (int)uY);
				
				// mark the current point
				g2.setColor(Color.MAGENTA);
				g2.fillRect((int)(antX * uX), (int)(antY * uY), (int)uX, (int)uY);
			}
			
		}
	}

	/**
	 * Inner class that manages the threading process that executes runAnt() function.
	 * Threading is necessary to allow the GUI to remain active during the simulation.
	 */
	public class RunningThread extends Thread {
		private boolean running = false;
		private boolean paused = false;
		
		/**
	     * Determines whether the thread is running.
	     * @return true if the thread is running, false otherwise
	     */
		public boolean getRunning() { return running; }
		/**
	     * Determines whether the thread has been paused.
	     * @return true if the thread has been paused, false otherwise
	     */
		public boolean getPaused() { return paused; }
				
		@Override
		public void run() {
			running = true;
			runAnt();
			running = false;
			dp.repaint();
		}
		
		/**
		 * Pauses the runAnt() threading process.
		 */
		public void pause() {
			paused = true;
		}
		
		/**
		 * Resumes the runAnt() threading process.
		 */
		public void wake() {
			synchronized(this) {
				paused = false;
				notifyAll();
			}
		}
		
		/**
		 * Terminates the runAnt() threading process.
		 */
		public void die() {
			running = false;
		}
	}
}