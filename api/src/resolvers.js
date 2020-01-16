module.exports = {
  Query: {
    wordsInText: (_, { text }, { dataSources }) =>
      dataSources.comprehendAPI.getWordsInText({ text: text })
  }
};
