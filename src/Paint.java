import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;

import javax.swing.JFrame;
import javax.swing.JPanel;
public class Paint extends JPanel{
	public void paint(Graphics g) {
	    super.paint(g);
	    Graphics2D g2d = (Graphics2D) g;

	    GradientPaint gp1 = new GradientPaint(0, 0, new Color(100,255,100), 900, 300, Color.white, false);

	    g2d.setPaint(gp1);
	    g2d.fillRect(0, 0, 1000, 200);

	  }
	public static void main(String[] args) {

	    JFrame frame = new JFrame("GradientsLine");
	    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	    frame.add(new Paint());
	    frame.setSize(1000, 350);
	    frame.setLocationRelativeTo(null);
	    frame.setVisible(true);
	  }

}
