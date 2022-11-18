package net.runelite.rsb.plugin;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import net.runelite.client.ui.components.materialtabs.MaterialTab;
import net.runelite.rsb.internal.launcher.BotLite;
import net.runelite.rsb.internal.ScriptHandler;
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


@Slf4j
public class ScriptSelector extends JDialog {

	private static final String[] COLUMN_NAMES = new String[]{"", "Name", "Author"};

	private ScriptTableModel model;
	public JTable table;

	// ZZZ none of these should be here
	private BotLite bot;
	private List<ScriptDefinition> scripts;
	static ScriptSource SRC_PRECOMPILED = new FileScriptSource(new File(GlobalConfiguration.Paths.getScriptsPrecompiledDirectory()));

	// ZZZ only ScriptHandler (or rename ScriptManager)

	public ScriptSelector(BotLite bot) {
		super((Frame.getFrames().length > 0) ? Frame.getFrames()[0] : null, "Script Selector", false);
		this.bot = bot;
		this.scripts = new ArrayList<>();
		this.model = new ScriptTableModel(this.scripts);
	}

	public JTable getTable(int icon, int name) {
		table = new JTable(model);
		table.setRowHeight(20);
		table.setIntercellSpacing(new Dimension(1, 1));
		table.setShowGrid(false);
		table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		setColumnWidths(table, icon, name);
		return table;
	}


	/**
	 * Loads the scripts from the script directories
	 */
	public void load() {

		ScriptHandler sh = bot.getScriptHandler();
		sh.stopScript();

		while (sh.scriptRunning()) {
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
			}
		}

		scripts.clear();
		scripts.addAll(SRC_PRECOMPILED.list());

		for (ScriptDefinition def: scripts) {
			log.info(String.format("loading '%s'", def.name));
		}

		table = (table == null) ? getTable(0, 150) : table;
	}

	public void startAction() {
		// forward to manager
		ScriptDefinition def = model.getDefinition(table.getSelectedRow());
		try {
			bot.getScriptHandler().runScript(def.source.load(def));
		} catch (ServiceException exception) {
			exception.printStackTrace();
		}
	}

	public void stopAction() {
		ScriptHandler sh = bot.getScriptHandler();
		sh.stopScript();
	}

	public void pauseAction() {
		bot.getScriptHandler().pauseScript();
	}

	private void setColumnWidths(JTable table, int... widths) {
		for (int i = 0; i < widths.length; ++i) {
			table.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);
			table.getColumnModel().getColumn(i).setMinWidth(widths[i]);
			table.getColumnModel().getColumn(i).setMaxWidth(widths[i]);
		}
	}

	public static class ScriptTableModel extends AbstractTableModel {
		private final List<ScriptDefinition> scripts;

		public ScriptTableModel(List<ScriptDefinition> scripts) {
			this.scripts = scripts;
		}

		public ScriptDefinition getDefinition(int rowIndex) {
			return scripts.get(rowIndex);
		}

		public int getRowCount() {
			return scripts.size();
		}

		public int getColumnCount() {
			return COLUMN_NAMES.length;
		}

		public Object getValueAt(int rowIndex, int columnIndex) {
			if (rowIndex >= 0 && rowIndex < scripts.size()) {
				ScriptDefinition def = scripts.get(rowIndex);
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
