const { SQLDataSource } = require("datasource-sql");

class MyDatabase extends SQLDataSource {
  async addWord( { language, text, partOfSpeech, lemma } ) {
    return await this.knex
      .insert( {language: language, text: text, part_of_speech: partOfSpeech, lemma: lemma } )
      .into("words");
  }
  async getWord( { text } ) {
    return await this.knex
      .select("*")
      .from("words")
      .where({ text: text });
  }
}

module.exports = MyDatabase;