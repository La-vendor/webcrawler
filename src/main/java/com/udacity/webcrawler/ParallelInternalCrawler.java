package com.udacity.webcrawler;

import com.udacity.webcrawler.parser.PageParser;
import com.udacity.webcrawler.parser.PageParserFactory;

import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.RecursiveAction;
import java.util.regex.Pattern;

public class ParallelInternalCrawler extends RecursiveAction {

    private final Clock clock = Clock.systemUTC();
    private String url;
    private PageParserFactory parserFactory;
    private Instant deadline;
    private int maxDepth;
    private List<Pattern> ignoredUrls;
    private ConcurrentHashMap<String, Integer> countsInternal = new ConcurrentHashMap<>();
    private ConcurrentSkipListSet<String> visitedUrlsInternal = new ConcurrentSkipListSet<>();

    public ParallelInternalCrawler() {
    }

    public ParallelInternalCrawler setUrl(String url) {
        this.url = url;
        return this;
    }

    public ParallelInternalCrawler setParserFactory(PageParserFactory parserFactory) {
        this.parserFactory = parserFactory;
        return this;
    }

    public ParallelInternalCrawler setDeadline(Instant deadline) {
        this.deadline = deadline;
        return this;
    }

    public ParallelInternalCrawler setMaxDepth(int maxDepth) {
        this.maxDepth = maxDepth;
        return this;
    }

    public ParallelInternalCrawler setIgnoredUrls(List<Pattern> ignoredUrls) {
        this.ignoredUrls = ignoredUrls;
        return this;
    }

    public ParallelInternalCrawler setCountsInternal(ConcurrentHashMap<String, Integer> countsInternal) {
        this.countsInternal = countsInternal;
        return this;
    }

    public ParallelInternalCrawler setVisitedUrlsInternal(ConcurrentSkipListSet<String> visitedUrlsInternal) {
        this.visitedUrlsInternal = visitedUrlsInternal;
        return this;
    }

    @Override
    protected void compute() {

        if ((maxDepth == 0) || clock.instant().isAfter(deadline)) {
            return;
        }
        for (Pattern pattern : ignoredUrls) {
            if (pattern.matcher(url).matches()) {
                return;
            }
        }

        if (!visitedUrlsInternal.add(url)) {
            return;
        }

        PageParser.Result result = parserFactory.get(url).parse();


        result.getWordCounts().forEach((key, value) ->
                countsInternal.merge(key, value, Integer::sum)
        );

        List<ParallelInternalCrawler> internalCrawlers = new ArrayList<>();

        for (String link : result.getLinks()) {
            internalCrawlers.add(new ParallelInternalCrawler().
                    setUrl(link).
                    setParserFactory(parserFactory).
                    setDeadline(deadline).
                    setMaxDepth(maxDepth - 1).
                    setIgnoredUrls(ignoredUrls).
                    setCountsInternal(countsInternal).
                    setVisitedUrlsInternal(visitedUrlsInternal));
        }

        invokeAll(internalCrawlers);

    }
}