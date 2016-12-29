import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.lang.reflect.Method;
import javax.swing.JMenuItem;

public class DynamicMenuItem extends JMenuItem implements ActionListener
{
	String className;
	String methodName;

	public DynamicMenuItem(String textLabel, String className, String methodName)
	{
		super(textLabel);

		this.className = className;
		this.methodName = methodName;

		addActionListener(this);
	}

	public void actionPerformed(ActionEvent a)
	{
		try
		{
			Class<?> c = Class.forName(this.className);
			Method m = c.getDeclaredMethod(this.methodName);
			m.invoke(null);
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}
}
