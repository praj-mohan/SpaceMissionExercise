package com.example;

import java.io.FileReader;

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
        System.out.println(getMissionCountByCompany("EER"));


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
        return 0;

    }

    public static List<String> getMissionsByDateRange(String startDate, String endDate) {

         List<String> test = new ArrayList<String>();

         test.add("hi");

         return test;


    }

    public static int getTopCompaniesByMissionCount(int n) {

        return 0;

    }

    public static HashMap<String, String> getMissionStatusCount() {

        return new HashMap<String, String>();

    }

    public static int getMissionsByYear(int year) {
        return 0;

    }

    public static String getMostUsedRocket() {
        return "test";

    }

    public static float getAverageMissionsPerYear(int startYear, int endYear) {

        return 0;

    }
}