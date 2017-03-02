import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Properties;
import java.sql.Driver;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import javax.swing.*;
import javax.swing.event.*;

import com.mysql.jdbc.Statement;

public class mainwindow extends JFrame 
implements ActionListener {
	
	/////////// Creating components for window ///////////
	private JPanel mainPanel = new JPanel();
	private JLabel word = new JLabel();
	private JLabel correctGender = new JLabel();
	private JLabel wrongGender = new JLabel();
	private Boolean translation = true;
	private JButton der = new JButton("Der");
	private JButton die = new JButton("Die");
	private JButton das = new JButton("Das");
	private JButton next = new JButton("Next Word");
	
	/////////// JMenuBar items ///////////
	private JMenuBar menuBar = new JMenuBar();
	private JMenu translate = new JMenu("Translate");
	private JMenuItem english = new JMenuItem("English");
	
	/////////// Objects needed for connecting to DB ///////////
	private Connection connection = null;
	private java.sql.Statement st = null;
	private ResultSet rs = null;
	private String url = "jdbc:mysql://localhost:3306/website";	
	private String userName = "root";							
	private String password = "Password";
	private Boolean inAnswer = false;
	
	/////////// Colours //////////
	private Color DarkGreen = new Color(0,153,0);
	private Color colour = new Color( 148, 138,138);
	private Color colour2 = new Color(255,255,255);
	
	public mainwindow() {
		
		/////////// Setting up the Frame ///////////
		setTitle("Learn genders of German words");
		mainPanel.setLayout(new GridBagLayout());			//Using GridBag to make more custom
		setLayout(new BorderLayout());
		setPreferredSize(new Dimension(500,200));			//Test Size of Window frame
		mainPanel.setPreferredSize(new Dimension(500,200));		//Test Size of Pane
		word.setFont(new Font("Helvetica", Font.PLAIN, 20));	//Test Size of text
		correctGender.setFont(new Font("Helvetica", Font.PLAIN,20)); //Test Size of AnswerText
		wrongGender.setFont(new Font("Helvetica", Font.PLAIN,20)); //Test Size of AnswerText
		mainPanel.setFocusable(true);
		
		/////////// Set Colour of everything ///////////
		mainPanel.setBackground(new Color(233,233,233));
		der.setBackground(colour);
		die.setBackground(colour);
		das.setBackground(colour);
		next.setBackground(colour);
		der.setForeground(colour2);
		die.setForeground(colour2);
		das.setForeground(colour2);
		next.setForeground(colour2);
		
		mainPanel.addKeyListener(new KeyListener() {
			public void keyPressed(KeyEvent e) {
				if (e.getKeyCode() == 49) {
					if (inAnswer == false) {
						Der();
					}
				}
				if (e.getKeyCode() == 50) {
					if (inAnswer == false) {
						Die();
					}
				}
				if (e.getKeyCode() == 51) {
					if (inAnswer == false) {
						Das();
					}
				}
				if (e.getKeyCode() == 32) {
					
					if (inAnswer == true) {
						nextWord();
					}
				}
				if (e.getKeyCode() == KeyEvent.VK_ENTER || e.getKeyCode() == 32){
					if (inAnswer == true) {
						nextWord();
					}
				}
				
				
			}
			public void keyReleased(KeyEvent e) {
				
			}

			public void keyTyped(KeyEvent e) {

			}
			
		});
		
		/////////// Adding MenuBar and items //////////
		menuBar.add(translate);
		translate.add(english);
		
		
		/////////// Button Listeners ///////////
		der.addActionListener(this);			//Add "Der" button listener
		die.addActionListener(this);			//Add "Die" button listener
		das.addActionListener(this);			//Add "Das" button listener
		next.addActionListener(this);			//Add "Next Word" button listener
		english.addActionListener(this);		//Add  "English" tanslation menu button listener
				
		
		addWindowListener(new WindowListener(){

			public void windowActivated(WindowEvent arg0) {
			}
			public void windowClosed(WindowEvent arg0) {
			}
			/////////// Disconnecting from DB when finished ///////////
			public void windowClosing(WindowEvent arg0) {
				try {
					connection.close();
					System.out.println("Database disconnected!");
				} catch (SQLException e) {
					throw new IllegalStateException("Could not disconnect from database!", e);
				}
			}
			public void windowDeactivated(WindowEvent arg0) {
			}
			public void windowDeiconified(WindowEvent arg0) {	
			}
			public void windowIconified(WindowEvent arg0) {	
			}
			
			/////////// Connecting to DB when Window is opened ///////////
			public void windowOpened(WindowEvent arg0) {
				getConnection();
			}
			
		});
		
		/////////// Setting GridBagConstraints on components ///////////
		GridBagConstraints gc = new GridBagConstraints();
		
		// First Row
		gc.gridx = 0;
		gc.gridy = 0;
		gc.insets = new Insets(3,3,3,3);
		mainPanel.add(correctGender, gc);
		
		gc.weightx = 0;
		gc.weighty = 0;
		gc.gridx = 1;
		gc.gridy = 0;
		gc.fill = GridBagConstraints.BOTH;
		gc.fill = GridBagConstraints.CENTER;
		mainPanel.add(word, gc);
		
		
		//Second Row
		gc.gridx = 0;
		gc.gridy = 1;
		//gc.anchor = GridBagConstraints.WEST;
		mainPanel.add(der, gc);
		
		gc.gridx = 1;
		gc.gridy = 1;
		//gc.anchor = GridBagConstraints.CENTER;
		mainPanel.add(die, gc);
		
		gc.gridx = 2;
		gc.gridy = 1;
		//gc.anchor = GridBagConstraints.EAST;
		mainPanel.add(das, gc);
		
		//Third Row
		gc.gridx = 1;
		gc.gridy = 2;
		mainPanel.add(next, gc);
		next.setVisible(false);
		
		
		gc.gridx = 0;
		gc.weightx = 0;
		gc.gridwidth = 3;
		gc.gridy = 3;
		gc.fill = GridBagConstraints.BOTH;
		gc.fill = GridBagConstraints.WEST;
		mainPanel.add(wrongGender, gc);
		
		//Add Panel and menubar to Frame
		setJMenuBar(menuBar);
		add(mainPanel, BorderLayout.CENTER);
		pack();
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		setVisible(true);
	}
	
	
	/////////// Function to get the next word from DB and place it in Label ///////////
	private void nextWord() {
		correctGender.setText(null);
		wrongGender.setText(null);
		correctGender.setForeground(word.getForeground());
		wrongGender.setForeground(word.getForeground());
		das.setEnabled(true);
		die.setEnabled(true);
		der.setEnabled(true);
		der.setVisible(true);
		die.setVisible(true);
		das.setVisible(true);
		next.setVisible(false);
		inAnswer = false;
		try {
			rs = st.executeQuery("SELECT word FROM wordgender ORDER BY RAND() LIMIT 1");
			rs.next();
			word.setText(rs.getString(1));
			if (translation) {
				rs = st.executeQuery("SELECT english FROM wordgender WHERE word = \'" + word.getText() + "\'");
				rs.next();
				wrongGender.setText(rs.getString(1));
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	
	}
	/////////// Code for connecting to DB ///////////
	private void getConnection() {
		System.out.println("Loading Driver...");
		try {
			Class.forName("com.mysql.jdbc.Driver");
			System.out.println("Driver Loaded!");
		} catch (ClassNotFoundException e) {
			throw new IllegalStateException("Cannot find the driver in the classpath!", e);
		}
		
		System.out.println("Connecting to database....");
		try {
			connection = DriverManager.getConnection(url, userName, password);
			System.out.println("Database connected!");
			st = connection.createStatement();
			rs = st.executeQuery("SELECT word FROM wordgender ORDER BY RAND() LIMIT 1");
			rs.next();
			word.setText(rs.getString(1));
			rs = st.executeQuery("SELECT english FROM wordgender WHERE word = \'" + word.getText() + "\'");
			rs.next();
			wrongGender.setText(rs.getString(1));
			
		} catch (SQLException e) {
			throw new IllegalStateException("Cannot connect to the database!", e);
		}
		
	}
	/////////// Methods //////////
	public void Der() {
		String guess = "Der";
		try {
			rs = st.executeQuery("SELECT gender FROM wordgender WHERE word = \'" + word.getText() + "\'");
			rs.next();
			if (rs.getString(1).equals(guess)) {
				//correctGender.setText("Der");
				//correctGender.setForeground(Color.black);
				word.setText(guess + " " + word.getText());
				wrongGender.setText("Correct!");
				wrongGender.setForeground(DarkGreen);
				der.setVisible(false);
				die.setVisible(false);
				das.setVisible(false);
				next.setVisible(true);
				inAnswer = true;
			}
			else {
				wrongGender.setText("Wrong, try again");
				wrongGender.setForeground(Color.red);
				der.setEnabled(false);
			}
		} catch (SQLException e1) {
			e1.printStackTrace();
		}
	}
	public void Die() {
		String guess = "Die";
		try {
			rs = st.executeQuery("SELECT gender FROM wordgender WHERE word = \'" + word.getText() + "\'");
			rs.next();
			if (rs.getString(1).equals(guess)) {
				//correctGender.setText("Die");
				//correctGender.setForeground(Color.black);
				word.setText(guess + " " + word.getText());
				wrongGender.setText("Correct!");
				wrongGender.setForeground(DarkGreen);
				der.setVisible(false);
				die.setVisible(false);
				das.setVisible(false);
				next.setVisible(true);
				inAnswer = true;
			}
			else {
				wrongGender.setText("Wrong, try again");
				wrongGender.setForeground(Color.red);
				die.setEnabled(false);
			}
		} catch (SQLException e1) {
			e1.printStackTrace();
		}
	}
	public void Das() {
		String guess = "Das";
		try {
			rs = st.executeQuery("SELECT gender FROM wordgender WHERE word = \'" + word.getText() + "\'");
			rs.next();
			if (rs.getString(1).equals(guess)) {
				//correctGender.setText("Das");
				//correctGender.setForeground(Color.black);
				word.setText(guess + " " + word.getText());
				wrongGender.setText("Correct!");
				wrongGender.setForeground(DarkGreen);
				der.setVisible(false);
				die.setVisible(false);
				das.setVisible(false);
				next.setVisible(true);
				inAnswer = true;
			}
			else {
				wrongGender.setText("Wrong, try again");
				wrongGender.setForeground(Color.red);
				das.setEnabled(false);
			}
		} catch (SQLException e1) {
			e1.printStackTrace();
		}
	}

	/////////// ActionListeners for all the buttons	///////////
	public void actionPerformed(ActionEvent e) {
		if(e.getSource() == der) {
			Der();
		}
		if(e.getSource() == die) {
			Die();
		}
		if(e.getSource() == das) {
			Das();
		}
		if(e.getSource() == next) {
			nextWord();
		}
		if(e.getSource() == english) {
			if (translation) {
				translation = false;
				wrongGender.setText(null);
			} else {
				translation = true;
				try {
					rs = st.executeQuery("SELECT english FROM wordgender WHERE word = \'" + word.getText() + "\'");
					rs.next();
					wrongGender.setText(rs.getString(1));
				} catch (SQLException e1) {
					e1.printStackTrace();
				}
				
			}
		}
	}
	
	/////////// Main ///////////
	public static void main(String[] args) {
		new mainwindow();
}

	
	
}
	
	