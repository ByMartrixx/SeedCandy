package gui.components.extrapanel;

import javax.swing.*;

public class ExtraPanel extends JPanel {
    public GeneratorPanel generatorPanel;
    public ToolsPanel toolsPanel;
    public LocatorPanel locatorPanel;
    public final JTabbedPane tabbedPane;

    public ExtraPanel() {
        this.generatorPanel = new GeneratorPanel();
        this.toolsPanel = new ToolsPanel();
        this.locatorPanel = new LocatorPanel();
        this.tabbedPane = new JTabbedPane();

        this.tabbedPane.add(this.generatorPanel);
        this.tabbedPane.add(this.toolsPanel);
        this.tabbedPane.add(this.locatorPanel);
        this.add(this.tabbedPane);

        this.setName("Extra");
    }
}
