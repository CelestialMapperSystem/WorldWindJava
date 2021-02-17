/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */

import gov.nasa.worldwind.util.*;
import gov.nasa.worldwind.wms.WMSTiledImageLayer;
import org.w3c.dom.Document;

/**
 * @author tag
 * @version $Id: LOLAColor.java 1958 2014-04-24 19:25:37Z tgaskins $
 */
public class LOLAColor extends WMSTiledImageLayer
{
    public LOLAColor()
    {
        super(getConfigurationDocument(), null);
    }

    protected static Document getConfigurationDocument()
    {
        return WWXML.openDocumentFile("gov/nasa/cms/config/Moon/LOLAColor.xml", null);
    }
}
