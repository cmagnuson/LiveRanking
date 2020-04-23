package com.mtecresults.ranking;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class TestDriver {

    public static void main(String[] args) throws Exception {
        //generate some test data
        //id,sex,div,age,chip start,gun start,split1, split2, split3, ..., time
        List<String> divs = new ArrayList();
        for(int i=0; i<30; i++){
            //30 divisions on average
            divs.add(""+i);
        }

        List<Participant> participants = new ArrayList<>();
        for(int i=0; i<20_000; i++){
            participants.add(new Participant(i, Math.random() > 0.5 ? "M" : "F", divs.get((int)(Math.floor(Math.random()* divs.size()))), (int)(Math.random()*100)));
        }

        List<Split> splits = new ArrayList<>();
        splits.add(new Split("Gun", Split.SplitType.GUNSTART));
        splits.add(new Split("Chip", Split.SplitType.CHIPSTART));
        splits.add(new Split("5K", Split.SplitType.SPLIT));
        splits.add(new Split("10K", Split.SplitType.SPLIT));
        splits.add(new Split("15K", Split.SplitType.SPLIT));
        splits.add(new Split("20K", Split.SplitType.SPLIT));
        splits.add(new Split("25K", Split.SplitType.SPLIT));
        splits.add(new Split("30K", Split.SplitType.SPLIT));
        splits.add(new Split("35K", Split.SplitType.SPLIT));
        splits.add(new Split("40K", Split.SplitType.SPLIT));
        splits.add(new Split("Finish", Split.SplitType.FINISH));

        //generate random times for participants and splits
        List<SplitTime> randomTimes = new ArrayList<>();
        for(Participant p: participants){
            for(Split s: splits){
                SplitTime st = new SplitTime(s, p, (long)(Math.random() * Long.MAX_VALUE));
                randomTimes.add(st);
            }
        }
        Collections.shuffle(randomTimes);

        List<Ranking> rankings = getRankingsForSplits(splits, participants);

        System.out.println(""+rankings.size()+" rankings created");

        long timeBefore = System.nanoTime();
        int count = 0;
        for(SplitTime st: randomTimes) {
            processTime(st, rankings);
            count++;
        }
        long timeAfter = System.nanoTime();
        System.out.println("Processed "+count+" time/ranking combinations in "+ TimeUnit.NANOSECONDS.toMillis(timeAfter - timeBefore)+" ms");

        for(Ranking ranking: rankings){
            System.out.println("Ranking size: "+ranking.size());
        }

//        System.out.println("Rankings memory size: "+ ObjectGraphMeasurer.measure(rankings));
    }

    public static void processTime(SplitTime st, Collection<Ranking> rankings){
        st.getParticipant().getSplitTimes().put(st.getSplit(), st.getTime());
        for(Ranking ranking: rankings){
            ranking.processTime(st, null);
        }
    }

    public static List<Ranking> getRankingsForSplits(List<Split> splits, Collection<Participant> participants){
        List<String> divs = participants.stream().map(participant -> participant.getDivision()).distinct().collect(Collectors.toList());
        List<Ranking> rankings = new ArrayList<>();
        for(Split s: splits){
            if(s.getType() == Split.SplitType.SPLIT || s.getType() == Split.SplitType.FINISH){
                //gun ranking, chip ranking
                for(Split start: splits){
                    if(start.getType()== Split.SplitType.CHIPSTART || start.getType()== Split.SplitType.GUNSTART){
                        //add overall 2x - one for regular and one for age graded
                        rankings.add(Ranking.createOverall(start, s));
                        rankings.add(Ranking.createOverall(start, s));
                        rankings.add(Ranking.createSex(start, s, "M"));
                        rankings.add(Ranking.createSex(start, s, "F"));
                        for(String division: divs){
                            rankings.add(Ranking.createDivision(start, s, division));
                        }
                    }
                }

                //create tri rankings
                Split before = splits.get(splits.indexOf(s) - 1);
                if(before.getType() == Split.SplitType.SPLIT){
                    rankings.add(Ranking.createOverall(before, s));
                    rankings.add(Ranking.createOverall(before, s));
                    rankings.add(Ranking.createSex(before, s, "M"));
                    rankings.add(Ranking.createSex(before, s, "F"));
                    for(String division: divs){
                        rankings.add(Ranking.createDivision(before, s, division));
                    }
                }
            }
        }
        return rankings;
    }

}
