package com.udacity.webcrawler;

import com.udacity.webcrawler.parser.PageParser;
import com.udacity.webcrawler.parser.PageParserFactory;

import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.RecursiveAction;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class ParallelInternalCrawler extends RecursiveAction {

    private final Clock clock = Clock.systemUTC();
    private final String url;
    private final PageParserFactory parserFactory;
    private final Instant deadline;
    private final int maxDepth;
    private final List<Pattern> ignoredUrls;
    public static ConcurrentHashMap<String, Integer> countsCollector = new ConcurrentHashMap<>();
    public static ConcurrentSkipListSet<String> visitedUrlsCollector = new ConcurrentSkipListSet<>();

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
        countsCollector = counts;
        visitedUrlsCollector = visitedUrls;
    }

    private ParallelInternalCrawler updateCrawler(String url) {
        return new Builder().
                setUrl(url).
                setParserFactory(parserFactory).
                setDeadline(deadline).
                setMaxDepth(maxDepth - 1).
                setIgnoredUrls(ignoredUrls).
                setCounts(countsCollector).
                setVisitedUrl(visitedUrlsCollector).
                build();
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
        if (visitedUrlsCollector.contains(url)) {
            return;
        }
        visitedUrlsCollector.add(url);

        PageParser.Result result = parserFactory.get(url).parse();

        for (ConcurrentMap.Entry<String, Integer> e : result.getWordCounts().entrySet()) {
            countsCollector.compute(e.getKey(), (k, v) -> (v == null) ? e.getValue() : e.getValue() + v);
        }

        List<ParallelInternalCrawler> internalCrawlers = result.getLinks().stream().map(this::updateCrawler).collect(Collectors.toList());

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