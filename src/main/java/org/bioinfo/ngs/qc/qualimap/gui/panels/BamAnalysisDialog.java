package org.bioinfo.ngs.qc.qualimap.gui.panels;

import net.miginfocom.swing.MigLayout;
import org.bioinfo.commons.io.utils.FileUtils;
import org.bioinfo.ngs.qc.qualimap.gui.frames.HomeFrame;
import org.bioinfo.ngs.qc.qualimap.gui.threads.BamAnalysisThread;
import org.bioinfo.ngs.qc.qualimap.gui.utils.Constants;
import org.bioinfo.ngs.qc.qualimap.gui.utils.JTextFieldLimit;
import org.bioinfo.ngs.qc.qualimap.gui.utils.PopupKeyListener;
import org.bioinfo.ngs.qc.qualimap.gui.utils.TabPropertiesVO;

import javax.activation.MimetypesFileTypeMap;
import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyListener;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.List;

/**
 * Created by kokonech
 * Date: 12/8/11
 * Time: 11:18 AM
 */
public class BamAnalysisDialog extends JDialog {

    JButton startAnalysisButton, pathDataFileButton, pathGffFileButton;
    JTextField pathDataFile, pathGffFile, valueNw;
    JCheckBox drawChromosomeLimits, computeOutsideStats, advancedInfoCheckBox;
    JProgressBar progressBar;
    JLabel progressStream, labelPathDataFile, labelPathAditionalDataFile;
    HomeFrame homeFrame;
    File inputFile, regionFile;
    boolean  analyzeRegions;

    StringBuilder stringValidation;

    final String startButtonText = ">>> Start analysis";
    private int typeAnalysis;

    static class ItemStateChangeAction implements ActionListener {

        List<Component> items;
        ItemStateChangeAction() {
            items = new ArrayList<Component>();
        }

        void addItem(Component item) {
            items.add(item);
        }

        @Override
        public void actionPerformed(ActionEvent actionEvent) {
            for (Component item : items)  {
                item.setEnabled(!item.isEnabled());
            }

        }
    }


    public BamAnalysisDialog(HomeFrame homeFrame, int typeAnalysis) {

        this.homeFrame = homeFrame;
        this.typeAnalysis = typeAnalysis;
        analyzeRegions = typeAnalysis == Constants.TYPE_BAM_ANALYSIS_EXOME;

        KeyListener keyListener = new PopupKeyListener(homeFrame, this, progressBar);
        getContentPane().setLayout(new MigLayout("insets 20"));

        labelPathDataFile = new JLabel();
        labelPathDataFile.setText("Select BAM file:");
        add(labelPathDataFile, "");

        pathDataFile = new JTextField(40);
        pathDataFile.addKeyListener(keyListener);
        pathDataFile.setToolTipText("Path to BAM alignment file");
        add(pathDataFile, "grow");

        pathDataFileButton = new JButton();
		pathDataFileButton.setAction(getActionLoadBamFile());
        pathDataFileButton.setText("...");
		pathDataFileButton.addKeyListener(keyListener);
        add(pathDataFileButton, "align center, wrap");

        // The gff file (region file) can only be available if we are doing the
		// exome analysis

        if (analyzeRegions) {
            labelPathAditionalDataFile = new JLabel("Select GFF file:");
            add(labelPathAditionalDataFile, "");

            pathGffFile = new JTextField(40);
            pathGffFile.addKeyListener(keyListener);
            pathGffFile.setToolTipText("Path to GFF file containing regions of interest");
            add(pathGffFile, "grow");

            pathGffFileButton = new JButton();
            pathGffFileButton.setAction(getActionLoadAdditionalFile());
            pathGffFileButton.addKeyListener(keyListener);
            pathGffFileButton.setText("...");
            add(pathGffFileButton, "align center, wrap");

            computeOutsideStats = new JCheckBox("Analyze outside regions");
            computeOutsideStats.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            computeOutsideStats.addKeyListener(keyListener);
            computeOutsideStats.setToolTipText("Controls whether to calculate also statistic for outside regions");
            computeOutsideStats.setSelected(true);
            add(computeOutsideStats, "wrap");

        }

        drawChromosomeLimits = new JCheckBox("Chromosome limits");
        drawChromosomeLimits.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        drawChromosomeLimits.addKeyListener(keyListener);
        drawChromosomeLimits.setToolTipText("Controls whether to draw chromosome limits in resulting graphs");
        add(drawChromosomeLimits, "wrap");

        // Input Line of information (check to show the advance info)
        advancedInfoCheckBox = new JCheckBox("Advanced options");
        advancedInfoCheckBox.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        advancedInfoCheckBox.addKeyListener(keyListener);
        add(advancedInfoCheckBox,"wrap");

        JLabel labelNw = new JLabel();
		labelNw.setText("Number Of windows:");
		labelNw.setEnabled(false);
		add(labelNw, "gapleft 20");

        valueNw = new JTextField(10);
		valueNw.setText("" + Constants.DEFAULT_NUMBER_OF_WINDOWS);
        valueNw.setDocument(new JTextFieldLimit(6, true));
		valueNw.addKeyListener(keyListener);
	    valueNw.setEnabled(false);
        add(valueNw, "wrap");

        ItemStateChangeAction stateChangeAction = new ItemStateChangeAction();
        stateChangeAction.addItem(labelNw);
        stateChangeAction.addItem(valueNw);
        advancedInfoCheckBox.addActionListener(stateChangeAction);

        // Action done while the statistics graphics are loaded
        progressStream = new JLabel();
        progressStream.setVisible(true);
        progressStream.setText("Status");
        add(progressStream, "align center");


        // Progress Bar to show while the statistics graphics are loaded
        UIManager.put("ProgressBar.selectionBackground", Color.black);
        UIManager.put("ProgressBar.selectionForeground", Color.black);
        progressBar = new JProgressBar(0, 100);
        progressBar.setVisible(true);
        progressBar.setStringPainted(true);
        progressBar.setBorderPainted(true);
        progressBar.setForeground(new Color(244, 200, 120));
        add(progressBar, "grow, wrap 30px");

        startAnalysisButton = new JButton();
        startAnalysisButton.setAction(getActionLoadQualimap());
        startAnalysisButton.setText(startButtonText);
        startAnalysisButton.addKeyListener(keyListener);

        add(new JLabel(""), "span 2");
        add(startAnalysisButton, "wrap");

        pack();

        setTitle(analyzeRegions ? "Analyze genomic region" : "Analyze genomic dataset");
        setResizable(false);

    }

