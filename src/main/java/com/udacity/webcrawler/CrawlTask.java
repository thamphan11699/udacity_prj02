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

  private Map<String, Integer> counts;

  private Set<String> visitedUrls;

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

    if (!(maxDepth > 0) || clock.instant().isAfter(deadline)) {
      return;
    }
    for (Pattern pattern : ignoredUrls) {
      if (pattern.matcher(url).matches()) return;
    }
    if (visitedUrls.contains(url)) {
      return;
    }
    visitedUrls.add(url);
    PageParser.Result result = parserFactory.get(url).parse();

    for (Map.Entry<String, Integer> e : result.getWordCounts().entrySet()) {
      if (counts.containsKey(e.getKey())) {
        counts.put(e.getKey(), e.getValue() + counts.get(e.getKey()));
      } else {
        counts.put(e.getKey(), e.getValue());
      }
    }
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
                        maxDepth - 1,
                        clock,
                        ignoredUrls))
            .toList();
    invokeAll(subtasks);
  }
}
