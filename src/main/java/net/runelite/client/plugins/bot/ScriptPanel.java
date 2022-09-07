package net.runelite.client.plugins.bot;

import net.runelite.client.ui.PluginPanel;
import net.runelite.client.ui.components.materialtabs.MaterialTab;
import net.runelite.client.ui.components.materialtabs.MaterialTabGroup;
import net.runelite.client.util.ImageUtil;
import net.runelite.rsb.botLauncher.BotLite;
import net.runelite.rsb.internal.globval.GlobalConfiguration;
import net.runelite.rsb.plugin.ScriptSelector;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.border.CompoundBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.IOException;

public class ScriptPanel extends PluginPanel {
	private BotLite bot;
	private JScrollPane scriptsSelectionScrollPane;
	private JTable scriptsTable;

	private ScriptSelector scriptSelector;
	private MaterialTabGroup scriptPanelToolbar;

	private MaterialTab buttonStopTab;
	private MaterialTab buttonPauseTab;
	private MaterialTab buttonStartTab;
	private MaterialTab buttonReloadTab;

	public ScriptPanel(BotLite bot) {
		this.bot = bot;
		scriptSelector = new ScriptSelector(bot);
		initComponents();
	}

	private void initComponents() {
		scriptsSelectionScrollPane = new JScrollPane();

		// Make a search area
		scriptSelector.load();

		scriptPanelToolbar = new MaterialTabGroup();
		scriptPanelToolbar.setLayout(new GridLayout(1, 5, 5, 5));

		//======== scripts scroll pane ========
		scriptsSelectionScrollPane.setViewportView(scriptSelector.table);

		//---- buttonStart ----
		final BufferedImage startIcon = ImageUtil.loadImageResource(getClass(), "start.png");
		buttonStartTab = new MaterialTab(new ImageIcon(startIcon.getScaledInstance(24, 24, 5)), scriptPanelToolbar, null);
		buttonStartTab.setSize(new Dimension(28, 28));
		buttonStartTab.setMinimumSize(new Dimension(0, 28));
		buttonStartTab.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent mouseEvent)
			{
				scriptSelector.startAction();;
			}
		});
		scriptPanelToolbar.addTab(buttonStartTab);

		//---- buttonPause ----
		final BufferedImage pauseIcon = ImageUtil.loadImageResource(getClass(), "pause.png");
		buttonPauseTab = new MaterialTab(new ImageIcon(pauseIcon.getScaledInstance(20, 20, 5)), scriptPanelToolbar, null);
		buttonPauseTab.setSize(new Dimension(28, 28));
		buttonPauseTab.setMinimumSize(new Dimension(0, 0));
		buttonPauseTab.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent mouseEvent) {
				scriptSelector.pauseAction();
			}
		});
		scriptPanelToolbar.addTab(buttonPauseTab);

		//---- buttonStop ----
		final BufferedImage stopIcon = ImageUtil.loadImageResource(getClass(), "stop.png");
		buttonStopTab = new MaterialTab(new ImageIcon(stopIcon.getScaledInstance(20, 20, 5)), scriptPanelToolbar, null);
		buttonStopTab.setSize(new Dimension(28, 28));
		buttonStopTab.setMinimumSize(new Dimension(0, 28));
		buttonStopTab.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent mouseEvent)	{
				scriptSelector.stopAction();
			}
		});
		scriptPanelToolbar.addTab(buttonStopTab);

		//---- buttonReload ----
		final BufferedImage iconImage = ImageUtil.loadImageResource(getClass(), "reload.png");
		buttonReloadTab = new MaterialTab(new ImageIcon(iconImage.getScaledInstance(20, 20, 5)), scriptPanelToolbar, null);
		buttonReloadTab.setSize(new Dimension(28, 28));
		buttonReloadTab.setMinimumSize(new Dimension(0, 28));
		buttonReloadTab.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent mouseEvent) {
				scriptSelector.load();
			}
		});
		scriptPanelToolbar.addTab(buttonReloadTab);
		assignLayouts();
	}

	/**
	 * Assigns the layouts for the script panel
	 */
	private void assignLayouts() {
		GroupLayout layout = new GroupLayout(this);
		setLayout(layout);

		layout.setHorizontalGroup(
				layout.createParallelGroup()
				.addComponent(scriptPanelToolbar, 0, 240, Short.MAX_VALUE)
				.addGap(10, 10, 10)
				.addComponent(scriptsSelectionScrollPane, GroupLayout.DEFAULT_SIZE, 240, Short.MAX_VALUE)
				.addGroup(layout.createSequentialGroup()
						  .addGroup(layout.createParallelGroup()
									.addGroup(layout.createSequentialGroup()
											  .addGap(47, 47, 47))
									.addGap(10, 10, 10))
						  .addContainerGap(30, Short.MAX_VALUE)));

		layout.setVerticalGroup(layout.createParallelGroup()
								.addGroup(layout.createSequentialGroup()
										  .addComponent(scriptPanelToolbar, 28, 40, 40)
										  .addComponent(scriptsSelectionScrollPane, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
										  .addGap(0, 10, Short.MAX_VALUE)
										  .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)).addContainerGap(10, Short.MAX_VALUE)));
	}

}
