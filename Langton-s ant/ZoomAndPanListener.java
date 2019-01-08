/**
* <h1>Langton's Ant</h1>
* HCI 2018-2019 Programming Assignment
* Prof. Andrew D. Bagdanov
* <p>
* @author Cristiano Gelli
* @version 1.0
* @since 2019-01-14
*/

import java.awt.Component;
import java.awt.Cursor;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.geom.AffineTransform;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Point2D;

public class ZoomAndPanListener implements MouseListener, MouseMotionListener, MouseWheelListener {
    private Component targetComponent;

    private int zoomLevel = 0;
    private int minZoomLevel = 0;
    private int maxZoomLevel = 10;
    private double zoomMultiplicationFactor = 1.2;
    
    private Point dragStartScreen;
    private Point dragEndScreen;
    
    // Current coordinate system
    private AffineTransform coordTransform = new AffineTransform();

    /**
     * Constructor of the class. Set the component that can make use of zoomming and panning functionalities.
     * @param targetComponent Component that will have zoom and pannning functionalities.
     */
    public ZoomAndPanListener(Component targetComponent) {
        this.targetComponent = targetComponent;
    }

    public void mouseClicked(MouseEvent e) {}
    
    /**
     * When mouse have been pressed, we keep track of the initial mouse drag movement.
     * @param e An event which indicates that a mouse action occurred in a component (target component).
     */
    public void mousePressed(MouseEvent e) {
        dragStartScreen = e.getPoint();
        dragEndScreen = null;
    }

    public void mouseReleased(MouseEvent e) {
    	targetComponent.setCursor(null);
    }

    public void mouseEntered(MouseEvent e) {}

    public void mouseExited(MouseEvent e) {}

    public void mouseMoved(MouseEvent e) {}

    /**
     * Each time mouse is dragged, camera is moved for the target component.
     */
    public void mouseDragged(MouseEvent e) {
        moveCamera(e);
    }

    /**
     * Each time mouse wheel is moved, camera is zoomed in/out for the target component.
     */
    public void mouseWheelMoved(MouseWheelEvent e) {
        zoomCamera(e);
    }

    /**
     * Performs the actual shifting of camera, keeping in mind the dragging movement occurred.
     * @param e An event which indicates that a mouse action occurred in a component (target component).
     * @see mouseClicked, mouseDragged, mouseReleased, zoomCamera
     */
    private void moveCamera(MouseEvent e) {
        try {
            dragEndScreen = e.getPoint();
            Point2D.Float dragStart = transformPoint(dragStartScreen);
            Point2D.Float dragEnd = transformPoint(dragEndScreen);
            double dx = dragEnd.getX() - dragStart.getX();
            double dy = dragEnd.getY() - dragStart.getY();
            if( zoomLevel > 0 ) {
            	targetComponent.setCursor(new Cursor(Cursor.MOVE_CURSOR));
	            coordTransform.translate(dx, dy);
	            dragStartScreen = dragEndScreen;
	            dragEndScreen = null;
	            targetComponent.repaint();
            }
        } catch (NoninvertibleTransformException ex) {
            ex.printStackTrace();
        }
    }

    /**
     * Performs the actual zoomming of camera, keeping in mind the scroll wheel movement occurred.
     * @param e An event which indicates that a mouse action occurred in a component (target component).
     * @see mouseWheelMoved, moveCamera
     */
    private void zoomCamera(MouseWheelEvent e) {
        try {
            int wheelRotation = e.getWheelRotation();
            Point p = e.getPoint();
            // zoom out
            if (wheelRotation > 0) {
            	if (zoomLevel > minZoomLevel) {
                    zoomLevel--;
                    if( zoomLevel > 0 ) {
	                    Point2D p1 = transformPoint(p);
	                    coordTransform.scale(1 / zoomMultiplicationFactor, 1 / zoomMultiplicationFactor);
	                    Point2D p2 = transformPoint(p);
	                    coordTransform.translate(p2.getX() - p1.getX(), p2.getY() - p1.getY());
                    }
                    else {			// zoomLevel = 0 --> resetZoom
                    	coordTransform.setToScale(1, 1);
                    	coordTransform.setToTranslation(0, 0);
                    }
                }
            } else {	// zoom in
                if (zoomLevel < maxZoomLevel) {
                    zoomLevel++;
                    Point2D p1 = transformPoint(p);
                    coordTransform.scale(zoomMultiplicationFactor, zoomMultiplicationFactor);
                    Point2D p2 = transformPoint(p);
                    coordTransform.translate(p2.getX() - p1.getX(), p2.getY() - p1.getY());
                }
            }
            targetComponent.repaint();
        } catch (NoninvertibleTransformException ex) {
            ex.printStackTrace();
        }
    }

    /**
     * Transforms a point on the basis of current coordinate system (AffineTransform).
     * @param p1 the Point to be transformed
     * @throws NonInvertibleTransformException if the coordinate system is not invertible.
     */
    private Point2D.Float transformPoint(Point p1) throws NoninvertibleTransformException {
        AffineTransform inverse = coordTransform.createInverse();

        Point2D.Float p2 = new Point2D.Float();
        inverse.transform(p1, p2);
        return p2;
    }

    /**
     * Returns the zoomLevel.
     * @return the zoom level.
     */
    public int getZoomLevel() {
    	return zoomLevel;
    }
    
    /**
     * Sets the zoom level, that must be a value between minZoomLevel and maxZoomLevel, otherwise
     * minZoomLevel is set.
     * @param zoomLevel the specified zoomLevel to which to set the zoom level
     */
    public void setZoomLevel(int zoomLevel) {
    	this.zoomLevel = (zoomLevel >= minZoomLevel && zoomLevel <= maxZoomLevel) ? zoomLevel : minZoomLevel;
    	targetComponent.repaint();
    }
    
    /**
     * Returns the current coordinate system.
     * @return the current coordinate system.
     */
    public AffineTransform getCoordTransform() {
    	return coordTransform;
    }

    /**
     * Sets the coordinate system.
     * @param coordTransform the specified coordTransform to which set the coordinate system
     */
    public void setCoordTransform(AffineTransform coordTransform) {
    	this.coordTransform = coordTransform;
    }
    
    /*
     * Implements the zoom reset, reinitializing the default coordinate system.
     */
    public void resetZoom() {
    	coordTransform = new AffineTransform();
    }
}
