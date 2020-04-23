package com.mtecresults.ranking;

import com.mtecresults.ranking.filtering.RankingFilter;
import com.opencsv.bean.CsvToBeanBuilder;
import org.junit.Assert;
import org.junit.Test;

import java.io.FileReader;
import java.util.*;
import java.util.concurrent.TimeUnit;
import static org.junit.Assert.assertEquals;

public class CorrectnessTestCSV {
    @Test
    public void testCsvData() throws Exception {
        //CSV Marathon Data
        List<CSVParticipant> beans = new CsvToBeanBuilder<CSVParticipant>(
                new FileReader(getClass().getResource("results-Marathon-51818.csv").getFile()))
                .withType(CSVParticipant.class).build().parse();

        Integer bib = 1;
        for(CSVParticipant participant: beans){
            participant.bib = bib.toString();
            bib++;
        }

        HashMap<String, Participant> participants = new HashMap<>();
        for(CSVParticipant participant: beans){
            participants.put(participant.bib, new Participant(Long.parseLong(participant.bib), participant.SEX, participant.DIV, Integer.parseInt(participant.AGE)));
        }

        List<Split> splits = new ArrayList<>();
        splits.add(new Split("GUNSTART", Split.SplitType.GUNSTART));
        splits.add(new Split("CHIPSTART", Split.SplitType.CHIPSTART));
        splits.add(new Split("5K", Split.SplitType.SPLIT));
        splits.add(new Split("10K", Split.SplitType.SPLIT));
        splits.add(new Split("HALF", Split.SplitType.SPLIT));
        splits.add(new Split("30K", Split.SplitType.SPLIT));
        splits.add(new Split("21K", Split.SplitType.SPLIT));
        splits.add(new Split("24M", Split.SplitType.SPLIT));
        splits.add(new Split("25.9M", Split.SplitType.SPLIT));
        splits.add(new Split("TIME", Split.SplitType.FINISH));

        long newMethodStart = System.nanoTime();
        RankingManager rankingManager = new RankingManager();
        for(CSVParticipant csvParticipant: beans){
            Participant participant = participants.get(csvParticipant.bib);
            rankingManager.processTime(getSplitTime(splits.get(0), csvParticipant.GUNSTART, participant), splits);
            rankingManager.processTime(getSplitTime(splits.get(1), csvParticipant.CHIPSTART, participant), splits);
            rankingManager.processTime(getSplitTime(splits.get(2), csvParticipant.fiveK, participant), splits);
            rankingManager.processTime(getSplitTime(splits.get(3), csvParticipant.tenK, participant), splits);
            rankingManager.processTime(getSplitTime(splits.get(4), csvParticipant.half, participant), splits);
            rankingManager.processTime(getSplitTime(splits.get(5), csvParticipant.thirtyK, participant), splits);
            rankingManager.processTime(getSplitTime(splits.get(6), csvParticipant.twentyOneM, participant), splits);
            rankingManager.processTime(getSplitTime(splits.get(7), csvParticipant.twentyFourM, participant), splits);
            rankingManager.processTime(getSplitTime(splits.get(8), csvParticipant.twentyFiveNineM, participant), splits);
            rankingManager.processTime(getSplitTime(splits.get(9), csvParticipant.TIME, participant), splits);
        }
        //correct chip start slower then gun start
        for(Participant participant: participants.values()){
            if(participant.getSplitTimes().get(splits.get(0)) > participant.getSplitTimes().get(splits.get(1))){
                rankingManager.processTime(new SplitTime(splits.get(1), participant, participant.getSplitTimes().get(splits.get(0))), splits);
            }
        }

        long newMethodEnd = System.nanoTime();
        System.out.println("Processed times in: "+ TimeUnit.NANOSECONDS.toMillis(newMethodEnd - newMethodStart)+" ms");
        System.out.println("Number or rankings: "+rankingManager.rankingsCount()+" Total ranked: "+rankingManager.rankingsFullCount());

        Assert.assertEquals(new RankingFilter.OverallRankingFilter(), new RankingFilter.OverallRankingFilter());

        //division
        Optional<IndividualRanking> ir = rankingManager.getRanking(new RankingType(new RankingFilter.DivisionRankingFilter("F5559"), splits.get(9), splits.get(1)), participants.get("5599"));
        Assert.assertTrue(ir.isPresent());
        assertEquals(101, ir.get().getTotal());
        assertEquals(47, ir.get().getPosition());

        //sex
        ir = rankingManager.getRanking(new RankingType(new RankingFilter.SexRankingFilter("F"), splits.get(9), splits.get(1)), participants.get("5599"));
        Assert.assertTrue(ir.isPresent());
        assertEquals(2899, ir.get().getTotal());
        assertEquals(1571, ir.get().getPosition());

        //overall
        ir = rankingManager.getRanking(new RankingType(new RankingFilter.OverallRankingFilter(), splits.get(9), splits.get(1)), participants.get("5599"));
        Assert.assertTrue(ir.isPresent());
        assertEquals(6747, ir.get().getTotal());
        assertEquals(4291, ir.get().getPosition());
    }

    private SplitTime getSplitTime(Split split, String field, Participant participant){
        long time = formattedToMilliseconds(field);
        return new SplitTime(split, participant, time);
    }

    private static long formattedToMilliseconds(String time) throws NumberFormatException {
        String[] parts = time.trim().split(":");
        String sec;
        long ret = 0;

        if(!time.equals("")){
            if(parts.length>3){
                throw new NumberFormatException("Invalid time: "+time+" too many colons, can't parse");
            }
            if(parts.length>2){
                ret+= Integer.parseInt(parts[0])*60*60*1000 + Integer.parseInt(parts[1])*60*1000;
                sec = parts[2];
            }
            else if(parts.length>1){
                ret+=Integer.parseInt(parts[0])*60*1000;
                sec = parts[1];
            }
            else{
                sec = parts[0];
            }

            if(sec.contains(".")){
                String[] parts2 = sec.split("\\.");
                ret+= Integer.parseInt(parts2[0])*1000 + Integer.parseInt((parts2[1]+"000").substring(0,3));
            }
            else{
                ret+= Integer.parseInt(sec)*1000;
            }

        }

        return ret;
    }

}
