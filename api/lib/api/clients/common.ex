defmodule Tesla.Middleware.RequestLogger do
  require Logger
  @behaviour Tesla.Middleware
  @moduledoc """
  Logs the requests made by Tesla clients
  """

  def call(%Tesla.Env{url: url, body: body, method: method} = env, next, _) do
    case method do
      :get ->
        Logger.debug("Calling " <> url)

      :post ->
        {:ok, body_json} = Jason.encode(body)
        Logger.debug("Calling " <> url <> " with body: " <> body_json)
    end

    Tesla.run(env, next)
  end
end

defmodule Tesla.Middleware.ResponseLogger do
  require Logger
  @behaviour Tesla.Middleware
  @moduledoc """
  Logs the responses recieved by Tesla clients
  """

  def call(env, next, _) do
    case Tesla.run(env, next) do
      {:ok, successful_env} ->
        %Tesla.Env{url: url} = successful_env
        Logger.debug("Successful response from " <> url)
        {:ok, successful_env}

      {:error, err} ->
        Logger.error(Atom.to_string(err))
    end
  end
end
