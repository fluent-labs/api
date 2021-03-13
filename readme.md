# FluentLabs Reader
A site for learning to read second languages.
Note: This is a work in progress. Part of the reason I'm doing it is to build an entire application from the ground up.

## Parts
### API
A glue layer that puts together all the different data sources and keeps track of the user's vocabulary. 
-   Built in Scala and served with the Play framework.
-   Language data stored with Elasticsearch
-   Part of speech tagging provided by Spacy and Google Cloud

### Jobs
Scheduled jobs that refresh language content
-   Jobs run on Apache Spark
-   Dictionary definitions come from wiktionary for Simple English, English, and Chinese
-   Some jobs are in progress
