package com.mtecresults.ranking;

import org.junit.Ignore;
import org.junit.Test;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.*;
import java.util.concurrent.TimeUnit;


public class PerformanceTestDatabaseRace {
    @Test
    @Ignore("Don't run in CI")
    public void testDatabaseData() throws Exception {
        Connection conn = DriverManager
                .getConnection("jdbc:mysql://localhost/results?"
                        + "user=rankingtest&password=rankingtest&serverTimezone=UTC");

        int minRaceId = 1;
        int maxRaceId = 1_000;
        HashMap<Long, Participant> participants = new HashMap<>();
        HashMap<Long, Split> splits = new HashMap<>();
        List<Split> splitsList = new ArrayList<>();
        Map<Long, RankingManager> raceRankingsMap = new HashMap<>();

        Statement stmt = conn.createStatement();
        Statement stmt2 = conn.createStatement();
        ResultSet rs = stmt2.executeQuery("SELECT id FROM race WHERE text_only=0 AND id >= "+minRaceId+" AND id<="+maxRaceId);
        long cumulativeTime = 0;
        long cumulativeSqlTime = 0;
        while(rs.next()){
            long sqlStart = System.nanoTime();
            long raceId = rs.getLong(1);

            participants.clear();
            ResultSet runners = stmt.executeQuery("SELECT id, bib, sex, division, age FROM runner WHERE race_id="+raceId);
            while(runners.next()){
                participants.put(runners.getLong(1), new Participant(runners.getLong(1), runners.getString(3), runners.getString(4), runners.getInt(5)));
            }

            splits.clear();
            splitsList.clear();
            ResultSet splitsRs = stmt.executeQuery("SELECT id, title, split_type FROM split WHERE split_type <> 'VIDEO' AND race_id="+raceId);
            while(splitsRs.next()){
                String splitType = splitsRs.getString(3);
                Split.SplitType st = Split.SplitType.SPLIT;
                if(splitType.equals("FINISH")){
                    st = Split.SplitType.FINISH;
                }
                else if(splitType.equals("CHIP_START")){
                    st = Split.SplitType.CHIPSTART;
                }
                else if(splitType.equals("GUN_START")){
                    st = Split.SplitType.GUNSTART;
                }
                splits.put(splitsRs.getLong(1), new Split(splitsRs.getString(2), st));
                splitsList.add(splits.get(splitsRs.getLong(1)));
            }

            ResultSet splitTimes = stmt.executeQuery("SELECT time_milli, runner_id, split_id FROM split_time, split WHERE split_time.split_id=split.id AND split.race_id="+raceId+" AND split.split_type<> 'VIDEO'");

            //create and store new ranking manager
            //we want to profile memory use of many races worth
            RankingManager rm = new RankingManager();
            raceRankingsMap.put(raceId, rm);
            List<SplitTime> gottenSplitTimes = new ArrayList<>();
            while(splitTimes.next()){
                SplitTime st = new SplitTime(splits.get(splitTimes.getLong(3)), participants.get(splitTimes.getLong(2)), splitTimes.getLong(1));
                if(st.getParticipant() == null){
                    continue;
                }
                gottenSplitTimes.add(st);
            }
            long sqlEnd = System.nanoTime();

            long newMethodStart = System.nanoTime();
            for(SplitTime st: gottenSplitTimes){
                rm.processTime(st, splitsList);
            }
            long newMethodEnd = System.nanoTime();
//            System.out.println("Processed times in: "+ TimeUnit.NANOSECONDS.toMillis(newMethodEnd - newMethodStart)+" ms");
//            System.out.println("Number or rankings: "+rm.rankingsCount()+" Total ranked: "+rm.rankingsFullCount());
            cumulativeTime += (newMethodEnd - newMethodStart);
            cumulativeSqlTime += (sqlEnd - sqlStart);
        }
        System.out.println("Total ranked races: "+raceRankingsMap.size()+" total time: "+TimeUnit.NANOSECONDS.toMillis(cumulativeTime)+" ms  sql time: "+TimeUnit.NANOSECONDS.toMillis(cumulativeSqlTime)+" ms");
    }

}
