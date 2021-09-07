package com.example.upmessenger.Activity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class intersection {
    private static ArrayList<Integer> intersection(int[]... list) {
        // write your code here.

        List<Integer> commonElements = new ArrayList<>();
        for(int curr:list[0])
            commonElements.add(curr);

        int n = list.length;
        for (int i = 1; i < n; i++) {
            Set<Integer> currArray = new HashSet<>();
            for (int curr:currArray)
                currArray.add(curr);

            int currLength = commonElements.size();
            int count = 0,curr=0;
            while (count<currLength){

                if(currArray.contains(commonElements.get(curr)))
                    commonElements.remove(curr);
                else
                    curr++;

                count++;

            }

        }
        return new ArrayList<>(commonElements);
    }


}


