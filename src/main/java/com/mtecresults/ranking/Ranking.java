package com.mtecresults.ranking;

import lombok.Value;

import java.util.Optional;
import java.util.function.Function;

@Value
public class Ranking {
    final Split beforeSplit;
    final Split atSplit;
    final Function<SplitTime, Boolean> interestedIn;
    final Function<Integer, Integer> timeTransform;

    final OrderStatisticTree<Integer> rankingTree = new OrderStatisticTree<>();

    public Optional<IndividualRanking> getRankingForParticipant(Participant participant){
        Integer time = getTimeDifference(participant);
        if(time != null){
            int rank = rankingTree.rank(time);
            if(rank > -1){
                return Optional.of(new IndividualRanking(rank, size()));
            }
        }
        return Optional.empty();
    }

    public static Ranking createOverall(Split beforeSplit, Split atSplit){
        return new Ranking(beforeSplit, atSplit, splitTime -> splitTime.getSplit()==beforeSplit || splitTime.getSplit()==atSplit, wrappedParticipant -> wrappedParticipant);
    }

    public static Ranking createAgeGraded(Split beforeSplit, Split atSplit){
        //FIXME: process wrappedParticipant time to be age graded here
        // need to make timeTransform act on a Participant or age+sex to calculate this
        return new Ranking(beforeSplit, atSplit, splitTime -> splitTime.getSplit()==beforeSplit || splitTime.getSplit()==atSplit, wrappedParticipant -> wrappedParticipant);
    }

    public static Ranking createDivision(Split beforeSplit, Split atSplit, String division){
        return new Ranking(beforeSplit, atSplit, splitTime -> (splitTime.getSplit()==beforeSplit || splitTime.getSplit()==atSplit) && splitTime.getParticipant().getDivision().equals(division), wrappedParticipant -> wrappedParticipant);
    }

    public static Ranking createSex(Split beforeSplit, Split atSplit, String sex){
        return new Ranking(beforeSplit, atSplit, splitTime -> (splitTime.getSplit()==beforeSplit || splitTime.getSplit()==atSplit) && splitTime.getParticipant().getSex().equals(sex), wrappedParticipant -> wrappedParticipant);
    }

    public int size() {
        return rankingTree.sizeOfAllElements();
    }

    protected Integer getTimeDifference(Participant participant){
        Long timeBefore = participant.getSplitTimes().get(beforeSplit);
        Long timeAt = participant.getSplitTimes().get(atSplit);
        if(timeBefore != null && timeAt != null) {
            //they have a before split, can do ranking on
            return (int) (timeAt - timeBefore);
        }
        else{
            return null;
        }
    }

    //FIXME: make a single update method for map - if removing and adding so we can do notifications on change to ranking of whole process
    //oor - have listener on Ranking so after processTime completes - we can notify any interested listeners by getting place of newly added
    void processTime(SplitTime splitTime, Long previousTime){
        if(interestedIn.apply(splitTime)){
            Integer timeDifference = getTimeDifference(splitTime.getParticipant());
            if(timeDifference != null){
                //they have a before split, can do ranking on

                //check if they exist here and remove if they do
                if(previousTime != null){
                    Long timeBefore = splitTime.getParticipant().getSplitTimes().get(beforeSplit);
                    Long timeAt = splitTime.getParticipant().getSplitTimes().get(atSplit);
                    long previousDifference;
                    if(splitTime.getSplit().equals(atSplit)){
                        previousDifference = previousTime - timeBefore;
                    } else{
                        previousDifference = timeAt - previousTime;
                    }
                    rankingTree.remove((int)previousDifference);
                }

                //put their time
                //apply and time changes - for example from regular to age graded time
                int adjustedTime = timeTransform.apply(timeDifference);
                rankingTree.add(adjustedTime);
            }
        }
    }
}
