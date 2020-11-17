package gui;

import swing.components.SelectionBox;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;

public abstract class MoveButtonPanel<E> extends JPanel {
    protected SelectionBox<E> tabSelector;

    public MoveButtonPanel(SelectionBox.StringMapper<E> stringMapper) {
        super(new GridLayout(0, 2));
        JButton button = new JButton("move");
        this.tabSelector = new SelectionBox<>(stringMapper, this.getValues());

        button.addActionListener(this.buttonListener());

        this.add(button);
        this.add(this.tabSelector);
    }

    protected abstract E[] getValues();

    protected abstract ActionListener buttonListener();

    public interface Tab {
        String getName();
    }
}
