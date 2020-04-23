package com.mtecresults.ranking;

import com.mtecresults.ranking.filtering.RankingFilter;
import lombok.Value;

@Value
public class RankingType {
    public final RankingFilter rankingFilter;
    public final Split selectedSplit;
    public final Split previousSplit;

    public Ranking createRanking(){
        return rankingFilter.createRankingForFilter(this);
    }

}
