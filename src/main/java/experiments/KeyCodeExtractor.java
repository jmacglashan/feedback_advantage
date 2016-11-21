package experiments;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

/**
 * @author James MacGlashan.
 */
public class KeyCodeExtractor {

	public static void main(String[] args) {
		JFrame frame = new JFrame();
		frame.setPreferredSize(new Dimension(100, 100));
		frame.setLayout(new BorderLayout());

		final JLabel label = new JLabel("<html><center>Press the key/button to see its code</center></html>");
		label.setPreferredSize(new Dimension(200, 100));
		frame.add(label, BorderLayout.CENTER);

		frame.addKeyListener(new KeyListener() {
			@Override
			public void keyTyped(KeyEvent e) {

			}

			@Override
			public void keyPressed(KeyEvent e) {
				System.out.println(""+e.getKeyChar() + " : " + e.getKeyCode());
				label.setText(""+e.getKeyChar() + " : " + e.getKeyCode());
			}

			@Override
			public void keyReleased(KeyEvent e) {

			}
		});

		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		frame.pack();
		frame.setVisible(true);
	}

}
