/******************************************************************************
 *
 *  Copyright 2014 Paphus Solutions Inc.
 *
 *  Licensed under the Eclipse Public License, Version 1.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.eclipse.org/legal/epl-v10.html
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 ******************************************************************************/

package android.widget;

import java.awt.FlowLayout;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.geom.RoundRectangle2D;

import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.Timer;

import android.content.Context;
import android.graphics.Color;

/**
 * Stub class.
 */
//public class Toast {
//	public static int LENGTH_SHORT = 0;
//	public static int LENGTH_LONG = 1;
//	
//	public String text = "";
//	
//	public static Toast makeText(Context context, String text, int value) {
//		Toast toast = new Toast();
//		toast.text = text;
//		return toast;
//	}
//	
//	public void show() {
//		
//	}
//}
public class Toast extends JDialog {
	 
    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	String msg;
    JFrame frame;
    // you can set the time for how long the dialog will be displayed
    public static final int LENGTH_LONG = 5000;
    public static final int LENGTH_SHORT = 2000;
 
    public Toast(JFrame frame, boolean modal, String msg) {
        super(frame, modal);
        this.msg = msg;
        this.frame=frame;
        initComponents();        
    }
 
    private void initComponents() {
        setLayout(new FlowLayout(FlowLayout.CENTER));
        addComponentListener(new ComponentAdapter() {
            // Give the window an rounded rect shape.
 
            @Override
            public void componentResized(ComponentEvent e) {
                setShape(new RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), 30, 30));
            }
        });
        setUndecorated(true);
        setSize(300, 50);
        setLocationRelativeTo(frame);// adding toast to frame or use null
        getContentPane().setBackground(Color.BLACK);
         
        // Determine what the GraphicsDevice can support.
        GraphicsEnvironment ge =
                GraphicsEnvironment.getLocalGraphicsEnvironment();
        GraphicsDevice gd = ge.getDefaultScreenDevice();
        final boolean isTranslucencySupported =
                gd.isWindowTranslucencySupported(GraphicsDevice.WindowTranslucency.TRANSLUCENT);
 
        //If shaped windows aren't supported, exit.
        if (!gd.isWindowTranslucencySupported(GraphicsDevice.WindowTranslucency.PERPIXEL_TRANSPARENT)) {
            System.err.println("Shaped windows are not supported");
        }
 
        //If translucent windows are supported, 
        //create an opaque window.
        // Set the window to 50% translucency, if supported.
        if (isTranslucencySupported) {
            setOpacity(0.5f);
        } else {
            System.out.println("Translucency is not supported.");
        }
 
        JLabel label = new JLabel();
        label.setForeground(Color.WHITE);
        label.setText(msg);
        add(label);
    }

	public JDialog showToast(final int DURATION) {
		JDialog dialog = this;
		Timer timer = new Timer(DURATION, new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				dialog.setVisible(false);
				dialog.dispose();
			}
		});
		timer.setRepeats(false);
		timer.start();
		dialog.setVisible(true); // if modal, application will pause here
		return dialog;
	}
}