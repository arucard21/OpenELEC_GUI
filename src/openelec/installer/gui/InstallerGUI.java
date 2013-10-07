package openelec.installer.gui;

import java.awt.EventQueue;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingWorker;
import javax.swing.border.EmptyBorder;
import javax.swing.JComboBox;
import javax.swing.JButton;

import java.util.ArrayList;
import java.util.List;

import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.factories.FormFactory;
import com.jgoodies.forms.layout.RowSpec;

import javax.swing.JTextArea;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

import javax.swing.JScrollPane;
import javax.swing.JProgressBar;

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
	 * 
	 */
	private static final long serialVersionUID = 5548063809414629404L;
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
	private String diskManagementCmd = "parted";
	private String diskManagementParam = "-lm";
	private String installCmd = "./create_sdcard";
	private String installWorkingDir = "./";
	private String installParam = "";
	private JPanel contentPane;
	private JButton installButton;
	private JComboBox<Disk> comboBox;
	JTextArea textArea;
	/**
	 * Contains the disks that were last considered to be available
	 * 
	 * Every Disk entry in the List is a contrainer class for a disk device, with fields:
	 * "path", "size", "transport-type", "logical-sector-size", "physical-sector-size", "partition-table-type", "model-name"
	 *  
	 */
	private List<Disk> disks;
	private JScrollPane scrollPane;
	private JProgressBar progressBar;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
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
		setTitle("OpenELEC SD Card Installation");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 450, 300);
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
				RowSpec.decode("default:grow"),}));
		comboBox = new JComboBox<Disk>();
		comboBox.setToolTipText("Select the disk to which you want to install OpenELEC. Note that all data on this disk will be removed");
				
		//set empty disk as default entry
		String[] emptyDiskAttr = {"", "", "", "", "", "", "<Select Installation Disk (Removes All Data from Disk!)>"};
		Disk emptyDisk = new Disk(emptyDiskAttr);
		comboBox.addItem(emptyDisk);
		//update disks
		updateDisks();
		//add to combo box
		for(Disk disk : disks){
			comboBox.addItem(disk);
		}
		contentPane.add(comboBox, "2, 2, fill, fill");
		
		installButton = new JButton("Install OpenELEC");
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
		
		installButton.setEnabled(false);
		contentPane.add(installButton, "2, 4, fill, fill");
		
		progressBar = new JProgressBar();
		progressBar.setStringPainted(true);
		progressBar.setToolTipText("Progress of installation process");
		contentPane.add(progressBar, "2, 6");
		
		scrollPane = new JScrollPane();
		contentPane.add(scrollPane, "2, 8, fill, fill");
		
		textArea = new JTextArea();
		scrollPane.setViewportView(textArea);
		textArea.setEditable(false);
		
		comboBox.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				//enable the install button if the combobox has a disk selected
				if(comboBox.getSelectedIndex()!=0){
					installButton.setEnabled(true);
					progressBar.setValue(10);
				}
				else{
					installButton.setEnabled(false);
				}
			}
		});
	}

	public void startInstallation() {
		final Disk selectedDisk = (Disk) comboBox.getSelectedItem();
		if(!selectedDisk.getPath().isEmpty()){
			installParam = selectedDisk.getPath();
			try {
				ProcessBuilder installPB = new ProcessBuilder(installCmd, installParam);
				
				installPB.directory(new File(installWorkingDir));
				Process installProc = installPB.start();							
				BufferedReader installReader = new BufferedReader(new InputStreamReader(installProc.getInputStream()));
				String line = null;
				while ((line = installReader.readLine()) != null ){
					textArea.append(line+"\n");
					progressBar.setValue(progressBar.getValue()+1);
				}
				progressBar.setValue(100);
			}
			catch (IOException e1) {
				e1.printStackTrace();
			}
		}
	}
	
	/**
	 * Updates the disks variable with the currently available disks
	 */
	private void updateDisks(){
		List<Disk> newDisks = new ArrayList<Disk>();
		ProcessBuilder partedPB = new ProcessBuilder(diskManagementCmd, diskManagementParam);
		
		Process partedProc;
		try {
			partedProc = partedPB.start();
			partedProc.waitFor();
			BufferedReader partedReader = new BufferedReader(new InputStreamReader(partedProc.getInputStream()));
			while (partedReader.ready()){
				if(partedReader.readLine().equals("BYT;")){					
					String[] diskAttr = partedReader.readLine().split(":|;");
					Disk d = new Disk(diskAttr);
					newDisks.add(d);
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
