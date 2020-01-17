const { RESTDataSource } = require("apollo-datasource-rest");

const AWS = require("aws-sdk");
AWS.config.update({ region: "us-west-2" });

const comprehend = new AWS.Comprehend();

class AWSComprehendAPI extends RESTDataSource {
  constructor() {
    super();
  }

  getWordsInText({ text }) {
    return comprehend
      .detectSyntax({
        LanguageCode: "en",
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
