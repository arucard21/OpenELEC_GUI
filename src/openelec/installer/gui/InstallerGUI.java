package openelec.installer.gui;

import java.awt.EventQueue;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingWorker;
import javax.swing.border.EmptyBorder;
import javax.swing.JComboBox;
import javax.swing.JButton;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.factories.FormFactory;
import com.jgoodies.forms.layout.RowSpec;

import javax.swing.JTextArea;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JProgressBar;
import javax.swing.JLabel;
import javax.swing.SwingConstants;

import java.awt.Font;

/**
 * GUI for installing OpenELEC to an SD card, using the linux install script, create_sdcard. 
 * It should be run as root using gksudo or kdesudo in order to correctly list the disks and install 
 * OpenELEC on the SD card. 
 * 
 * Might be possible to use this to run install scripts on different operating systems. You would only
 * have to change the parameters in the configuration file (./etc/OpenELEC_GUI.properties)
 * 
 * 
 * @author Arucard21
 *
 */
public class InstallerGUI extends JFrame {
	/**
	 * Parted machine-readable format:
	 * 
	 * - All lines end with a semicolon (;)
	 * - The first line indicates the units in which the output is expressed.
	 * CHS, CYL and BYT stands for CHS, Cylinder and Bytes respectively.
	 * 
	 * - The second line is made of disk information in the following format (This is what we need here):
	 * "path":"size":"transport-type":"logical-sector-size":"physical-sector-size":"partition-table-type":"model-name";
	 * 
	 * - The third line shows partition information (which will not be needed here, but here it is anyway)
	 * The format for CHS/CYL is: 
	 * "number":"begin":"end":"filesystem-type":"partition-name":"flags-set";
	 * The BYT format is:
	 * "number":"begin":"end":"size":"filesystem-type":"partition-name":"flags-set";
	 * 
	 **/

	private static final long serialVersionUID = 5548063809414629404L;
	//config variables
	private static Path configLocation = Paths.get("./etc/OpenELEC_GUI.properties");
	private Properties config;
	//GUI elements
	private JPanel contentPane;
	private JButton installButton;
	private JComboBox<Disk> comboBox;
	JTextArea textArea;
	/**
	 * Contains the disks that were last considered to be available
	 * 
	 * Every Disk entry in the List is a contrainer class for a disk device, with fields:
	 * "path", "size" and "model-name"
	 *  
	 */
	private List<Disk> disks;
	private JScrollPane scrollPane;
	private JProgressBar progressBar;
	private JLabel label;

