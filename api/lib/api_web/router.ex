defmodule ApiWeb.Router do
  use ApiWeb, :router

  pipeline :api do
    plug :accepts, ["json"]
  end

  scope "/api", ApiWeb do
    pipe_through :api
  end

  scope "/", ApiWeb do
    pipe_through :api

    get "/health", HealthController, :index
  end
end
