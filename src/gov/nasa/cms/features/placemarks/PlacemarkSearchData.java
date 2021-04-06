/*
 * Copyright (C) 2021 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */

/*
* @author: gknorman
 */

package gov.nasa.cms.features.placemarks;

import gov.nasa.cms.CelestialMapper;
import gov.nasa.worldwind.WorldWindow;

import java.io.*;
import java.util.*;

public class PlacemarkSearchData
{
    private static String [] csvFromPlaceNameShapeFiles;
    CelestialMapper cms;
    WorldWindow wwd;

    private static HashMap<Object, HashMap<String, Object>> hashMap;
    private static ArrayList<Object> rowList;
    private static String [] tableHeader;

    public PlacemarkSearchData(WorldWindow wwd, CelestialMapper cms)
    {
        this.cms = cms;
        this.wwd = wwd;
        hashMap = new HashMap<>();
        rowList = new ArrayList<>();

        csvFromPlaceNameShapeFiles = new String[]{
            "cms-data/placenames/Oceanus/Oceanus.csv",
            "cms-data/placenames/Mare/Mare.csv",
            "cms-data/placenames/Mons/Mons.csv",
            "cms-data/placenames/Catena/Catena.csv",
            "cms-data/placenames/Vallis/Vallis.csv",
            "cms-data/placenames/Crater/Crater.csv",
            "cms-data/placenames/Dorsum/Dorsum.csv",
            "cms-data/placenames/Lacus/Lacus.csv",
            "cms-data/placenames/Sinus/Sinus.csv",
            "cms-data/placenames/Rima/Rima.csv",
            "cms-data/placenames/Promontorium/Promontorium.csv",
            "cms-data/placenames/Palus/Palus.csv",
            "cms-data/placenames/Rupes/Rupes.csv",
            "cms-data/placenames/Landing Site/LandingSiteName.csv",
            "cms-data/placenames/Planitia/Planitia.csv",
            "cms-data/placenames/Albedo/Albedo.csv",
            "cms-data/placenames/Satellite/Satellite.csv",
        };

        fillTableData();

    }

    private static void fillTableData()
    {

        Arrays.stream(csvFromPlaceNameShapeFiles).forEach(s -> {
            String filename = s.substring(s.lastIndexOf("/") +1, s.lastIndexOf("."));

            // From this example for handling quoted commas in csv's
            // https://stackoverflow.com/questions/1757065/java-splitting-a-comma-separated-string-but-ignoring-commas-in-quotes
            String otherThanQuote = " [^\"] ";
            String quotedString = String.format(" \" %s* \" ", otherThanQuote);
            String regex = String.format("(?x) "+ // enable comments, ignore white spaces
                    ",                         "+ // match a comma
                    "(?=                       "+ // start positive look ahead
                    "  (?:                     "+ //   start non-capturing group 1
                    "    %s*                   "+ //     match 'otherThanQuote' zero or more times
                    "    %s                    "+ //     match 'quotedString'
                    "  )*                      "+ //   end group 1 and repeat it zero or more times
                    "  %s*                     "+ //   match 'otherThanQuote'
                    "  $                       "+ // match the end of the string
                    ")                         ", // stop positive look ahead
                otherThanQuote, quotedString, otherThanQuote);

            try ( BufferedReader in = new BufferedReader(new FileReader(s)))
            {

//                System.out.println(filename);
                ArrayList<String> headers = new ArrayList(Arrays.asList(in.readLine().split(",")));
                headers.add(0, "Category");

                in.lines().forEach(s1 -> {
                    String[] line =  s1.split(regex, -1);

                    // Only here for debugging purposes
//                    System.out.println("ORIGINAL FILENAME & STRING - " + filename + ": " + s1);
//                    System.out.println("HEADER SIZE: " + headers.length);
//                    System.out.println(String.join(",",headers));
//                    System.out.println("LINE SIZE: " + line.length);
//                    System.out.println(String.join(",",line));

                    // Original data structure before starting the Table Model
//                    HashMap<String, Object> hash = IntStream.range(0, headers.length)
//                        .boxed()
//                        .collect(Collectors.toMap(
//                            i -> headers[i],
//                            i -> line[i],
//                            (u,v) -> { throw new IllegalStateException(String.format("Duplicate key %s", u)); },
//                            HashMap::new
//                        ));
//                    hash.put("category", filename);
//                    hashMap.put(hash.get("clean_name"),hash);

                    // It will be easier to extract the data for each row of the table as a list
                    // instead of an unordered hashmap
                    ArrayList row = new ArrayList(Arrays.asList(line));

                    // TODO - make the category / Filename the first column and possibly a row integer
                    row.add(0,filename);
                    rowList.add(row);

                });
                tableHeader = headers.toArray(new String[0]);
            }
            catch (FileNotFoundException e)
            {
                System.out.println("File Not Found!!");
                e.printStackTrace();
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        });
    }

    public WorldWindow getWwd()
    {
        return wwd;
    }

    public String[] getHeaders()
    {
        return tableHeader;
    }

    public static ArrayList<Object> getRowList()
    {
        return rowList;
    }
}
