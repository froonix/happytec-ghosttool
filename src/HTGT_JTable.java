/**
 * HTGT_JTable.java: Custom JTable implementation
 * Copyright (C) 2016-2022 Christian Schrötter <cs@fnx.li>
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301  USA
 */

import java.awt.event.KeyEvent;

import javax.swing.ActionMap;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.JTable;
import javax.swing.KeyStroke;
import javax.swing.SwingConstants;

import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableModel;

@SuppressWarnings("serial")
class HTGT_JTable extends JTable
{
	DefaultTableCellRenderer renderLeft = new DefaultTableCellRenderer();
	DefaultTableCellRenderer renderCenter = new DefaultTableCellRenderer();
	DefaultTableCellRenderer renderRight = new DefaultTableCellRenderer();
	{
		renderLeft.setHorizontalAlignment(SwingConstants.LEFT);
		renderCenter.setHorizontalAlignment(SwingConstants.CENTER);
		renderRight.setHorizontalAlignment(SwingConstants.RIGHT);
	}

	public HTGT_JTable(TableModel dm)
	{
		super(dm);

		HTGT_ActionListener action;
		ActionMap actionMap = getActionMap();
		InputMap inputMap = getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);

		inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_PAGE_UP,         HTGT.NONE             ), "scrollUpChangeSelection");
		inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_PAGE_DOWN,       HTGT.NONE             ), "scrollDownChangeSelection");

		inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_UP,              HTGT.NONE             ), "selectPreviousRow");
		inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_KP_UP,           HTGT.NONE             ), "selectPreviousRow");
		inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_UP,              HTGT.SHIFT            ), "selectPreviousRowExtendSelection");
		inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_KP_UP,           HTGT.SHIFT            ), "selectPreviousRowExtendSelection");

		inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_DOWN,            HTGT.NONE             ), "selectNextRow");
		inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_KP_DOWN,         HTGT.NONE             ), "selectNextRow");
		inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_DOWN,            HTGT.SHIFT            ), "selectNextRowExtendSelection");
		inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_KP_DOWN,         HTGT.SHIFT            ), "selectNextRowExtendSelection");

		inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_HOME,            HTGT.NONE             ), "selectFirstRow");
		inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_HOME,            HTGT.CTRL             ), "selectFirstRow");
		inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_HOME,            HTGT.SHIFT            ), "selectFirstRowExtendSelection");
		inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_HOME,            HTGT.SHIFT + HTGT.CTRL), "selectFirstRowExtendSelection");

		inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_END,             HTGT.NONE             ), "selectLastRow");
		inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_END,             HTGT.CTRL             ), "selectLastRow");
		inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_END,             HTGT.SHIFT            ), "selectLastRowExtendSelection");
		inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_END,             HTGT.SHIFT + HTGT.CTRL), "selectLastRowExtendSelection");

		inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE,          HTGT.NONE             ), "clearSelection");
		inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_A,               HTGT.CTRL             ), "selectAll");

		inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_DIVIDE ,         HTGT.NONE             ), "moveGhostsToProfile");
		inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_MULTIPLY ,       HTGT.NONE             ), "copyGhostsToProfile");

		inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_SUBTRACT ,       HTGT.NONE             ), "selectPreviousGameProfile");
		inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_KP_LEFT,         HTGT.NONE             ), "selectPreviousGameProfile");
		inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_MINUS,           HTGT.NONE             ), "selectPreviousGameProfile");

		inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_ADD,             HTGT.NONE             ), "selectNextGameProfile");
		inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_KP_RIGHT,        HTGT.NONE             ), "selectNextGameProfile");
		inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_PLUS,            HTGT.NONE             ), "selectNextGameProfile");

		inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_COMMA,           HTGT.NONE             ), "selectSpecialGameProfile");
		inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_PERIOD,          HTGT.NONE             ), "selectSpecialGameProfile");
		inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_DECIMAL,         HTGT.NONE             ), "selectSpecialGameProfile");
		inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_SEPARATOR,       HTGT.NONE             ), "selectSpecialGameProfile");
		inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_CIRCUMFLEX,      HTGT.NONE             ), "selectSpecialGameProfile");
		inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_DEAD_CIRCUMFLEX, HTGT.NONE             ), "selectSpecialGameProfile");

		inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_NUMBER_SIGN,     HTGT.NONE             ), "selectDefaultGameProfile");
		inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER,           HTGT.NONE             ), "selectRegularGameProfile");

		action = new HTGT_ActionListener();
		action.setPrivateAction("moveGhosts");
		actionMap.put("moveGhostsToProfile", action);

		action = new HTGT_ActionListener();
		action.setPrivateAction("copyGhosts");
		actionMap.put("copyGhostsToProfile", action);

		action = new HTGT_ActionListener();
		action.setPrivateAction("selectPrevProfile");
		actionMap.put("selectPreviousGameProfile", action);

		action = new HTGT_ActionListener();
		action.setPrivateAction("selectNextProfile");
		actionMap.put("selectNextGameProfile", action);

		action = new HTGT_ActionListener();
		action.setPrivateAction("selectDefaultProfile");
		actionMap.put("selectDefaultGameProfile", action);

		action = new HTGT_ActionListener();
		action.setPrivateAction("selectRegularProfile");
		actionMap.put("selectRegularGameProfile", action);

		action = new HTGT_ActionListener();
		action.setPrivateArguments(new Object[]{-1});
		action.setPrivateAction("selectProfileByNumber");
		actionMap.put("selectSpecialGameProfile", action);

		for(int i = 0; i < 10; i++)
		{
			try
			{
				inputMap.put(KeyStroke.getKeyStroke(KeyEvent.class.getField("VK_NUMPAD" + i).getInt(null), HTGT.NONE), "selectGameProfileWithNumber" + i);
				inputMap.put(KeyStroke.getKeyStroke(KeyEvent.class.getField("VK_" + i).getInt(null), HTGT.NONE), "selectGameProfileWithNumber" + i);

				action = new HTGT_ActionListener();
				action.setPrivateArguments(new Object[]{i});
				action.setPrivateAction("selectProfileByNumber");
				actionMap.put("selectGameProfileWithNumber" + i, action);
			}
			catch(NoSuchFieldException|IllegalAccessException e)
			{
				e.printStackTrace();
			}
		}

		//getColumnModel().addColumnModelListener(this);
		getModel().addTableModelListener(this);
	}

	@Override
	public TableCellRenderer getCellRenderer(int row, int column)
	{
		if(column == 0)
		{
			return renderLeft;
		}
		else if(column == 5)
		{
			return renderRight;
		}
		else
		{
			return renderCenter;
		}
	}

	@Override
	public boolean isCellEditable(int row, int column)
	{
		return false;
	}
}
