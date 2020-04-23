package com.mtecresults.ranking;

import com.opencsv.bean.CsvBindByName;

public class CSVParticipant {
    String bib;

    @CsvBindByName
    String SEX;

    @CsvBindByName
    String AGE;

    @CsvBindByName
    String DIV;

    @CsvBindByName
    String GUNSTART;

    @CsvBindByName
    String CHIPSTART;

    @CsvBindByName(column = "5K")
    String fiveK;

    @CsvBindByName(column = "10k")
    String tenK;

    @CsvBindByName(column = "13.1M")
    String half;

    @CsvBindByName(column = "30K")
    String thirtyK;

    @CsvBindByName(column = "21M")
    String twentyOneM;

    @CsvBindByName(column = "24M")
    String twentyFourM;

    @CsvBindByName(column = "25.9M")
    String twentyFiveNineM;

    @CsvBindByName
    String TIME;
}
