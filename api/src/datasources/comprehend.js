const { RESTDataSource } = require("apollo-datasource-rest");
const AWS = require("aws-sdk");
AWS.config.update({ region: "us-west-2" });

const comprehend = new AWS.Comprehend();

class AWSComprehendAPI extends RESTDataSource {
  constructor() {
    super();
  }

  async getWordsInText({ text }) {
    console.log("running");
    const response = await comprehend.detectSyntax({
      LanguageCode: "en",
      Text: text
    });
    console.log("processing");
    const tokens = response["SyntaxTokens"];
    const words = Array.isArray(tokens) ? tokens.map(token => token.Text) : [];
    console.log(words);
    return words;
  }
}

module.exports = AWSComprehendAPI;
