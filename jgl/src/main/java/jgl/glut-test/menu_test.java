/*
 * menu_test.java
 * This program is freely distributable without licensing fees 
 * and is provided without guarantee or warrantee expressed or 
 * implied. This program is -not- in the public domain.
 */

import java.lang.Integer;
import java.lang.String;

import jgl.GL;
import jgl.GLUT;
import jgl.GLApplet;

public class menu_test extends GLApplet {

    private int win, subwin;
    private int mainmenu, submenu;
    private int item = 666;

    public void display () {
	myGL.glClear (GL.GL_COLOR_BUFFER_BIT);
	myGL.glFlush ();
    }

    /* ARGSUSED1 */
    private void gokey (char key, int x, int y) {
	String str;
	int mods;

	mods = myUT.glutGetModifiers();
	System.out.println ("key = "+key+", mods = 0x"+Integer.toHexString(mods));
	if ((mods & GLUT.GLUT_ACTIVE_ALT) != 0) {
	    switch (key) {
		case '1':
		    System.out.println ("Change to sub menu 1");
		    myUT.glutChangeToSubMenu (1, "sub 1", submenu);
		    break;
		case '2':
		    System.out.println ("Change to sub menu 2");
		    myUT.glutChangeToSubMenu (2, "sub 2", submenu);
		    break;
		case '3':
		    System.out.println ("Change to sub menu 3");
		    myUT.glutChangeToSubMenu (3, "sub 3", submenu);
		    break;
		case '4':
		    System.out.println ("Change to sub menu 4");
		    myUT.glutChangeToSubMenu (4, "sub 4", submenu);
		    break;
		case '5':
		    System.out.println ("Change to sub menu 5");
		    myUT.glutChangeToSubMenu (5, "sub 5", submenu);
		    break;
	    }
	} else {
	    switch (key) {
		case '1':
		    System.out.println ("Change to menu entry 1");
		    myUT.glutChangeToMenuEntry (1, "entry 1", 1);
		    break;
		case '2':
		    System.out.println ("Change to menu entry 2");
		    myUT.glutChangeToMenuEntry (2, "entry 2", 2);
		    break;
		case '3':
		    System.out.println ("Change to menu entry 3");
		    myUT.glutChangeToMenuEntry (3, "entry 3", 3);
		    break;
		case '4':
		    System.out.println ("Change to menu entry 4");
		    myUT.glutChangeToMenuEntry (4, "entry 4", 4);
		    break;
		case '5':
		    System.out.println ("Change to menu entry 5");
		    myUT.glutChangeToMenuEntry (5, "entry 5", 5);
		    break;
		case 'a':
		case 'A':
		    System.out.println ("Adding menu entry "+item);
		    str = new String ("added entry "+item);
		    myUT.glutAddMenuEntry (str, item);
		    item++;
		    break;
		case 's':
		case 'S':
		    System.out.println ("Adding submenu "+item);
		    str = new String ("added submenu "+item);
		    myUT.glutAddSubMenu (str, submenu);
		    item++;
		    break;
		case 'q':
		    System.out.println ("Remove 1");
		    myUT.glutRemoveMenuItem (1);
		    break;
		case 'w':
		    System.out.println ("Remove 2");
		    myUT.glutRemoveMenuItem (2);
		    break;
		case 'e':
		    System.out.println ("Remove 3");
		    myUT.glutRemoveMenuItem (3);
		    break;
		case 'r':
		    System.out.println ("Remove 4");
		    myUT.glutRemoveMenuItem (4);
		    break;
		case 't':
		    System.out.println ("Remove 5");
		    myUT.glutRemoveMenuItem (5);
		    break;
	    }
	}
    }

    public void keyboard (char key, int x, int y) {
	myUT.glutSetMenu (mainmenu);
	gokey(key, x, y);
    }

    public void keyboard2 (char key, int x, int y) {
	myUT.glutSetMenu (submenu);
	gokey(key, x, y);
    }

    public void menu (int value) {
	System.out.println ("menu: entry = "+value);
    }

    public void menu2 (int value) {
	System.out.println ("menu2: entry = "+value);
    }

    public void init () {
	myUT.glutInitWindowSize (200, 200);
	myUT.glutInitWindowPosition (0, 0);
	myUT.glutCreateWindow (this);
	myGL.glClearColor (0.3f, 0.3f, 0.3f, 0.0f);
	myUT.glutDisplayFunc ("display");
	myUT.glutKeyboardFunc ("keyboard");
	submenu = myUT.glutCreateMenu ("menu2");
	myUT.glutAddMenuEntry ("Sub menu 1", 1001);
	myUT.glutAddMenuEntry ("Sub menu 2", 1002);
	myUT.glutAddMenuEntry ("Sub menu 3", 1003);
	mainmenu = myUT.glutCreateMenu ("menu");
	myUT.glutAddMenuEntry ("First", -1);
	myUT.glutAddMenuEntry ("Second", -2);
	myUT.glutAddMenuEntry ("Third", -3);
	myUT.glutAddSubMenu ("Submenu init", submenu);
	myUT.glutAttachMenu (GLUT.GLUT_RIGHT_BUTTON);
/*
	subwin = myUT.glutCreateSubWindow (win, 50, 50, 50, 50);
	myGL.glClearColor (0.7f, 0.7f, 0.7f, 0.0f);
	myUT.glutDisplayFunc ("display");
	myUT.glutKeyboardFunc ("keyboard2");
*/
	myUT.glutMainLoop ();
    }

}
