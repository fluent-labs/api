module.exports = {
  Query: {
    wordsInText: async (_, { text }, { dataSources }) => {
      return dataSources.googleAPI.getWordsInText({ text: text});
    }
  }
};
