package com.mtecresults.ranking;

import com.mtecresults.ranking.filtering.RankingFilter;
import lombok.Value;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Value
public class Split {
    final String name;
    final SplitType type;

    public boolean isNotStart() {
        return type != SplitType.GUNSTART && type != SplitType.CHIPSTART;
    }

    public boolean isStart() {
        return !isNotStart();
    }

    public enum SplitType {

        GUNSTART{
            @Override
            Collection<RankingType> getPossibleAffectedRankings(List<Split> splits, Split currentSplit, Participant participant){
                return getStartAffectedRankings(splits, currentSplit, participant);
            };
        }, CHIPSTART {
            @Override
            Collection<RankingType> getPossibleAffectedRankings(List<Split> splits, Split currentSplit, Participant participant) {
                return getStartAffectedRankings(splits, currentSplit, participant);
            }
        }, SPLIT {
            @Override
            Collection<RankingType> getPossibleAffectedRankings(List<Split> splits, Split currentSplit, Participant participant) {
                return getNonStartAffectedRankings(splits, currentSplit, participant);
            }
        }, FINISH {
            @Override
            Collection<RankingType> getPossibleAffectedRankings(List<Split> splits, Split currentSplit, Participant participant) {
                return getNonStartAffectedRankings(splits, currentSplit, participant);
            }
        },
        ;

        abstract Collection<RankingType> getPossibleAffectedRankings(List<Split> splits, Split currentSplit, Participant participant);

        private static Collection<RankingType> getStartAffectedRankings(List<Split> splits, Split currentSplit, Participant participant){
            List<RankingType> rankingTypes = new ArrayList<>();
            Collection<RankingFilter> filters = RankingFilter.getPossibleRankingsForParticipant(participant);
            for(RankingFilter rankingFilter: filters){
                for(Split s: splits){
                    if(s.isNotStart()){
                        rankingTypes.add(new RankingType(rankingFilter, s, currentSplit));
                    }
                }
            }
            return rankingTypes;
        }

        private static Collection<RankingType> getNonStartAffectedRankings(List<Split> splits, Split currentSplit, Participant participant){
            List<RankingType> rankingTypes = new ArrayList<>();
            Collection<RankingFilter> filters = RankingFilter.getPossibleRankingsForParticipant(participant);
            for(RankingFilter rankingFilter: filters){
                for(Split s: splits){
                    if(s.isStart()){
                        //add gun/chip rankings
                        rankingTypes.add(new RankingType(rankingFilter, currentSplit, s));
                    }
                    else if(splits.indexOf(s) == splits.indexOf(currentSplit) - 1){
                        //add tri ranking between this and previous split
                        rankingTypes.add(new RankingType(rankingFilter, currentSplit, s));
                    }
                    else if(splits.indexOf(s) == splits.indexOf(currentSplit) + 1){
                        //add tri ranking between this and next
                        rankingTypes.add(new RankingType(rankingFilter, s, currentSplit));
                    }
                }
            }
            return rankingTypes;
        }
    }
}

