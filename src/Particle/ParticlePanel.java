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
import com.leapmotion.leap.Vector;

import java.util.concurrent.*;
import java.util.*;

/**
 * Particle panel demo to allow users to move one hand over the Leap Motion controller, 
 * and draw the particle lines.   
 * 
 * @author Ruida Cheng
 *
 */
public class ParticlePanel implements GLEventListener, KeyListener {

	private GLUT glut;
	private GLU glu;
	
	private GLCanvas m_kCanvas;

	private static int MAXLEN = 20;

	// finger hashtable
	private ConcurrentMap<Integer, ParticleArray> fingers;

	private float no_mat[] = { 0.0f, 0.0f, 0.0f, 1.0f };
	private float mat_ambient[] = { 0.7f, 0.7f, 0.7f, 1.0f };
	private float mat_ambient_color[] = { 0.8f, 0.8f, 0.2f, 1.0f };
	private float mat_diffuse[] = { 0.1f, 0.5f, 0.8f, 1.0f };
	private float mat_specular[] = { 1.0f, 1.0f, 1.0f, 1.0f };
	private float no_shininess[] = { 0.0f };
	private float low_shininess[] = { 5.0f };
	private float high_shininess[] = { 100.0f };
	private float mat_emission[] = { 0.3f, 0.2f, 0.2f, 0.0f };

	/**
	 * Finger array to hold the particles array and current index for the particle array. 
	 * @author Ruida Cheng
	 */
	class ParticleArray {
		/** particles array */ 
		public Particle[] particles;
		/** current index */
		public int index;

		public ParticleArray() {
			/** define the particle array with max length */
			particles = new Particle[MAXLEN];
			index = 0;
		}

	    /** add the particle location and color type.
	     *   each index finger has its own color type.  */
		public void add(Vector loc, int colorType) {
			particles[index] = new Particle(loc, colorType);
			index++;
			// wrap around the index when exceeding the max length. 
			if (index >= MAXLEN) {
				index = 0;
			}
		}

		public int getIndex() {
			return index;
		}

		public Particle getParticle(int idx) {
			return particles[idx];
		}

		public void setNull(int idx) {
			particles[idx] = null;
		}
	}

