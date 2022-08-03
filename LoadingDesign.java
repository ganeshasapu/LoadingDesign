//Ganesh A.
//May 26, 2022
//Creating a loading icon design using moving objects, fractal trees, looping background colors, etc.

package graphics;

import java.awt.*;
import java.awt.geom.AffineTransform;
import javax.swing.Timer;

import javax.swing.*;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class LoadingDesign extends JFrame{
    boolean reverseDirection = false;

    static int numBirds = 10;
    static Boid[] dayFlock = new Boid[numBirds];
    static Boid[] nightFlock = new Boid[numBirds];
    static double rotationAngle = 0.0; // the current angle of rotation of the 
    static double backgroundTimer = 0.0; // the timer to control the change of colour of the background (only changes during rotations)
    static int numStars = 50;
    static double[][] stars = new double[numStars][3];
    static int windowWidth = 400;
    static int windowHeight = 400;
    static double time; // time variable that updates based on swing timer
    static Timer swingTimer;

     // used to rotate window around center of circle
    static int yOffset = 400;
    static int xOffset = 200;

    // colors
    Color blue = new Color(99, 156, 242);
    Color backgroundColor = new Color(242, 201, 124);
    Color skyColor;
    Color sunColor = new Color(227, 201, 86);
    Color birdColor = new Color(0, 0, 0);
    DrawingPanel panel;

    //class used for bird (I did not create all methods and class)
    //Link to original project: https://p5js.org/examples/simulate-flocking.html
    class Boid{
        double[] velocity = {(Math.random() * 2) - 1, (Math.random() * 2) - 1};
        double[] position = {0.0, 0.0};
        double[] acceleration = {0, 0};
        double r;
        double maxspeed;    // Maximum speed
        double maxforce; // Maximum steering force
        double offset;
        double testX;
        double testY;

        Boid(double x, double y){
            this.position[0] = x;
            this.position[1] = y;
            this. r = 2.0;
            this.maxspeed = 2;    // Maximum speed
            this.maxforce = 0.1; // Maximum steering force
            this.offset = Math.random() * Math.PI;
        }

        private void applyForce(double[] force){
            this.acceleration[0] += force[0];
            this.acceleration[1] += force[1];
        }

        public void run(Graphics2D g2d, Boid[] flock, double xOff, double yOff){
            this.flock(flock);
            this.update();
            this.borders(g2d, xOff, yOff);
            this.render(g2d);
        }

        private void flock(Boid[] flock){
            double[] sep = this.separate(flock);   // Separation
            double[] ali = this.align(flock);      // Alignment
            double[] coh = this.cohesion(flock); 

            sep[0] *= 1.5;
            sep[1] *= 1.5;

            ali[0] *= 1;
            ali[1] *= 1;

            coh[0] *= 1;
            coh[1] *= 1;

            this.applyForce(sep);
            this.applyForce(ali);
            this.applyForce(coh);

        }

        private double[] seek(double[] target){

            double[] desired = {target[0] - this.position[0], target[1] - this.position[1]};  // A vector pointing from the location to the target
            // Normalize desired and scale to maximum speed
            double vectorLength = Math.sqrt(Math.pow(desired[0], 2)+ Math.pow(desired[1], 2));
            desired[0] /= vectorLength;
            desired[1] /= vectorLength;

            desired[0] *= this.maxspeed;
            desired[1] *= this.maxspeed;
            // Steering = Desired minus Velocity
            double[] steer = {desired[0] - this.velocity[0], desired[1] - this.velocity[1]};

            vectorLength = Math.sqrt(Math.pow(steer[0], 2)+ Math.pow(steer[1], 2));
            if(vectorLength > this.maxforce){
                steer[0] /= vectorLength;
                steer[1] /= vectorLength;

                steer[0] *= this.maxforce;
                steer[1] *= this.maxforce;
            }

            return steer;
        }

        private double[] separate(Boid[] flock){
            double desiredseparation = 25.0;
            double[] steer ={0.0,0.0};
            int count = 0;
            // For every boid in the system, check if it's too close
            //
            for (int i = 0; i < flock.length; i++) {
                double d = Math.sqrt((Math.pow(this.position[0] - flock[i].position[0], 2) + Math.pow(this.position[1] - flock[i].position[1], 2)));
                // If the distance is greater than 0 and less than an arbitrary amount (0 when you are yourself)
                if ((d > 0) && (d < desiredseparation)) {
                // Calculate vector pointing away from neighbor
                double[] diff = {this.position[0] - flock[i].position[0], this.position[1] - flock[i].position[1]};

                double vectorLength = Math.sqrt(Math.pow(diff[0], 2)+ Math.pow(diff[1], 2));
                diff[0] /= vectorLength;
                diff[1] /= vectorLength;

                diff[0] /= d;
                diff[1] /= d;

                steer[0] += diff[0];
                steer[1] += diff[1];
                count++;            // Keep track of how many
                }
            }
            // Average -- divide by how many
            if (count > 0) {
                steer[0] /= count;
                steer[1] /= count;
            }

            // As long as the vector is greater than 0
            if (Math.sqrt(Math.pow(steer[0], 2)+ Math.pow(steer[1], 2)) > 0) {
                // Implement Reynolds: Steering = Desired - Velocity
                double vectorLength = Math.sqrt(Math.pow(steer[0], 2)+ Math.pow(steer[1], 2));
                steer[0] /= vectorLength;
                steer[1] /= vectorLength;

                steer[0] *= this.maxspeed;
                steer[1] *= this.maxspeed;

                steer[0] -= this.velocity[0];
                steer[1] -= this.velocity[1];

                vectorLength = Math.sqrt(Math.pow(steer[0], 2)+ Math.pow(steer[1], 2));
                if(vectorLength > this.maxforce){
                    steer[0] /= vectorLength;
                    steer[1] /= vectorLength;

                    steer[0] *= this.maxforce;
                    steer[1] *= this.maxforce;
                }
            }
            return steer;
        }

        private double[] align(Boid[] flock){
            double neighbordist = 50.0;
            double[] sum = {0.0, 0.0};
            int count = 0;
            for (int i = 0; i < flock.length; i++) {
                double d = Math.sqrt((Math.pow(this.position[0] - flock[i].position[0], 2) + Math.pow(this.position[1] - flock[i].position[1], 2)));
                if ((d > 0) && (d < neighbordist)) {
                
                sum[0] += flock[i].velocity[0];
                sum[1] += flock[i].velocity[1];
                count++;
                }
            }
            if (count > 0) {
                sum[0] /= count;
                double vectorLength = Math.sqrt(Math.pow(sum[0], 2)+ Math.pow(sum[1], 2));
                sum[0] /= vectorLength;
                sum[1] /= vectorLength;

                sum[0] *= this.maxspeed;
                sum[1] *= this.maxspeed;

                double[] steer = {sum[0] - this.velocity[0],sum[1] - this.velocity[1]};
                vectorLength = Math.sqrt(Math.pow(steer[0], 2)+ Math.pow(steer[1], 2));
                if(vectorLength > this.maxforce){
                    steer[0] /= vectorLength;
                    steer[1] /= vectorLength;

                    steer[0] *= this.maxforce;
                    steer[1] *= this.maxforce;
                }

                return steer;
            } else {
                return new double[] {0.0, 0.0};
            }
        }  

        private double[] cohesion(Boid[] flock){
            double neighbordist = 50.0;
            double[] sum = {0.0, 0.0};   // Start with empty vector to accumulate all locations
            int count = 0;
            for (int i = 0; i < flock.length; i++) {
                 double d = Math.sqrt((Math.pow(this.position[0] - flock[i].position[0], 2) + Math.pow(this.position[1] - flock[i].position[1], 2)));
                if ((d > 0) && (d < neighbordist)) {
                sum[0] += flock[i].position[0];
                sum[1] += flock[i].position[1];
                count++;
                }
            }
            if (count > 0) {
                sum[0] /= count;
                sum[1] /= count;
                return this.seek(sum);  // Steer towards the location
            } else {
                return new double[]{0.0, 0.0};
            }
        }

        private void update(){
            this.velocity[0] += this.acceleration[0];
            this.velocity[1] += this.acceleration[1];
            double vectorLength = Math.sqrt(Math.pow(this.velocity[0], 2)+ Math.pow(this.velocity[1], 2));
            if(vectorLength > this.maxspeed){
                this.velocity[0] /= vectorLength;
                this.velocity[1] /= vectorLength;

                this.velocity[0] *= this.maxspeed;
                this.velocity[1] *= this.maxspeed;
            }

            this.position[0] += this.velocity[0];
            this.position[1] += this.velocity[1];

            this.acceleration[0] = 0.0;
            this.acceleration[1] = 0.0;
        }

        private void borders(Graphics g2d, double xOff, double yOff){

            //this method was updated and created by me
            
            if(yOff == -400){
                double dist = Math.sqrt(Math.pow((this.position[0] + xOff - 200), 2) +   Math.pow((this.position[1] + yOff + 400), 2));
                boolean inside = dist < 125;
                if (this.position[0] + this.r < 40 - xOff) this.position[0] = 350 - xOff;
                if (this.position[1] + this.r < -350)  this.position[1] = -Math.sqrt(Math.pow(this.position[0], 2) + 15625);
                if (this.position[0] - this.r > 350 - xOff) this.position[0] = 40 - xOff;
                if (inside || this.position[1] > 0) this.position[1] = -340;
            }
            else{
                double dist = Math.sqrt(Math.pow((this.position[0] + xOff -200), 2) +   Math.pow((this.position[1] + yOff - 400), 2));
                boolean inside = dist < 125;
                if (this.position[0] + this.r < 40 - xOff)  this.position[0] = 350 - xOff;
                if (this.position[1] + this.r < 40 - yOff)  this.position[1] = -Math.sqrt(Math.pow(this.position[0], 2) + 15625);
                if (this.position[0] - this.r > 350 - xOff) this.position[0] = 40 - xOff;
                if (inside || this.position[1] > 0) this.position[1] = 40 - yOff;
            }
        }

        private void render(Graphics2D g2d){
            //this method was modified by me
            double theta = (Math.atan(velocity[1] / this.velocity[0])) + (Math.PI / 2);
            g2d.setColor(birdColor);
            AffineTransform beforeDrawingBird = g2d.getTransform();
            g2d.translate(this.position[0], this.position[1]);
            g2d.rotate(theta);
            g2d.fillPolygon(new int[] {0, (int) -this.r, (int) this.r}, new int[] { (int) (-this.r * 2), (int)  (this.r * 2), (int) (this.r * 2)}, 3);
            g2d.setStroke(new BasicStroke((float) (Math.sin((time / 20 + this.offset) % Math.PI) * 1.5 + 0.75)));
            g2d.drawLine(-4, 0, 4, 0);
            g2d.setTransform(beforeDrawingBird);
        }
    }   

    //used to draw rays around the sun given point of sun
    void drawSunRays(Graphics2D g2d, int x, int y){
        g2d.setColor(sunColor);
        g2d.setStroke(new BasicStroke(5));
        for(int i = 0; i < 20; i++){
            double rayAngle = ((6.28 / 20) * i) + (time / 200);
            double newX1 = 50 * Math.cos(rayAngle) + x;
            double newY1 = 50 * Math.sin(rayAngle) + y;
            
            double newX2 = 55 * Math.cos(rayAngle) + x;
            double newY2 = 55 * Math.sin(rayAngle) + y;
            g2d.drawLine((int) newX1, (int) newY1, (int) newX2, (int) newY2);
        }
        g2d.setStroke(new BasicStroke(1));
    }

    void drawMoon(Graphics2D g2d){
        g2d.setColor(new Color(161, 192, 207));
        g2d.fillOval(200 - xOffset - (75 / 2), 115 - yOffset - (75 / 2), 75, 75);
        g2d.setColor(skyColor);
        g2d.fillOval(220 - xOffset - (75 / 2), 105 - yOffset - (75 / 2), 75, 75);
    }

    //uses array of stars to draw stars
    void drawStars(Graphics2D g2d){
        for (double[] star : stars){
            g2d.setColor(new Color(255, 255, 255));
            g2d.setStroke(new BasicStroke(1));
            double starLength = Math.sin(((time / 50) + star[2] % 3.14)) * 2;
            g2d.drawLine((int) (star[0] - xOffset - starLength), (int) (star[1] - yOffset), (int) (star[0] - xOffset + starLength), (int) (star[1] - yOffset));
            
            g2d.drawLine((int) (star[0] - xOffset), (int) (star[1] - yOffset - starLength), (int) (star[0] - xOffset), (int) (star[1] - yOffset + starLength));
        }
    }

    //used to draw fractal tree given length
    void branch(Graphics2D g2d, double len, double offset){
        if(len > 7){
            g2d.setColor(new Color(158, 82, 47));
        }
        else{
            g2d.setColor(new Color(23, 115, 26));
        }
        g2d.drawLine(0, 0, 0, (int) -len);
        g2d.translate(0, -len);
        if (len > 2) {
            AffineTransform beforeRightBranch = g2d.getTransform();
            g2d.rotate(Math.sin((time / 200) + offset));
            branch(g2d, len * 0.67, offset);
            g2d.setTransform(beforeRightBranch);

            AffineTransform beforeLeftBranch = g2d.getTransform();
            g2d.rotate(Math.sin(-(time / 200) - offset));
            branch(g2d, len * 0.67, offset);
            g2d.setTransform(beforeLeftBranch);
        }
    }

    void drawDayObjects(Graphics2D g2d){
        //sun
        g2d.setColor(sunColor);
        g2d.fillOval(200 - xOffset - (75/2), 125 - yOffset - (75/2), 75, 75);

        drawSunRays(g2d, 200 - xOffset, 125 - yOffset);

        AffineTransform beforeMiddleTree = g2d.getTransform(); // preserves original position
        g2d.translate(200 - xOffset, 275 - yOffset); // middle tree
        branch(g2d, 25, 0.3);
        g2d.setTransform(beforeMiddleTree); // restores original position

        AffineTransform beforeRightTree = g2d.getTransform();
        g2d.translate(280 - xOffset, 303 - yOffset); // right tree
        branch(g2d, 25, 0.3);
        g2d.setTransform(beforeRightTree);

        AffineTransform beforeLeftTree = g2d.getTransform();
        g2d.translate(120 - xOffset, 303 - yOffset); // left tree
        branch(g2d, 25, 0.3);
        g2d.setTransform(beforeLeftTree);

        //make bird move and display
        for (Boid bird : dayFlock) {
            bird.run(g2d, dayFlock, xOffset, yOffset);
        }
    }
    
    void drawNightObjects(Graphics2D g2d){
        drawMoon(g2d);
        drawStars(g2d);

        AffineTransform beforeMiddleTree = g2d.getTransform(); //preserves old position
        g2d.translate(200 - xOffset, 275 - yOffset);
        branch(g2d, 25, 0.3); //middle tree
        g2d.setTransform(beforeMiddleTree);

        AffineTransform beforeRightTree = g2d.getTransform();
        g2d.translate(280 - xOffset, 303 - yOffset);
        branch(g2d, 25, 0.3); //right tree
        g2d.setTransform(beforeRightTree);

        AffineTransform beforeLeftTree = g2d.getTransform();
        g2d.translate(120 - xOffset, 303 - yOffset);
        branch(g2d, 25, 0.3); //left tree
        g2d.setTransform(beforeLeftTree);

        //make bird move and display
        for (Boid bird : nightFlock) {
            bird.run(g2d, nightFlock, xOffset, -yOffset);
        }
    }
     public static void main(String[] args) {
         //generating stars
        for(int i = 0; i< 50; i++){
            double x = Math.random() * 400;
            double y = Math.random() * 300;
            
            double blinkOffset = Math.random(); //determines the "blink" of the star, so they do not blink all at once
            stars[i][0] = x;
            stars[i][1] = y;
            stars[i][2] = blinkOffset;
        }
        LoadingDesign window = new LoadingDesign();
        window.setSize(windowWidth, windowHeight);
        window.setVisible(true);
        window.setBackground(new Color(99, 156, 242));
    }

    LoadingDesign(){
        panel = new DrawingPanel();
        panel.setBackground(blue);
        this.add(panel);
    }

    private class DrawingPanel extends JPanel {
      DrawingPanel() {  //constructor
        //generating boids
        for (int i = 0; i < numBirds; i++) {
            Boid b1 = new Boid(Math.random() * 400 - xOffset, Math.random() * 400 - yOffset);
            Boid b2 = new Boid(Math.random() * 400 - xOffset, Math.random() * 400 - yOffset);
            dayFlock[i] = b1;
            nightFlock[i] = b2;
        }

        //this is the method that is called by swing timer
        //updates time & changes rotationAngle and backgroundTimer depending on time
        ActionListener updateTime = new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                time += 1;
                if(time > 100 && time < 257){rotationAngle += 0.02; backgroundTimer += 1;}
  
                //stop rotating
                if(time >= 257 && time < 357) {}

                ////start rotation night -> day
                if(time >= 357 && time <= 514) {rotationAngle += 0.02; backgroundTimer += 1;}
                //reset cycle
                if(time == 514) {time = 0; rotationAngle = 0; backgroundTimer = 0;}
        
                //sunRay reverse direction
                if ((time/ 200) > 76.5 || (time / 200) < 75.8) {reverseDirection = !reverseDirection;}
                repaint();
            }
        };

        swingTimer = new Timer(20, updateTime);
        swingTimer.start();
      }
      @Override
      public void paintComponent(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;
        super.paintComponent(g2d);
        AffineTransform oldDrawingTransformation = g2d.getTransform(); //transform before rotation of window
        g2d.translate(xOffset, yOffset);
        g2d.rotate(rotationAngle);

        //start with a color, subtract each rgb component by a value determined by a sin function, this allows for looping of color from 0 to PI
        skyColor = new Color((int) (126 - (Math.sin((backgroundTimer /314 * Math.PI) % Math.PI) * 70)), (int) (186 - (Math.sin((backgroundTimer/314 * Math.PI) % Math.PI) * 70)), (int)(224 - (Math.sin((backgroundTimer/314 * Math.PI) % Math.PI) * 70)));
        panel.setBackground(new Color((int) (126 - (Math.sin((backgroundTimer /314 * Math.PI) % Math.PI) * 70)), (int) (186 - (Math.sin((backgroundTimer/314 * Math.PI) % Math.PI) * 70)), (int)(224 - (Math.sin((backgroundTimer/314 * Math.PI) % Math.PI) * 70))));
        g2d.setColor(new Color(255, 255, 255));

        drawDayObjects(g2d);
        g2d.rotate(Math.PI);
        drawNightObjects(g2d);
        g2d.setTransform(oldDrawingTransformation); // reverting to old transform because objects after this should not be affected by rotation

        //middle ground circles
        g2d.setColor(new Color(40, 173, 58));
        g2d.fillOval(xOffset - (250 / 2), yOffset - (250 / 2), 250, 250);
        g2d.setColor(new Color(176, 121, 72));
        g2d.fillOval(xOffset - (235 / 2), yOffset - (235 / 2), 235, 235);

        //Outside circle 
        g2d.setStroke(new BasicStroke(150));
        g2d.setColor(backgroundColor);
        g2d.drawOval(200 - (500 / 2), 200 - (500 / 2) - 13, 500, 500);
        g2d.setStroke(new BasicStroke(1));
        g2d.setColor(new Color(255, 255, 255));
        
      }
    }
}
