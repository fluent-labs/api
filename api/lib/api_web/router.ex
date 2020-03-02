defmodule ApiWeb.Router do
  use ApiWeb, :router

  pipeline :api do
    plug :accepts, ["json"]
  end

  scope "/api" do
    pipe_through :api

    forward "/graphiql", Absinthe.Plug.GraphiQL,
      schema: ApiWeb.Schema

    forward "/", Absinthe.Plug,
      schema: ApiWeb.Schema
  end

  scope "/", ApiWeb do
    pipe_through :api

    get "/health", HealthController, :index
  end
end
