package net.runelite.rsb.plugin;

import lombok.Getter;
import net.runelite.client.ui.components.materialtabs.MaterialTab;
import net.runelite.rsb.botLauncher.BotLite;
import net.runelite.rsb.internal.ScriptHandler;
import net.runelite.rsb.internal.ScriptListener;
import net.runelite.rsb.script.Script;
import net.runelite.rsb.service.FileScriptSource;
import net.runelite.rsb.service.ScriptDefinition;
import net.runelite.rsb.service.ScriptSource;
import net.runelite.rsb.service.ServiceException;
import net.runelite.rsb.internal.globval.GlobalConfiguration;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.AbstractTableModel;
import java.awt.*;
import java.awt.event.*;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;

public class ScriptSelector extends JDialog implements ScriptListener {

	public static void main(String[] args) {
		new ScriptSelector(null, null).setVisible(true);
	}

	private static final long serialVersionUID = 5475451138208522511L;

	private static final String[] COLUMN_NAMES = new String[]{"", "Name", "Author"};

	private ScriptTableModel model;
	@Getter
	private List<ScriptDefinition> scripts;
	private List<String> tmpFileNames;
	private boolean connected = true;
	private BotLite bot;
	public JTable table;
	public JTextField search;
	public MaterialTab buttonStart;
	public MaterialTab buttonPause;
	public MaterialTab buttonStop;
	public MaterialTab buttonReload;

	static ScriptSource SRC_SOURCES = new FileScriptSource(new File(GlobalConfiguration.Paths.getScriptsSourcesDirectory()));
	static ScriptSource SRC_PRECOMPILED = new FileScriptSource(new File(GlobalConfiguration.Paths.getScriptsPrecompiledDirectory()));
	static ScriptSource SRC_BUNDLED = (GlobalConfiguration.RUNNING_FROM_JAR) ?
		new FileScriptSource(new File(GlobalConfiguration.Paths.getScriptsExtractedCache())) :
		new FileScriptSource(new File("." + File.separator + GlobalConfiguration.Paths.SCRIPTS_NAME_SRC));

	/**
	 * Creates a script selector for the given bot instance
	 * @param bot	the bot instance
	 */
	public ScriptSelector(BotLite bot) {
		super((Frame.getFrames().length > 0) ? Frame.getFrames()[0] : null, "Script Selector", false);
		this.bot = bot;
		this.scripts = new ArrayList<>();
		this.tmpFileNames = new ArrayList<>();
		this.model = new ScriptTableModel(this.scripts);
	}

	/**
	 * Creates a script selector for the given bot instance
	 * @param frame the frame to bind this to
	 * @param bot	the bot instance
	 */
	public ScriptSelector(Frame frame, BotLite bot) {
		super(frame, "Script Selector", true);
		this.bot = bot;
		this.scripts = new ArrayList<>();
		this.tmpFileNames = new ArrayList<>();
		this.model = new ScriptTableModel(this.scripts);
	}


	/**
	 * Updates the script panel
	 */
	public void update() {
		boolean available = bot.getScriptHandler().getRunningScripts().size() == 0;
		if (buttonStart != null) {
			buttonStart.setEnabled(available && table.getSelectedRow() != -1);
			table.setEnabled(available);
			search.setEnabled(available);
			table.clearSelection();
		}
	}

	/**
	 * Loads the scripts from the script directories
	 */
	public void load() {
		scripts.clear();
		File testFile = new File(GlobalConfiguration.Paths.getScriptsSourcesDirectory());
		FileScriptSource test = new FileScriptSource(testFile);
		scripts.addAll(SRC_BUNDLED.list());
		scripts.addAll(SRC_PRECOMPILED.list());
		scripts.addAll(SRC_SOURCES.list());
		for (ScriptDefinition def: scripts) {
			System.out.println(String.format("loading '%s'", def.name));
		}

		if (search != null)
			model.search(search.getText());
		table = (table == null) ? getTable(0, 150) : table;
	}

