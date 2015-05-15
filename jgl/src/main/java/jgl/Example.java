package jgl;

import java.awt.Frame;
import java.io.IOException;

/*
 *  bezmesh.java
 *  This program renders a lighted, filled Bezier surface,
 *  using two-dimensional evaluators.
 */

public class Example extends GLCanvas {
    int spin = -30, x=-2, y=-2, z=-2;
    GLAUX myAUX = new GLAUX(myGL);
    float lineMatDiffuse[] = {0.6f, 0.6f, 0.6f, 1.0f};
    float lineMatSpecular[] = {1.0f, 1.0f, 1.0f, 1.0f};
    float lineMatShininess[] = {0.0f};
    float neuronMatDiffuse[] = {0.6f, 0.0f, 0.3f, 1.0f};
    float neuronMatSpecular[] = {0.6f, 0.0f, 0.3f, 1.0f};
    float neuronMatShininess[] = {50.0f};

    private void initlights () {
	float ambient[] = {0.2f, 0.2f, 0.2f, 1.0f};
	float position[] = {50.0f, 50.0f, 50.0f, 1.0f};
	float mat_diffuse[] = {0.6f, 0.6f, 0.6f, 1.0f};
	float mat_specular[] = {1.0f, 1.0f, 1.0f, 1.0f};
        float mat_shininess[] = {50.0f};

        myGL.glEnable (GL.GL_LIGHTING);
        myGL.glEnable (GL.GL_LIGHT0);

        myGL.glLightfv (GL.GL_LIGHT0, GL.GL_AMBIENT, ambient);
        myGL.glLightfv (GL.GL_LIGHT0, GL.GL_POSITION, position);

    }

    public void drawAxes(int len)
    {
        myGL.glMaterialfv (GL.GL_FRONT, GL.GL_DIFFUSE, lineMatDiffuse);
        myGL.glMaterialfv (GL.GL_FRONT, GL.GL_SPECULAR, lineMatSpecular);
        myGL.glMaterialfv (GL.GL_FRONT, GL.GL_SHININESS, lineMatShininess);
        drawLine(0,0,0,0,len,0);
        for(int x=0; x < len; x++){
            drawLine(x,0,-.5f,x,0,.5f);
        }
        drawLine(0,0,0,0,0,len);
        for(int y=0; y < len; y++){
            drawLine(-.5f,y,0,.5f,y,0);
        }
        drawLine(0,0,0,len,0,0);
        for(int z=0; z < len; z++){
            drawLine(-.5f,0,z,.5f,0,z);
        }
        drawLine(len,0,0,len,0,len);
        drawLine(0,len,0,0,len,len);
        drawLine(0,0,len,len,0,len);
        drawLine(0,len,len,len,len,len);
        drawLine(len,len,0,len,len,len);
        drawLine(len,0,len,len,len,len);
        drawLine(0,len,0,len,len,0);
        drawLine(0,0,len,0,len,len);

    }

    public void drawLine(float x0, float y0, float z0,
            float x1, float y1, float z1)
    {
        myGL.glBegin(GL.GL_LINES);
        myGL.glVertex3f(x0,y0,z0);
        myGL.glPushMatrix();
        myGL.glTranslated(x1, y1, z1);
        myGL.glVertex3f(0,0,0);
        myGL.glPopMatrix();
        myGL.glEnd();

    }

    public void idle(){
        spin = spin + 2;
        if(spin > 60)
          spin = spin - 120;
        z++;
        if(z == 2){
            z = -2;
            y++;
            if(y==3){
                y = -2;
                x++;
                if(x ==3){
                    x=-2;
                    y=-2;
                    z=-2;
                }
            }

        }
        myUT.glutPostRedisplay();
    }


