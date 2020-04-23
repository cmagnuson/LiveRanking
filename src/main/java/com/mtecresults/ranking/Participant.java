package com.mtecresults.ranking;

import lombok.Value;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@Value
public class Participant {
    final long uniqueId;
    final String sex;
    final String division;
    final int age;
    final Map<Split, Long> splitTimes = new HashMap<>();

    @Override
    public boolean equals(Object o){
        return o instanceof Participant && uniqueId == ((Participant)o).uniqueId;
    }

    @Override
    public int hashCode() {
        return Objects.hash(uniqueId);
    }
}
