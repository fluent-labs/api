defmodule Api.Word do
  use Ecto.Schema
  import Ecto.Changeset

  @moduledoc """
  This encapsulates data about a given word
  """

  schema "words" do
    field :lemma, :string
    field :part_of_speech, :string
    field :text, :string
    field :language, :id

    timestamps()
  end

  @doc false
  def changeset(word, attrs) do
    word
    |> cast(attrs, [:text, :part_of_speech, :lemma])
    |> validate_required([:text, :part_of_speech, :lemma])
  end
end
