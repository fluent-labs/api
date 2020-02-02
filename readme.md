# Foreign Language Reader
A site for learning to read foreign languages.

## Parts
### Frontend
A react application that allows users to enter text and see which words they don't know.
### API
A glue layer that puts together all the different data sources and keeps track of the user's vocabulary. Built in Elixir and served with the Phoenix framework.
### Vocabulary
A lambda that scrapes Wiktionary for definitions of all vocabulary. Written in Python, and leans heavily on the WiktionaryParser open source project.