	/**
	 * Launch the application. Allows a custom configuration file as parameter
	 */
	public static void main(String[] args) {
		if(args.length > 0 && !args[0].isEmpty()){
			//override config location with the supplied custom location
			try{
				configLocation = Paths.get(args[0]);
			}
			catch(InvalidPathException invalPath){
				System.err.println("The provided argument does not refer to a file");
			}
		}
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					InstallerGUI frame = new InstallerGUI();
					frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the frame.
	 */
	public InstallerGUI() {
		//retrieve the configuration properties
		config = new Properties();
		try {
			config.load(Files.newInputStream(configLocation));
		} catch (IOException e) {
			System.err.println("Could not load configuration file, using default settings");
			// run with default values
			config.setProperty("gui.window.title","OpenELEC SD Card Installation");
			config.setProperty("gui.window.description","Install OpenELEC to an SD card for Raspberry Pi");
			config.setProperty("gui.disklist.InformationalEntry","<Select Installation Disk (Removes All Data from Disk!)>");
			config.setProperty("gui.disklist.tooltip","Select the disk to which you want to install OpenELEC. Note that all data on this disk will be removed");
			config.setProperty("gui.installbutton.title","Install OpenELEC");
			config.setProperty("gui.progressbar.max",Integer.toString(100));
			config.setProperty("gui.progressbar.increment",Integer.toString(1));
			config.setProperty("gui.progressbar.tooltip","Progress of installation process");
			config.setProperty("gui.confirmInstall.title","Are you sure you want to continue?");
			config.setProperty("gui.confirmInstall.message","This installation will remove all data from the selected disk.\nAre you sure you wish to continue your installation to:");
			config.setProperty("gui.confirmInstall.cancelled","The installation was cancelled by the user");
			config.setProperty("diskInfo.command.name","parted");
			config.setProperty("diskInfo.command.parameters","-lm");
			config.setProperty("diskInfo.output.precedingRegex","BYT");
			config.setProperty("diskInfo.output.matchingRegex","(?<path>.*):(?<size>.*):.*:.*:.*:.*:(?<modelName>.*);");
			config.setProperty("install.command","./create_sdcard");
			config.setProperty("install.workingDir","./");
			config.setProperty("install.additionalParams","");
		}
		setTitle(config.getProperty("gui.window.title"));
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 480, 480);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(new FormLayout(new ColumnSpec[] {
				FormFactory.RELATED_GAP_COLSPEC,
				ColumnSpec.decode("default:grow"),},
			new RowSpec[] {
				FormFactory.RELATED_GAP_ROWSPEC,
				FormFactory.DEFAULT_ROWSPEC,
				FormFactory.RELATED_GAP_ROWSPEC,
				FormFactory.DEFAULT_ROWSPEC,
				FormFactory.RELATED_GAP_ROWSPEC,
				FormFactory.DEFAULT_ROWSPEC,
				FormFactory.RELATED_GAP_ROWSPEC,
				FormFactory.DEFAULT_ROWSPEC,
				FormFactory.RELATED_GAP_ROWSPEC,
				RowSpec.decode("default:grow"),}));
		comboBox = new JComboBox<Disk>();
		comboBox.setEditable(true);
		comboBox.setFont(new Font("Liberation Sans", Font.PLAIN, 14));
		comboBox.setToolTipText(config.getProperty("gui.disklist.tooltip"));
				
		//set empty disk as default entry
		Disk emptyDisk = new Disk("/dev/null", "0B", config.getProperty("gui.disklist.InformationalEntry"));
		comboBox.addItem(emptyDisk);
		//update disks
		updateDisks();
		//add to combo box
		for(Disk disk : disks){
			comboBox.addItem(disk);
		}
		
		label = new JLabel(config.getProperty("gui.window.description"));
		label.setFont(new Font("Liberation Sans", Font.PLAIN, 12));
		label.setHorizontalAlignment(SwingConstants.CENTER);
		contentPane.add(label, "2, 2");
		contentPane.add(comboBox, "2, 4, fill, fill");
		
		installButton = new JButton(config.getProperty("gui.installbutton.title"));
		installButton.setFont(new Font("Liberation Sans", Font.PLAIN, 14));
		installButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				SwingWorker<Void, Void> install = new SwingWorker<Void, Void>(){

					@Override
					protected Void doInBackground() throws Exception {
						startInstallation();
						return null;
					}
				};
				install.execute();
				
			}
		});
		contentPane.add(installButton, "2, 6, fill, fill");
		
		progressBar = new JProgressBar(0, Integer.parseInt(config.getProperty("gui.progressbar.max")));
		progressBar.setFont(new Font("Liberation Sans", Font.PLAIN, 14));
		progressBar.setStringPainted(true);
		progressBar.setToolTipText(config.getProperty("gui.progressbar.tooltip"));
		contentPane.add(progressBar, "2, 8");
		
		scrollPane = new JScrollPane();
		scrollPane.setFont(new Font("Liberation Mono", Font.PLAIN, 12));
		contentPane.add(scrollPane, "2, 10, fill, fill");
		
		textArea = new JTextArea();
		textArea.setFont(new Font("Liberation Mono", Font.PLAIN, 12));
		scrollPane.setViewportView(textArea);
		textArea.setEditable(false);
	}

	public void startInstallation() {
		progressBar.setValue(0);
		Disk selectedDisk;
		if(comboBox.getSelectedIndex() > 0){
			selectedDisk = (Disk) comboBox.getSelectedItem();
		}
		else{
			selectedDisk = new Disk(comboBox.getSelectedItem().toString(), "", "");
		}
		//pop up an "are you sure" dialog
		int confirm = JOptionPane.showConfirmDialog(contentPane, config.getProperty("gui.confirmInstall.message")+"\n"+selectedDisk.getPath(), config.getProperty("gui.confirmInstall.title"), JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
		if(confirm == JOptionPane.YES_OPTION){
			if(!selectedDisk.getPath().isEmpty()){
				try {
					String params;
					if(config.getProperty("install.additionalParams").isEmpty()){
						params = selectedDisk.getPath();
					}
					else{
						params = config.getProperty("install.additionalParams") + " " + selectedDisk.getPath();
					}
					ProcessBuilder installPB = new ProcessBuilder(config.getProperty("install.command"), params);				
					installPB.directory(Paths.get(config.getProperty("install.workingDir")).toFile());
					installPB.redirectErrorStream(true);
					Map<String, String> defaultEnv = installPB.environment();
					defaultEnv.clear();
					defaultEnv.putAll(System.getenv());
					Process installProc = installPB.start();							
					BufferedReader installReader = new BufferedReader(new InputStreamReader(installProc.getInputStream()));
					String line = null;
					while ((line = installReader.readLine()) != null ){
						textArea.append(line+"\n");
						progressBar.setValue(progressBar.getValue()+Integer.parseInt(config.getProperty("gui.progressbar.increment")));
					}
					progressBar.setValue(Integer.parseInt(config.getProperty("gui.progressbar.max")));
				}
				catch (IOException e1) {
					e1.printStackTrace();
				}
			}
		}
		else{
			String line = config.getProperty("gui.confirmInstall.cancelled");
			textArea.append(line+"\n");
		}
	}
	
	/**
	 * Updates the disks variable with the currently available disks
	 */
	private void updateDisks(){
		List<Disk> newDisks = new ArrayList<Disk>();
		ProcessBuilder partedPB = new ProcessBuilder(config.getProperty("diskInfo.command.name"), 
				config.getProperty("diskInfo.command.parameters"));
		
		Process partedProc;
		try {
			partedProc = partedPB.start();
			partedProc.waitFor();
			BufferedReader partedReader = new BufferedReader(new InputStreamReader(partedProc.getInputStream()));
			while (partedReader.ready()){
				//find out if a preceding line regex has been provided
				String precedingLine = config.getProperty("diskInfo.output.precedingRegex");
				if(precedingLine != null && !precedingLine.isEmpty()){
					//check every line for the preceding line regex and only check the following line for the matching regex
					if(partedReader.readLine().contains(precedingLine)){					
						Pattern matchLine = Pattern.compile(config.getProperty("diskInfo.output.matchingRegex"));
						Matcher readLine = matchLine.matcher(partedReader.readLine());
						if (readLine.matches()){
							Disk discoveredDisk = new Disk(readLine.group("path"),readLine.group("size"),readLine.group("modelName"));
							newDisks.add(discoveredDisk);
						}
						else{
							System.err.println("Preceding line matched, but next line did not. Check your regex.");
						}
					}
				}
				else{
					//check every line for the matching regex
					Pattern matchLine = Pattern.compile(config.getProperty("diskInfo.output.matchingRegex"));
					Matcher readLine = matchLine.matcher(partedReader.readLine());
					if (readLine.matches()){
						Disk discoveredDisk = new Disk(readLine.group("path"),readLine.group("size"),readLine.group("modelName"));
						newDisks.add(discoveredDisk);
					}
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		disks = newDisks;
		
	}
}
