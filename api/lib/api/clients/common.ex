defmodule Tesla.Middleware.RequestLogger do
  @behaviour Tesla.Middleware
  @moduledoc """
  Logs the requests made by Tesla clients
  """

  def call(%Tesla.Env{url: url, body: body, method: method} = env, next, _) do
    case method do
      :get ->
        IO.puts("Calling " <> url)

      :post ->
        {:ok, body_json} = Jason.encode(body)
        IO.puts("Calling " <> url <> " with body: " <> body_json)
    end

    Tesla.run(env, next)
  end
end

defmodule Tesla.Middleware.ResponseLogger do
  @behaviour Tesla.Middleware
  @moduledoc """
  Logs the responses recieved by Tesla clients
  """

  def call(env, next, _) do
    case Tesla.run(env, next) do
      {:ok, successful_env} ->
        %Tesla.Env{url: url} = successful_env
        IO.puts("Successful response from " <> url)
        {:ok, successful_env}

      {:error, err} ->
        IO.puts(err)
    end
  end
end