    /**
	 * Action to load the input data file.
	 *
	 * @return AbstractAction with the event
	 */
	private AbstractAction getActionLoadBamFile() {

		 return new AbstractAction() {
			private static final long serialVersionUID = -8111339366112980049L;

			public void actionPerformed(ActionEvent evt) {
				if (homeFrame.getFileOpenChooser() == null) {
					homeFrame.setFileOpenChooser(new JFileChooser());
				}
				FileFilter filter = new FileFilter() {
					public boolean accept(File fileShown) {
						boolean result = true;

						if (!fileShown.isDirectory() && !fileShown.getName().substring(fileShown.getName().lastIndexOf(".") + 1).equalsIgnoreCase(Constants.FILE_EXTENSION_DATA_INPUT)) {
							result = false;
						}

						return result;
					}

					public String getDescription() {
						return ("Bam Files (*.bam)");
					}
				};
				homeFrame.getFileOpenChooser().setFileFilter(filter);

				int valor = homeFrame.getFileOpenChooser().showOpenDialog(homeFrame.getCurrentInstance());

				if (valor == JFileChooser.APPROVE_OPTION) {
					pathDataFile.setText(homeFrame.getFileOpenChooser().getSelectedFile().getPath());
				}
			}
		};

	}

    /**
	 * Action to calculate the qualimap with the input data.
	 *
	 * @return AbstractAction with the event
	 */
	private AbstractAction getActionLoadQualimap() {

        return new AbstractAction() {
			private static final long serialVersionUID = 8329832238125153187L;

			public void actionPerformed(ActionEvent evt) {
                // We can load from file or from a BAM file
                TabPropertiesVO tabProperties = new TabPropertiesVO();
				if (validateInput()) {
					// If the input has the required values, load the
					// results
					runAnalysis(tabProperties);
				} else {
					JOptionPane.showMessageDialog(null, stringValidation.toString(), "Error", 0);
				}
			}
		};

	}


