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
import java.util.stream.*;

public class PlacemarkSearchData
{
    private static String [] csvFromPlaceNameShapeFiles;
    private final String regex;
    CelestialMapper cms;
    WorldWindow wwd;

//    private static HashMap<Object, HashMap<String, Object>> hashMap;
    private static ArrayList<String []> rowList;
    private static String [] tableHeader;

    public PlacemarkSearchData(WorldWindow wwd, CelestialMapper cms)
    {
        this.cms = cms;
        this.wwd = wwd;
//        hashMap = new HashMap<>();
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

        String otherThanQuote = " [^\"] ";
        String quotedString = String.format(" \" %s* \" ", otherThanQuote);
        regex = String.format("(?x) "+ // enable comments, ignore white spaces
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

        fillTableData();

    }

    private void fillTableData()
    {

        // This is where the compiled CSV will be stored
        // For now, if you add a new CSV to the csvFromPlaceNameShapeFiles array, you'll have to manually
        // delete this file.
        // There's no logic yet to check if the number of distinct categories
        // matches the number of CSV's in csvFromPlaceNameShapeFiles.
        File tableData = new File("cms-data/placenames/placemarkTableData.csv");

        if(tableData.exists()){
            try
            {
                BufferedReader csvReader = new BufferedReader(new FileReader(tableData));
                String row;

                // Get first line of the CSV as the headers for the table
                tableHeader = csvReader.readLine().split(regex, -1);

                // Loop over the rest of the CSV to fill up the table rows
                while ((row = csvReader.readLine()) != null) {
                    String[] data = row.split(regex, -1);
                    rowList.add(data);
                }

                // Make sure to close the buffered reader
                csvReader.close();
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        } else {

            Arrays.stream(csvFromPlaceNameShapeFiles).forEach(s -> {

                // Grab the filename as a string to add as the first column value later, under the header of "Category"
                String filename = s.substring(s.lastIndexOf("/") +1, s.lastIndexOf("."));

                // From this example for handling quoted commas in csv's
                // https://stackoverflow.com/questions/1757065/java-splitting-a-comma-separated-string-but-ignoring-commas-in-quotes


                try ( BufferedReader in = new BufferedReader(new FileReader(s)))
                {

//                System.out.println(filename);
                    ArrayList<String> headers = new ArrayList<>(Arrays.asList(in.readLine().split(",")));
                    headers.add(0, "Category");

                    tableHeader = headers.toArray(new String[0]);

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
                        //
                        ArrayList<String> row = new ArrayList<>(Arrays.asList(line));

                        // Make the category / Filename the first column
                        row.add(0,filename);

                        rowList.add(row.toArray(new String[0]));
                    });


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
            ArrayList<String[]> allData = new ArrayList<>();
            allData.add(tableHeader);
            allData.addAll(rowList);
            try
            {
                PrintWriter printWriter = new PrintWriter(new FileWriter(tableData.getPath()));
                allData.stream().map(this::convertToCSV).forEach(printWriter::println);
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        }
    }

    public String convertToCSV(String[] data) {
        return Stream.of(data)
            .map(this::escapeSpecialCharacters)
            .collect(Collectors.joining(","));
    }

    public String escapeSpecialCharacters(String data) {
        String escapedData = data.replaceAll("\\R", " ");
        if (data.contains(",") || data.contains("\"") || data.contains("'")) {
            
            // add an extra \ to escape any current \'s in a cell
            data = data.replace("\"", "\"\"");
            
            // surround the data for a cell with escaped quote marks
            escapedData = "\"" + data + "\"";
        }
        return escapedData;
    }

    public WorldWindow getWwd()
    {
        return wwd;
    }

    public String[] getHeaders()
    {
        return tableHeader;
    }

    public static ArrayList<String[]> getRowList()
    {
        return rowList;
    }
}
