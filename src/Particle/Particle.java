package Particle;

import com.leapmotion.leap.Vector;

/**
 * Paricle class present the particle's locaiton, life span and color type. 
 * @author Ruida Cheng
 *
 */
public class Particle {
	Vector location;
	int life;
	int colorType;
	
	public Particle(Vector _location, int _colorType) {
		location = _location;
		colorType = _colorType;
		life = 50;
	}
	
	public void update() {
		life -= 2;
	}
	
	public boolean isAlive() {
		if ( life > 0 )
			return true;
		else 
			return false;
	}
	
	public Vector getLocation() {
		return location;
	}
	
	public void setLocation(Vector loc) {
		location = loc;
	}
	
	public void setColorType(int type) {
		colorType = type;
	}
	
	public int getColorType() {
		return colorType;
	}
	
}