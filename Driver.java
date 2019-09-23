import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridLayout;
import java.io.File;
import java.io.FileNotFoundException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Scanner;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

/**
 * Reads the file releases.csv and creates a Gantt-Chart.
 * @author Jordan Chrisostomidis
 */
public class Driver{
	//0 = Open-date order, 1 = dependency-date order.
	private static int preference = 0;

	public static void main(String[] args) {
		Scanner scan = null;
		List<Release> releases = new ArrayList<Release>();
		JFileChooser jfc;
		File file;
		
		
		//Open releases.csv
		try {
			jfc = new JFileChooser(System.getProperty("user.dir"));
			jfc.showOpenDialog(new JFrame());
			file = jfc.getSelectedFile();
			
			if(file == null || !file.getName().equals("releases.csv"))
				System.exit(0);
			
			scan = new Scanner(file);
		} 
		catch (FileNotFoundException e) {
			e.printStackTrace();
			System.exit(0);
		}
		
		//Don't read the first line.
		scan.nextLine();
		
		//Read each line and add the release object to releases.
		while(scan.hasNextLine()) {
			StringBuffer line = new StringBuffer();
			String[] data;
			int numQuotes = 0;
			
			//Add a \ before all commas not between quotes.
			for(char c : scan.nextLine().toCharArray()) {
				if(c == '"')
					numQuotes++;
				
				if(c != ',') {
					line.append(c);
					continue;
				}
				
				if(numQuotes % 2 == 0)
					line.append('\\');
				
				line.append(c);
			}
			
			data = line.toString().split("\\\\,");
			releases.add(new Release(data));
		}
		
		new GanttChart(releases);
	}
	
	@SuppressWarnings("serial")
	private static class GanttChart extends JFrame{
		private JPanel namePanel;
		private JPanel datePanel = new JPanel(new GridLayout(1, 36));
		private JPanel chartPanel;
		private JScrollPane scrollPane;
		
		
		public GanttChart(List<Release> releases) {
			super("Gantt Chart");
			LocalDate startDate, endDate;
			
			if(preference == 0)
				Collections.sort(releases, Release.OPEN_DATE_ORDER);
			else
				Collections.sort(releases, Release.DEPENDENCY_DATE_ORDER);
			
			namePanel = new JPanel(new GridLayout(releases.size() + 1, 1));
			chartPanel = new JPanel(new GridLayout(releases.size() + 1, 1));
			
			//Add the names.
			for(int i = 0; i < releases.size(); i++) {
				namePanel.add(new JLabel(releases.get(i).getName()));
			}
			namePanel.add(new JLabel(preference == 0? "SORTED BY OPEN DATE." : "SORTED BY DEPENDENCY DATE."));
			
			//Add the dates.
			startDate = preference == 0? releases.get(0).getOpenDate() : releases.get(0).getDependencyDate();
			endDate = releases.get(releases.size()-1).getRtmDate();
			for(; startDate.compareTo(endDate) <= 0; startDate = startDate.plusMonths(1)) {
				datePanel.add(new JLabel("  " + startDate.toString() + "  "));
			}
			
			//Color chart.
			for(int i = 0; i < releases.size(); i++) {
				JPanel temp = new JPanel(new GridLayout(1, 0));//Each row is a grid.
				Release release = releases.get(i);
				LocalDate date1 = preference == 0? release.getOpenDate() : release.getDependencyDate();
				LocalDate rtmDate = release.getRtmDate();
				startDate = preference == 0? releases.get(0).getOpenDate() : releases.get(0).getDependencyDate();
				endDate = releases.get(releases.size()-1).getRtmDate();
				Color color = Color.RED;
				
				//If a date is missing, leave the row blank.
				if(date1 == null || rtmDate == null) {
					temp.setBackground(Color.WHITE);
					temp.setToolTipText(release.toString());
					chartPanel.add(temp);
					continue;
				}
				
				switch(i % 7) {
				case 0: color = Color.RED;
						break;
				case 1: color = Color.YELLOW;
						break;
				case 2: color = Color.BLUE;
						break;
				case 3: color = Color.ORANGE;
						break;
				case 4: color = Color.MAGENTA;
						break;
				case 5: color = Color.GREEN;
						break;
				case 6: color = Color.BLACK;
				}
				
				//Determine for each day if it should be colored.
				for(; startDate.compareTo(endDate) <= 0; startDate = startDate.plusDays(1)) {
					JPanel temp2 = new JPanel();
					
					if(startDate.compareTo(date1) >= 0 && startDate.compareTo(rtmDate) <= 0) {
						temp2.setBackground(color);
						temp2.setToolTipText(release.toString());
					}
					else
						temp2.setBackground(Color.WHITE);
					
					temp.add(temp2);	
				}
				
				chartPanel.add(temp);
			}
			chartPanel.add(datePanel);
			
			
			namePanel.setBackground(Color.LIGHT_GRAY);
			datePanel.setBackground(Color.LIGHT_GRAY);
			
			scrollPane = new JScrollPane(chartPanel);
			scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
			scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);
			
			//Position the panels.
			getContentPane().add(namePanel, BorderLayout.WEST);
			getContentPane().add(scrollPane, BorderLayout.CENTER);
			
			//Display Frame.
			setExtendedState(JFrame.MAXIMIZED_BOTH);
			setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			pack();
			setVisible(true);
			
		}
		
	}

}
