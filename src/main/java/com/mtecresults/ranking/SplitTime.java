package com.mtecresults.ranking;

import lombok.Value;

@Value
public class SplitTime {
    final Split split;
    final Participant participant;
    final long time;
}