	/**
	 * Generates and returns the script table
	 *
	 * @param icon 		The icon for the script
	 * @param name 		The name of the script
	 * @param version   The version of the script
	 * @return script table
	 */
	public JTable getTable(int icon, int name) {
		bot.getScriptHandler().addScriptListener(ScriptSelector.this);
		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(final WindowEvent e) {
				bot.getScriptHandler().removeScriptListener(ScriptSelector.this);
				dispose();
			}
		});
		table = new JTable(model);
		table.addMouseListener(new MouseAdapter() {
			public void mousePressed(MouseEvent e) {
				if ((e.getModifiers() & InputEvent.BUTTON3_MASK) == InputEvent.BUTTON3_MASK) {
					final int row = table.rowAtPoint(e.getPoint());
					table.getSelectionModel().setSelectionInterval(row, row);
					showMenu(e);
				}
			}


			/**
			 * @author GigiaJ
			 * @description Changes the right click menu
			 *
			 * @param e
			 */
			private void showMenu(MouseEvent e) {
				final int row = table.rowAtPoint(e.getPoint());
				final ScriptDefinition def = model.getDefinition(row);

				JPopupMenu contextMenu = new JPopupMenu();
				JMenuItem visit = new JMenuItem();
				visit.setText("GitHub");
				visit.setIcon(new ImageIcon(GlobalConfiguration.getImage(GlobalConfiguration.Paths.Resources.ICON)));
				visit.addMouseListener(new MouseAdapter() {
					public void mousePressed(MouseEvent e) {
						//GlobalConfiguration.Paths.URLs.PROJECT; TODO: Update later
					}
				});
				contextMenu.add(visit);

				if (def.website == null || def.website.isEmpty()) {
					visit.setEnabled(false);
				}

				contextMenu.show(table, e.getX(), e.getY());
			}
		});
		table.setRowHeight(20);
		table.setIntercellSpacing(new Dimension(1, 1));
		table.setShowGrid(false);
		table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		table.getSelectionModel().addListSelectionListener(new TableSelectionListener());
		setColumnWidths(table, icon, name);
		return table;
	}

	/**
	 * Sets the action to occur when the reload button is pressed.
	 */
	public boolean buttonReloadActionPerformed() {

		stopAction();
		//
		//Make a search area
		getSearch();
		//Reload the scripts
		load();
		//buttonStart = scriptSelector.getSubmit();
		return true;

	}

	/**
	 * Sets the action to occur when the start button is pressed.
	 *
	 * @param e		the action event
	 */
	public void buttonStartActionPerformed(ActionEvent e) {
		startAction();
		buttonStart.setEnabled(false);
	}

	/**
	 * The actions to perform to start a script
	 */
	private void startAction() {
		ScriptDefinition def = model.getDefinition(table.getSelectedRow());
		try {
			bot.getScriptHandler().runScript(def.source.load(def));
			bot.getScriptHandler().removeScriptListener(this);
		} catch (ServiceException exception) {
			exception.printStackTrace();
		}
	}

	/**
	 * Sets the action to occur when the pause button is pressed.
	 *
	 * @param e		the action event
	 */
	public void buttonPauseActionPerformed(ActionEvent e) {
		ScriptHandler sh = bot.getScriptHandler();
		Map<Integer, Script> running = sh.getRunningScripts();
		if (running.size() > 0) {
			int id = running.keySet().iterator().next();
			sh.pauseScript(id);
			//Swaps the displayed text
			if (buttonPause.getText().equals("Pause")) {
				buttonPause.setText("Play");
			}
			else {
				buttonPause.setText("Pause");
			}
		}
	}

	/**
	 * Sets the action to occur when the stop button is pressed.
	 *
	 * @param e		the action event
	 */
	public void buttonStopActionPerformed(ActionEvent e) {
		//Sets the value back to Pause
		if (buttonPause.getText().equals("Play")) {
			buttonPause.setText("Pause");
		}
		stopAction();

	}

	/**
	 * The actions to perform to stop a script
	 */
	private void stopAction() {
		ScriptHandler sh = bot.getScriptHandler();
		Map<Integer, Script> running = sh.getRunningScripts();
		if (running.size() > 0) {
			int id = running.keySet().iterator().next();
			//Script s = running.get(id);
			//ScriptManifest prop = s.getClass().getAnnotation(ScriptManifest.class);
			//int result = JOptionPane.showConfirmDialog(this, "Would you like to stop the script " + prop.name() + "?", "Script", JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);
			//if (result == JOptionPane.OK_OPTION) {
			sh.stopScript(id);
			//}
		}
	}

	/**
	 * Generates and returns the search button
	 *
	 * @return search button
	 */
	public JTextField getSearch() {
		search = new JTextField();
		search.addFocusListener(new FocusAdapter() {
			@Override
			public void focusGained(FocusEvent e) {
				table.clearSelection();
			}
		});
		search.addKeyListener(new KeyAdapter() {
			@Override
			public void keyTyped(KeyEvent e) {
				model.search(search.getText());
				table.revalidate();
			}
		});
		return search;
	}


	private void setColumnWidths(JTable table, int... widths) {
		for (int i = 0; i < widths.length; ++i) {
			table.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);
			table.getColumnModel().getColumn(i).setMinWidth(widths[i]);
			table.getColumnModel().getColumn(i).setMaxWidth(widths[i]);
		}
	}

	public void scriptStarted(ScriptHandler handler, Script script) {
		update();
	}

	public void scriptStopped(ScriptHandler handler, Script script) {
		update();
	}

	public void scriptResumed(ScriptHandler handler, Script script) {
	}

	public void scriptPaused(ScriptHandler handler, Script script) {
	}

	public void inputChanged(BotLite bot, int mask) {
	}

	private class TableSelectionListener implements ListSelectionListener {
		public void valueChanged(ListSelectionEvent evt) {
			if (!evt.getValueIsAdjusting()) {
				buttonStart.setEnabled(table.getSelectedRow() != -1);
			}
		}
	}

	public static class ScriptTableModel extends AbstractTableModel {
		private static final long serialVersionUID = 1L;
		private final List<ScriptDefinition> scripts;
		private final List<ScriptDefinition> matches;

		public ScriptTableModel(List<ScriptDefinition> scripts) {
			this.scripts = scripts;
			this.matches = new ArrayList<>();
		}

		public void search(String substr) {
			matches.clear();
			if (substr.length() == 0) {
				matches.addAll(scripts);
			} else {
				substr = substr.toLowerCase();
				for (ScriptDefinition def : scripts) {
					if (def.name.toLowerCase().contains(substr)) {
						matches.add(def);
					} else {
						for (String keyword : def.keywords) {
							if (keyword.toLowerCase().contains(substr)) {
								matches.add(def);
								break;
							}
						}
					}
				}
			}
			fireTableDataChanged();
		}

		public ScriptDefinition getDefinition(int rowIndex) {
			return matches.get(rowIndex);
		}

		public int getRowCount() {
			return matches.size();
		}

		public int getColumnCount() {
			return COLUMN_NAMES.length;
		}

		public Object getValueAt(int rowIndex, int columnIndex) {
			if (rowIndex >= 0 && rowIndex < matches.size()) {
				ScriptDefinition def = matches.get(rowIndex);
				if (columnIndex == 1) {
					return def.name;
				}
				if (columnIndex == 2) {
					StringBuilder b = new StringBuilder();
					for (String author : def.authors) {
						b.append(author).append(", ");
					}
					return b.replace(b.length() - 2, b.length(), "");
				}
			}
			return null;
		}

		@Override
		public Class<?> getColumnClass(int col) {
			if (col == 0) {
				return ImageIcon.class;
			}
			return String.class;
		}

		@Override
		public String getColumnName(int col) {
			return COLUMN_NAMES[col];
		}
	}
}
