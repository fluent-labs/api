const { RESTDataSource } = require("apollo-datasource-rest");

const language = require("@google-cloud/language");
const client = new language.LanguageServiceClient();

class GoogleNaturalLanguageAPI extends RESTDataSource {
  constructor() {
    super();
  }

  mapLanguage(languageCode) {
    switch (languageCode) {
      case "en":
        return "ENGLISH";
      case "es":
        return "SPANISH";
      case "zh":
        return "CHINESE";
      default:
        throw Error("Unsupported language requested");
    }
  }

  async getWordsInText({ text }) {
    const document = {
      content: text,
      type: "PLAIN_TEXT"
    };
    const [words] = await client.analyzeSyntax({ document });
    const wordLanguage = this.mapLanguage(words.language);

    return words.tokens.map(token => {
      return {
        language: wordLanguage,
        text: token.text.content,
        partOfSpeech: token.partOfSpeech.tag,
        lemma: token.lemma
      };
    });
  }
}

module.exports = GoogleNaturalLanguageAPI;
