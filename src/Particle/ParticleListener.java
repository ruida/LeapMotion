package Particle;

import com.leapmotion.leap.*;

public class ParticleListener extends Listener
{

 // minimum time interval between listener's changes to the dots	
  private static final int CHANGE_INTERVAL = 50;  
  
  // reference back to particle panel
  private ParticlePanel particlePanel;
  
  // used to control the frequency of listener changes
  private long startTime;       
  
  /**
   * Constructor for particle listener
   * @param _particlePanel
   */
  public ParticleListener(ParticlePanel _particlePanel)
  { 
	  super();
	  particlePanel = _particlePanel;
	  startTime = System.currentTimeMillis(); 
  }


  public void onConnect(Controller controller)
  {  
	 System.out.println("Controller has been connected");  
     controller.enableGesture(Gesture.Type.TYPE_CIRCLE);
  }


  public void onExit(Controller controller)
  {  System.out.println("Exited");  }


  public void onFrame(Controller controller)
  {
    Frame frame = controller.frame();

    // if no hand detected, give up
    if (frame.hands().isEmpty())
      return;

    // if the screen isn't available give up
    Screen screen = controller.locatedScreens().get(0);
    if (screen == null) {
      System.out.println("No screen found");
      return;
    } 
    if (!screen.isValid()) {
      System.out.println("Screen not valid");
      return;
    } 

    // slow down processing rate to be every CHANGE_INTERVAL ms
    long currTime = System.currentTimeMillis();
    if (currTime - startTime < CHANGE_INTERVAL)
      return;     // don't do anything until CHANGE_INTERVAL time has passed

    int handsCount = frame.hands().count();
    if (handsCount == 1) {      // one hand
      Hand hand =  frame.hands().get(0);
      
      FingerList fingerList = hand.fingers();
      for ( Finger finger : fingerList ) {
          int fingerID = finger.id() % 5;
          Vector normPt = calcIntersectionPoint(finger, controller);
	      if (fingerID == 0) {
	    	  particlePanel.addPoint(0, normPt, 0); 
	      } else if (fingerID == 1)  { 
	    	 particlePanel.addPoint(1, normPt, 1);
	      } else if (fingerID == 2) {   
	    	  particlePanel.addPoint(2, normPt, 2);
	      } else if (fingerID == 3) {  
	    	  particlePanel.addPoint(3, normPt, 3);
	      } else if (fingerID == 4) {  
	    	  particlePanel.addPoint(4, normPt, 4);
	      } else if (fingerID == 5) {  
	    	  particlePanel.addPoint(5, normPt, 5);
	      }
      }
    }
    startTime = System.currentTimeMillis();    // reset start time
  }  // end of onFrame()

    /**
     * Calculate the finger tip location. Normalized to Interaction box, then scale to canvas size
     * @param finger   
     * @param controller
     * @return  point on canvas location. 
     */
	private Vector calcIntersectionPoint(Finger finger, Controller controller) {

		Vector tipPos = finger.tipPosition();

		InteractionBox iBox = controller.frame().interactionBox();

		Vector normalizedPoint = iBox.normalizePoint(tipPos, true);

		if (Float.isNaN(normalizedPoint.getX())
				|| Float.isNaN(normalizedPoint.getY())
				|| Float.isNaN(normalizedPoint.getZ()))
			return null;

		// constrain x, y to -1.5 -- 1.5, z to -10 to 10
		float xNorm = (Math.min(1, Math.max(0, normalizedPoint.getX())) - 0.5f) * 3;
		float yNorm = (Math.min(1, Math.max(0, normalizedPoint.getY())) - 0.5f) * 3;
		float zNorm = (Math.min(1, Math.max(0, normalizedPoint.getZ())) - 0.5f) * 20;

		return new Vector(xNorm, yNorm, zNorm);
	}
 
}  
