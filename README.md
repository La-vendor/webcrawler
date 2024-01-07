# Udacity Project - Parallel Web Crawler

This Java-based web crawler project, initially provided as a single-threaded implementation within an Udacity course,
has been improved through the course to implement multithreading.
It's designed to efficiently collect words from websites.

## Technologies used
* Maven
* Javax
* Jackson
* Jsoup
* JUnit

## Features
* **Enhanced Performance:** Implements multithreading to significantly increase speed and efficiency in the crawling process.
* **JSON Configuration:** Utilizes JSON configuration files for easy customization.
* **Data Export:** Supports exporting scraped data to JSON file.

## Usage

`java -classpath target/udacity-webcrawler-1.0.jar com.udacity.webcrawler.main.WebCrawlerMain src/main/config/sample_config.json`

## Configuration

Example configuration file:
```
{
  "startPages": ["https://www.udacity.com/"],
  "ignoredUrls": ["https://blog.udacity.com/.*"],
  "ignoredWords": ["^.{1,3}$"],
  "parallelism": 4,
  "maxDepth": 5,
  "timeoutSeconds": 10,
  "popularWordCount": 5,
  "profileOutputPath": "profileData.txt",
  "resultPath": "resultData.txt"
}
```

`startPages`: Array of URLs representing the initial starting points for the web crawler's exploration.

`ignoredUrls`: Array of URL patterns or regex expressions defining which URLs should be ignored or skipped during crawling.

`ignoredWords`: Array of words or regex patterns specifying which words should be ignored in the crawled content.

`parallelism`: Specifies the number of concurrent threads or parallel processes the crawler should utilize during its operations.

`maxDepth`: Defines the maximum depth or level of links that the crawler should follow during the crawling process.

`timeoutSeconds`: Sets the maximum duration the crawler will run its operations for the entire crawling process.

`popularWordCount`: Indicates the number of most frequently occurring words to be extracted from the crawled content.

`profileOutputPath`: Path or file location where profiling data will be saved.

`resultPath`: Path or file location where the final result from the crawling process will be saved.




## Contributing
Contributions are welcomed! Please open an issue to discuss proposed changes or fork this repository and create a pull request with your improvements.

## Acknowledgments
* This project originated from an Udacity course.
* Most of the initial program structure was provided by Udacity, and it has been extended to implement multithreading.
