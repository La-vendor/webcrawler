package com.udacity.webcrawler.main;

import com.google.inject.Guice;
import com.udacity.webcrawler.WebCrawler;
import com.udacity.webcrawler.WebCrawlerModule;
import com.udacity.webcrawler.json.ConfigurationLoader;
import com.udacity.webcrawler.json.CrawlResult;
import com.udacity.webcrawler.json.CrawlResultWriter;
import com.udacity.webcrawler.json.CrawlerConfiguration;
import com.udacity.webcrawler.profiler.Profiler;
import com.udacity.webcrawler.profiler.ProfilerModule;

import javax.inject.Inject;
import java.io.BufferedWriter;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;

public final class WebCrawlerMain {

    private final CrawlerConfiguration config;

    private WebCrawlerMain(CrawlerConfiguration config) {
        this.config = Objects.requireNonNull(config);
    }

    @Inject
    private WebCrawler crawler;

    @Inject
    private Profiler profiler;

    private void run() throws Exception {
        Guice.createInjector(new WebCrawlerModule(config), new ProfilerModule()).injectMembers(this);

        CrawlResult result = crawler.crawl(config.getStartPages());
        CrawlResultWriter resultWriter = new CrawlResultWriter(result);
        if (!config.getResultPath().isEmpty()) {
            resultWriter.write(Paths.get(config.getResultPath()));
        } else {
            try(Writer systemOutWriter = new BufferedWriter((new OutputStreamWriter(System.out)))){
                resultWriter.write(systemOutWriter);
            }


        }

        if (!config.getProfileOutputPath().isEmpty()) {
            profiler.writeData(Paths.get(config.getProfileOutputPath()));
        } else {
            try(Writer systemOutWriter = new BufferedWriter((new OutputStreamWriter(System.out)))) {
                profiler.writeData(systemOutWriter);
            }

        }
        // TODO: Write the crawl results to a JSON file (or System.out if the file name is empty)
        // TODO: Write the profile data to a text file (or System.out if the file name is empty)
    }

    public static void main(String[] args) throws Exception {
//        if (args.length != 1) {
//            System.out.println("Usage: WebCrawlerMain [starting-url]");
//            return;
//                    }

        CrawlerConfiguration config;
        if (args.length != 1) {
            config = new ConfigurationLoader(Path.of("src/main/config/sample_config.json")).load();
        }else{
            config = new ConfigurationLoader(Path.of(args[0])).load();
        }
        new WebCrawlerMain(config).run();


    }
}