    public void display () {
	myGL.glClear (GL.GL_COLOR_BUFFER_BIT | GL.GL_DEPTH_BUFFER_BIT);
        //myGL.glRotated(-spin,0,1,0);
        //myGL.glColor3f (1.0f, 1.0f, 1.0f);

        System.out.println(spin);
        myGL.glPushMatrix ();
	//myGL.glRotatef (5.0f, 0.0f, 1.0f, 0.0f);
	//myGL.glEvalMesh2 (GL.GL_FILL, 0, 20, 0, 20);
        //for(int xc=-2; xc <= x; xc++){
        //    for(int yc=-2; yc <= y; yc++){
        //        for(int zc=-2; zc <= z; zc++){
                    myGL.glPushMatrix ();
                    //myGL.glTranslated(xc, yc, zc);
                    myGL.glTranslated(0,0,spin);
                    myGL.glMaterialfv (GL.GL_FRONT, GL.GL_DIFFUSE, neuronMatDiffuse);
                    myGL.glMaterialfv (GL.GL_FRONT, GL.GL_SPECULAR, neuronMatSpecular);
                    myGL.glMaterialfv (GL.GL_FRONT, GL.GL_SHININESS, neuronMatShininess);
                    myAUX.auxSolidSphere(0.5);
                    /*myGL.glBegin (GL.GL_POLYGON);
                        myGL.glVertex3f (-0.25f, -0.25f, 0.0f);
                        myGL.glVertex3f (0.25f, -0.25f, 0.0f);
                        myGL.glVertex3f (0.25f, 0.25f, 0.0f);
                        myGL.glVertex3f (-0.25f, 0.25f, 0.0f);
                    myGL.glEnd ();*/

                    myGL.glPopMatrix ();
                //}
            //}
        //}
        drawAxes(10);
        myGL.glPopMatrix();
	myGL.glFlush ();
        // Todo, only set this once
        myUT.glutIdleFunc("idle");
    }

    private void myinit () {
	myGL.glClearColor (1.0f, 1.0f, 1.0f, 1.0f);
	myGL.glEnable (GL.GL_DEPTH_TEST);
	//myGL.glMap2f (GL.GL_MAP2_VERTEX_3, 0.0f, 1.0f, 3, 4,
	//	      0.0f, 1.0f, 12, 4, ctrlpoints);
	//myGL.glEnable (GL.GL_MAP2_VERTEX_3);
	//myGL.glEnable (GL.GL_AUTO_NORMAL);
	//myGL.glEnable (GL.GL_NORMALIZE);
	//myGL.glMapGrid2f (20, 0.0f, 1.0f, 20, 0.0f, 1.0f);
        myGL.glShadeModel(GL.GL_SMOOTH);
	initlights ();		/* for lighted version only */
    }

    public void myReshape (int w, int h) {
        myGL.glViewport (0, 0, w, h);
        myGL.glMatrixMode (GL.GL_PROJECTION);
        myGL.glLoadIdentity ();
	/*if (w <= h) {
	    myGL.glOrtho (-4.0f, 4.0f,
	    		  -4.0f *(float)h/(float)w,
			   4.0f *(float)h/(float)w,
			  -4.0f, 4.0f);
	} else {
	    myGL.glOrtho (-4.0f *(float)w/(float)h,
	    		   4.0f *(float)w/(float)h,
			  -4.0f, 4.0f,
			  -4.0f, 4.0f);
	}*/
        myGLU.gluPerspective (60.0, 1.0 * (double)w/(double)h, 1.0, 60.0);
        myGLU.gluLookAt(10,15,20,3,0,0,0,1,0);
        myGL.glMatrixMode (GL.GL_MODELVIEW);
        myGL.glLoadIdentity ();

    }

    public void keyboard (char key, int x, int y) {
	switch (key) {
	    case 27:
		System.exit(0);
	    default:
		break;
	}
    }

    public void init () {
	myUT.glutInitWindowSize (200, 200);
	myUT.glutInitWindowPosition (0, 0);
	myUT.glutCreateWindow (this);
	myinit ();
	myUT.glutReshapeFunc ("myReshape");
	myUT.glutDisplayFunc ("display");
	myUT.glutKeyboardFunc ("keyboard");
	myUT.glutMainLoop ();
    }

    static public void main (String args[]) throws IOException {
	Frame mainFrame = new Frame ();
	mainFrame.setSize (508, 527);
	Example mainCanvas = new Example ();
	mainCanvas.init();
	mainFrame.add (mainCanvas);
	mainFrame.setVisible (true);
    }

}
