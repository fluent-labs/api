module.exports = {
  Query: {
    wordsInText: (_, { text }, { dataSources }) => {
      console.log("Calling");
      words = dataSources.comprehendAPI.getWordsInText({ text: text });
      console.log("got words");
      console.log(words);
    }
  }
};
