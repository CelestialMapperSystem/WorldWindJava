Shapefile of lunar pit locations in IAU MOON2000 geographic reference system. 
Lunar pits are small collapse features (10s to 100s of meter scale) 
characterized by vertical walls. For each pit, we have recorded the location, 
dimensions, and various feature descriptors (such as whether the pit is within a
positive or negative relief feature, or has a path to the floor uninterrupted by
a vertical wall), as well as a detailed description of the pit and environs. 
Where possible, depths are calculated from shadow measurements on three 
minimally-slewed images. Each pit is named based on the host feature's name, 
usually followed by a number roughly corresponding to  order of discovery and a 
letter if the pit is part of a multi-pit cluster (e.g. "King 1a" and "King 1b").
Latitude and longitude coordinates were computed from an average of multiple 
images and so should be more accurate than the ~31 m uncertainty of an arbitrary
NAC image. However, we also report the line/sample location of the center of 
each pit in one LROC NAC frame to ensure unambiguous identification of each pit 
regardless of any future updates to the LROC ephemeris or changes to the lunar 
reference frame.

When citing this product, use the following reference: 
    Wagner, R. V. and Robinson, M. S. (2021). Occurrence and Origin of Lunar 
    Pits: Observations from a New Catalog. 52nd Lunar and Planetary Science 
    Conference, Abstract #2530.
    https://www.hou.usra.edu/meetings/lpsc2021/pdf/2530.pdf
  
Longitude Domain    Product Name
  -180 to +180      LUNAR_PIT_LOCATIONS_180
     0 to +360      LUNAR_PIT_LOCATIONS_360


Column Descriptions:

Name: Unique identifier for the pit.  Usually consists of 
      "<Crater Name> <Number><Optional Letter>", where:
  - <Crater Name> is the name of the host crater
  - <Number> is an arbitrary number, roughly corresponding to discovery order, 
    but not neccesarily sequential.
  - <Optional Letter> is a further identifier for pits within clusters (groups 
    of multiple pits within ~100 meters of one another), usually ordered from 
    largest to smallest or along a line.  In cases where a pit has a deeper pit 
    in its floor, those pits are listed separately, lettered from outermost to 
    innermost.

Terrain: Host terrain of the pit. Mare, Highland, or Impact melt.

Type: Sub-categorization of pit.
  - Fracture: This pit is a wide section of a longer fracture.
  - Large Bowl: This pit is a wide bowl-shaped collapse feature, with most of 
    the depth coming from the floor, rather than walls, but does have a vertical
    wall that completely or almost completely encircles the pit.
  - Uncertain: It is not clear whether or not this feature qualifies as a "pit".

HostFeatur: Host feature: Name of the feature the pit is in (e.g. "Copernicus"
            or "Mare Tranquillitatis").

Latitude: Latitude coordinate in degrees of the approximate center of the pit. 

