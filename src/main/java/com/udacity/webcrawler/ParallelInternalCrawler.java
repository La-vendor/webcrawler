package com.udacity.webcrawler;

import com.udacity.webcrawler.parser.PageParser;
import com.udacity.webcrawler.parser.PageParserFactory;

import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.RecursiveAction;
import java.util.regex.Pattern;

public class ParallelInternalCrawler extends RecursiveAction {

    private final Clock clock = Clock.systemUTC();
    private final String url;
    private final PageParserFactory parserFactory;
    private final Instant deadline;
    private final int maxDepth;
    private final List<Pattern> ignoredUrls;
    public static ConcurrentHashMap<String, Integer> countsInternal = new ConcurrentHashMap<>();
    public static ConcurrentSkipListSet<String> visitedUrlsInternal = new ConcurrentSkipListSet<>();

    public ParallelInternalCrawler(
            String url,
            PageParserFactory parserFactory,
            Instant deadline,
            int maxDepth,
            List<Pattern> ignoredUrls,
            ConcurrentHashMap<String, Integer> counts,
            ConcurrentSkipListSet<String> visitedUrls) {
        this.url = url;
        this.parserFactory = parserFactory;
        this.deadline = deadline;
        this.maxDepth = maxDepth;
        this.ignoredUrls = ignoredUrls;
        countsInternal = counts;
        visitedUrlsInternal = visitedUrls;
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

        if(!visitedUrlsInternal.add(url)) {
            return;
        }

        PageParser.Result result = parserFactory.get(url).parse();


        for (Map.Entry<String, Integer> e : result.getWordCounts().entrySet()) {
            if (countsInternal.containsKey(e.getKey())) {
                countsInternal.put(e.getKey(), e.getValue() + countsInternal.get(e.getKey()));
            } else {
                countsInternal.put(e.getKey(), e.getValue());
            }
        }

        List<ParallelInternalCrawler> internalCrawlers = new ArrayList<>();

        for (String link : result.getLinks()) {
            internalCrawlers.add(new ParallelInternalCrawler.Builder().
                    setUrl(link).
                    setParserFactory(parserFactory).
                    setDeadline(deadline).
                    setMaxDepth(maxDepth - 1).
                    setIgnoredUrls(ignoredUrls).
                    setCounts(countsInternal).
                    setVisitedUrl(visitedUrlsInternal).
                    build());
        }

        invokeAll(internalCrawlers);

    }

    public static final class Builder {

        String url;
        PageParserFactory parserFactory;
        Instant deadline;
        int maxDepth;
        List<Pattern> ignoredUrls;
        ConcurrentHashMap<String, Integer> counts;
        ConcurrentSkipListSet<String> visitedUrl;

        public Builder setUrl(String url) {
            this.url = url;
            return this;
        }

        public Builder setParserFactory(PageParserFactory parserFactory) {
            this.parserFactory = parserFactory;
            return this;
        }

        public Builder setDeadline(Instant deadline) {
            this.deadline = deadline;
            return this;
        }

        public Builder setMaxDepth(int maxDepth) {
            this.maxDepth = maxDepth;
            return this;
        }

        public Builder setIgnoredUrls(List<Pattern> ignoredUrls) {
            this.ignoredUrls = ignoredUrls;
            return this;
        }

        public Builder setCounts(ConcurrentHashMap<String, Integer> counts) {
            this.counts = counts;
            return this;
        }

        public Builder setVisitedUrl(ConcurrentSkipListSet<String> visitedUrl) {
            this.visitedUrl = visitedUrl;
            return this;
        }


        public ParallelInternalCrawler build() {
            return new ParallelInternalCrawler(
                    this.url,
                    this.parserFactory,
                    this.deadline,
                    this.maxDepth,
                    this.ignoredUrls,
                    this.counts,
                    this.visitedUrl);
        }

    }
}