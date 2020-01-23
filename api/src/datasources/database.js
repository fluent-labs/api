const { SQLDataSource } = require("datasource-sql");

class MyDatabase extends SQLDataSource {
  async addWord( { language, text, partOfSpeech, lemma } ) {
    return await this.knex
      .returning(["id", "language", "text", "part_of_speech", "lemma"])
      .insert( {language: language, text: text, part_of_speech: partOfSpeech, lemma: lemma } )
      .into("words");
  }
}

module.exports = MyDatabase;