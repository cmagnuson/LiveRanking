package com.mtecresults.ranking.filtering;

import com.mtecresults.ranking.Participant;
import com.mtecresults.ranking.Ranking;
import com.mtecresults.ranking.RankingType;
import lombok.EqualsAndHashCode;
import lombok.Value;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public abstract class RankingFilter {
    public abstract Ranking createRankingForFilter(RankingType rankingType);

    public static Collection<RankingFilter> getPossibleRankingsForParticipant(Participant p){
        List<RankingFilter> possibleRankings = new ArrayList<>();
        possibleRankings.add(new OverallRankingFilter());
        possibleRankings.add(new AgeGradedRankingFilter());
        possibleRankings.add(new SexRankingFilter(p.getSex()));
        possibleRankings.add(new DivisionRankingFilter(p.getDivision()));
        return possibleRankings;
    }

    @EqualsAndHashCode(callSuper = false)
    @Value
    public static class OverallRankingFilter extends RankingFilter {
        @Override
        public Ranking createRankingForFilter(RankingType rankingType) {
            return Ranking.createOverall(rankingType.previousSplit, rankingType.selectedSplit);
        }
    }

    @EqualsAndHashCode(callSuper = false)
    @Value
    public static class AgeGradedRankingFilter extends RankingFilter {
        @Override
        public Ranking createRankingForFilter(RankingType rankingType) {
            return Ranking.createAgeGraded(rankingType.previousSplit, rankingType.selectedSplit);
        }
    }

    @EqualsAndHashCode(callSuper = false)
    @Value
    public static class SexRankingFilter extends RankingFilter {
        public final String sex;

        @Override
        public Ranking createRankingForFilter(RankingType rankingType) {
            return Ranking.createSex(rankingType.previousSplit, rankingType.selectedSplit, sex);
        }
    }

    @EqualsAndHashCode(callSuper = false)
    @Value
    public static class DivisionRankingFilter extends RankingFilter {
        public final String division;

        @Override
        public Ranking createRankingForFilter(RankingType rankingType) {
            return Ranking.createDivision(rankingType.previousSplit, rankingType.selectedSplit, division);
        }
    }
}
