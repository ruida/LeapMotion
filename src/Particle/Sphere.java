package Particle;

import java.awt.event.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;

import javax.media.opengl.*;
import javax.media.opengl.awt.GLCanvas;
import javax.media.opengl.glu.GLU;

import com.jogamp.opengl.util.Animator;
import com.jogamp.opengl.util.gl2.GLUT;
import com.leapmotion.leap.Controller;

/**
 * One front most finger movement that drag the 3D sphere inside the panel. 
 * @author Ruida Cheng
 */
public class Sphere implements GLEventListener, KeyListener {

	private GLUT glut;
	private GLU glu;
	private float x = 0, y = 0, z = 0;
	private GLCanvas m_kCanvas;

	public Sphere() {
		InitGL();
	}

	/** GLCanvas for Java/JOGL */
	void InitGL() {
		GLProfile kProfile = GLProfile.getMaxProgrammable(true);
		GLCapabilities kGlCapabilities = new GLCapabilities(kProfile);
		kGlCapabilities.setHardwareAccelerated(true);
		m_kCanvas = new GLCanvas(kGlCapabilities);
		m_kCanvas.setSize(500, 500);
		m_kCanvas.addGLEventListener(this);
		m_kCanvas.addKeyListener(this);
	}

	public GLCanvas GetCanvas() {
		return m_kCanvas;
	}

	public static void main(String[] args) {

		Sphere kWorld = new Sphere();
		Frame frame = new Frame("Drag Sphere");
		frame.add(kWorld.GetCanvas());
		frame.setSize(kWorld.GetCanvas().getWidth(), kWorld.GetCanvas().getHeight());
		/* Animator serves the purpose of the idle function, calls display: */
		final Animator animator = new Animator(kWorld.GetCanvas());
		frame.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				// Run this on another thread than the AWT event queue to
				// avoid deadlocks on shutdown on some platforms
				new Thread(new Runnable() {
					public void run() {
						animator.stop();
						System.exit(0);
					}
				}).start();
			}
		});
		frame.setVisible(true);
		animator.start();

		final SphereListener sphereListener = new SphereListener(kWorld);
		Controller controller = new Controller();
		controller.addListener(sphereListener);
		System.out.println("Press Enter to quit...");
		try {
			System.in.read();
		} catch (IOException e) {
			e.printStackTrace();
		}

		// Remove the sample listener when done
		controller.removeListener(sphereListener);

	}

	/*
	 * Initialize material property, light source, lighting model, and depth
	 * buffer.
	 */
	public void init(GLAutoDrawable drawable) {
		GL2 gl = drawable.getGL().getGL2();
		glut = new GLUT();
		glu = new GLU();

		float mat_specular[] = { 1.0f, 1.0f, 1.0f, 1.0f };
		float mat_shininess[] = { 50.0f };
		float light_position[] = { 1.0f, 1.0f, 1.0f, 0.0f };
		//
		gl.glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
		gl.glShadeModel(GL2.GL_SMOOTH);
		//
		gl.glMaterialfv(GL.GL_FRONT, GL2.GL_SPECULAR, mat_specular, 0);
		gl.glMaterialfv(GL.GL_FRONT, GL2.GL_SHININESS, mat_shininess, 0);
		gl.glLightfv(GL2.GL_LIGHT0, GL2.GL_POSITION, light_position, 0);
		//
		gl.glEnable(GL2.GL_LIGHTING);
		gl.glEnable(GL2.GL_LIGHT0);
		gl.glEnable(GL.GL_DEPTH_TEST);
		gl.glDepthFunc(GL.GL_LESS);
	}

	public void display(GLAutoDrawable drawable) {

		float no_mat[] = { 0.0f, 0.0f, 0.0f, 1.0f };
		float mat_ambient[] = { 0.7f, 0.7f, 0.7f, 1.0f };
		float mat_ambient_color[] = { 0.8f, 0.8f, 0.2f, 1.0f };
		float mat_diffuse[] = { 0.1f, 0.5f, 0.8f, 1.0f };
		float mat_specular[] = { 1.0f, 1.0f, 1.0f, 1.0f };
		float no_shininess[] = { 0.0f };
		float low_shininess[] = { 5.0f };
		float high_shininess[] = { 100.0f };
		float mat_emission[] = { 0.3f, 0.2f, 0.2f, 0.0f };

		GL2 gl = drawable.getGL().getGL2();

		gl.glClear(GL.GL_COLOR_BUFFER_BIT | GL.GL_DEPTH_BUFFER_BIT);
		gl.glLoadIdentity();
		
		gl.glPushMatrix();
        gl.glTranslated(x, y, z);
		gl.glMaterialfv(GL.GL_FRONT, GL2.GL_SPECULAR, mat_ambient_color, 0);
        gl.glMaterialfv(GL.GL_FRONT, GL2.GL_DIFFUSE, mat_diffuse, 0);
        gl.glMaterialfv(GL.GL_FRONT, GL2.GL_SPECULAR, mat_specular, 0);
        gl.glMaterialfv(GL.GL_FRONT, GL2.GL_SHININESS, high_shininess, 0);
        gl.glMaterialfv(GL.GL_FRONT, GL2.GL_EMISSION, no_mat, 0);
        glut.glutSolidSphere(0.05f, 20, 20);
        gl.glPopMatrix();
		gl.glFlush();
	}

	public void updateLocation(float _x, float _y, float _z) {
		System.err.println("x = " + x + " y = " + y + " z = " + z);
		x = _x;
		y = _y;
		z = _z;

	}

	public void reshape(GLAutoDrawable drawable, int x, int y, int w, int h) {
		GL2 gl = drawable.getGL().getGL2();

		gl.glViewport(0, 0, w, h);
		gl.glMatrixMode(GL2.GL_PROJECTION);
		gl.glLoadIdentity();
		// float aspect = w / h;
		// glu.gluPerspective(50, aspect, 1.0, 100.0);

		if (w <= h) //
			gl.glOrtho(-1.5, 1.5,//
					-1.5 * (float) h / (float) w,//
					1.5 * (float) h / (float) w,//
					-10.0, 10.0);
		else
			gl.glOrtho(-1.5 * (float) w / (float) h, //
					1.5 * (float) w / (float) h, //
					-1.5, 1.5, -10.0, 10.0);

		gl.glMatrixMode(GL2.GL_MODELVIEW);
		gl.glLoadIdentity();
		// glu.gluLookAt(0.0, 0.0, 10.0, 0.0, 0.0, 0.0, 0.0, 1.0, 0.0);
	}

	public void displayChanged(GLAutoDrawable drawable, boolean modeChanged,
			boolean deviceChanged) {
	}

	public void keyTyped(KeyEvent key) {
	}

	public void keyPressed(KeyEvent key) {
		switch (key.getKeyChar()) {
		case KeyEvent.VK_ESCAPE:
			System.exit(0);
			break;

		default:
			break;
		}
	}

	public void keyReleased(KeyEvent key) {
	}

	public void dispose(GLAutoDrawable arg0) {

	}

}
