defmodule ApiWeb.HealthController do
  use ApiWeb, :controller

  def index(conn, _params) do
    json(conn, %{health: "ok"})
  end
end
