const { ApolloServer } = require('apollo-server');
const typeDefs = require('./schema');
const resolvers = require('./resolvers');
const ComprehendAPI = require('./datasources/comprehend');
const GoogleNaturalLanguageAPI = require('./datasources/google');

const server = new ApolloServer({
  typeDefs,
  resolvers,
  dataSources: () => ({
    comprehendAPI: new ComprehendAPI(),
    googleAPI: new GoogleNaturalLanguageAPI()
  })
});

server.listen().then(({ url }) => {
  console.log(`🚀 Server ready at ${url}`);
});
