defmodule Tesla.Middleware.RequestLogger do
  @behaviour Tesla.Middleware
  @moduledoc """
  Logs the requests made by Tesla clients
  """

  def call(env, next, _) do
    env
    |> IO.inspect()
    |> Tesla.run(next)
  end
end

defmodule Tesla.Middleware.ResponseLogger do
  @behaviour Tesla.Middleware
  @moduledoc """
  Logs the responses recieved by Tesla clients
  """

  def call(env, next, _) do
    env
    |> Tesla.run(next)
    |> IO.inspect()
  end
end
