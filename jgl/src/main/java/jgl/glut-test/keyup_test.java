/*
 * keyup_test.java
 */

import java.lang.Integer;
import java.lang.String;

import jgl.GL;
import jgl.GLUT;
import jgl.GLApplet;

public class keyup_test extends GLApplet {

    private int win, subwin;
    private int mainmenu, submenu;
    private int item = 666;

    public void key (char key, int x, int y) {
	System.out.println ("kDN: "+key+" <"+(int)key+"> @ ("+x+","+y+")");
    }

    public void keyup (char key, int x, int y) {
	System.out.println ("kUP: "+key+" <"+(int)key+"> @ ("+x+","+y+")");
    }

    public void special (char key, int x, int y) {
	System.out.println ("sDN: "+key+" <"+(int)key+"> @ ("+x+","+y+")");
    }

    public void specialup (char key, int x, int y) {
	System.out.println ("sUP: "+key+" <"+(int)key+"> @ ("+x+","+y+")");
    }

    public void menu (int value) {
	switch(value) {
/*
	    case 1:
		myUT.glutIgnoreKeyRepeat (1);
		break;
	    case 2:
		myUT.glutIgnoreKeyRepeat (0);
		break;
*/
	    case 3:
		myUT.glutKeyboardFunc (null);
		break;
	    case 4:
		myUT.glutKeyboardFunc ("key");
		break;
	    case 5:
		myUT.glutKeyboardUpFunc (null);
		break;
	    case 6:
		myUT.glutKeyboardUpFunc ("keyup");
		break;
	    case 7:
		myUT.glutSpecialFunc (null);
		break;
	    case 8:
		myUT.glutSpecialFunc ("special");
		break;
	    case 9:
		myUT.glutSpecialUpFunc (null);
		break;
	    case 10:
		myUT.glutSpecialUpFunc ("specialup");
		break;
	}
    }

    public void display () {
	myGL.glClear (GL.GL_COLOR_BUFFER_BIT);
	myGL.glFlush ();
    }

    public void init () {
	myUT.glutCreateWindow (this);
	myGL.glClearColor (0.49f, 0.62f, 0.75f, 0.0f);
	myUT.glutDisplayFunc ("display");
	myUT.glutKeyboardFunc ("key");
	myUT.glutKeyboardUpFunc ("keyup");
	myUT.glutSpecialFunc ("special");
	myUT.glutSpecialUpFunc ("specialup");
	myUT.glutCreateMenu ("menu");
/*
	myUT.glutAddMenuEntry ("Ignore autorepeat", 1);
	myUT.glutAddMenuEntry ("Accept autorepeat", 2);
*/
	myUT.glutAddMenuEntry ("Stop key", 3);
	myUT.glutAddMenuEntry ("Start key", 4);
	myUT.glutAddMenuEntry ("Stop key up", 5);
	myUT.glutAddMenuEntry ("Start key up", 6);
	myUT.glutAddMenuEntry ("Stop special", 7);
	myUT.glutAddMenuEntry ("Start special", 8);
	myUT.glutAddMenuEntry ("Stop special up", 9);
	myUT.glutAddMenuEntry ("Start special up", 10);
	myUT.glutAttachMenu (GLUT.GLUT_RIGHT_BUTTON);
	myUT.glutMainLoop ();
    }

}
