const { RESTDataSource } = require("apollo-datasource-rest");

const AWS = require("aws-sdk");
AWS.config.update({ region: "us-west-2" });

const comprehend = new AWS.Comprehend();

class AWSComprehendAPI extends RESTDataSource {
  constructor() {
    super();
  }

  getWordsInText({ text, language }) {
    let languageCode;
    if (language == "ENGLISH") languageCode = "en";
    else if (language == "SPANISH") languageCode = "es";
    else throw Error("Unsupported language");

    return comprehend
      .detectSyntax({
        LanguageCode: languageCode,
        Text: text
      })
      .promise()
      .then(response => {
        const tokens = response["SyntaxTokens"];
        return Array.isArray(tokens) ? tokens.map(token => token.Text) : [];
      });
  }
}

module.exports = AWSComprehendAPI;
