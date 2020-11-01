# Foreign Language Reader
A site for learning to read foreign languages.
Note: This is a work in progress. Part of the reason I'm doing it is to build an entire application from the ground up.

## Where is the site?
Infrastructure has been disabled for now until development is complete. Why pay for it when nobody is using it?

## Parts
### Frontend
A React application that allows users to enter text and see which words they don't know.

### API
A glue layer that puts together all the different data sources and keeps track of the user's vocabulary. Built in Scala and served with the Play framework.

### Language-Service
A service that provides text processing methods.
-   Service served by flask
-   Language data stored with Elasticsearch
-   English and Spanish part of speech tagging provided by Spacy
-   Chinese part of speech tagging provided by jieba
-   Vocabulary provided by WiktionaryParser

### Infrastructure
Terraform scripts and Kubernetes YAML files that describe all of our infrastructure.
-   Hosted on Digital Ocean
-   Managed by Kubernetes
-   Containers and static content hosted on AWS

### Future plans
-   Finish up API to keep track of Vocabulary
-   Example sentences
-   Get more dictionaries
-   Automated language content updates
