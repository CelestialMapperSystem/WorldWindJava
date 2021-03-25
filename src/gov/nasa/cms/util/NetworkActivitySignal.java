/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */

package gov.nasa.cms.util;

import gov.nasa.worldwind.WorldWind;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author tag
 * @version $Id: NetworkActivitySignal.java 1171 2013-02-11 21:45:02Z dcollins $
 */
public class NetworkActivitySignal 
{
    public interface NetworkUser
    {
        public boolean hasNetworkActivity();
    }

    private ArrayList<NetworkUser> networkUsers = new ArrayList<NetworkUser>();
    private AtomicBoolean isNetworkAvailable = new AtomicBoolean(true);
    private JLabel networkLabel = new JLabel();
    private ImageIcon busySignal;

    public NetworkActivitySignal()
    {

        this.networkLabel = new JLabel();
        this.networkLabel.setOpaque(false);

        NetworkUser downloadUser = new NetworkUser()
        {
            public boolean hasNetworkActivity()
            {
                return WorldWind.getRetrievalService().hasActiveTasks();
            }
        };
        this.networkUsers.add(downloadUser);

        Timer activityTimer = new Timer(500, new ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent actionEvent)
            {
                if (!isNetworkAvailable.get())
                {
                    if (networkLabel.getText() == null)
                    {
                        networkLabel.setIcon(null);
                        networkLabel.setText("No network");
                        networkLabel.setForeground(Color.RED);
                        networkLabel.setVisible(true);
                    }
                }
                else
                {
                    for (NetworkUser user : networkUsers)
                    {
                        if (user.hasNetworkActivity())
                        {
                            runBusySignal(true);
                            return;
                        }
                    }
                    runBusySignal(false);
                }
            }
        });
        activityTimer.start();

        Timer netCheckTimer = new Timer(1000, new ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent actionEvent)
            {
                Thread t = new Thread(new Runnable()
                {
                    public void run()
                    {
                        isNetworkAvailable.set(!WorldWind.getNetworkStatus().isNetworkUnavailable());
                    }
                });
                t.start();
            }
        });
        netCheckTimer.start();
    }

    private void runBusySignal(boolean tf)
    {
        if (tf)
        {
            if (this.networkLabel.getIcon() == null)
            {
                this.networkLabel.setIcon(this.busySignal);
                this.networkLabel.setText(null);
                this.networkLabel.setVisible(true);
            }
        }
        else
        {
            if (this.networkLabel.isVisible())
            {
                this.networkLabel.setText(null);
                this.networkLabel.setIcon(null);
                this.networkLabel.setVisible(false);
            }
        }
    }

    public JLabel getLabel()
    {
        return this.networkLabel;
    }

    public void addNetworkUser(NetworkUser user)
    {
        this.networkUsers.add(user);
    }

    public void removeNetworkUser(NetworkUser user)
    {
        this.networkUsers.remove(user);
    }
}
