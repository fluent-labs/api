const chinese = require("../content/chinese/cedict.json").reduce((acc, word) => {
  acc[word.simplified] = word;
  return acc;
}, {});

module.exports = {
  Query: {
    wordsInText: (_parent, { text }, { dataSources }) => {
      return dataSources.googleAPI.getWordsInText({ text: text });
    },
    health: (_parent, _args, { dataSources }) => {
      return "OK";
    }
  },
  Word: {
    definitions: (word, _args, _context) => {
      if (word.language == 'CHINESE') {
        if (chinese.hasOwnProperty(word.text)) {
          return chinese[word.text].definitions;
        }
      }
    }
  }
};