	/**
	 * Constructor to initial GL and fingers hash table. 
	 */
	public ParticlePanel() {
		InitGL();
		fingers = new ConcurrentHashMap<Integer, ParticleArray>();
		for (int i = 0; i <= 5; i++) {
			fingers.put(i, new ParticleArray());
		}
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

	/**
	 * Get canvas. 
	 * @return  canvas.
	 */
	public GLCanvas GetCanvas() {
		return m_kCanvas;
	}

	public static void main(String[] args) {

		ParticlePanel kWorld = new ParticlePanel();
		Frame frame = new Frame("Particles Drawing");
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

		// add the particle listener to the Leap Motion controller. 
		final ParticleListener particleListener = new ParticleListener(kWorld);
		Controller controller = new Controller();
		controller.addListener(particleListener);
		System.out.println("Press Enter to quit...");
		try {
			System.in.read();
		} catch (IOException e) {
			e.printStackTrace();
		}

		// Remove the sample listener when done
		controller.removeListener(particleListener);
		
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

		GL2 gl = drawable.getGL().getGL2();
		gl.glClear(GL.GL_COLOR_BUFFER_BIT | GL.GL_DEPTH_BUFFER_BIT);
		
		for (Map.Entry entry : fingers.entrySet()) {
			ParticleArray array = (ParticleArray) entry.getValue();
			for (int i = 0; i < array.getIndex(); i++) {
				Particle particle = array.getParticle(i);
				if (particle != null) {
					particle.update();

					if (particle.isAlive()) {

						Vector loc = particle.getLocation();
						if (loc != null) {
							gl.glLoadIdentity();
							gl.glPushMatrix();
							gl.glTranslated(loc.getX(), loc.getY(), loc.getZ());

							// each finger has different color when drawing the 3D particle on the screen. 
							int type = particle.getColorType();

							if (type == 0) {
								gl.glMaterialfv(GL.GL_FRONT, GL2.GL_SPECULAR,
										mat_ambient_color, 0);
								gl.glMaterialfv(GL.GL_FRONT, GL2.GL_DIFFUSE,
										mat_diffuse, 0);
								gl.glMaterialfv(GL.GL_FRONT, GL2.GL_SPECULAR,
										mat_specular, 0);
								gl.glMaterialfv(GL.GL_FRONT, GL2.GL_SHININESS,
										high_shininess, 0);
								gl.glMaterialfv(GL.GL_FRONT, GL2.GL_EMISSION,
										no_mat, 0);
							} else if (type == 1) {
								gl.glMaterialfv(GL.GL_FRONT, GL2.GL_SPECULAR,
										no_mat, 0);
								gl.glMaterialfv(GL.GL_FRONT, GL2.GL_DIFFUSE,
										mat_diffuse, 0);
								gl.glMaterialfv(GL.GL_FRONT, GL2.GL_SPECULAR,
										no_mat, 0);
								gl.glMaterialfv(GL.GL_FRONT, GL2.GL_SHININESS,
										no_shininess, 0);
								gl.glMaterialfv(GL.GL_FRONT, GL2.GL_EMISSION,
										mat_emission, 0);
							} else if (type == 2) {
								gl.glMaterialfv(GL.GL_FRONT, GL2.GL_SPECULAR,
										mat_ambient, 0);
								gl.glMaterialfv(GL.GL_FRONT, GL2.GL_DIFFUSE,
										mat_diffuse, 0);
								gl.glMaterialfv(GL.GL_FRONT, GL2.GL_SPECULAR,
										no_mat, 0);
								gl.glMaterialfv(GL.GL_FRONT, GL2.GL_SHININESS,
										no_shininess, 0);
								gl.glMaterialfv(GL.GL_FRONT, GL2.GL_EMISSION,
										no_mat, 0);
							} else if (type == 3) {
								gl.glMaterialfv(GL.GL_FRONT, GL2.GL_SPECULAR,
										mat_ambient, 0);
								gl.glMaterialfv(GL.GL_FRONT, GL2.GL_DIFFUSE,
										mat_diffuse, 0);
								gl.glMaterialfv(GL.GL_FRONT, GL2.GL_SPECULAR,
										mat_specular, 0);
								gl.glMaterialfv(GL.GL_FRONT, GL2.GL_SHININESS,
										low_shininess, 0);
								gl.glMaterialfv(GL.GL_FRONT, GL2.GL_EMISSION,
										no_mat, 0);
							} else if (type == 4) {
								gl.glMaterialfv(GL.GL_FRONT, GL2.GL_SPECULAR,
										mat_ambient, 0);
								gl.glMaterialfv(GL.GL_FRONT, GL2.GL_DIFFUSE,
										mat_diffuse, 0);
								gl.glMaterialfv(GL.GL_FRONT, GL2.GL_SPECULAR,
										mat_specular, 0);
								gl.glMaterialfv(GL.GL_FRONT, GL2.GL_SHININESS,
										high_shininess, 0);
								gl.glMaterialfv(GL.GL_FRONT, GL2.GL_EMISSION,
										no_mat, 0);
							} else if (type == 5) {
								gl.glMaterialfv(GL.GL_FRONT, GL2.GL_SPECULAR,
										mat_ambient_color, 0);
								gl.glMaterialfv(GL.GL_FRONT, GL2.GL_DIFFUSE,
										mat_diffuse, 0);
								gl.glMaterialfv(GL.GL_FRONT, GL2.GL_SPECULAR,
										no_mat, 0);
								gl.glMaterialfv(GL.GL_FRONT, GL2.GL_SHININESS,
										no_shininess, 0);
								gl.glMaterialfv(GL.GL_FRONT, GL2.GL_EMISSION,
										mat_emission, 0);
							}

							glut.glutSolidSphere(0.01f, 20, 20);
							gl.glPopMatrix();
						}
					} else {
						array.setNull(i);
					}
				}
			}
		}

		gl.glFlush();
	}

	public void addPoint(int index, Vector loc, int colorType) {
		ParticleArray particleArray = fingers.get(index);
		particleArray.add(loc, colorType);
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

	/** 
	 * dispose memory.
	 */
	public void dispose(GLAutoDrawable arg0) {
		for (Map.Entry entry : fingers.entrySet()) {
			ParticleArray array = (ParticleArray) entry.getValue();
			array.particles = null;
		}
			
		fingers = null;
	}

}
