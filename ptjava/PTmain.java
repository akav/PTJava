/*
 * The MIT License
 *
 * Copyright 2023 akava.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package ptjava;

import java.io.IOException;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;

public class PTmain {
    public static void main(String[] args) throws InterruptedException, IOException {

        int WIDTH = 960;
        int HEIGHT = 540;
        BufferedImage renderedImage = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_RGB);

        JFrame frame = new JFrame("PTJava");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JPanel renderPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                g.drawImage(renderedImage, 0, 0, this);
            }
        };
        renderPanel.setPreferredSize(new Dimension(WIDTH, HEIGHT));

        frame.add(renderPanel);
        frame.pack();
        frame.setVisible(true);

        Thread renderingThread = new Thread(() -> {
            try {
                Example.MaterialSpheres(WIDTH, HEIGHT, renderedImage, renderPanel);
            } catch (InterruptedException | IOException e) {
                e.printStackTrace();
            } catch (Exception e) {
                System.err.println("An unexpected error occurred: " + e.getMessage());
                e.printStackTrace();
            }
        });

        renderingThread.start();
    }
}
