const { gql } = require("apollo-server");

const typeDefs = gql`
  type Query {
    wordsInText(text: String!): [Word!]!
    wordInformation(words: [String!]!): [Word!]!
    me: User
    health: String
  }

  interface Word {
    language: Language!
    text: String!
    partOfSpeech: String
    lemma: String
    definitions: [String!]
  }

  type ChineseWord implements Word {
    language: Language!
    text: String!
    partOfSpeech: String
    lemma: String
    definitions: [String!]
    hsk: Int
    pinyin: [String!]
  }

  enum Language {
    CHINESE
    ENGLISH
    SPANISH
  }

  type Vocabulary {
    id: ID!
    user: User
    word: Word!
    added: String
  }

  type User {
    id: ID!
    email: String!
    vocabulary: [Vocabulary!]!
  }
`;

module.exports = typeDefs;
