# Library of Alexandria

![LoA Logo](https://i.imgur.com/xJLWPpf.png)

Library of Alexandria (LoA in short) is a project that aims to collect and archive documents from the internet.

In our modern age new text documents are born in a blink of an eye then (often just as quickly) disappear from the internet. We find it a noble task to save these documents for future generations.

This project aims to support this noble goal in a scalable way. We want to make the archival activity streamlined and easy to do even in a huge (Terabyte / Petabyte) scale. This way we hope that more and more people can start their own collection helping the archiving effort.

## Applications

The project consist of five applications. Not all of them are required for collection however. If you take time to analyze the capabilities of these applications you will see what the project can/want to achieve.

### Collector application

Responsible for collecting the documents from either a list of URLs from a file or a list of URLs parsed from the [Common Crawl](http://commoncrawl.org/) corpus.

### Administrator application

Responsible for administration tasks on the databases (migration between releases etc).

### Vault application

Responsible for storing documents on the filesystem and making them available via a web endpoint.

### Indexer application

Responsible for indexing the documents in an Elasticsearch database. After indexing the documents will be easily searchable.

### Web application

Responsible for providing a UI to search the documents or display statistics about the speed of collection.

## Documentation

We have an extensive documentation for the applications! It is available in our [wiki page](https://github.com/bottomless-archive-project/library-of-alexandria/wiki)!
