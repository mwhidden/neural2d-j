/*
 * menu_test.java
 * This program is freely distributable without licensing fees 
 * and is provided without guarantee or warrantee expressed or 
 * implied. This program is -not- in the public domain.
 *
 * timer_test is supposed to demonstrate that window system
 * event related callbacks (like the keyboard callback) do not
 * "starve out" the dispatching of timer callbacks.  Run this
 * program and hold down the space bar.  The correct behavior
 * (assuming the system does autorepeat) is interleaved "key is 
 * 32" and "timer called" messages.  If you don't see "timer
 * called" messages, that's a problem.  This problem exists in
 * GLUT implementations through GLUT 3.2.
 */

import jgl.GL;
import jgl.GLUT;
import jgl.GLApplet;

public class timer_test extends GLApplet {

    private boolean beenhere = false;

    public void display () {
	myGL.glClear (GL.GL_COLOR_BUFFER_BIT);
	myGL.glFlush ();
    }

    /* ARGSUSED */
    public void timer (int value) {
	System.out.println ("timer called");
	myUT.glutTimerFunc (500, "timer", 0);
    }

    /* ARGSUSED1 */
    public void keyboard (char key, int x, int y) {
	if (!beenhere) {
	    myUT.glutTimerFunc (500, "timer", 0);
	    beenhere = true;
	}
	System.out.println ("key is "+key);
    }

    public void init () {
	myUT.glutCreateWindow (this);
	myGL.glClearColor (0.49f, 0.62f, 0.75f, 0.0f);
	myUT.glutDisplayFunc ("display");
	myUT.glutKeyboardFunc ("keyboard");
	myUT.glutMainLoop ();

    }

}
