# Foreign Language Reader
A site for learning to read foreign languages.

## Parts
### Frontend
A react application that allows users to enter text and see which words they don't know.

### API
A glue layer that puts together all the different data sources and keeps track of the user's vocabulary. Built in Elixir and served with the Phoenix framework.

### Language-Service
A service that provides text processing methods.
- Service served by flask
- English and Spanish part of speech tagging provided by Spacy
- Chinese part of speech tagging provided by jieba
- Vocabulary provided by WiktionaryParser

### Infrastructure
Terraform scripts that describe all of our infrastructure.
- Hosted on Digital Ocean
- Managed by Kubernetes
- Container hosting by AWS
