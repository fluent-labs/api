const chinese = require("../content/chinese/cedict.json").reduce((acc, word) => {
  if (acc.hasOwnProperty(word.simplified)) {
    acc[word.simplified].push(word);
  } else {
    acc[word.simplified] = [word];
  }
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
    __resolveType: (word, _context, _info) => {
      if (word.language == "CHINESE") return "ChineseWord";
      else return GenericWord;
    }
  },
  ChineseWord: {
    definitions: (word, _args, _context) => {
      if (chinese.hasOwnProperty(word.text)) {
        return chinese[word.text].flatMap(word => word.definitions);
      }
    },
    hsk: (word, _args, _context) => {
      if (chinese.hasOwnProperty(word.text)) {
        const levels = chinese[word.text]
          .filter(word => word.hasOwnProperty("HSK"))
          .map(word => word["HSK"]);

        if (levels != []) {
          return Math.min(levels);
        }
      }
    },
    pinyin: (word, _args, _context) => {
      if (chinese.hasOwnProperty(word.text)) {
        return chinese[word.text].map(word => word.pinyin);
      }
    }
  }
};
