/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.nasa.cms.util;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.LayoutManager;
import java.awt.Rectangle;
import java.util.HashSet;
import javax.swing.JPanel;

/**
 *
 * @author kjdickin
 */
public class TransparentPanel extends JPanel 
{
    public TransparentPanel()
    {
        this.setOpaque(false);
    }
    @Override
    public void paintComponent(Graphics g) {
      //  g.setColor(getBackground());
        g.setColor(new Color(0, 0, 0, 0));
        
        Rectangle r = g.getClipBounds();
        g.fillRect(r.x, r.y, r.width, r.height);
        super.paintComponent(g);
    }
}