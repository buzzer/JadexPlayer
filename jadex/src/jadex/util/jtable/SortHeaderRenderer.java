package jadex.util.jtable;

import jadex.util.jtable.ISorterFilterTableModel;
import jadex.util.jtable.SortArrowIcon;

import java.awt.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.*;

public class SortHeaderRenderer extends DefaultTableCellRenderer
{
//    public static Icon NONE = new SortArrowIcon(SortArrowIcon.NONE);
    public static Icon ASCENDING = new SortArrowIcon(SortArrowIcon.ASCENDING);
    public static Icon DECENDING = new SortArrowIcon(SortArrowIcon.DECENDING);

    public SortHeaderRenderer()
    {
        setHorizontalTextPosition(LEFT);
        setHorizontalAlignment(CENTER);
    }

    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int col)
    {
        int index = -1;
        int direction = 0;

        if (table != null)
        {         
            if (table.getModel() instanceof ISorterFilterTableModel) {
                ISorterFilterTableModel model = (ISorterFilterTableModel) table.getModel();
                index = table.convertColumnIndexToView(model.getSortColumn());
                direction = model.getSortDirection();
            }

            JTableHeader header = table.getTableHeader();
            if (header != null)
            {
                setForeground(header.getForeground());
                setBackground(header.getBackground());
                setFont(header.getFont());
            }
        }
        setIcon(col==index && direction!=ISorterFilterTableModel.NONE ? new SortArrowIcon(direction): null);
        setText((value == null) ? "" : value.toString());
        setBorder(UIManager.getBorder("TableHeader.cellBorder"));
        return this;
    }
}

