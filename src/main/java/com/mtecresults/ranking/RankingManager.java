package com.mtecresults.ranking;

import java.util.*;

public class RankingManager {
    private final Map<RankingType, Ranking> rankingMap = new HashMap<>();

    //FIXME: handle changes to Participants - age/sex/div
    //FIXME: handle changes to split list - or have ranking manager based on splits?? Remove from process time call, rebuild if splits changed??

    public void processTime(SplitTime st, List<Split> splits) {
        Long previousTime = st.getParticipant().getSplitTimes().get(st.getSplit());
        st.getParticipant().getSplitTimes().put(st.getSplit(), st.getTime());
        Collection<RankingType> affectedRankingTypes = st.getSplit().getType().getPossibleAffectedRankings(splits, st.getSplit(), st.getParticipant());
        for(RankingType rankingType: affectedRankingTypes){
           Ranking ranking = rankingMap.get(rankingType);
           if(ranking == null){
               ranking = rankingType.createRanking();
               rankingMap.put(rankingType, ranking);
           }
           ranking.processTime(st, previousTime);
        }
    }

    public int rankingsCount() {
        return rankingMap.size();
    }

    public int rankingsFullCount() {
        return rankingMap.values().stream().mapToInt(Ranking::size).sum();
    }

    public Optional<IndividualRanking> getRanking(RankingType rankingType, Participant participant){
        Ranking ranking = rankingMap.get(rankingType);
        if(ranking != null){
            return ranking.getRankingForParticipant(participant);
        }
        return Optional.empty();
    }

    protected OrderStatisticTree getDebugTree(RankingType rankingType){
        Ranking ranking = rankingMap.get(rankingType);
        if(ranking != null){
            return ranking.getRankingTree();
        }
        return null;
    }

}
