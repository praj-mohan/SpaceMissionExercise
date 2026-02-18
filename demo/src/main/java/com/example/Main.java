package com.example;

import java.io.FileReader;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.*;

import com.opencsv.*;

import java.util.*;

public class Main {
    
    static List<String[]> spaceMission;

    public static void main(String[] args) {
        try {
        FileReader file = new FileReader("space_missions.csv");
        CSVReader  csv = new CSVReader(file);
        spaceMission = csv.readAll();
        file.close();
        csv.close();
        spaceMission.remove(0);
        //float test = getSuccessRate("Arianespace");
        //System.out.println(test);
        //System.out.println(getMissionsByYear(1969));        


        }
        catch (Exception noFile) {
            System.out.println("File not found");

        }
        //System.out.println("Hello world!");
        
    }

    public static int getMissionCountByCompany(String companyName) {
        int result = 0; 
        for (String[] missions : spaceMission) {
            if (companyName.equals(missions[0])) {
                result++;
            }

        }

        return result;
    }

    public static float getSuccessRate(String companyName) {
        int success = 0;
        int total = getMissionCountByCompany(companyName);
        for (String[] missions : spaceMission) {
            if (companyName.equals(missions[0]) && missions[8].equals("Success")) {
                success++;
            }

        }
        
        float result = (float) success / (float) total;
        result = result * 100;
        BigDecimal rounder = new BigDecimal(Float.toString(result));
        if (result == 0) {
            rounder = rounder.setScale(1, RoundingMode.HALF_UP);
        }
        else {
            rounder = rounder.setScale(2, RoundingMode.HALF_UP);

        }
        result = rounder.floatValue();        



        return result;

    }

    public static List<String> getMissionsByDateRange(String startDate, String endDate) {
         LocalDate start = LocalDate.parse(startDate);
         LocalDate end = LocalDate.parse(endDate);
         
         List<String> result = new ArrayList<String>();
        
         for (String[] missions : spaceMission) {
            //System.out.println(missions[2]);
            LocalDate missionDate = LocalDate.parse(missions[2]);
            if (missionDate.isAfter(start) && missionDate.isBefore(end)) {
                result.add(missions[5]);
            }
         }
         return result;


    }

    public static List<String> getTopCompaniesByMissionCount(int n) {
       
        List<String> test = new ArrayList<String>();

        test.add("hi");

        return test;

    }

    public static HashMap<String, String> getMissionStatusCount() {

        return new HashMap<String, String>();

    }

    public static int getMissionsByYear(int year) {
        int result = 0;
        for (String[] missions : spaceMission) {
            LocalDate missionDate = LocalDate.parse(missions[2]);
            if (year == missionDate.getYear()) {
                result++;

            }
        }

        return result;

    }

    public static String getMostUsedRocket() {
        return "test";

    }

    public static float getAverageMissionsPerYear(int startYear, int endYear) {

        return 0;

    }
}