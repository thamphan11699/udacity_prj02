package com.udacity.webcrawler;

import com.udacity.webcrawler.json.CrawlResult;
import com.udacity.webcrawler.parser.PageParserFactory;

import javax.inject.Inject;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.ForkJoinPool;
import java.util.regex.Pattern;

/**
 * A concrete implementation of {@link WebCrawler} that runs multiple threads on a {@link
 * ForkJoinPool} to fetch and process multiple web pages in parallel.
 */
final class ParallelWebCrawler implements WebCrawler {
  private final Clock clock;
  private final PageParserFactory parserFactory;
  private final Duration timeout;
  private final int popularWordCount;
  private final int maxDepth;
  private final List<Pattern> ignoredUrls;
  private final ForkJoinPool pool;

  @Inject
  ParallelWebCrawler(
      Clock clock,
      PageParserFactory parserFactory,
      @Timeout Duration timeout,
      @PopularWordCount int popularWordCount,
      @MaxDepth int maxDepth,
      @IgnoredUrls List<Pattern> ignoredUrls,
      @TargetParallelism int threadCount) {
    this.clock = clock;
    this.parserFactory = parserFactory;
    this.timeout = timeout;
    this.popularWordCount = popularWordCount;
    this.maxDepth = maxDepth;
    this.ignoredUrls = ignoredUrls;
    this.pool = new ForkJoinPool(Math.min(threadCount, getMaxParallelism()));
  }

  @Override
  public CrawlResult crawl(List<String> startingUrls) {

    Instant deadline = clock.instant().plus(timeout);
    Map<String, Integer> counts = new ConcurrentHashMap<>();
    Set<String> visitedUrls = new ConcurrentSkipListSet<>();

    startingUrls.forEach(
        url ->
            pool.invoke(
                new CrawlTask(
                    url,
                    deadline,
                    counts,
                    visitedUrls,
                    parserFactory,
                    maxDepth,
                    clock,
                    ignoredUrls)));

    if (counts.isEmpty()) {
      return new CrawlResult.Builder()
          .setWordCounts(counts)
          .setUrlsVisited(visitedUrls.size())
          .build();
    }

    return new CrawlResult.Builder()
        .setWordCounts(WordCounts.sort(counts, popularWordCount))
        .setUrlsVisited(visitedUrls.size())
        .build();
  }

  @Override
  public int getMaxParallelism() {
    return Runtime.getRuntime().availableProcessors();
  }
}