    /**
	 * Action to load the additional input data file.
	 *
	 * @return AbstractAction with the event
	 */
	private AbstractAction getActionLoadAdditionalFile() {
		return new AbstractAction() {
			private static final long serialVersionUID = -1601146976209876607L;

			public void actionPerformed(ActionEvent evt) {
				if (homeFrame.getFileOpenChooser() == null) {
					homeFrame.setFileOpenChooser(new JFileChooser());
				}
				FileFilter filter = new FileFilter() {
					public boolean accept(File fileShown) {
						boolean result = true;

						if (!fileShown.isDirectory() && !Constants.FILE_EXTENSION_REGION.containsKey(fileShown.getName().substring(fileShown.getName().lastIndexOf(".") + 1).toUpperCase())) {
							result = false;
						}

						return result;
					}

					public String getDescription() {
						return ("Region Files (*.gff)");
					}
				};
				homeFrame.getFileOpenChooser().setFileFilter(filter);
				int valor = homeFrame.getFileOpenChooser().showOpenDialog(homeFrame.getCurrentInstance());

				if (valor == JFileChooser.APPROVE_OPTION) {
					pathGffFile.setText(homeFrame.getFileOpenChooser().getSelectedFile().getPath());
				}
			}
		};

    }

    /**
	 * Test if the input data correct.
	 *
	 * @return boolean, true if the input data are correct.
	 */
	private boolean validateInput() {
		boolean validate = true;

		stringValidation = new StringBuilder();

		// Validation for the input data file
		if (pathDataFile.getText().isEmpty() || (inputFile = new File(pathDataFile.getText())) == null) {
			stringValidation.append(" • The path of the Input Data File is required \n");
		} else if (inputFile != null) {

			String mimeType = new MimetypesFileTypeMap().getContentType(inputFile);
			String extension = inputFile.getName().substring(inputFile.getName().lastIndexOf(".") + 1);
			if (mimeType == null || !extension.equalsIgnoreCase(Constants.FILE_EXTENSION_DATA_INPUT)) {
				stringValidation.append(" • Incorrect MimeType for the Input Data File (*.bam) \n");
			}
		} else {
			try {
				FileUtils.checkFile(inputFile);
			} catch (IOException e) {
				stringValidation.append(" • " + e.getMessage() + " \n");
			}
		}

		// Validation for the region file
		if (analyzeRegions) {
			if (!pathGffFile.getText().isEmpty() && (regionFile = new File(pathGffFile.getText())) != null) {
				String mimeType = new MimetypesFileTypeMap().getContentType(regionFile);
				String extension = regionFile.getName().substring(regionFile.getName().lastIndexOf(".") + 1);

				if (mimeType == null || !Constants.FILE_EXTENSION_REGION.containsKey(extension.toUpperCase())) {
					stringValidation.append(" • Incorrect MimeType for the Region Data File (*.gff) \n");
				}
			}
			if (regionFile == null) {
				stringValidation.append(" • Region Data File Is Required \n");
			} else {
				try {
					FileUtils.checkFile(regionFile);
				} catch (IOException e) {
					stringValidation.append(" • ").append(e.getMessage()).append("\n");
				}
			}
		}

		// If we have got any error, we reset the invalidate flag
		if (stringValidation.length() > 0) {
			validate = false;
		}

		return validate;
	}

    /*
	 * Function that execute the quality map program an show the results from
	 * the input data files
	 */
	private synchronized void runAnalysis(TabPropertiesVO tabProperties) {
		BamAnalysisThread t;
		tabProperties.setTypeAnalysis(typeAnalysis);
		t = new BamAnalysisThread("StatisticsAnalysisProcessThread", this, tabProperties);

		t.start();
	}

    public JProgressBar getProgressBar() {
        return progressBar;
    }

    public JLabel getProgressStream() {
        return progressStream;
    }

    public int getNumberOfWindows() {
        try {
         return Integer.parseInt(valueNw.getText());
        } catch (NumberFormatException ex) {
            return Constants.DEFAULT_NUMBER_OF_WINDOWS;
        }
    }

    public File getInputFile() {
        return inputFile;
    }

    public File getRegionFile() {
        return regionFile;
    }

    public boolean getDrawChromosomeLimits() {
        return drawChromosomeLimits.isSelected();
    }

    public boolean getComputeOutsideRegions() {
        return analyzeRegions && computeOutsideStats.isSelected();
    }

    public void setUiEnabled(boolean  enabled ) {
        startAnalysisButton.setEnabled(enabled);
        pathDataFile.setEnabled(enabled);
        pathDataFileButton.setEnabled(enabled);
        labelPathDataFile.setEnabled(enabled);

        if (analyzeRegions) {
            pathGffFile.setEnabled(enabled);
            pathGffFileButton.setEnabled(enabled);
            computeOutsideStats.setEnabled(enabled);
            labelPathAditionalDataFile.setEnabled(enabled);
        }

        advancedInfoCheckBox.setEnabled(enabled);
        drawChromosomeLimits.setEnabled(enabled);


    }

    public void addNewPane(TabPropertiesVO tabProperties) {
        homeFrame.addNewPane(inputFile.getName(), tabProperties);
    }

}
