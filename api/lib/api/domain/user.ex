defmodule Api.User do
  use Ecto.Schema
  import Ecto.Changeset

  @moduledoc """
  This keeps track of the users of the application
  """

  schema "users" do
    field :email, :string
    field :name, :string

    timestamps()
  end

  @doc false
  def changeset(user, attrs) do
    user
    |> cast(attrs, [:name, :email])
    |> validate_required([:name, :email])
    |> unique_constraint(:email)
  end
end
