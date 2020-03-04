defmodule Tesla.Middleware.RequestLogger do
  @behaviour Tesla.Middleware

  def call(env, next, _) do
    env
    |> IO.inspect()
    |> Tesla.run(next)
  end
end

defmodule Tesla.Middleware.ReturnErrorTupleOnError do
  @moduledoc """
  Return :ok/:error tuples for successful HTTP transations, i.e. when the request is completed
  (no network errors etc) - but it can still be an application-level error (i.e. 404 or 500)
  """
  def call(env, next, _opts) do
    try do
      {:ok, Tesla.run(env, next)}
    rescue
      er in Tesla.Error -> {:error, er}
    end
  end
end

defmodule Tesla.Middleware.ResponseLogger do
  @behaviour Tesla.Middleware

  def call(env, next, _) do
    env
    |> Tesla.run(next)
    |> IO.inspect()
  end
end
