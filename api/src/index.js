const { ApolloServer } = require('apollo-server');
const typeDefs = require('./schema');
const resolvers = require('./resolvers');
const ComprehendAPI = require('./datasources/comprehend');

const server = new ApolloServer({
  typeDefs,
  resolvers,
  dataSources: () => ({
    comprehendAPI: new ComprehendAPI()
  })
});

server.listen().then(({ url }) => {
  console.log(`🚀 Server ready at ${url}`);
});