Longitude: Longitude coordinate in degrees of the approximate center of the pit.
           Coordinates are estimated from multiple NAC images, and are usually 
           accurate to within 30 m (see Wagner et al. 2017,
           https://doi.org/10.1016/j.icarus.2016.05.011/ ).

FunMaxDiam: Funnel Max Diameter (m)
  - Diameter measured across the greatest extent of the funnel/depression 
    surrounding the pit, determined from a low-Sun image. Funnel diameters are a
    rough measure out to where there is some photometric darkening/brightening 
    of the terrain due to slope change, and should be assumed to have an 
    uncertainty of ~5 meters, erring on the side of reporting too small a value.

FunMinDiam: Funnel Min Diameter (m)
  - Diameter perpendicular to the Funnel Max Diameter.

InMaxDiam: Inner Max. Diameter (m)
  - Length of the maximum dimension of the vertical-walled inner pit. May be 
    "N/A" if there are not vertical walls bounding both ends of the long axis of
    the pit (e.g. a wall collapse, as in the Lacus Mortis Pit). For rectilinear 
    pits, this is usually parallel to the longest walls, rather than the true
    longest diagonal dimension.

InMinDiam: Inner Min Diam (m)
  - Maximum vertical-wall-to-vertical-wall distance perpendicular to the long 
    axis of the pit. May be N/A if there are not vertical walls on both sides.

Azimuth:  Orientation of the long axis of the inner pit (where applicable) in 
          degrees clockwise from north, rounded to the nearest 5 degrees.  All 
          azimuths are >0; 0 is reserved as a special value for pits with no 
          long axis.

Depth: Average of depth measurements in meters from up to three individual 
       images (see below). In cases where only minimum depth measurements exist,
       depth is based on the highest-Sun image(s).

Depth_Num: Numeric only version of the Depth field (see above).

Depth1/Depth2/Depth3: Fields for depth measurement in meters from shadow 
                      measurements on individual NAC images.
  - Depth1 is usually from the image with the shadow boundary closest to the 
    middle or apparent lowest point of the pit.
  - In general, measurements are from the top of a vertical wall to the floor, 
    but some measurements may include some funnel, and impact melt pits rarely 
    have uniform rim heights.
  - Where possible, depth measurements from at least two walls are included.
  - If possible, only images with emission angles <3 degrees were used to
    measure depths.
  - If a depth measurement is "greater than X", then no floor was visible, and 
    "X" is the lowest point that sunlight could have reached within the pit in 
    that image. Parts of the floor could lie above this depth if they were on 
    the sunward side of the pit in that image.
  - See Warnings entry for specific possible complications in the depth 
    measurements of a pit.

Image1/Image2/Image3: Fields for Image IDs of the images used to get 
                      Depth1/Depth2/Depth3.

Image1Line: Line in unprojected NAC image (Image1) as a projection-independent 
            reference for exactly what feature this pit is.
Image1Samp: Sample in unprojected NAC image (Image1) as a projection-independent
            reference for exactly what feature this pit is.

Inc1/Inc2/Inc3: Fields for the incidence angles of Image1/Image2/Image3, signed 
                such that images with negative numbers have the Sun in the West,
                and those with positive numbers have the Sun in the East.

Em1/Em2/Em3: Fields for emission angles of Image1/Image2/Image3, signed such 
             that images with negative numbers have the spacecraft in the West, 
             and those with positive numbers have the spacecraft in the East.

Time1/Time2/Time3: Fields for local solar time-of-day of the image. 
  - 6.00 is dawn, 12.00 is noon, 18.00 is dusk.  This is not an <hour>.<minutes>
    format: the decimal fraction goes from 00 to 99.

Res1/Res2/Res3: Fields for resolution of Image1/Image2/Image3 in meters/pixel.

StereoIDs: Image IDs of the the best stereo pair of the pit, if any exist.

DTMs: PDS identifier of the best NAC DTM that includes the pit, if any exist.

OtherImgs: Other Useful Images; Images that may be helpful for interpreting the 
           shape of the pit, other than the depth measurement images and any 
           stereo pair.

Warnings: Specific warnings about things that may affect the interpretation of 
          the recorded data (unusual wall or floor topography, high uncertainty 
          in shadow lengths, etc.).

Description: (CSV ONLY; frequently exceeds the ESRI shapefile limit of 256 
             characters) Verbose description of the pit shape and surrounding 
             terrain features.
  - For pits in a cluster (e.g. 1a, 1b, 1c), the descriptions assume that you 
    are reading all of them in order. Most regional description will be in the 
    description for pit "a".
  - Common abbreviations: North (N), South (S), East (E), West (W), Positive
    Relief Feature (PRF).

--------
The following fields are Yes/No flags of whether the pit has a given feature.
  - Possible values are yes (Y), no (N), likely yes (Y?), likely no (N?), 
    or unknown (?).
  - Note that for mare and highland pits, the radius for "nearby" is increased 
    to ~2 km from the impact melt pit standard radius of ~300 m.
--------

EntryRamp: Whether the pit appears to have an unbroken slope from surrounding 
           surface to floor. Note that this ramp may not actually be 
           traversable in practice, due to slope or rockiness.

Overhang: Whether there is a confirmed overhang of any depth on any side, based 
          on off-nadir imaging. Usually unknown.

NearPits: Nearby Pit(s); Whether there is any other vertical-walled collapse 
          feature (including other pits of the same cluster) within ~300 m.

NearFract: Nearby Fracture(s); Whether there are any fractures within ~300 m.

NearDepr: Nearby Depression(s); Whether there are any negative relief features 
          other than apparent impact craters, pits, or fractures within ~300 m.

InDome: Whether the pit is on some sort of local positive relief feature.

BesideDome: Beside Dome; Whether the pit is on flat ground within ~1 pit 
            diameter of the base of some sort of local positive relief feature.

InDepr: In depression; Whether the pit is in some sort of negative relief 
        feature that appears unrelated to the funnel.

ColinePit: Colinear pits; Whether there are at least two other pits that form an
           approximate line with this one.

ColineDepr: Colinear depression(s); Whether this pit is roughly in line with any
            nearby non-fracture elongate negative relief feature.  If the pit is
            in a depression, disregards continuously-connected parts of that 
            depression.

ColinFract: Colinear fracture(s); Whether this pit is roughly in line with a 
            nearby fracture.

Shapefile updated on 3/9/2021
