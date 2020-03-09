defmodule ApiWeb.Schema.DefinitionTypes do
  @moduledoc """
  The schema for our GraphQL endpoint
  """
  use Absinthe.Schema.Notation

  interface :definition do
    field :subdefinitions, %Absinthe.Type.List{of_type: non_null(:string)}
    field :examples, %Absinthe.Type.List{of_type: non_null(:string)}

    resolve_type(fn
      %{pinyin: _}, _ -> :chinese_definition
      _, _ -> :generic_definition
    end)
  end

  object :generic_definition do
    field :subdefinitions, %Absinthe.Type.List{of_type: non_null(:string)}
    field :examples, %Absinthe.Type.List{of_type: non_null(:string)}

    interface(:definition)
  end

  object :chinese_definition do
    field :subdefinitions, %Absinthe.Type.List{of_type: non_null(:string)}
    field :examples, %Absinthe.Type.List{of_type: non_null(:string)}
    field :traditional, :string
    field :simplified, :string
    field :pinyin, :string

    interface(:definition)
  end
end
