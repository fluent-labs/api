const { RESTDataSource } = require("apollo-datasource-rest");
const AWS = require("aws-sdk");
AWS.config.update({ region: "us-west-2" });

const comprehend = new AWS.Comprehend();

class AWSComprehendAPI extends RESTDataSource {
  constructor() {
    super();
  }

  detectSyntaxReducer(response) {
    return {
      language: "ENGLISH",
      text: response.Text,
      partOfSpeech: response.PartOfSpeech.Tag
    };
  }

  getWordsInText({ text }) {
    comprehend.detectSyntax({ LanguageCode: "en", Text: text }, (err, data) => {
      if (err) {
        throw err;
      } else {
        const words = data["SyntaxTokens"];
        return Array.isArray(words)
          ? words.map(word => this.detectSyntaxReducer(word))
          : [];
      }
    });
  }
}

module.exports = AWSComprehendAPI;
