package com.udacity.webcrawler;

import com.udacity.webcrawler.parser.PageParser;
import com.udacity.webcrawler.parser.PageParserFactory;
import java.time.Clock;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.RecursiveAction;
import java.util.regex.Pattern;

public class CrawlTask extends RecursiveAction {

  private final String url;

  private final Instant deadline;

  private final Map<String, Integer> counts;

  private final Set<String> visitedUrls;

  private final PageParserFactory parserFactory;

  private final int maxDepth;

  private final Clock clock;

  private final List<Pattern> ignoredUrls;

  public CrawlTask(
      String url,
      Instant deadline,
      Map<String, Integer> counts,
      Set<String> visitedUrls,
      PageParserFactory parserFactory,
      @MaxDepth int maxDepth,
      Clock clock,
      @IgnoredUrls List<Pattern> ignoredUrls) {
    this.url = url;
    this.deadline = deadline;
    this.counts = counts;
    this.visitedUrls = visitedUrls;
    this.parserFactory = parserFactory;
    this.maxDepth = maxDepth;
    this.clock = clock;
    this.ignoredUrls = ignoredUrls;
  }

  @Override
  protected void compute() {

    if (maxDepth == 0 || clock.instant().isAfter(deadline)) {
      return;
    }
    for (Pattern pattern : ignoredUrls) {
      if (pattern.matcher(url).matches()) {
        return;
      }
    }
    if (visitedUrls.contains(url)) {
      return;
    }
    visitedUrls.add(url);
    PageParser.Result result = parserFactory.get(url).parse();

    result
        .getWordCounts()
        .forEach(
            (key, value) -> {
              counts.compute(key, (k, v) -> Objects.isNull(v) ? value : value + v);
            });
    List<CrawlTask> subtasks =
        result.getLinks().stream()
            .map(
                url ->
                    new CrawlTask(
                        url,
                        deadline,
                        counts,
                        visitedUrls,
                        parserFactory,
                        maxDepth,
                        clock,
                        ignoredUrls))
            .toList();
    invokeAll(subtasks);
  }
}
