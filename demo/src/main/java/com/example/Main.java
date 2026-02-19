package com.example;

import java.io.FileReader;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.*;
import org.javatuples.Pair;

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
        System.out.println(getAverageMissionsPerYear(2020, 2022));
        }
        catch (Exception noFile) {
            System.out.println("File not found");

        }        
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

    public static List<Pair<String, Integer>> getTopCompaniesByMissionCount(int n) {
       
        if (n <= 0) {
            System.out.println("This is an invalid input");
        }

        List<Pair<String, Integer>> result = new ArrayList<Pair<String, Integer>>();

        PriorityQueue<Map.Entry<String, Integer>> topCompanies = new PriorityQueue<>(Map.Entry.comparingByValue(Comparator.naturalOrder()));
        HashMap<String, Integer> presort = new HashMap<String, Integer>();
        List<String[]> sortByCompanies = spaceMission;

        Collections.sort(sortByCompanies, new Comparator<String[]>(){
            public int compare(String[] s1, String[] s2) {
                return s1[0].compareTo(s2[0]);
            }
        });

        String currCompany = "";
        int currcount = 0;

        for (String[] missions:sortByCompanies) {
            if (currCompany.equals("")) {
                currCompany = missions[0];
            }
            if (currCompany.equals(missions[0])) {
                currcount++;
            }
            else {
                presort.put(currCompany, currcount);
                currCompany = missions[0];
                currcount = 1;
            }
        }
        presort.put(currCompany, currcount);
        for (Map.Entry<String, Integer> company : presort.entrySet()) {
            topCompanies.add(company);
            if (topCompanies.size() > n) {
                topCompanies.poll();
            }
        }

        while (topCompanies.size() > 0) {
            Map.Entry<String, Integer> convTuple = topCompanies.poll();
            Pair<String, Integer> compTuple = Pair.with(convTuple.getKey(), convTuple.getValue());
            result.add(0, compTuple);
        }


        return result;

    }

    public static HashMap<String, Integer> getMissionStatusCount() {
        
        HashMap<String, Integer> result = new HashMap<String, Integer>();
         for (String[] missions : spaceMission) {
            if (result.containsKey(missions[8])) {
                result.put(missions[8], result.get(missions[8]) + 1);
            }
            else {
                result.put(missions[8], 1);
            }
        }
        
        return result;

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
        
        String result = "";
        int highest = 0;

        HashMap<String, Integer> rockets = new HashMap<String, Integer>();

        for (String[] missions : spaceMission) {
            if (rockets.containsKey(missions[4])) {
                rockets.put(missions[4], rockets.get(missions[4]) + 1);
                if (rockets.get(missions[4]) > highest) {
                    result = missions[4];
                    highest = rockets.get(missions[4]);
                }
            }
            else {
                rockets.put(missions[4], 1);
                if (result.equals("")) {
                    result = missions[4];
                    highest = 1;
                }
            }
        }


        return result;

    }

    public static float getAverageMissionsPerYear(int startYear, int endYear) {

        int range = (endYear - startYear) + 1;
        int total = 0;

        for (int i = startYear; i <= endYear; i++) {
            total += getMissionsByYear(i);
        }

        float result = (float) total / (float) range;
        BigDecimal rounder = new BigDecimal(Float.toString(result));
        rounder = rounder.setScale(2, RoundingMode.HALF_UP);

        result = rounder.floatValue();        


        return result;

    }
}