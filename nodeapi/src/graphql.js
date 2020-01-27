const { ApolloServer } = require("apollo-server-lambda");
const typeDefs = require("./schema");
const resolvers = require("./resolvers");
const GoogleNaturalLanguageAPI = require("./datasources/google");

const server = new ApolloServer({
  typeDefs,
  resolvers,
  dataSources: () => ({
    googleAPI: new GoogleNaturalLanguageAPI()
  })
});

exports.graphqlHandler = server.createHandler();
