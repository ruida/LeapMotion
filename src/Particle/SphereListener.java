package Particle;

import com.leapmotion.leap.*;

public class SphereListener extends Listener
{
  // (ms) minimum time interval between listener's changes to the dots  	
  private static final int CHANGE_INTERVAL = 100;  
  
  /** sphere panel reference. */
  private Sphere sphere;    
  
  /* used to control the frequency of listener changes. */
  private long startTime;      
  


  public SphereListener(Sphere _sphere)
  { super();
    sphere = _sphere;
    startTime = System.currentTimeMillis(); 
  }


  public void onConnect(Controller controller)
  {  System.out.println("Controller has been connected");  
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

    // slow down processing rate to be every CHANGE_INTERVAL ms
    long currTime = System.currentTimeMillis();
    if (currTime - startTime < CHANGE_INTERVAL)
      return;     // don't do anything until CHANGE_INTERVAL time has passed

    int handsCount = frame.hands().count();
    if (handsCount == 1) {      // one hand
      Hand hand =  frame.hands().get(0);
      
      Finger frontMostFinger = hand.fingers().frontmost();
      System.err.println("finger id = " + (frontMostFinger.id() % 5) );
      
      int fingerID = frontMostFinger.id() % 5;
      
      Vector normPt = calcIntersectionPoint(frontMostFinger, controller);
   
      if (fingerID == 1)  { 
    	  sphere.updateLocation(normPt.getX(), normPt.getY(), normPt.getZ());
      } else if (fingerID == 2) {  
    	  sphere.updateLocation(normPt.getX(), normPt.getY(), normPt.getZ());
      } 

    }
   
    startTime = System.currentTimeMillis();    // reset start time
  }  // end of onFrame()


	private Vector calcIntersectionPoint(Finger frontMostFinger, Controller controller) {

		Vector tipPos = frontMostFinger.tipPosition();
	
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
